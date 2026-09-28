# BlockingQueue family

## 1. What it is

`BlockingQueue<E>` (in `java.util.concurrent`) extends `Queue` with **blocking** insert/remove
operations -- `put(e)` waits if the queue is full, `take()` waits if it's empty -- making it the
standard building block for producer-consumer pipelines and bounded thread-pool work queues. Six
implementations cover distinct backing structures and blocking semantics:
`ArrayBlockingQueue`, `LinkedBlockingQueue`, `PriorityBlockingQueue`, `DelayQueue`, and
`SynchronousQueue` (plus `LinkedBlockingDeque`, the double-ended variant, not covered separately
here).

## 2. How it works internally

`BlockingQueue` adds four operations per insert/remove, beyond `Queue`'s throw/sentinel pair,
giving each a **blocking** and a **timed-blocking** variant:

```
                insert                    remove
throws:         add(e)                    remove()
sentinel:       offer(e)                  poll()
blocks:         put(e)                    take()
timed:          offer(e, time, unit)      poll(time, unit)
```

**`ArrayBlockingQueue`** -- fixed-capacity (must be specified at construction), backed by a
circular array almost identical in spirit to `ArrayDeque`'s (topic 2), but bounded and guarded
by a **single `ReentrantLock`** shared by both `put` and `take`, with two `Condition`s
(`notEmpty`, `notFull`) used to park/wake waiting threads. Because both ends share one lock,
producers and consumers **cannot proceed truly concurrently** -- each `put`/`take` briefly
serializes on the same lock, even though the actual array operation is O(1). An optional `fair`
constructor flag (`new ArrayBlockingQueue<>(cap, true)`) makes the lock FIFO-fair (uses
`ReentrantLock(true)`), guaranteeing waiting threads are served in arrival order at the cost of
noticeably lower throughput (fair locks disable "barging," a major source of throughput under an
unfair lock).

**`LinkedBlockingQueue`** -- optionally bounded (default capacity `Integer.MAX_VALUE`,
i.e. effectively **unbounded** if you don't pass a capacity -- a common production footgun, see
pitfalls), backed by singly linked nodes. Unlike `ArrayBlockingQueue`, it uses **two separate
locks** -- `putLock` and `takeLock` -- plus an `AtomicInteger` for the current count, so a
producer inserting at the tail and a consumer removing from the head can proceed **truly
concurrently** in most cases (the classic two-lock concurrent queue algorithm). This generally
gives `LinkedBlockingQueue` **higher throughput than `ArrayBlockingQueue` under contention**,
at the cost of per-node allocation overhead (same array-vs-linked-node trade-off as topic 2).

**`PriorityBlockingQueue`** -- unbounded, heap-ordered (same binary-heap array core as
`PriorityQueue`, topic 3), guarded by a single lock. `take()` blocks when empty; `put()`
**never blocks** (no capacity ceiling -- though it can still throw `OutOfMemoryError` if
producers vastly outpace consumers). No ordering guarantee among equal-priority elements, same
as plain `PriorityQueue`.

**`DelayQueue<E extends Delayed>`** -- unbounded; each element implements `Delayed`
(`getDelay(TimeUnit)` + `compareTo`) and is only eligible to be returned by `poll()`/`take()`
once its own delay has **expired** (`getDelay <= 0`). Internally backed by a `PriorityQueue`
ordered by remaining delay, so the soonest-to-expire element is always at the head. `take()`
blocks until the head element's delay actually expires (not just until the queue is non-empty).
Common uses: scheduling retries with backoff, cache-entry expiry, delayed task execution.

**`SynchronousQueue<E>`** -- has **zero internal capacity**; it stores nothing. Every `put()`
must rendezvous directly with a matching `take()` (and vice versa) -- whichever thread calls
first simply **blocks until the other arrives**, then the value is handed off directly. It's
best understood as a synchronous hand-off channel, not really a "queue" with storage at all. Has
a `fair` mode (FIFO-ordered waiting threads) like `ArrayBlockingQueue`. `Executors
.newCachedThreadPool()` uses a `SynchronousQueue` as its work queue specifically because it has
no storage: a submitted task either hands off to an already-idle thread immediately, or (since
nothing can queue) forces the pool to spin up a new thread rather than letting work pile up
invisibly.

