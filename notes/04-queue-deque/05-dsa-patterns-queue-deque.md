# DSA patterns: stack via Deque, BFS, monotonic deque, top-K, merge-K

## 1. What it is

Five recurring patterns built on `Queue`/`Deque`/`PriorityQueue` that solve a large share of
interview problems: using a **`Deque` as a stack** for matching/nesting problems, **BFS with a
queue** for shortest paths in unweighted graphs/grids, a **monotonic deque** for sliding-window
extremum problems in O(n), **top-K with a heap** for "k largest/smallest/most frequent" problems
in O(n log k), and **merge-K-sorted with a heap** for combining many sorted sequences in
O(n log k). Recognizing which pattern a problem statement implies is the actual interview skill.

## 2. How it works internally

**Stack via `Deque`** -- push/pop via `addFirst`/`removeFirst` (aliased as `push`/`pop`); used
for anything with a "most recent unmatched thing" shape: balanced brackets, next-greater-element,
expression evaluation, undo stacks. The key recognition signal: "the most recently seen X needs
to be resolved before anything seen earlier" -- LIFO is exactly that.

```
"({[]})" :  push (  push (  push [  see ] pop [ (match)  see } ... etc.
stack grows on open brackets, shrinks on matching closes, must be empty at the end
```

**BFS with a queue** -- explore a graph/grid level by level: enqueue the start node, then
repeatedly dequeue a node, process it, and enqueue all unvisited neighbors (marking them visited
**at enqueue time**, not dequeue time, to avoid enqueuing the same node multiple times). Because
a queue is FIFO, all nodes at distance `d` are fully processed before any node at distance
`d+1` is dequeued -- which is exactly what guarantees the **first time a target node is
dequeued, its recorded distance is the shortest possible**.
```
level 0:  [start]
level 1:  [n1, n2, n3]        <- all neighbors of start, enqueued together
level 2:  [n4, n5, ...]       <- neighbors of n1/n2/n3, enqueued after all of level 1
```

**Monotonic deque (sliding window maximum)** -- maintain a `Deque<Integer>` of **indices**
whose corresponding values are strictly decreasing from front to back. The front index is
always the current window's maximum. On each step: pop from the **back** while its value is
smaller than the incoming value (it can never be the max again while the new, larger value
remains in range); push the new index at the back; pop from the **front** if it has slid outside
the window. Each index is pushed once and popped at most once across the whole scan, giving
**O(n) total**, not O(n log n) or O(n*k).

Trace for `nums = [1, 3, -1, -3, 5, 3, 6, 7]`, `k = 3` (deque shown as values, front -> back):
```
i  nums[i]  deque(front->back)   window max
0     1     [1]                  -
1     3     [3]                  -            (1 popped: 1 < 3)
2    -1     [3, -1]              3
3    -3     [3, -1, -3]          3
4     5     [5]                  5            (3,-1,-3 all popped: all < 5)
5     3     [5, 3]               5
6     6     [6]                  6            (5,3 popped: both < 6)
7     7     [7]                  7            (6 popped: < 7)
```
Result: `[3, 3, 5, 5, 6, 7]`.

**Top-K with a heap** -- to find the k largest (or most frequent, etc.) items among n without
sorting all n (`O(n log n)`), keep a **min-heap bounded to size k**: offer each candidate, and
if the heap's size exceeds k, poll (evict the current smallest of the k kept so far). Whatever
remains after processing all n items is exactly the top-k, in `O(n log k)` -- meaningfully
cheaper than `O(n log n)` when `k` is much smaller than `n`.

**Merge-K-sorted with a heap** -- seed a min-heap with the current head of each of the k
sequences; repeatedly poll the overall smallest, emit it, and if the sequence it came from has a
next element, offer that back into the heap. Each of the n total elements across all k sequences
is offered and polled exactly once, each heap operation is `O(log k)`, giving `O(n log k)` total
-- versus `O(n*k)` for naively merging the k lists two at a time.

