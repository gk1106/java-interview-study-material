# Synchronizers: CountDownLatch, CyclicBarrier, Semaphore, Phaser

## 1. What it is

The `java.util.concurrent` synchronizers are ready-made coordination primitives for common
multi-thread patterns that would otherwise need hand-rolled `wait`/`notify` logic: "wait until N
things have happened" (`CountDownLatch`), "wait until N threads all reach the same point, then let
them all go together, repeatedly" (`CyclicBarrier`), "limit how many threads can use a resource
concurrently" (`Semaphore`), and "coordinate phases of work with a dynamically changing number of
participants" (`Phaser`).

## 2. How it works internally

### `CountDownLatch` — a one-shot gate

Built on `AbstractQueuedSynchronizer` (AQS, the same framework backing `ReentrantLock`), a
`CountDownLatch` wraps a single AQS `state` int initialized to the given count. `countDown()`
decrements it (CAS loop); `await()` blocks (parking on AQS's wait queue) until `state` reaches
zero, then **every** waiting thread is released simultaneously. Critically: **it cannot be reset**.
Once it reaches zero, it stays at zero forever — `await()` on an already-zero latch returns
immediately, and `countDown()` on an already-zero latch is a no-op.

Two classic usages:
- **"Wait for N workers to finish"**: main thread creates `new CountDownLatch(n)`, each worker
  calls `countDown()` when done, main thread calls `await()` (optionally with a timeout).
- **"Start N threads at exactly the same instant"**: a `startGate` latch of count 1; every worker
  thread calls `startGate.await()` before doing anything; the main thread calls
  `startGate.countDown()` once, releasing all workers simultaneously — this is how you write a
  genuinely concurrent "all threads hit this line together" test, instead of hoping `Thread.sleep`
  lined them up.

```java
CountDownLatch startGate = new CountDownLatch(1);
CountDownLatch doneGate = new CountDownLatch(workerCount);
for (int i = 0; i < workerCount; i++) {
    pool.submit(() -> {
        startGate.await();       // all workers wait here until released together
        doWork();
        doneGate.countDown();
    });
}
startGate.countDown();           // release all workers at once
doneGate.await(5, TimeUnit.SECONDS);   // wait for all to finish, bounded
```

### `CyclicBarrier` — a reusable rendezvous point

Where `CountDownLatch` is "N events, one gate, never resets," `CyclicBarrier` is "N *threads*
(not events) must all arrive at the same point before any of them proceeds — and then the barrier
automatically resets for the next round." Constructed with the party count and an optional
`Runnable` **barrier action**, run by whichever thread happens to be the *last* to arrive, exactly
once per round, before releasing everyone. Useful for phased computations (e.g., N worker threads
each finish phase 1 of a simulation, all wait at the barrier, one thread aggregates/logs, then all
proceed to phase 2 together — repeating each round).

```java
CyclicBarrier barrier = new CyclicBarrier(workerCount, () -> System.out.println("all arrived, phase complete"));
// each worker thread, each phase:
doPhaseWork();
barrier.await();   // blocks until all `workerCount` threads have called await() this round
// barrier auto-resets; can be reused next phase
```
If any thread times out, is interrupted, or the barrier is explicitly `reset()` while others are
waiting, every other waiting thread wakes with a `BrokenBarrierException` — the barrier is a
**fail-together** primitive by design: a partial round is treated as invalid for everyone.

### `Semaphore` — a counting permit pool

A `Semaphore` maintains an internal permit count (again AQS-based `state`, CAS-decremented on
`acquire()`, CAS-incremented on `release()`). `acquire()` blocks if no permits are available;
`release()` returns a permit, waking one blocked acquirer if any are waiting. Unlike a lock, a
`Semaphore` has **no notion of ownership** — any thread can call `release()`, even one that never
called `acquire()` (this is a footgun and a feature: it lets you build patterns where a
producer-like thread "creates" permits it never itself holds).

- **Binary semaphore** (`new Semaphore(1)`) behaves like a lock, but *without* reentrancy and
  *without* ownership checking — a thread can `release()` a permit it never acquired, which
  `ReentrantLock.unlock()` would reject with `IllegalMonitorStateException`.
- **Counting semaphore** (`new Semaphore(n)`) is the classic tool for bounding concurrent access to
  a limited resource pool — e.g., "at most 10 concurrent connections to this downstream service,"
  independent of how many threads are trying.
- **Fairness**: like `ReentrantLock`, `new Semaphore(n, true)` gives FIFO ordering to waiting
  acquirers; default is unfair/barging for throughput.

```java
Semaphore connectionLimiter = new Semaphore(10);   // at most 10 concurrent downstream calls
void callDownstream() throws InterruptedException {
    connectionLimiter.acquire();
    try {
        doCall();
    } finally {
        connectionLimiter.release();   // MUST be in finally, exactly like a Lock
    }
}
```

### `Phaser` — a flexible, dynamic-party barrier

`Phaser` generalizes `CyclicBarrier`: parties can be **registered and deregistered dynamically**
(`register()`/`bulkRegister(n)`/`arriveAndDeregister()`), and it supports both synchronous
("`arriveAndAwaitAdvance()`" — block like a barrier) and asynchronous
("`arriveAndDeregister()`"/"`arrive()`" — signal arrival without blocking, for a party that's
leaving the coordination entirely) participation. Each completed round is a **phase**
(`getPhase()` returns an incrementing int). It's the right tool when the number of participating
threads isn't fixed up front, or when different threads need different levels of
synchronous/asynchronous involvement across phases — `CyclicBarrier`'s party count is fixed at
construction and can't change. In practice `Phaser` is rarely reached for in application code
(most phased-computation needs are served by `CyclicBarrier`); know it exists and what problem it
solves for interviews, but default to `CyclicBarrier` when the party count is fixed.

### Choosing between them

| Need | Use |
|------|-----|
| Wait for N one-off events/tasks to complete, then proceed (never repeats) | `CountDownLatch` |
| Release N waiting threads at once, exactly one time | `CountDownLatch(1)` as a start gate |
| N threads repeatedly rendezvous at a shared point, round after round | `CyclicBarrier` |
| Limit concurrent access to a bounded resource/pool | `Semaphore` |
| Phased coordination with a dynamically changing number of participants | `Phaser` |

### ASCII diagram — CountDownLatch vs CyclicBarrier

```
CountDownLatch(3)                         CyclicBarrier(3)
  worker1 -- countDown() -- count=2         worker1 -- await() -- waits
  worker2 -- countDown() -- count=1         worker2 -- await() -- waits
  worker3 -- countDown() -- count=0         worker3 -- await() -- 3rd arrival!
  main    -- await() returns immediately      -> barrier action runs once
  (latch now permanently at 0,                 -> all 3 released together
   countDown()/await() again are no-ops)       -> barrier auto-resets for next round
```

## 3. Complexity

| Operation | Time | Notes |
|-----------|------|-------|
| `CountDownLatch.countDown()` | O(1) | CAS decrement |
| `CountDownLatch.await()` (count already 0) | O(1) | returns immediately |
| `CountDownLatch.await()` (count > 0) | blocks until 0 or timeout | releases all waiters at once when it hits 0 |
| `CyclicBarrier.await()` | O(1) bookkeeping, blocks until all parties arrive | barrier action runs once per round, on the last-arriving thread |
| `Semaphore.acquire()`/`release()` | O(1) amortized (CAS) | blocks if no permits available |
| `Phaser.arriveAndAwaitAdvance()` | O(1) bookkeeping, blocks until phase advances | supports dynamic registration, unlike `CyclicBarrier` |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/SynchronizersDemo.java` —
  demonstrates a `CountDownLatch` start-gate proving simultaneous release, a `CyclicBarrier` running
  two rendezvous rounds with a barrier action, a `Semaphore` proving it never allows more than N
  concurrent holders via a live-holder counter, and a brief `Phaser` phase-advance example.

```java
CountDownLatch startGate = new CountDownLatch(1);
CountDownLatch allStarted = new CountDownLatch(workerCount);
// each worker signals it's ready, then waits on the shared start gate
```
Expected console output (abbreviated):
```
CountDownLatch: all 5 workers released simultaneously after countDown() = true
CyclicBarrier: round 1 barrier action ran once, all 4 threads proceeded together
CyclicBarrier: round 2 barrier action ran once, all 4 threads proceeded together
Semaphore: peak concurrent holders observed = 3 (never exceeded permit count of 3)
Phaser: advanced from phase 0 to phase 1 after all registered parties arrived
```

## 5. When to use / when NOT to use

- Use `CountDownLatch` for one-shot "wait for N things" coordination — task completion tracking,
  or a start gate to make a test/benchmark genuinely concurrent instead of relying on
  `Thread.sleep` timing.
- Use `CyclicBarrier` for repeated phased computation where every participant must finish a phase
  before any proceeds to the next (parallel simulations, iterative algorithms split across
  threads).
- Use `Semaphore` to bound concurrent access to a limited resource (connection pools, rate-limited
  downstream calls) — independent of which specific thread "owns" a permit.
- Reach for `Phaser` only when the participant count genuinely changes at runtime or you need mixed
  synchronous/asynchronous phase participation — otherwise its extra flexibility just adds
  complexity over `CyclicBarrier`.
- For simple producer/consumer handoff, prefer a `BlockingQueue` (topic 9) over hand-rolling
  coordination with these primitives — they solve *coordination points*, not general data transfer.

## 6. Common pitfalls & gotchas

**Trying to reuse a `CountDownLatch` after it reaches zero**:
```java
CountDownLatch latch = new CountDownLatch(1);
latch.countDown();
// ... later, wanting to coordinate again ...
latch.await();   // BUG: returns immediately forever -- CountDownLatch cannot be reset
// fix: construct a NEW CountDownLatch for each round, or use CyclicBarrier if truly cyclic
```

**Forgetting `Semaphore.release()` in `finally`**:
```java
semaphore.acquire();
doWork();               // BUG: if this throws, the permit is never returned -- permanent leak,
semaphore.release();     // shrinking the effective pool size by one, forever
// fix:
semaphore.acquire();
try { doWork(); } finally { semaphore.release(); }
```

**Assuming a `CyclicBarrier` failure (timeout/interrupt on one thread) doesn't affect the others**:
```java
// BUG assumption: "only the thread that timed out is affected"
// reality: EVERY other thread currently waiting at that barrier immediately wakes with
// BrokenBarrierException -- one thread's failure breaks the round for everyone
```

**Calling `Semaphore.release()` more times than `acquire()`** — legal (no ownership tracking), but
silently inflates the permit count beyond what was intended, defeating the concurrency bound:
```java
Semaphore sem = new Semaphore(3);
sem.release();   // BUG: no matching acquire() -- permit count is now 4, not 3, silently
```

## 7. Interview questions

- [Basic] What's the key difference between `CountDownLatch` and `CyclicBarrier`? →
  `CountDownLatch` counts down *events* (any thread can call `countDown()`, possibly more times
  than there are waiters) and can never be reset once it hits zero; `CyclicBarrier` waits for a
  fixed number of *threads* to all call `await()`, then automatically resets for reuse in the next
  round. → Follow-up: *Can a CountDownLatch be reused for a second round of coordination?* No —
  you must create a new one; there is no reset method.
- [Basic] What does `Semaphore` coordinate, and how many threads can call `acquire()`
  successfully at once? → It coordinates access to a limited number of "permits"; up to the
  configured permit count can `acquire()` without blocking simultaneously, and any further
  `acquire()` calls block until a permit is `release()`d. → Follow-up: *Does a Semaphore track
  which thread holds which permit?* No — it has no ownership concept; any thread can call
  `release()`, even one that never called `acquire()`.
- [Basic] How would you make a JUnit-style concurrency test start N threads at exactly the same
  moment instead of hoping `Thread.sleep` lines them up? → Use a `CountDownLatch(1)` as a "start
  gate": every worker thread calls `startGate.await()` first, and the test releases them all at
  once with a single `startGate.countDown()`. → Follow-up: *Why is this more reliable than
  Thread.sleep-based coordination?* It's a real synchronization point with a happens-before
  guarantee, not a timing guess — every worker is guaranteed to be parked and ready before any of
  them proceeds, regardless of scheduling variance.
- [Intermediate] Explain the "barrier action" in `CyclicBarrier` — who runs it, and when? → The
  optional `Runnable` passed to the constructor runs exactly once per round, executed by whichever
  thread happens to be the *last* one to call `await()` for that round, immediately before all
  waiting threads (including that last one) are released. → Follow-up: *What's a practical use of
  the barrier action?* Aggregating/logging per-phase results (e.g., summing partial results from
  each worker) exactly once per round, without needing a separate coordinating thread.
- [Intermediate] Why must `Semaphore.release()` always be paired with `acquire()` in a
  `try/finally`, just like a `Lock`? → Because a permit that's acquired but never released due to
  an exception permanently shrinks the effective concurrency limit — over time, repeated leaks can
  reduce the semaphore's real capacity to zero, silently deadlocking all future acquirers even
  though nothing looks obviously wrong. → Follow-up: *Is there a bounded-wait alternative to plain
  acquire()?* Yes — `tryAcquire(timeout, unit)`, analogous to `Lock.tryLock(timeout, unit)`.
- [Intermediate] What happens to the other waiting threads if one thread times out or is
  interrupted while waiting at a `CyclicBarrier`? → The barrier is "broken" for that round — every
  other thread currently blocked in `await()` immediately wakes and throws
  `BrokenBarrierException`, even though they themselves didn't time out or get interrupted; this is
  intentional fail-together semantics, since a partial round (some threads proceeding without
  others having actually reached the rendezvous point) would violate the barrier's whole purpose. →
  Follow-up: *How would you recover a broken CyclicBarrier for a later round?* Call `reset()` on
  it — but any threads still waiting on it when `reset()` is called also get
  `BrokenBarrierException`; care is needed to only reset once all threads have observed the failure.
- [Intermediate] When would you reach for `Phaser` instead of `CyclicBarrier`? → When the number of
  participating threads changes dynamically at runtime (workers can register/deregister between or
  even during phases) or when some participants need only asynchronous "I've arrived" signaling
  without blocking, rather than every participant needing to fully block-and-wait like
  `CyclicBarrier` requires. → Follow-up: *Why is CyclicBarrier still the more common choice in
  practice?* Most phased-computation scenarios have a fixed, known party count decided up front, so
  `Phaser`'s extra flexibility (and API complexity) isn't needed.
- [Advanced] Design a downstream-call rate limiter using `Semaphore` that also needs a bounded
  wait instead of blocking indefinitely if the downstream is overloaded. → `Semaphore limiter = new
  Semaphore(maxConcurrent); if (limiter.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) { try {
  callDownstream(); } finally { limiter.release(); } } else { /* fail fast / reject / fallback */ }`
  — `tryAcquire(timeout, unit)` gives bounded waiting instead of an unbounded `acquire()`, letting
  the caller degrade gracefully (reject, return cached data, etc.) instead of piling up blocked
  threads indefinitely when the downstream is genuinely saturated. → Follow-up: *How does this
  compare to a ThreadPoolExecutor's bounded queue + rejection policy (topic 6) for the same goal?*
  Conceptually similar backpressure, but a `Semaphore` bounds *concurrent in-flight calls*
  regardless of how those calls are executed (could be on the caller's own thread), whereas a
  `ThreadPoolExecutor`'s queue/rejection bounds *queued+running tasks specifically submitted to that
  pool* — a `Semaphore` is often layered on top of an executor to add a concurrency ceiling
  independent of thread pool sizing (e.g., "run on any of my 50 threads, but never more than 10
  concurrent calls to this specific fragile downstream").
- [Advanced] Why can `Semaphore.release()` be called by a thread that never called `acquire()`, and
  when is that actually useful? → Because `Semaphore` tracks only a permit *count*, not per-thread
  ownership (unlike `ReentrantLock`, which tracks the owning thread and hold count) — this is a
  deliberate design choice that enables patterns where permits are "produced" by one role and
  "consumed" by another, e.g., a producer thread calling `release()` to signal "one more unit of
  work is available" for consumer threads that `acquire()` — effectively using the semaphore's
  permit count itself as a lightweight counting signal, similar in spirit to (though less flexible
  than) a `BlockingQueue`. → Follow-up: *What's the risk of this flexibility?* An accidental extra
  `release()` call (a bug, not a deliberate producer signal) silently inflates the permit count with
  no error raised, quietly defeating the concurrency bound the semaphore was meant to enforce.

## 8. Exercises

No dedicated exercises for this topic — `notes/09-multithreading-concurrency/10-classic-concurrency-problems.md`'s
producer-consumer exercises apply these same coordination ideas end to end.

## 9. Quick recap

- `CountDownLatch`: one-shot, counts down events, cannot reset — ideal for "wait for N things" or
  a simultaneous start gate.
- `CyclicBarrier`: waits for a fixed number of *threads* to all arrive, runs an optional barrier
  action once per round, then auto-resets — ideal for repeated phased computation.
- `Semaphore`: bounds concurrent access via a permit count with no ownership tracking — any thread
  can `release()`, which is both a footgun and occasionally a feature.
- `Phaser`: a flexible, dynamic-party generalization of `CyclicBarrier` — reach for it only when
  the participant count changes at runtime.
- All four are AQS-based under the hood, same family as `ReentrantLock` — CAS-based state,
  park/unpark waiting threads via `LockSupport`.