**Fairness, generally:** several of these support a `fair` boolean constructor parameter. `fair
= true` uses a fair lock (`ReentrantLock(true)`), guaranteeing threads acquire access in the
order they started waiting -- important for predictable latency under contention, but
meaningfully slower under high throughput since it forbids "lock barging" (a thread grabbing the
lock immediately if it's free, even if others have waited longer). Default is `fair = false`.

## 3. Complexity

| Implementation | put/take | Backing structure | Bounded? | Blocks on put? |
|---|---|---|---|---|
| `ArrayBlockingQueue` | O(1) | circular array, 1 lock | yes (required) | yes, if full |
| `LinkedBlockingQueue` | O(1) | linked nodes, 2 locks | optional (default unbounded) | only if bounded and full |
| `PriorityBlockingQueue` | O(log n) | binary heap, 1 lock | no | never (no capacity limit) |
| `DelayQueue` | O(log n) | binary heap (by delay), 1 lock | no | never |
| `SynchronousQueue` | O(1) (direct hand-off) | none -- no storage | n/a (capacity 0) | yes, until a taker arrives |

## 4. Example code

- Runnable class: `src/main/java/com/gk/study/queuedeque/examples/BlockingQueueDemo.java` --
  covers all five with small, bounded, fast operations (tiny delays in the tens-of-milliseconds
  range for `DelayQueue`, small `join()` timeouts as a safety net) so the demo runs
  deterministically in well under a second and never risks hanging.

Deterministic parts of the expected output:
```
== PriorityBlockingQueue: heap-ordered, unbounded ==
  offered 5,1,4,2,3 -> drained in priority order: 1 2 3 4 5

== DelayQueue: elements surface only after their delay expires ==
  took fast(5ms)  (expected order: fast, medium, slow)
  took medium(15ms)  (expected order: fast, medium, slow)
  took slow(30ms)  (expected order: fast, medium, slow)
```
The `ArrayBlockingQueue` producer/consumer and `SynchronousQueue` hand-off sections involve two
real threads, so their exact interleaving (which line prints first) is not deterministic across
runs -- only the final drained values and successful completion are guaranteed, which is normal
for concurrency demos and does not indicate a bug.

## 5. When to use / when NOT to use

- **`ArrayBlockingQueue`**: bounded thread-pool work queues where you want a hard memory ceiling
  and predictable backpressure; simplest mental model of the family.
- **`LinkedBlockingQueue`**: higher-throughput producer-consumer pipelines under contention
  (thanks to the two-lock design) -- **always pass an explicit bound** in production; the
  no-arg constructor's `Integer.MAX_VALUE` default capacity is a common cause of
  slow-motion `OutOfMemoryError` incidents when a consumer stalls and producers keep enqueuing.
- **`PriorityBlockingQueue`**: a concurrent task scheduler where tasks have priorities and
  producers should never block (e.g. an unbounded priority work queue feeding a fixed pool of
  worker threads).
- **`DelayQueue`**: retry-with-backoff scheduling, TTL/expiry-based caches, "run this after X"
  task queues -- anywhere "not ready yet" is a first-class concept, not just "empty."
- **`SynchronousQueue`**: direct hand-off between exactly one producer and one consumer at a
  time with no buffering desired (e.g. `newCachedThreadPool`'s internal work queue); NOT
  appropriate when you want any buffering at all -- a `put()` with no waiting consumer blocks
  indefinitely.
- Avoid any unbounded queue (`LinkedBlockingQueue` with no capacity, `PriorityBlockingQueue`,
  `DelayQueue`) feeding from a producer that can outpace its consumer indefinitely in a banking
  or other high-throughput system -- prefer a bounded queue plus an explicit rejection/backoff
  policy so failure is visible early rather than as a slow OOM.

## 6. Common pitfalls & gotchas

**Using `new LinkedBlockingQueue<>()` (no-arg) in production, assuming "unbounded" is safe:**
```java
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(); // capacity = Integer.MAX_VALUE
// if producers outpace consumers under load, this grows without bound -> eventual OOM,
// often discovered only in production under peak load, not in dev/test with light traffic
// fix: new LinkedBlockingQueue<>(10_000) -- pick a bound and an explicit overflow policy
```

**Forgetting that `SynchronousQueue.put()` blocks forever if no thread ever calls `take()`** (and
vice versa) -- there is no internal storage to "hold" the value while waiting; a mismatched
producer/consumer count (e.g. two `put()`s but only one `take()`) leaves a thread permanently
blocked unless interrupted or given a timeout.

**Assuming `DelayQueue.poll()` (non-blocking) returns an element the moment the queue is
non-empty** -- it doesn't; `poll()` returns `null` unless the **head element's delay has actually
expired**, even if the queue contains not-yet-ready elements. Use `take()` (or a timed
`poll(timeout, unit)`) if you want to wait for the next element to become ready.

**Choosing `ArrayBlockingQueue` for a high-contention pipeline without realizing its single lock
serializes producers and consumers** -- under heavy concurrent load, `LinkedBlockingQueue`'s
two-lock design often measurably outperforms it, despite the per-node allocation overhead; profile
before assuming a fixed array is automatically faster.

**Using `fair = true` everywhere "to be safe"** -- fairness trades meaningfully lower throughput
for FIFO ordering guarantees under contention; only enable it when starvation of some waiting
threads would be a real correctness/latency problem, not by default.

## 7. Interview questions

- [Basic] What's the difference between `offer(e)` and `put(e)` on a `BlockingQueue`? →
  `offer(e)` returns immediately (`true`/`false`, or blocks only up to an optional timeout
  variant); `put(e)` blocks the calling thread indefinitely until space becomes available. →
  Follow-up: *Which would you use in a producer thread that should slow down naturally under
  load (backpressure)?* `put(e)` -- blocking is the desired behaviour there.
- [Basic] Why is `ArrayBlockingQueue`'s capacity fixed at construction, unlike
  `LinkedBlockingQueue`? → It's backed by a single fixed-size array allocated up front (bounded
  by design, matching its use case of applying a hard capacity ceiling); `LinkedBlockingQueue`'s
  node-based structure can grow node by node up to an optional bound, or unboundedly if none is
  given. → Follow-up: *Can `ArrayBlockingQueue`'s capacity be changed after construction?* No --
  it's immutable for the life of the queue.
- [Basic] What does `SynchronousQueue` actually store? → Nothing -- it has zero internal
  capacity; every `put()` must directly rendezvous with a matching `take()`, functioning as a
  hand-off channel rather than a buffer. → Follow-up: *Where does the JDK itself use
  `SynchronousQueue`?* As the work queue of `Executors.newCachedThreadPool()`, so submitted tasks
  either hand off to an idle thread immediately or force a new thread to be created, rather than
  queueing invisibly.
- [Intermediate] Why does `LinkedBlockingQueue` typically outperform `ArrayBlockingQueue` under
  high contention? → `LinkedBlockingQueue` uses two separate locks (`putLock`/`takeLock`),
  letting a producer and a consumer operate concurrently on the head and tail independently;
  `ArrayBlockingQueue` uses a single lock shared by both `put` and `take`, so they always
  serialize on it, even though each individual operation is cheap. → Follow-up: *Does this mean
  `LinkedBlockingQueue` is always the better choice?* Not always -- it has per-node allocation
  overhead and worse cache locality than a contiguous array, and if bounded capacity + a hard
  memory ceiling matters more than raw throughput, `ArrayBlockingQueue`'s simpler, more
  predictable footprint can still be preferable.
- [Intermediate] What real-world production incident does an unbounded
  `new LinkedBlockingQueue<>()` risk causing, and how do you prevent it? → If producers
  persistently outpace consumers (e.g. a downstream service slows down), the queue grows without
  bound, consuming heap until an `OutOfMemoryError` -- often only surfacing under peak
  production load, not in lighter testing. Prevent it by always specifying an explicit capacity
  and deciding an overflow policy (reject, drop-oldest, block the producer) up front. →
  Follow-up: *What would you check if you suspected this was already happening in a running
  system?* Monitor the queue's `size()` over time (or JVM heap growth trending with GC pressure)
  and compare producer vs. consumer throughput rates.
- [Intermediate] How does `DelayQueue` decide which element is "ready" to be returned by
  `poll()`/`take()`? → Elements implement `Delayed`, whose `getDelay(TimeUnit)` reports
  remaining time until expiry; `DelayQueue` keeps elements in a heap ordered by that remaining
  delay (via `compareTo`), and an element is only eligible for removal once `getDelay() <= 0`. →
  Follow-up: *What does `poll()` (non-blocking) return if the queue has elements but none have
  expired yet?* `null` -- being "present" in the queue and being "ready" are different
  conditions; `poll()` never returns a not-yet-expired element even if the queue is non-empty.
- [Intermediate] What is lock fairness, and what's the trade-off in enabling it on
  `ArrayBlockingQueue`/`SynchronousQueue`? → A fair lock (`ReentrantLock(true)`) guarantees
  threads acquire it in the order they began waiting (FIFO), preventing thread starvation under
  contention; the trade-off is meaningfully lower throughput, since it disables "barging" (a
  newly-arriving thread grabbing a just-released lock immediately, which is usually faster on
  average but can starve long-waiting threads). → Follow-up: *When would you actually enable
  it?* When predictable per-request latency (no thread waiting arbitrarily long while others
  repeatedly barge ahead) matters more than maximum aggregate throughput -- e.g. a
  latency-sensitive request queue with an SLA per request.
- [Advanced] Why does `PriorityBlockingQueue.put()` never block, unlike `put()` on
  `ArrayBlockingQueue`/`LinkedBlockingQueue`(bounded)? → `PriorityBlockingQueue` has no capacity
  ceiling at all (it's always effectively unbounded, growing its internal heap array as needed),
  so there's never a "full" condition for `put()` to block on -- the only blocking behaviour in
  this class is `take()` waiting for the queue to become non-empty. → Follow-up: *Is
  "never blocks on put" always a good thing?* No -- it means `PriorityBlockingQueue` provides no
  natural backpressure mechanism; if producers vastly outpace consumers, memory grows without
  bound just like an unbounded `LinkedBlockingQueue`, just via heap-array growth instead of node
  allocation.
- [Advanced] Compare the concurrency-control strategy of `ArrayBlockingQueue` (single lock),
  `LinkedBlockingQueue` (two locks), and `ConcurrentLinkedQueue` (not a `BlockingQueue`, but
  worth contrasting -- lock-free via CAS). What determines which is appropriate? → Single-lock
  designs are simplest and sufficient when contention is moderate and simplicity/predictability
  matters (`ArrayBlockingQueue`); two-lock designs improve throughput specifically for the
  producer/consumer-at-opposite-ends access pattern by letting head and tail operations proceed
  independently (`LinkedBlockingQueue`); fully lock-free CAS-based designs
  (`ConcurrentLinkedQueue`) avoid blocking/locking entirely for maximum throughput under very
  high contention, at the cost of not supporting blocking operations at all (no `put`/`take`) --
  you'd use it only when you need a non-blocking concurrent queue and can poll/retry instead of
  waiting. → Follow-up: *Why can't a fully lock-free design easily support blocking `take()`?*
  Blocking requires parking a thread and later waking it (via `Condition`/`LockSupport`), which
  inherently needs some coordination structure beyond pure CAS loops on the data structure
  itself -- lock-free structures are built for "never block, just retry," which is a different
  contract than `BlockingQueue` promises.

## 8. Exercises

No dedicated numbered exercises for this topic (concurrency correctness is hard to unit-test
deterministically at this level) -- the runnable
`src/main/java/com/gk/study/queuedeque/examples/BlockingQueueDemo.java` is the hands-on artifact:
read it, run it, and try modifying the capacity/delay values to observe the blocking behaviour
change.

## 9. Quick recap

- `BlockingQueue` adds `put`/`take` (block indefinitely) and timed `offer`/`poll` variants on
  top of `Queue`'s throw/sentinel pair.
- `ArrayBlockingQueue`: fixed capacity, circular array, **one** lock (producers/consumers
  serialize). `LinkedBlockingQueue`: optionally bounded (default unbounded!), linked nodes,
  **two** locks (higher throughput under contention).
- `PriorityBlockingQueue`: unbounded heap, `take()` blocks, `put()` never blocks.
  `DelayQueue`: unbounded heap ordered by remaining delay; elements surface only once expired.
  `SynchronousQueue`: zero capacity, direct hand-off only -- used by `newCachedThreadPool`.
- Always give `LinkedBlockingQueue` (and any queue you can) an explicit bound in production --
  the unbounded default is a classic slow-motion `OutOfMemoryError` trap.
- Fairness (`fair=true`) trades throughput for FIFO-ordered waiter access; default is unfair
  (higher throughput, no ordering guarantee under contention).