## 3. Complexity

| Pattern | Time | Space | Turns what into what |
|---|---|---|---|
| Stack via Deque | O(n) | O(n) worst case | avoids re-scanning backward to find a match |
| BFS with a queue | O(V + E) (or O(rows*cols) for a grid) | O(V) for the visited set + queue | guarantees shortest path in an unweighted graph without trying every path |
| Monotonic deque | O(n) total | O(k) | O(n*k) brute-force re-scan per window -> O(n) |
| Top-K with a heap | O(n log k) | O(k) | O(n log n) full sort -> O(n log k) |
| Merge-K-sorted with a heap | O(n log k) | O(k) | O(n*k) naive pairwise merge -> O(n log k) |

## 4. Example code

- Runnable class:
  `src/main/java/com/gk/study/queuedeque/examples/QueueDequePatternsDemo.java` -- self-contained
  illustrations of all five patterns with printed traces.

Expected console output (key lines, verified by hand-tracing each algorithm):
```
isValid("({[]})") -> true
isValid("(]") -> false
isValid("(()") -> false

shortest path length from (0,0) to (3,3): 6

nums=[1, 3, -1, -3, 5, 3, 6, 7] k=3
window maxima -> [3, 3, 5, 5, 6, 7]

nums=[1, 1, 1, 2, 2, 3, 4, 4, 4, 4] k=2
top 2 frequent -> [...] (heap only guarantees TOP-k membership, not a fully sorted result)

lists=[[1, 4, 7], [2, 5, 8, 9], [0, 3, 6]]
merged -> [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
```

## 5. When to use / when NOT to use

