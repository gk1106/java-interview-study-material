# Cheat Sheet — 03: List

One-page pre-interview skim. Full notes: `notes/03-list/`.

## Complexity table

| Operation | ArrayList | LinkedList | ArrayDeque (as stack/queue) | CopyOnWriteArrayList |
|---|---|---|---|---|
| get(index) | O(1) | O(n) | n/a (not a List) | O(1) |
| add at end | O(1) amortized | O(1) | O(1) amortized | O(n) (full copy) |
| add at front | O(n) | O(1) | O(1) amortized | O(n) |
| add/remove middle | O(n) shift | O(1) splice once positioned, O(n) to get there | n/a | O(n) |
| contains/indexOf | O(n) | O(n) | O(n) | O(n) |
| subList(a,b) | O(1) — live view | O(1) — live view | n/a | n/a |

`ArrayList` growth: **1.5x** (`oldCapacity + (oldCapacity >> 1)`) via `Arrays.copyOf`/`System.arraycopy`.
`ArrayDeque` growth: **doubling**.

## Most-likely-asked facts

1. `ArrayList` backing store is `Object[] elementData`; `add` is amortized O(1), worst case O(n) on the resize call itself.
2. `LinkedList` is doubly-linked with `first`/`last` refs; implements **both** `List` and `Deque` — pick one method vocabulary per call site.
3. ArrayList usually beats LinkedList in wall-clock even for equal Big-O (cache locality of a contiguous array vs pointer-chasing scattered nodes).
4. `Vector`/`Stack` are legacy, synchronized on every call; `Stack extends Vector` so it leaks index-based `List` methods (`stack.get(0)`) that break LIFO discipline — use `Deque`/`ArrayDeque` instead.
5. `CopyOnWriteArrayList`: every write copies the whole backing array + atomic (volatile) swap-in; reads never lock; iterators are **snapshots**, never throw CME, and `iterator().remove()` throws `UnsupportedOperationException`.
6. `ListIterator` is the only sanctioned way (besides `removeIf`) to mutate a `List` mid-iteration — supports backward traversal + in-place `set`/`add`/`remove`.
7. `subList` shares the parent's `modCount` tracking — a structural change to the parent outside the sublist invalidates it (throws CME on next sublist access).
8. `RandomAccess` marker interface lets generic algorithms choose an O(1)-index loop vs an iterator-based loop.
9. `Arrays.asList` = fixed-size, backed directly by the array (writes through the array are visible in the list too); `List.of` = fully immutable.
10. `remove(int)` vs `remove(Object)`: `list.remove(1)` on `List<Integer>` removes by INDEX; force the Object overload with `Integer.valueOf(1)` or `(Integer) 1`.

## Top pitfalls

- **Indexed `get(i)` loop on a `LinkedList`** → silently O(n²); use an iterator/for-each instead.
- **Removing in a forward indexed loop** (`list.remove(i)`) — doesn't throw, silently skips the next element (indices shift). Fix: iterate backward, `Iterator.remove()`, or `removeIf`.
- **`Stack`'s inherited `Vector` methods** (`get(0)`, `add(0, x)`) silently break LIFO — use `Deque`.
- **Not pre-sizing** a known-size `ArrayList` — pays repeated 1.5x grow/copy cycles for nothing.
- **`CopyOnWriteArrayList` for a write-heavy list** — O(n) copy per write compounds into O(n²) total; only good for read-heavy/write-rare (listener lists).

## When to use / not use

- Default `ArrayList`; reach for `LinkedList` only when profiling shows heavy middle-insertion via an already-positioned `ListIterator` (rare — usually a net loss).
- Prefer `ArrayDeque` over `LinkedList`/`Stack`/`Vector` for any stack or queue use — no per-node allocation, better cache locality, rejects `null` (unambiguous empty signal).
- Prefer `List.removeIf(predicate)` > `Iterator.remove()` in a while loop > backward indexed loop, in that order, when removing during iteration.

## DSA patterns (module 03 §8) — turns what into what

| Pattern | Time/Space | Idea |
|---|---|---|
| Two pointers | O(n)/O(1) | converging (sorted/palindrome) or fast/slow — O(n²)→O(n) |
| Sliding window | O(n)/O(1) or O(k) | running aggregate, add-on-enter/subtract-on-exit |
| Prefix sum | O(n) preprocess, O(1)/query | `prefix[i+1]=prefix[i]+arr[i]`, length n+1, `prefix[0]=0` |
| In-place reversal | O(n)/O(1) iterative | save `next` **before** rewiring `curr.next` |
