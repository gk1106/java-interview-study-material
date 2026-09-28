# Queue interface

## 1. What it is

`Queue<E>` extends `Collection<E>` and adds a FIFO-oriented contract for three operations --
insert, remove-the-head, examine-the-head -- each with **two** method variants: one that
**throws** on failure, one that returns a **sentinel value**. `Deque<E>` (topic 2) extends
`Queue` and adds explicit first/last-end operations; `PriorityQueue` (topic 3) and the
`BlockingQueue` family (topic 4) are the other major implementations you'll actually use.

## 2. How it works internally

`Queue` itself holds no state -- it's a pure interface. The behaviour worth memorizing is the
method-family contract, because it trips people up constantly in interviews and in real code:

```
              insert            remove head        examine head
throws:       add(e)            remove()            element()
sentinel:     offer(e)          poll()              peek()
```

- **`add(e)`**: returns `true` on success (per `Collection.add`'s contract); throws
  `IllegalStateException` if the queue is **capacity-restricted and full** (e.g.
  `ArrayBlockingQueue`). For unbounded queues (`ArrayDeque`, `LinkedList`) `add` essentially
  never fails from capacity.
- **`offer(e)`**: returns `false` instead of throwing when the insert can't happen (full
  capacity-restricted queue). Same success behaviour as `add` otherwise.
- **`remove()`**: removes and returns the head; throws `NoSuchElementException` if the queue is
  **empty**.
- **`poll()`**: removes and returns the head, or returns **`null`** if the queue is empty --
  never throws for emptiness.
- **`element()`**: returns (without removing) the head; throws `NoSuchElementException` if
  empty.
- **`peek()`**: returns the head without removing it, or `null` if empty.

**Why null is reserved:** most `Queue` implementations (`ArrayDeque`, `PriorityQueue`, the
`BlockingQueue`s) **prohibit `null` elements**, throwing `NullPointerException` from
`add`/`offer`/`put` if you try to insert one. This is deliberate: `null` is the sentinel
`poll()`/`peek()` use to signal "empty." If `null` were a legal element, `poll()` returning
`null` would be ambiguous between "the queue is empty" and "the head element genuinely is
null." `LinkedList` (which is simultaneously a `List` and a `Deque`) technically still permits
`null` because it's list-based and predates this convention being emphasized, but relying on
that is a well-known footgun -- avoid it.

**Rule of thumb interviewers expect:** prefer `offer`/`poll`/`peek` over `add`/`remove`/`element`
whenever the queue might be capacity-restricted or might be empty at the call site, since
handling a `false`/`null` return is usually cheaper and clearer than a `try/catch` for a
routine, expected condition (exceptions should signal exceptional situations, not routine
"nothing there yet" checks).

## 3. Complexity

