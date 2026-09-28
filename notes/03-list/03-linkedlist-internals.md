# LinkedList internals

## 1. What it is

`LinkedList<E>` is a `List` implementation backed by a **doubly linked list** of `Node<E>`
objects — no contiguous array at all. It also implements `Deque<E>` (and `Queue<E>`), so it can
be used as a stack, queue, or double-ended queue in addition to a positional list.

## 2. How it works internally

Each element lives in a private static `Node<E>` (paraphrased):
```
private static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;
}
```
The list keeps `first` and `last` references (no dummy sentinel nodes in the JDK implementation)
and a `size` count.

ASCII diagram — doubly linked chain of 3 elements:
```
first                                   last
  |                                       |
  v                                       v
[prev=null|A|next]<->[prev| B |next]<->[prev| C |next=null]
```
Each node knows both neighbours, so traversal works forward *or* backward.

**`addLast(e)` / `add(e)`** — O(1): allocate a node, link it after the current `last`, update
`last`.

**`addFirst(e)`** — O(1): allocate a node, link it before `first`, update `first`.

**`get(index)`** — the list first checks whether `index` is in the first half or second half of
`size` and walks from `first` forward or from `last` backward, whichever is shorter — still O(n)
worst case (about `size/4` average hops), but roughly 2x faster than a naive always-forward walk.

**`add(index, e)` / `remove(index)` in the middle** — must first *find* the node (O(n) walk as
above), then splice it in O(1) by rewiring `prev`/`next` pointers — no shifting of other elements,
unlike `ArrayList`:
```
remove node X:            insert node Y before X:
 ...<->B<->X<->D<->...      ...<->B<->Y<->X<->...
 B.next = D                 B.next = Y ; Y.prev = B
 D.prev = B                 Y.next = X ; X.prev = Y
 X.prev = X.next = null (unlink, help GC)
```

**As a `Deque`**: `addFirst`/`addLast`/`removeFirst`/`removeLast`/`peekFirst`/`peekLast` are all
O(1) because `first`/`last` are direct references — this is what makes `LinkedList` usable as a
stack or FIFO queue without any index walking.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `addFirst` / `addLast` / `removeFirst` / `removeLast` | O(1) | O(1) | direct pointer updates |
| `get(index)` / `set(index, e)` | O(n) | O(1) | walks from nearer end; no random access |
| `add(index, e)` / `remove(index)` | O(n) to find + O(1) to splice | O(1) | dominated by the find |
| `addLast`/`add(e)` (append via API) | O(1) | O(1) | `last` reference, no walk |
| `contains` / `indexOf` | O(n) | O(1) | linear scan |
| iteration (`Iterator`/`ListIterator`) | O(n) total | O(1) | O(1) per step, pointer-chasing |

