# ArrayList internals

## 1. What it is

`ArrayList<E>` is `List` backed by a **resizable array** (`Object[] elementData`). It gives O(1)
amortized append and O(1) index access, at the cost of O(n) insert/remove in the middle and
periodic O(n) resize copies. It is the default, general-purpose `List` implementation.

## 2. How it works internally

Fields (paraphrased from the JDK, not copied verbatim):
- `Object[] elementData` — the backing array. Cast to `E` on read (unchecked, hidden by generics
  erasure).
- `int size` — number of *logical* elements currently stored; `elementData.length` (capacity) is
  usually **larger** than `size` to leave room to grow without reallocating on every `add`.
- `int modCount` (inherited from `AbstractList`) — incremented on every structural change; used by
  iterators to fail fast.
- A brand-new `new ArrayList<>()` does **not** allocate an array immediately — it holds a shared
  empty-array sentinel; the array is allocated lazily (size 10) on the first `add`.

**Growth algorithm** (`grow()` / `newCapacity()`), conceptually:
```
newCapacity = oldCapacity + (oldCapacity >> 1)     // old * 1.5, using a bit-shift, not oldCapacity * 1.5
if newCapacity < minCapacity: newCapacity = minCapacity
if newCapacity - MAX_ARRAY_SIZE > 0: newCapacity = hugeCapacity(minCapacity)
elementData = Arrays.copyOf(elementData, newCapacity);
```
Growth is **1.5x**, not doubling (that's `Vector`'s default when no increment is set, and some
other languages' array-lists). Using `oldCapacity >> 1` avoids overflow issues and a
multiplication.

**`add(E e)`** (append):
```
if size == elementData.length: grow()      // amortized O(1) — see complexity note below
elementData[size++] = e;
```

**`add(int index, E e)`** (insert in the middle):
```
ensure capacity (grow if needed)
System.arraycopy(elementData, index, elementData, index + 1, size - index);  // shift right
elementData[index] = e;
size++;
```

**`remove(int index)`**:
```
System.arraycopy(elementData, index + 1, elementData, index, size - index - 1); // shift left
elementData[--size] = null;   // let GC reclaim the reference
```

ASCII diagram of a growth event (capacity 4 → 6, adding a 5th element):
```
size=4, capacity=4:  [ A | B | C | D ]      elementData.length = 4
add(E) -> grow():    newCapacity = 4 + (4>>1) = 6
Arrays.copyOf ->      [ A | B | C | D | _ | _ ]   (new array, length 6)
elementData[4]=E ->   [ A | B | C | D | E | _ ]   size = 5
```
`Arrays.copyOf` internally calls `System.arraycopy`, a JVM intrinsic that does a raw
memory-block copy — much faster than a Java `for` loop, which is why array-shift/resize
operations, while O(n), have a very small constant factor in practice.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `get(index)` / `set(index, e)` | O(1) | O(1) | direct array index |
| `add(e)` (append) | O(1) amortized, O(n) worst case | O(1) amortized extra | worst case only on the resize call |
| `add(index, e)` | O(n) | O(1) | `System.arraycopy` shift |
| `remove(index)` / `remove(Object)` | O(n) | O(1) | shift + (for Object) linear scan first |
| `contains` / `indexOf` | O(n) | O(1) | linear scan, `equals()` |
| `size()` | O(1) | O(1) | tracked field |
| iteration | O(n) | O(1) | cache-friendly, sequential memory |

**Why is `add` "amortized" O(1)?** Most calls are O(1) (just write + increment). Every ~1.5x
calls trigger an O(n) copy, but that cost is "spread" over the n cheap calls that got you there —
summing the geometric series of copy costs (n + n/1.5 + n/1.5² + ...) converges to O(n) total for
n appends, i.e. O(1) **per** append on average. This is the textbook example of amortized
analysis (see `notes/01-java-foundations-for-dsa`).

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/ArrayListInternalsDemo.java`

```java
List<Integer> list = new ArrayList<>();
int lastCapacityMilestone = -1;
for (int i = 0; i < 20; i++) {
    list.add(i);
    // demo prints a line whenever size crosses a known JDK growth boundary (10, 15, 22, ...)
}
```
Expected console output (abridged): prints the element count at each observed growth boundary,
e.g. `size hit 10 (initial default capacity)`, `size hit 15 (grew from 10 -> 15)`, etc. — see the
demo class for the full, commented trace since capacity isn't directly exposed by the public API
and the demo infers growth points instead of using reflection.

## 5. When to use / when NOT to use

- Use when reads (`get`) dominate writes, or writes are mostly **appends** at the end.
- Use when you know (or can estimate) the final size — pass it to `new ArrayList<>(initialCapacity)`
  to avoid repeated resize copies entirely (common in banking batch jobs reading a known row count).
- Avoid when you frequently insert/remove at the **front or middle** of a large list — every such
  op is an O(n) array shift; prefer `LinkedList`/`ArrayDeque` for a queue-like access pattern, or
  batch the mutations and rebuild once.

## 6. Common pitfalls & gotchas

**Not pre-sizing a large, known-size list** wastes cycles on repeated 1.5x grow/copy cycles:
```java
List<Row> rows = new ArrayList<>();       // starts at capacity 0/10, resizes ~log_1.5(n) times
List<Row> rows = new ArrayList<>(expectedRowCount); // one allocation, no resize copies
```

**Removing while iterating with a plain `for`/`Iterator` shifts indices under you:**
```java
for (int i = 0; i < list.size(); i++) {
    if (predicate.test(list.get(i))) list.remove(i); // BUG: skips the next element
}
// fix: iterate backwards, or use Iterator.remove(), or removeIf() — see topic 7
```

**Autoboxing + `Integer` cache** surprises equality checks in lists of boxed ints outside
`[-128, 127]`, but `equals()`-based `contains`/`indexOf` is unaffected since `Integer.equals`
compares values, not references — the trap is `==` on two `get()` results, not a `List` bug per
se, but it shows up constantly in `ArrayList<Integer>` banking code doing manual comparisons.

## 7. Interview questions

- [Basic] What is the default initial capacity of `ArrayList`? → 10, but the backing array isn't
  allocated until the first element is added (lazy allocation of the empty-array sentinel). →
  Follow-up: *What is `new ArrayList<>()` capacity before any `add`?* Effectively 0 (shared empty
  array constant), grows to 10 on first `add`.
- [Basic] What is the growth factor when `ArrayList` resizes? → ~1.5x (`oldCapacity +
  oldCapacity/2`), computed as `oldCapacity + (oldCapacity >> 1)`. → Follow-up: *Why not double
  like some other languages' dynamic arrays?* Trade-off tuned by the JDK team between wasted
  memory and copy frequency; 1.5x wastes less memory headroom for large lists while still keeping
  amortized O(1) inserts.
- [Basic] What happens internally when you call `add()` and the array is full? → A new array
  ~1.5x the size is allocated via `Arrays.copyOf` (which uses `System.arraycopy`), all existing
  elements are copied over, then the new element is written and `size` incremented. → Follow-up:
  *Is the old array garbage collected immediately?* Yes, once no reference remains — `elementData`
  is reassigned to the new array.
- [Intermediate] Why is `get(index)` O(1) but `remove(index)` O(n)? → `get` is a direct array
  index computation; `remove` must shift every element after `index` left by one via
  `System.arraycopy` to keep the array contiguous with no gaps. → Follow-up: *Is removing the
  last element still O(n)?* No — removing the last element is O(1) (no shift needed, just null
  out and decrement `size`).
- [Intermediate] What is `modCount` and why does `ArrayList` need it? → A structural-change
  counter incremented on every add/remove; iterators snapshot it at creation and compare on each
  `next()`, throwing `ConcurrentModificationException` if it changed — a fail-fast safety net, not
  a concurrency guarantee. → Follow-up: *Does `set(index, e)` change `modCount`?* No — `set`
  replaces a value in place without changing structure/size, so it does not trip fail-fast
  iterators.
- [Intermediate] How would you avoid resize overhead when you know you'll insert 100,000
  elements? → `new ArrayList<>(100_000)` to pre-allocate exact capacity, or use `ensureCapacity
  (n)` on an existing instance before a bulk-add loop. → Follow-up: *Does over-sizing hurt?*
  Yes — wasted heap for unused slots; if the final size is uncertain, `trimToSize()` after
  population can reclaim it.
- [Advanced] Why does `Arrays.copyOf` outperform a manual copy loop? → It delegates to
  `System.arraycopy`, a JVM intrinsic implemented as a native, often vectorized/block memory-move
  operation, versus a bytecode loop with per-element bounds checks and array-store checks. →
  Follow-up: *Does `System.arraycopy` bypass array-store type checks?* For object arrays it still
  performs a store-type check per JLS semantics unless the JIT proves it unnecessary; the
  performance win is from bulk native copying, not skipping type safety.
- [Advanced] Why does `ArrayList` null out the vacated slot after `remove`/`clear`? → To avoid
  "loitering" object references that would otherwise keep large objects reachable and unreclaimed
  by the GC even though they're no longer logically in the list. → Follow-up: *Does this matter
  for `int`/primitive-heavy lists?* `ArrayList<Integer>` still stores boxed `Integer` object
  references, so the same GC-reachability concern applies; there's no true primitive `ArrayList`
  in the standard library.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E1 | Easy | Remove duplicates from a **sorted** array/list in-place, return new logical length | Two pointers | `exercises/RemoveDuplicatesSorted.java` |
| E2 | Easy | Reverse an array/list in-place without extra storage | Two pointers | `exercises/ReverseArrayInPlace.java` |

**E1 — Remove duplicates from sorted list in-place**
- Input: `[1,1,2,3,3]` → Output: logical length `3`, list prefix becomes `[1,2,3,...]`
- Constraint: O(n) time, O(1) extra space (mutate the given list in place).
- <details><summary>Hint</summary>Keep a slow pointer for the last unique position and a fast
  pointer scanning ahead; when `fast` finds a new value, advance `slow` and overwrite.</details>

**E2 — Reverse array/list in-place**
- Input: `[1,2,3,4,5]` → Output: `[5,4,3,2,1]`
- Constraint: O(n) time, O(1) extra space.
- <details><summary>Hint</summary>Swap `list.get(left)`/`list.get(right)` while `left < right`,
  moving both pointers inward.</details>

Solutions are in the `solutions` package (`RemoveDuplicatesSortedSolution`,
`ReverseArrayInPlaceSolution`) — not shown here; attempt the stubs first.

## 9. Quick recap

- Backing store is `Object[] elementData`; `size` tracks logical count, capacity can exceed it.
- Growth is 1.5x via `oldCapacity + (oldCapacity >> 1)`, implemented with `Arrays.copyOf` /
  `System.arraycopy`.
- `add`(append)/`get` are O(1) (amortized for add); middle `add`/`remove` are O(n) due to shifting.
- Pre-size with `new ArrayList<>(n)` when the final size is known to skip resize copies.
- `modCount` powers fail-fast iteration, not thread safety.
