# PriorityQueue internals

## 1. What it is

`PriorityQueue<E>` is an unbounded queue backed by a **binary heap** stored in an array: instead
of FIFO order, `poll()` always returns the **smallest** element according to natural ordering
(elements must implement `Comparable`) or a supplied `Comparator`. It's the standard tool for
"repeatedly get the min/max so far" problems -- top-K, Dijkstra, merge-K-sorted, scheduling.

## 2. How it works internally

A **binary heap** is a complete binary tree (every level full except possibly the last, filled
left to right) satisfying the **heap-order invariant**: every parent is `<=` both of its
children (min-heap; a max-heap flips the comparison). A complete binary tree can be stored
compactly in a flat array with no pointers at all, using index arithmetic:

```
for a node at array index i (0-based):
  left child  = 2*i + 1
  right child = 2*i + 2
  parent      = (i - 1) / 2   (integer division)

Tree view:                    Array view (same heap):
        1                     index: 0  1  2  3  4  5
      /   \                   value: 1  3  2  5  9  8
     3     2
    / \   /
   5   9 8
```

**`offer(e)` -- insert, then sift up ("bubble up" / "percolate up"):**
1. Append `e` at the end of the array (index `size`), increment `size`.
2. Compare it with its parent; if it's smaller (per the comparator), swap them and repeat from
   the new position; stop when the parent is smaller-or-equal, or the root is reached.

**`poll()` -- remove the root, then sift down ("percolate down"):**
1. Save `array[0]` (the min) to return.
2. Move the **last** element in the array into index 0, shrink `size` by one.
3. Compare the new root with its children; swap with the **smaller** child if it violates the
   heap property; repeat from the new position until both children are `>=` it or it has no
   children.

Both operations touch at most the tree's height, so both are **O(log n)**.

**`peek()` is O(1)** -- the minimum is always at `array[0]` by the heap invariant.

**No O(1) `contains`/`indexOf`, and `remove(Object)` is O(n):** the heap invariant only
constrains parent-vs-child relationships; **siblings and unrelated nodes are not ordered
relative to each other at all**. So finding an arbitrary value requires a full linear scan; once
found, removing it from the middle requires moving the last element into its place and then
sifting in **whichever direction restores the invariant** (up if the replacement is smaller than
its new parent, down if larger than a child).

**Iteration order is NOT sorted order** -- this is one of the most common interview/production
gotchas. `iterator()` walks the raw backing array in storage order (heap order), which only
guarantees "parent <= children," not any total order. Only **repeatedly calling `poll()`**
produces elements in fully sorted order. `toArray()` behaves the same way -- it returns
`Arrays.copyOf(internalArray, size)`, i.e. the exact heap-order snapshot, not a sorted copy.