- **Stack via Deque**: nesting/matching problems, "most recent unresolved X," expression
  parsing -- NOT useful for problems needing FIFO order (that's a queue) or random access.
- **BFS**: shortest path / minimum steps in an **unweighted** graph or grid -- NOT the right
  tool for weighted shortest paths (use Dijkstra, itself `PriorityQueue`-based) or when you need
  *all* paths, not just the shortest.
- **Monotonic deque**: fixed-size sliding window extremum (max or min) queries -- NOT applicable
  if the window's aggregate isn't reducible to "compare and discard dominated candidates" (e.g.
  sliding window **sum** just needs a running total, no deque needed at all).
- **Top-K with a heap**: when `k` is meaningfully smaller than `n` and you don't need the full
  sorted order -- NOT worth it for large `k` close to `n` (just sort directly) or when you need a
  single pass online *and* the full sorted history (different problem shape).
- **Merge-K-sorted with a heap**: combining several already-sorted sequences -- NOT useful if
  the inputs aren't sorted (sort/merge-sort them first, a separate cost) or if `k` is 1 or 2
  (plain merge is simpler and equally efficient for small, fixed k).

## 6. Common pitfalls & gotchas

**Marking a BFS node visited at dequeue time instead of enqueue time:**
```java
while (!queue.isEmpty()) {
    int[] node = queue.poll();
    if (visited[node[0]][node[1]]) continue; // BUG-prone pattern: same node can be enqueued
    visited[node[0]][node[1]] = true;         // many times before any of them gets processed,
    // ... enqueue neighbors ...                inflating the queue size unnecessarily
}
// fix: set visited[...] = true at the moment you ENQUEUE a node, not when you dequeue it
```

**Popping the wrong end of the monotonic deque** -- candidates that are no longer the max get
popped from the **back** (they were just added and are smaller than the new value); candidates
that have slid out of the window get popped from the **front** (they're the oldest). Swapping
these breaks the invariant silently (no exception, just wrong answers).

**Forgetting the top-k heap must be a MIN-heap even when you want the K LARGEST elements** -- the
heap tracks "the smallest among the k I'm currently keeping," so it knows what to evict when a
better candidate arrives; a max-heap here would evict the wrong element.

**Comparing objects instead of a proper key in the merge-K-sorted heap** -- if the element type
doesn't implement `Comparable` in the way you need, you must pass an explicit
`Comparator` (e.g. `Comparator.comparingInt(node -> node.val)`), exactly like the
`PriorityQueue` gotcha in topic 3.

## 7. Interview questions

- [Basic] What problem shape signals "use a stack (via Deque)"? → Anything about matching the
  most recently seen unresolved item first -- balanced brackets, "next greater element to the
  right," undo functionality, expression parsing with nested operators. → Follow-up: *Why
  `Deque` and not the legacy `Stack` class?* Covered in topic 2 -- `ArrayDeque` avoids `Stack`'s
  unnecessary synchronization overhead.
- [Basic] What problem shape signals "use BFS with a queue" rather than DFS? → "Shortest
  path"/"minimum number of steps" in an **unweighted** graph or grid -- BFS explores in
  increasing-distance layers, so the first time it reaches the target, that distance is
  guaranteed minimal; DFS gives no such guarantee without extra bookkeeping. → Follow-up: *Would
  BFS still find the shortest path if edges had different weights?* No -- that requires
  Dijkstra's algorithm (itself built on a `PriorityQueue`, not a plain FIFO queue), since BFS's
  "layer by layer" guarantee assumes every edge costs the same (1 step).
- [Basic] Why must a BFS grid/graph traversal mark nodes visited at enqueue time rather than
  dequeue time? → To prevent the same node from being enqueued multiple times by different
  in-progress neighbors before any of those enqueues gets processed, which would waste work and
  could even cause incorrect distances to be recorded for a node reached both "correctly" and
  redundantly. → Follow-up: *Does the order matter for correctness, or just efficiency?* Both --
  without early marking, the same node's distance could theoretically be overwritten by a later
  (non-shortest) path processed out of order in some traversal variants; marking at enqueue
  avoids the ambiguity entirely.
- [Intermediate] Walk through how a monotonic deque solves sliding window maximum in O(n) total,
  and why simply recomputing the max for every window is worse. → Maintain a deque of indices
  with strictly decreasing values; before adding a new index, evict from the back every index
  whose value is smaller (it's now dominated and can never be the max again while the new,
  larger value stays in range); evict from the front once an index slides out of the window; the
  front is always the current max. Each index is pushed once and popped at most once total
  across the whole array, so total work across all windows is O(n), versus O(n*k) for
  recomputing each window's max by scanning all k elements. → Follow-up: *Why can you safely
  discard a smaller value from the back even though it's still technically inside the window?*
  Because the newly arriving, larger value will remain in the window at least as long as that
  smaller value would have (they entered in order, and the new one is bigger) -- so the smaller
  one can never become the max again before it would slide out anyway, making it safe to drop.
- [Intermediate] Why does the top-K pattern use a MIN-heap even when finding the K LARGEST
  elements? → The heap needs to efficiently answer "what's the weakest candidate currently being
  kept, so I know what to evict if something better shows up" -- for keeping the K largest, the
  "weakest kept candidate" is the smallest of the k, which is exactly what a min-heap's root
  gives you in O(1), letting you evict it in O(log k) when a larger candidate arrives. →
  Follow-up: *What would happen if you used a max-heap of unbounded size instead?* It would work
  correctness-wise (poll k times at the end) but costs O(n log n) to build and drain instead of
  O(n log k) -- you lose the benefit of bounding the heap's size to k throughout.
- [Intermediate] How does merging k sorted lists with a heap achieve O(n log k), and why is that
  better than repeatedly merging two lists at a time? → Seed the heap with each list's current
  head (O(k log k)); each of the n total elements is offered once and polled once as it's
  emitted, each heap op costing O(log k) since the heap never holds more than k elements at
  once, giving O(n log k) total. Repeatedly pairwise-merging (merge list 1+2, then merge that
  with list 3, etc.) processes early-merged elements again and again as the merged result grows,
  costing O(n*k) in the worst case (each of k merge passes touches up to n elements). →
  Follow-up: *Could you instead merge the lists pairwise in a divide-and-conquer tree (merge
  pairs, then merge pairs of results, etc.) to avoid the heap?* Yes -- that achieves the same
  O(n log k) by a different route (log k merge "rounds," each touching all n elements once); the
  heap approach is generally simpler to implement and reason about for this problem.
- [Advanced] For BFS shortest-path in a grid, what's the space complexity, and how would you
  reduce memory if the grid were extremely large? → O(rows * cols) for the visited array plus
  the queue in the worst case (a large fraction of cells can be in the queue simultaneously at
  the widest BFS layer). For very large grids, options include: bidirectional BFS (search from
  both start and target simultaneously, meeting in the middle, reducing the effective explored
  radius), using a `BitSet` instead of a `boolean[][]` for the visited set (8x memory reduction),
  or, if only the distance value (not the path) is needed, overwriting the grid in place instead
  of a separate visited structure when mutation is allowed. → Follow-up: *Does bidirectional BFS
  always find the shortest path correctly?* Yes, as long as you correctly detect the moment the
  two search frontiers meet and combine the accumulated distances from both sides at that
  meeting point.
- [Advanced] Both the monotonic-deque and top-K-heap patterns discard information ("dominated"
  candidates) to stay efficient -- what's the general principle that justifies safely discarding
  data in a streaming/online algorithm? → It's safe to discard a candidate exactly when you can
  prove it can **never** be part of any future optimal answer, given everything you know about
  how future inputs relate to what you've already seen -- in the monotonic deque, a smaller
  value trapped behind a larger, later-arriving value can never be a future window's max (the
  larger value dominates it for at least as long); in top-K, the k-th best-so-far can never be
  displaced by anything that isn't better than it. This "domination" argument is the same
  underlying idea behind many greedy and sliding-window optimizations. → Follow-up: *Can you
  think of a problem shape where this domination argument does NOT apply, making a monotonic
  structure the wrong tool?* Sliding window problems where the aggregate isn't reducible to "keep
  only non-dominated candidates" -- e.g. sliding window **median** (no simple domination
  relationship between values) typically needs a different structure entirely (e.g. two heaps or
  a balanced BST/order-statistics structure), not a monotonic deque.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|---|---|---|---|
