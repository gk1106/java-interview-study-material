# Cheat Sheet — 04: Queue / Deque

One-page pre-interview skim. Full notes: `notes/04-queue-deque/`.

## The throw-vs-sentinel duality (Queue contract)

| Action | Throws on failure | Returns sentinel on failure |
|---|---|---|
| insert | `add` → `IllegalStateException` | `offer` → `false` |
| remove head | `remove` → `NoSuchElementException` | `poll` → `null` |
| examine head | `element` → `NoSuchElementException` | `peek` → `null` |

Same duality applies at **both ends** of a `Deque` (`addFirst`/`offerFirst`, etc.).

## Complexity table

| Structure | insert | remove | peek | Notes |
|---|---|---|---|---|
| `ArrayDeque` (both ends) | O(1) amortized | O(1) | O(1) | circular array, doubling growth, rejects null |
| `PriorityQueue` | O(log n) offer | O(log n) poll | O(1) | binary heap; `contains`/`remove(Object)` O(n) |
| `ArrayBlockingQueue` | O(1) | O(1) | O(1) | bounded, **1 lock** |
| `LinkedBlockingQueue` | O(1) | O(1) | O(1) | optionally bounded (default unbounded!), **2 locks** |
| `PriorityBlockingQueue` | O(log n) | O(log n) | O(1) | unbounded heap, `take()` blocks, `put()` never blocks |
| `SynchronousQueue` | O(1) hand-off | O(1) hand-off | n/a | capacity 0, no storage — `put` blocks until a `take` arrives |
| `DelayQueue` | O(log n) | O(log n) | n/a | unbounded, elements surface only once delay expires |

Build heap from n elements (bulk constructor) = O(n) (heapify), **not** O(n log n).

## Most-likely-asked facts

1. `ArrayDeque` = one `Object[]` + `head`/`tail` indices as a circular buffer; growth is **doubling**, triggered the instant `head==tail` after insert (that state would otherwise be ambiguous with empty).
2. `PriorityQueue`: children at `2i+1`/`2i+2`, parent at `(i-1)/2`; `offer` = append + sift-up, `poll` = move-last-to-root + sift-down.
3. **Iteration/`toArray()` order on `PriorityQueue` is heap-array order, NOT sorted** — only repeated `poll()` gives sorted output. Single most common trap for this class.
4. No `Comparable`/no `Comparator` on `PriorityQueue<T>` compiles fine, throws `ClassCastException` at the **first** `offer()`.
5. `ArrayBlockingQueue` = 1 lock (producers/consumers serialize on it); `LinkedBlockingQueue` = 2 locks (usually higher throughput under contention) but defaults to **unbounded** — always give it an explicit capacity in production.
6. `SynchronousQueue` backs `Executors.newCachedThreadPool()`'s work queue.
7. Fairness (`fair=true`) trades throughput for FIFO-ordered waiter access — not the default, enable only when starvation is a real problem.
8. `ArrayDeque` forbids `null` (fails fast on `add(null)`) unlike `LinkedList`, specifically so `poll()`/`peek()` returning `null` unambiguously means "empty."
9. `AbstractQueue.add()` is implemented in terms of `offer()` by default, throwing `IllegalStateException("Queue full")` on a `false` result.
10. Top-K with a heap must be a **min**-heap bounded to size k even when finding the k **largest** — the heap needs to know the smallest-of-the-kept to evict.

## Top pitfalls

- **Assuming `PriorityQueue` iteration is sorted** — it's raw heap order; `poll()` in a loop is the only way to get sorted output.
- **Forgetting a Comparator for a non-Comparable element type** — compiles, `ClassCastException` at runtime.
- **Mutating a field the comparator reads on an object already in the heap** — doesn't re-sift; heap invariant silently breaks. Fix: `remove` then re-`offer`.
- **`new LinkedBlockingQueue<>()` (no-arg) in production** — unbounded by default, classic slow-motion OOM under sustained producer overload.
- **Marking BFS nodes visited at dequeue time instead of enqueue time** — inflates queue size with duplicate enqueues; mark visited at enqueue.
- **Popping the wrong end of a monotonic deque** — back = no-longer-max candidates, front = out-of-window candidates; swapping these silently breaks the invariant.

## When to use / not use

- `ArrayDeque` is the modern default for both stack (`push`/`pop`) and FIFO queue (`offer`/`poll`) use — preferred over legacy `Stack`/`Vector` and over `LinkedList` (no per-node allocation).
- Reach for `BlockingQueue` family before hand-rolling `wait`/`notify` producer-consumer coordination.

## DSA patterns (module 04 §5) — Big-O leverage

| Pattern | Time/Space | Turns what into what |
|---|---|---|
| Stack via Deque | O(n) | brackets, next-greater-element — LIFO for "most recent unresolved" |
| BFS with a queue | O(V+E) | shortest path, unweighted graphs/grids |
| Monotonic deque | O(n) | O(n·k) window re-scan → O(n) sliding-window extremum |
| Top-K heap | O(n log k) | O(n log n) full sort → O(n log k) |
| Merge-K-sorted heap | O(n log k) | O(n·k) naive pairwise merge → O(n log k) |