**Comparator vs. natural ordering:** with no-arg construction, elements must implement
`Comparable<E>`, or every `offer` throws `ClassCastException` at runtime (not compile time --
generics erasure means this can't be caught statically for an arbitrary `E`). Passing a
`Comparator<E>` to the constructor overrides natural ordering entirely (e.g.
`new PriorityQueue<>(Comparator.reverseOrder())` for a max-heap).

**Ties have no ordering guarantee**: among equal-priority elements, `PriorityQueue` does **not**
guarantee FIFO or any other stable order -- don't rely on insertion order being preserved for
ties.

**Growth and initial capacity:** default initial capacity is 11; growth roughly doubles for
small arrays and uses a smaller multiplier for larger ones (mirroring the general JDK pattern of
being more conservative with memory as arrays get large) -- pre-size with
`new PriorityQueue<>(expectedSize)` when the final size is roughly known, to avoid repeated
copies.

**Not thread-safe**, and has no O(1) way to increase/decrease an existing element's priority (a
common need in Dijkstra's algorithm) -- you'd typically remove-and-reinsert (O(n) find + O(log
n) reinsert) or use the "lazy deletion" trick (push a new entry, ignore stale ones on poll). The
concurrent counterpart, `PriorityBlockingQueue` (topic 4), adds blocking `take()`/internal
locking but has the same heap-array core and the same O(n) `remove(Object)` limitation.

## 3. Complexity

| Operation | Time | Space | Notes |
|---|---|---|---|
| `offer(e)` | O(log n) | O(1) amortized | append + sift-up |
| `poll()` | O(log n) | O(1) | move last to root + sift-down |
| `peek()` | O(1) | O(1) | always `array[0]` |
| `contains(Object)` | O(n) | O(1) | linear scan; heap order doesn't help |
| `remove(Object)` | O(n) | O(1) | O(n) to find + O(log n) to fix the heap after |
| Build heap from n elements (bulk constructor) | O(n) | O(n) | heapify, NOT n * O(log n) = O(n log n) |
| `toArray()` / iteration | O(n) | O(n) / O(1) | raw heap-order snapshot, NOT sorted |

## 4. Example code

- Runnable class:
  `src/main/java/com/gk/study/queuedeque/examples/PriorityQueueInternalsDemo.java` -- uses the
  public, reflection-free `toArray()` (which returns the exact internal array order) to print
  the heap array after every offer/poll.

Expected console output (min-heap, offering 5, 3, 8, 1, 9, 2 in order):
```
offer(5) -> heap array = [5]
offer(3) -> heap array = [3, 5]
offer(8) -> heap array = [3, 5, 8]
offer(1) -> heap array = [1, 3, 8, 5]
offer(9) -> heap array = [1, 3, 8, 5, 9]
offer(2) -> heap array = [1, 3, 2, 5, 9, 8]

poll() -> 1  remaining heap array = [2, 3, 8, 5, 9]
poll() -> 2  remaining heap array = [3, 5, 8, 9]
poll() -> 3  remaining heap array = [5, 9, 8]
poll() -> 5  remaining heap array = [8, 9]
poll() -> 8  remaining heap array = [9]
poll() -> 9  remaining heap array = []
```
Note `[1, 3, 2, 5, 9, 8]` is NOT sorted, yet is a valid heap (index 0's children, indices 1 and
2, are 3 and 2, both >= 1; index 1's children, indices 3 and 4, are 5 and 9, both >= 3, etc.).
The demo also shows: max-heap via `Comparator.reverseOrder()` produces heap array
`[9, 8, 5, 1, 3, 2]`; and `for`-each iteration over a fresh min-heap of the same values prints
`1 3 2 5 9 8` (raw heap order) while repeated `poll()` prints `1 2 3 5 8 9` (true sorted order)
-- the exact contrast that makes the iteration-order gotcha concrete.

## 5. When to use / when NOT to use

- Use `PriorityQueue` whenever a problem needs "repeatedly fetch the current min/max," without
  needing the full collection sorted at every intermediate step -- top-K elements, merge-K-sorted
  structures, event/task scheduling by priority, Dijkstra/Prim-style greedy algorithms.
- Use a `Comparator` (not natural ordering) whenever priority isn't simply "the object's own
  `compareTo`" -- e.g. prioritizing by a field, or building a max-heap via `reverseOrder()`.
- Do NOT use `PriorityQueue` when you need the elements in sorted order as a **list/array** at
  the end and don't need incremental access -- just sort the collection directly (`O(n log n)`
  either way, but simpler and avoids the heap-array/iteration-order trap).
- Do NOT iterate a `PriorityQueue` expecting sorted output -- drain it with `poll()` instead, or
  copy to a `List` and sort that copy if you need both incremental access AND a sorted view.
- Do NOT use it when you need O(1) `contains` or frequent arbitrary-element removal/priority
  updates -- an indexed heap (heap + a hash map from element to array index, not provided by the
  JDK) or a different structure entirely (e.g. a `TreeMap`) fits better.

## 6. Common pitfalls & gotchas

**Assuming iteration order is sorted:**
```java
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 3, 8, 1, 9, 2));
for (int v : pq) {
    System.out.println(v); // NOT 1,2,3,5,8,9 -- prints raw heap-array order!
}
// fix: drain with poll() to get sorted order, or copy to a List and Collections.sort() it
```

**Forgetting a `Comparator` when `E` doesn't implement `Comparable`** -- compiles fine (raw
types / unbounded generics hide it), then throws `ClassCastException` at the first `offer()`:
```java
PriorityQueue<Task> pq = new PriorityQueue<>(); // Task doesn't implement Comparable
pq.offer(task);  // ClassCastException at runtime, not a compile error
// fix: PriorityQueue<Task> pq = new PriorityQueue<>(Comparator.comparingInt(Task::priority));
```