| E01 | Easy | Valid parentheses -- check balanced/matching brackets | Stack via Deque | `exercises/ValidParentheses.java` |
| E02 | Easy | Implement a FIFO queue using two stacks | Two stacks (amortized) | `exercises/QueueUsingTwoStacks.java` |
| E03 | Easy | Sliding window maximum -- brute force | Brute-force baseline | `exercises/SlidingWindowMaxBruteForce.java` |
| M01 | Medium | Next greater element to the right, for every index | Monotonic stack | `exercises/NextGreaterElement.java` |
| M02 | Medium | Shortest path length in a 0/1 grid | BFS with a queue | `exercises/BfsShortestPathGrid.java` |
| M03 | Medium | Top K frequent elements | Top-K with a heap | `exercises/TopKFrequentElements.java` |
| H01 | Hard | Sliding window maximum -- optimal O(n) | Monotonic deque | `exercises/SlidingWindowMaximum.java` |
| H02 | Hard | Merge K sorted linked lists | Merge-K with a heap | `exercises/MergeKSortedLists.java` |

**E01 -- Valid parentheses**
- Input: `"({[]})"` -> Output: `true`. Input: `"(]"` -> Output: `false`.
- Constraint: O(n) time, O(n) space.
- <details><summary>Hint</summary>Push every opening bracket onto a `Deque`; on a closing
  bracket, the stack must be non-empty and its popped top must be the matching opener. A
  non-empty stack at the end means an unmatched opener remains.</details>

**E02 -- Queue using two stacks**
- Input: `enqueue(1); enqueue(2); enqueue(3); dequeue()` -> Output: `1`, then `2`, then `3`.
- Constraint: enqueue O(1) amortized, dequeue O(1) amortized.
- <details><summary>Hint</summary>`inStack` absorbs every enqueue directly. Only when
  `outStack` is empty, pop everything from `inStack` onto `outStack` (this reverses order back
  to FIFO), then pop from `outStack` for dequeue/peek.</details>