Complexity is entirely implementation-dependent (that's the point of an interface) -- see
topics 2-4 for `ArrayDeque`, `PriorityQueue`, and the `BlockingQueue` family specifically. The
contract itself adds no algorithmic cost: `offer`/`poll`/`peek` and `add`/`remove`/`element` are
the same underlying operation, just with a different failure-signaling convention.

| Method pair | On success | On failure (full / empty) |
|---|---|---|
| `add` / `offer` | identical | `add` throws `IllegalStateException`; `offer` returns `false` |
| `remove` / `poll` | identical | `remove` throws `NoSuchElementException`; `poll` returns `null` |
| `element` / `peek` | identical | `element` throws `NoSuchElementException`; `peek` returns `null` |

## 4. Example code

- Runnable class: `src/main/java/com/gk/study/queuedeque/examples/QueueInterfaceDemo.java`

```java
Queue<Integer> queue = new ArrayDeque<>();
System.out.println(queue.poll());   // null -- empty, no exception
queue.remove();                     // throws NoSuchElementException
```

Expected console output (abridged):
```
poll() on empty -> null
peek() on empty -> null
remove() on empty -> threw NoSuchElementException
element() on empty -> threw NoSuchElementException
queue after offer(1), add(2) -> [1, 2]
offer(3) when full -> false  (returns false, no exception)
add(3) when full -> threw IllegalStateException: Queue full
poll() -> 1
poll() -> 2
```
(The exact `IllegalStateException` message "Queue full" comes from `AbstractQueue.add`'s
default implementation, which calls `offer` and wraps a `false` result in that exception.)

## 5. When to use / when NOT to use

- Use `offer`/`poll`/`peek` as your default choice in almost all production code -- checking a
  `false`/`null` return is a normal control-flow branch, not an exceptional path.
- Use `add`/`remove`/`element` only when a failure genuinely represents a programming bug you
  *want* to fail loudly on (e.g. "this internal queue should never be empty here; if it is,
  something upstream is broken and I want a stack trace, not a silently swallowed null").
- Don't reach for `Queue` when you need random access by index (`get(i)`) -- that's a `List`
  concern; `Queue`/`Deque` are about the two ends only.

## 6. Common pitfalls & gotchas

**Treating `poll()`'s `null` as "the element was null" instead of "the queue was empty":**
```java
Queue<String> q = new LinkedList<>(); // LinkedList allows null elements (unlike ArrayDeque)
q.offer(null);
String head = q.poll();
if (head == null) {
    // is the queue now empty, or did we just remove a genuine null element? Ambiguous!
}
// fix: avoid nulls in queues entirely; use ArrayDeque (throws NPE on offer(null), forcing you
// to catch this at insertion time instead of discovering it later)
```

**Mixing `add`/`remove` with an unbounded queue and assuming they "can't fail," then porting the
code to a `BlockingQueue`-based design without re-checking:**
```java
Queue<Task> tasks = new ArrayDeque<>();      // add() essentially never throws here
tasks.add(task);
// later, someone swaps in: Queue<Task> tasks = new ArrayBlockingQueue<>(100);
// now add() CAN throw IllegalStateException under load -- a latent bug introduced by the swap
```

**Forgetting that `remove()`/`element()` throw on an *empty* queue, not just a *null-full*
one** -- a common mistake is guarding only against `null` elements and forgetting the "queue has
zero elements" case entirely, leading to an uncaught `NoSuchElementException` in production.

## 7. Interview questions

- [Basic] What's the difference between `add(e)` and `offer(e)`? → Both insert `e`; `add`
  throws `IllegalStateException` if a capacity-restricted queue is full, while `offer` returns
  `false` instead. For unbounded queues both essentially always succeed. → Follow-up: *Which
  should you prefer in production code?* `offer`, since a full queue during normal operation
  (e.g. backpressure) is an expected condition, not an exceptional one.
- [Basic] What does `poll()` return on an empty queue, and how does that differ from
  `remove()`? → `poll()` returns `null`; `remove()` throws `NoSuchElementException`. →
  Follow-up: *Why does the JDK offer both instead of just one?* Different callers want different
  failure-handling styles -- a `null` check is cheap control flow for "maybe nothing's there
  yet," while an exception is appropriate when emptiness should never legitimately happen.
- [Basic] Why do most `Queue` implementations forbid `null` elements? → Because `null` is the
  sentinel `poll()`/`peek()` use to signal "the queue is empty" -- allowing `null` as a real
  element would make that return value ambiguous. → Follow-up: *Does `LinkedList` allow nulls?*
  Yes, because it's `List`-based, but doing so undermines `poll()`'s sentinel semantics and is
  discouraged.
- [Intermediate] If you call `element()` on an empty `ArrayDeque`, what happens, and how would
  you avoid the exception? → It throws `NoSuchElementException`; check `isEmpty()` first, or use
  `peek()` and handle a `null` result instead. → Follow-up: *Is checking `isEmpty()` then calling
  `element()` thread-safe on a concurrent queue?* No -- between the check and the call another
  thread could drain the queue (TOCTOU race); on a `BlockingQueue` use `poll(timeout, unit)` or
  `take()` instead, which are atomic.
- [Intermediate] On a capacity-restricted queue like `ArrayBlockingQueue`, what's the difference
  in behaviour among `add`, `offer`, and `put` when the queue is full? → `add` throws
  `IllegalStateException` immediately; `offer` returns `false` immediately (no blocking); `put`
  (from `BlockingQueue`, not `Queue`) **blocks** the calling thread until space becomes
  available. → Follow-up: *Which would you use in a producer thread that should apply
  backpressure?* `put`, specifically because blocking is the desired behaviour -- it naturally
  slows the producer down to match the consumer's pace.
- [Intermediate] Why might swapping an unbounded `ArrayDeque`-backed queue for a bounded
  `ArrayBlockingQueue` introduce a bug if the code used `add()`? → `add()` on the unbounded
  queue essentially never throws, so existing code may have no `try/catch`; once bounded, `add()`
  can throw `IllegalStateException` under load, an unhandled exception that wasn't possible
  before. → Follow-up: *How would you make this swap safely?* Switch to `offer()` (or `put()`
  with a policy for what to do while blocked) and explicitly handle the failure/backpressure
  case at the call site.
- [Advanced] Is `Queue.add(e)`'s "throws when full" behaviour part of the `Queue` interface
  contract, or implementation-specific? → It's specified generically in `Queue`'s Javadoc contract
  ("throws IllegalStateException if no space is currently available") but the actual trigger
  condition -- what "no space available" means -- is implementation-specific (unbounded queues
  essentially never hit it; capacity-restricted ones do). → Follow-up: *Does `AbstractQueue`
  provide a default `add()` for implementers?* Yes -- `AbstractQueue.add(e)` is implemented in
  terms of `offer(e)`, throwing `IllegalStateException("Queue full")` if `offer` returns `false`,
  so most implementations only need to implement `offer` correctly to get `add` for free.
- [Advanced] Why does the JDK design `Queue` with two parallel method families instead of one
  method that always throws (forcing callers to catch) or one that always returns a sentinel
  (forcing callers to null-check even when they know it can't be empty)? → It's a deliberate
  API-ergonomics trade-off: forcing exceptions everywhere makes normal, expected conditions
  (like "queue is temporarily empty in a producer-consumer loop") awkward and slow (exception
  construction/stack-trace capture has real cost); forcing sentinel-only checks makes genuine
  programming-error conditions silently swallow-able. Offering both lets each call site pick the
  semantics that match whether the failure is "expected" or "a bug." → Follow-up: *Does this
  dual-method-family pattern appear elsewhere in the JDK?* Yes -- e.g. `Map.get(key)` (returns
  `null`) vs. nothing throwing equivalent directly, but `Optional`-returning APIs in `Map`
  (`getOrDefault`) and `Deque`'s own `getFirst()`/`peekFirst()` pair follow the same
  throw-vs-sentinel duality.

## 8. Exercises

No dedicated exercises for this topic -- the throw-vs-sentinel contract is foundational and gets
exercised implicitly throughout topics 2-6 wherever code calls `poll()`, `offer()`, `peek()`,
etc. See `notes/04-queue-deque/05-dsa-patterns-queue-deque.md` for the module's full exercise
set.

## 9. Quick recap

- Two parallel method families: throwing (`add`/`remove`/`element`) vs. sentinel-returning
  (`offer`/`poll`/`peek`) for insert/remove-head/examine-head respectively.
- `add` throws `IllegalStateException` only on capacity-restricted, full queues; `remove`/
  `element` throw `NoSuchElementException` on any empty queue.
- `poll`/`peek` return `null` for "empty" -- which is exactly why most implementations forbid
  `null` as a real element (ambiguity with the empty sentinel).
- Default to `offer`/`poll`/`peek` in production code; reserve `add`/`remove`/`element` for
  cases where failure truly indicates a bug.
- `AbstractQueue.add()` is implemented in terms of `offer()` by default, throwing
  `IllegalStateException("Queue full")` on a `false` result.