**Expecting FIFO among equal-priority elements** -- two elements with the same priority can come
out in either order; if stable tie-breaking matters (e.g. "process same-priority tasks in
arrival order"), add a secondary comparator key (an incrementing sequence number) rather than
relying on incidental heap behaviour.

**Trying to "update" an element's priority in place:** mutating a field that the comparator
reads, on an object already inside the heap, does NOT re-sift it -- the heap invariant silently
breaks:
```java
Task t = ...;
pq.offer(t);
t.setPriority(newPriority);  // heap is now potentially invalid; poll() may return the wrong element!
// fix: pq.remove(t); t.setPriority(newPriority); pq.offer(t);  (O(n) remove, unavoidable with
// the plain JDK PriorityQueue)
```

## 7. Interview questions

- [Basic] What data structure does `PriorityQueue` use internally? → A binary min-heap (or
  max-heap with a reversing comparator) stored as a flat array, exploiting the fact that a
  complete binary tree can be indexed without pointers: children of index `i` are `2i+1`/`2i+2`,
  parent is `(i-1)/2`. → Follow-up: *Why a binary heap instead of, say, a sorted array or a
  balanced BST?* A sorted array gives O(1) peek/poll-min but O(n) insert (shifting); a balanced
  BST gives O(log n) for everything but with pointer overhead and worse cache locality; a binary
  heap gives O(log n) insert AND O(log n) remove-min with a compact, cache-friendly array and
  O(1) peek -- the best fit for "insert anytime, always fetch the min" workloads specifically.
- [Basic] What is the time complexity of `offer()` and `poll()`, and why? → Both O(log n) --
  each operation does at most one pass from a leaf to the root (`offer`'s sift-up) or from the
  root to a leaf (`poll`'s sift-down), and a complete binary tree with n nodes has height
  O(log n). → Follow-up: *What's the complexity of `peek()`?* O(1) -- the minimum is always at
  array index 0 by the heap invariant, no traversal needed.
- [Basic] Does iterating over a `PriorityQueue` with a for-each loop return elements in sorted
  order? → No -- iteration walks the raw backing array in heap-storage order, which only
  guarantees each parent is `<=` its children, not a full sort; only repeated `poll()` calls
  produce sorted output. → Follow-up: *What does `toArray()` return, then?* The same raw
  heap-order snapshot -- it's literally `Arrays.copyOf` of the internal array, not a sorted copy.
- [Intermediate] Why does `PriorityQueue` have no O(1) `contains()`, unlike a `HashSet`? → The
  heap invariant only orders parent-child pairs; sibling subtrees and unrelated nodes have no
  ordering relationship to each other, so there's no way to binary-search or hash your way to an
  arbitrary value -- you must linearly scan all n elements. → Follow-up: *Could you add a
  `HashMap<E, Integer>` tracking each element's array index to get O(1) contains and O(log n)
  arbitrary removal?* Yes -- that's exactly the "indexed heap" pattern used in some Dijkstra
  implementations for efficient decrease-key; the plain JDK `PriorityQueue` doesn't provide this,
  so you'd build it yourself if needed.
- [Intermediate] How would you build a max-heap using `PriorityQueue`, which is a min-heap by
  default? → Pass `Comparator.reverseOrder()` (or an equivalent custom comparator) to the
  constructor: `new PriorityQueue<>(Comparator.reverseOrder())` -- this flips which element is
  considered "smallest" for heap-ordering purposes, so `poll()` now returns the largest value. →
  Follow-up: *Does this change the time complexity of any operation?* No -- it's still O(log n)
  offer/poll, O(1) peek; only *which* element ends up at the root changes.
- [Intermediate] What happens if you mutate a field that the comparator depends on, on an object
  already inside a `PriorityQueue`? → The heap invariant can silently become invalid, since
  nothing triggers a re-sift when an external mutation changes an element's relative priority --
  subsequent `poll()` calls may return incorrect results. → Follow-up: *How do you safely
  "update priority" then?* Remove the element first (`remove(Object)`, O(n)), mutate it, then
  `offer()` it again -- there's no built-in decrease-key operation.
- [Advanced] Building a `PriorityQueue` from an existing collection of n elements
  (`new PriorityQueue<>(collection)`) is O(n), not O(n log n) as you might expect from n
  individual `offer()` calls -- why? → It uses the classic **heapify** algorithm: place all
  elements into the array first (any order), then sift-down starting from the last non-leaf node
  backward to the root. Because most nodes in a heap are near the bottom (where sift-down does
  very little work) and only a few are near the top (where it does more), the total work sums to
  a converging series that's O(n), not O(n log n). → Follow-up: *Why doesn't offering n elements
  one at a time also achieve O(n)?* Each individual `offer()` sift-up can cost up to O(log n) in
  the worst case (a newly appended element bubbling all the way to the root), and there's no
  amortization across different elements the way heapify exploits the tree's shape -- so n
  individual offers is O(n log n) worst case, while bulk construction/heapify is O(n).
- [Advanced] Why does `PriorityQueue` provide no ordering guarantee among equal-priority
  elements, and how would you make tie-breaking deterministic? → The heap-order invariant only
  requires parent `<=` children; it says nothing about the relative order of equal elements that
  happen to live in different subtrees, and which one reaches the root first depends on the
  specific sequence of sift-up/sift-down swaps, which is an implementation detail, not a
  contract. → Follow-up: *What's a common technique for stable tie-breaking?* Wrap each element
  with a monotonically increasing sequence number and use it as a secondary comparator key
  (`Comparator.comparing(...).thenComparingLong(Entry::sequence)`), so equal-priority items
  naturally compare by insertion order as a tiebreaker.

## 8. Exercises

`PriorityQueue`-based practice exercises (top-K frequent elements, sliding window maximum,
merge-K-sorted-lists) live in
`notes/04-queue-deque/05-dsa-patterns-queue-deque.md`; a from-scratch `MyMinHeap<T>`
implementation is in `notes/04-queue-deque/06-build-it-yourself-queue-heap.md`.

## 9. Quick recap

- Backed by a flat array representing a complete binary tree: children at `2i+1`/`2i+2`, parent
  at `(i-1)/2`.
- `offer` = append + sift-up (O(log n)); `poll` = move-last-to-root + sift-down (O(log n));
  `peek` = O(1) (always `array[0]`).
- No O(1) `contains`; `remove(Object)` is O(n) since the heap only orders parent-vs-child, not
  siblings.
- **Iteration/`toArray()` order is heap-array order, NOT sorted order** -- only repeated `poll()`
  produces sorted output. This is the single most common interview trap for this class.
- Not thread-safe, no built-in decrease-key/update-priority; equal-priority ties have no
  ordering guarantee.