**E03 -- Sliding window maximum (brute force)**
- Input: `nums=[1,3,-1,-3,5,3,6,7]`, `k=3` -> Output: `[3,3,5,5,6,7]`.
- Constraint: O(n*k) time is acceptable (that's the point -- compare against H01).
- <details><summary>Hint</summary>For each of the `n-k+1` windows, scan all `k` elements to find
  the max directly.</details>

**M01 -- Next greater element**
- Input: `[2,1,2,4,3]` -> Output: `[4,2,4,-1,-1]`.
- Constraint: O(n) time, O(n) space.
- <details><summary>Hint</summary>Keep a stack of indices with strictly decreasing values as you
  scan left to right; whenever the current value beats the stack's top, that index has just
  found its next-greater element -- pop and record it, repeating until the top no longer
  qualifies (or the stack empties), then push the current index.</details>

**M02 -- BFS shortest path in a grid**
- Input: `{{0,0,0},{1,1,0},{0,0,0}}` -> Output: `4` (moves from (0,0) to (2,2)).
- Constraint: O(rows*cols) time and space.
- <details><summary>Hint</summary>Standard BFS with a queue of `(row, col, distance)`; mark
  cells visited at enqueue time; the first time the target cell is dequeued, its distance is the
  answer.</details>

**M03 -- Top K frequent elements**
- Input: `nums=[1,1,1,2,2,3]`, `k=2` -> Output: `[1,2]` (order not significant).
- Constraint: O(n log k) time using a size-bounded heap.
- <details><summary>Hint</summary>Count frequencies with a `HashMap`, then keep a min-heap
  (ordered by count) bounded to size `k`; evict the smallest-count entry whenever the heap grows
  past `k`.</details>

**H01 -- Sliding window maximum (optimal)**
- Input: `nums=[1,3,-1,-3,5,3,6,7]`, `k=3` -> Output: `[3,3,5,5,6,7]`.
- Constraint: O(n) time total, O(k) space.
- <details><summary>Hint</summary>See the monotonic-deque trace in section 2 above -- maintain a
  deque of indices with strictly decreasing values; pop from the back while dominated, pop from
  the front once out of window range, front is always the current max.</details>

**H02 -- Merge K sorted lists**
- Input: `lists = [1->4->5, 1->3->4, 2->6]` -> Output: `1->1->2->3->4->4->5->6`.
- Constraint: O(n log k) time, O(k) extra space for the heap.
- <details><summary>Hint</summary>Seed a min-heap with each list's head node (skip nulls); poll
  the smallest, splice it onto the result, and if it had a next node, offer that back into the
  heap.</details>

Solutions are in the `solutions` package (`ValidParenthesesSolution`,
`QueueUsingTwoStacksSolution`, `SlidingWindowMaxBruteForceSolution`,
`NextGreaterElementSolution`, `BfsShortestPathGridSolution`, `TopKFrequentElementsSolution`,
`SlidingWindowMaximumSolution`, `MergeKSortedListsSolution`) -- not shown here; attempt the
stubs first.

## 9. Quick recap

- Stack via `Deque`: LIFO for "most recently seen unresolved item" problems -- balanced
  brackets, next-greater-element.
- BFS with a queue: shortest path in **unweighted** graphs/grids; mark visited at enqueue time,
  not dequeue time, to avoid duplicate enqueues.
- Monotonic deque: O(n) sliding-window extremum by discarding "dominated" candidates from the
  back and out-of-window candidates from the front.
- Top-K with a heap: bound a **min**-heap to size k (even when finding the k **largest**) for
  O(n log k) instead of O(n log n) full sort.
- Merge-K-sorted with a heap: seed with each sequence's head, poll-emit-reoffer, O(n log k)
  instead of O(n*k) naive pairwise merging.