Compare with `ArrayList`: `LinkedList` trades O(1) `get` for O(1) head/tail insert-delete, and
pays per-node object overhead (each `Node` is a separate heap object with two pointers + item —
worse cache locality than a contiguous array).

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/LinkedListInternalsDemo.java`

```java
LinkedList<String> queue = new LinkedList<>();
queue.addLast("txn-1");
queue.addLast("txn-2");
queue.addFirst("txn-0");
System.out.println(queue);          // [txn-0, txn-1, txn-2]
System.out.println(queue.peekFirst()); // txn-0 (no removal)
System.out.println(queue.pollFirst()); // txn-0 (removes and returns)
System.out.println(queue);          // [txn-1, txn-2]
```
Expected console output:
```
[txn-0, txn-1, txn-2]
txn-0
txn-0
[txn-1, txn-2]
```

## 5. When to use / when NOT to use

- Use when you mainly push/pop/peek at the **ends** (queue, stack, deque, sliding-window buffer)
  — but for a *pure* stack/queue, prefer `ArrayDeque` (see topic 5), it's faster and lighter.
- Use when you need a `List` *and* frequent middle insert/delete **once you already hold the
  node/iterator position** (e.g. via `ListIterator.add`), since the splice itself is O(1).
- Avoid when you need random access (`get(i)` in a loop) — that's O(n) per call, O(n²) overall,
  a very common accidental-quadratic bug when someone swaps `ArrayList` for `LinkedList` in
  existing `for (i=0; i<list.size(); i++) list.get(i)` code.
- Avoid for large datasets when memory matters — per-node overhead (object header + 2 pointers +
  data) is significantly larger than an equivalent `ArrayList`'s packed array.

## 6. Common pitfalls & gotchas

**Indexed access in a loop silently becomes O(n²):**
```java
LinkedList<Integer> list = ...; // 100,000 elements
for (int i = 0; i < list.size(); i++) {
    process(list.get(i));   // BUG: O(n) per get -> O(n^2) total
}
// fix: use an Iterator (O(1) per step) or a for-each loop (uses Iterator under the hood)
for (Integer x : list) process(x);
```

**Mixing `Queue` and `Deque`/`List` methods causes confusing return-value semantics** — `Queue`'s
`add`/`remove`/`element` throw on failure, while `Deque`'s `offer`/`poll`/`peek` return
`null`/`false` — `LinkedList` implements both, so pick one vocabulary consistently per use site.

**`null` elements are allowed** in `LinkedList` (unlike some `Deque` implementations such as
`ArrayDeque`, which reject `null`) — `poll()` returning `null` can then ambiguously mean "empty"
*or* "the actual stored value was null". Prefer `ArrayDeque` (which forbids null) when you need
to distinguish "empty" from "null element" via `poll`.

## 7. Interview questions

- [Basic] How is `LinkedList` implemented internally? → As a **doubly** linked list of `Node`
  objects, each holding `item`, `next` and `prev`; the list keeps `first`/`last` head/tail
  references and a `size` counter. → Follow-up: *Singly or doubly linked?* Doubly — that's what
  makes `addLast`/`removeLast`/backward iteration O(1) instead of O(n).
- [Basic] What is the time complexity of `get(index)` on a `LinkedList`? → O(n) — it must walk
  node-by-node from whichever end (`first` or `last`) is closer to `index`. → Follow-up: *Is it
  ever O(1)?* Only for index 0 or `size-1`, effectively via `getFirst()`/`getLast()`.
- [Basic] Why does `LinkedList` implement `Deque`? → So it can serve as a stack, FIFO queue, or
  double-ended queue with O(1) operations at both ends, in addition to being a positional `List`.
  → Follow-up: *Name the Deque methods it adds.* `addFirst/addLast/offerFirst/offerLast/
  peekFirst/peekLast/pollFirst/pollLast/push/pop`.
- [Intermediate] Compare `ArrayList.add(0, e)` vs `LinkedList.addFirst(e)` complexity. →
  `ArrayList` insert-at-front is O(n) (shifts every existing element right); `LinkedList.
  addFirst` is O(1) (just relinks `first`). → Follow-up: *So is LinkedList always better for
  front inserts?* For raw front-insert throughput yes, but `ArrayDeque` beats both for that use
  case with less per-element overhead and better cache locality.
- [Intermediate] Why is `LinkedList.get(size/2)` (middle element) still roughly as fast as
  `get(size-1)`? → Because `LinkedList` picks the shorter walk direction (from `first` if
  `index < size/2`, else from `last`), so the worst-case distance is `size/2` hops regardless of
  which "half" you're accessing, not `size` hops. → Follow-up: *Does this change the Big-O?* No —
  still O(n), just a ~2x constant-factor improvement over a naive always-forward walk.
- [Intermediate] Why can middle insertion be "O(1) splice" but the overall `add(index, e)` call
  is still documented as O(n)? → Splicing the node is O(1) once you're positioned there, but
  *finding* that position by index requires an O(n) walk; if you already hold a `ListIterator`
  positioned there (from prior traversal), `listIterator.add(e)` really is O(1). → Follow-up:
  *Give a concrete example where this matters.* Repeatedly inserting at a remembered cursor while
  processing a stream of transactions in order — one O(n) walk instead of O(n) walks each time.
- [Advanced] Why does `LinkedList` generally have worse cache performance than `ArrayList` despite
  matching or better Big-O for some ops? → Nodes are separately heap-allocated and can be
  scattered across memory, so pointer-chasing causes CPU cache misses; `ArrayList`'s contiguous
  array means sequential access is very cache-friendly (prefetcher-friendly), often making
  `ArrayList` iteration faster in wall-clock time even though both are "O(n)". → Follow-up: *Does
  this show up in the benchmark in topic 4?* Yes — see
  `notes/03-list/04-arraylist-vs-linkedlist-benchmark.md`.
- [Advanced] How would you detect whether a linked structure has a cycle, and why can't you just
  use `get(index)`-style code to check? → Floyd's tortoise-and-hare (slow/fast pointer) algorithm
  detects a cycle in O(n) time, O(1) space by moving one pointer 1 step and another 2 steps until
  they meet or the fast pointer hits `null`; index-based access assumes a finite, acyclic
  structure and would loop forever (or throw once size accounting desyncs) on a cyclic list. →
  Follow-up: *How do you find the cycle's start node, not just detect it?* After detecting a
  meeting point, reset one pointer to the head and advance both one step at a time — they meet
  exactly at the cycle's start (classic Floyd's algorithm proof via the math of meeting-point
  distances). See exercise H2 below.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| M1 | Medium | Reverse a singly linked list, both iteratively and recursively | In-place reversal | `exercises/ReverseLinkedList.java` |
| M2 | Medium | Merge two sorted linked lists into one sorted list | Merge / two pointers | `exercises/MergeTwoSortedLists.java` |
| H1 | Hard | Detect a cycle in a linked list and return the node where the cycle begins (or `null`) | Floyd's cycle detection | `exercises/LinkedListCycleStart.java` |

**M1 — Reverse a linked list**
- Input: `1->2->3->null` → Output: `3->2->1->null`
- Constraint: O(n) time; iterative version O(1) space, recursive version O(n) call-stack space.
- <details><summary>Hint</summary>Iterative: walk with `prev`/`curr`/`next`, reversing one link
  per step. Recursive: reverse the tail first, then fix the one link at the head.</details>

**M2 — Merge two sorted lists**
- Input: `1->3->5`, `2->4->6` → Output: `1->2->3->4->5->6`
- Constraint: O(n+m) time, O(1) extra space (splice nodes, don't allocate new ones; a dummy head
  node is allowed as a technique, not counted against the space bound).
- <details><summary>Hint</summary>Use a dummy head + a tail pointer; repeatedly attach the
  smaller of the two current nodes, then attach whatever remains.</details>

**H1 — Linked list cycle start**
- Input: a list where the tail's `next` points back into the middle (or `null` if acyclic) →
  Output: the `Node` where the cycle begins, or `null`.
- Constraint: O(n) time, O(1) space (no `HashSet` of visited nodes).
- <details><summary>Hint</summary>Floyd's tortoise/hare: detect meeting point first, then reset
  one pointer to head and advance both by one until they meet again — that node is the cycle
  start.</details>

Solutions are in the `solutions` package (`ReverseLinkedListSolution`,
`MergeTwoSortedListsSolution`, `LinkedListCycleStartSolution`) — not shown here.

## 9. Quick recap

- Doubly linked `Node` chain with `first`/`last` refs; no backing array, no capacity/growth.
- `get`/`set`/indexed `add`/`remove` are O(n) (walk from nearer end); head/tail ops are O(1).
- It's simultaneously a `List`, a `Queue`, and a `Deque` — pick one method vocabulary per call
  site for clarity.
- Worse cache locality than `ArrayList` due to scattered node allocation — often loses real-world
  benchmarks despite comparable Big-O.
- Prefer `ArrayDeque` over `LinkedList` for pure stack/queue use (topic 5).
