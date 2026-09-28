# Arrays & the `Arrays` / `Collections` Utility Classes

## 1. What it is

A Java array (`int[]`, `String[]`, ...) is a fixed-size, contiguous,
type-homogeneous block of memory — size is fixed at creation and cannot
grow. `java.util.Arrays` is a static utility class with helpers for arrays
(`sort`, `binarySearch`, `equals`, `fill`, `copyOf`, `asList`, `stream`,
`deepEquals`). `java.util.Collections` is the analogous static utility class
for `Collection`/`List`/`Map` (`sort`, `unmodifiableList`, `synchronizedList`,
`emptyList`, `max`/`min`, `reverse`, `rotate`, `binarySearch`, `frequency`).

## 2. How it works internally

Arrays are a JVM primitive concept, not a library class — `new int[5]`
allocates 5 contiguous ints on the heap, zero-initialized, with O(1) index
access via pointer arithmetic (`base + index * elementSize`). This is why
array access is faster and more cache-friendly than a linked structure.

```
int[] arr = new int[5];      Heap layout (contiguous):
                              [ 0 ][ 0 ][ 0 ][ 0 ][ 0 ]
                                ^index0    ^index4
arr[2] = 99;                  access = base_address + 2 * 4 bytes  -> O(1)
                              [ 0 ][ 0 ][99 ][ 0 ][ 0 ]
```

Key `Arrays` internals worth knowing:
- **`Arrays.sort(int[])`** (primitives) uses a **dual-pivot quicksort**
  (paraphrased: picks two pivots, partitions into three regions), O(n log n)
  average, O(n^2) worst case (extremely rare with the engineered pivot
  selection) — NOT stable (doesn't need to be; primitives have no identity
  beyond value).
- **`Arrays.sort(Object[])`** uses a variant of **Timsort** — O(n log n)
  worst case guaranteed, and IS stable (important when objects carry
  identity/extra state beyond the sort key).
- **`Arrays.binarySearch`** requires a PRE-SORTED array; O(log n); returns
  a negative "insertion point" encoding (`-(insertionPoint) - 1`) if not
  found, not just `-1`.
- **`Arrays.asList(array)`** returns a FIXED-SIZE list backed directly by
  the array — `set()` writes through to the array, but `add()`/`remove()`
  throw `UnsupportedOperationException` because the list can't resize an
  array.
- **`Arrays.equals`** is shallow (one level); **`Arrays.deepEquals`**
  recurses into nested arrays (needed for `int[][]`, `Object[][]`, etc.).

Key `Collections` internals worth knowing:
- **`Collections.unmodifiableList(list)`** wraps (doesn't copy) the given
  list in a read-only view — mutations attempted through the wrapper throw
  `UnsupportedOperationException`, but the UNDERLYING list can still be
  mutated directly by whoever holds the original reference, and that change
  IS visible through the wrapper (it's a view, not a defensive copy).
- **`Collections.synchronizedList(list)`** wraps every method with
  `synchronized` on a common lock — but iteration still requires the CALLER
  to manually `synchronized(list) { ... }` around the loop, since a
  fail-fast iterator can't be made thread-safe just by synchronizing the
  individual `next()` calls.
- **`Collections.sort(list)`** on a `List` internally converts to an array,
  sorts with the object Timsort, and copies back — same complexity as
  `Arrays.sort(Object[])`.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `array[i]` read/write | O(1) | O(1) | direct index arithmetic |
| `Arrays.sort(int[])` | O(n log n) avg | O(log n) (in-place, recursion) | dual-pivot quicksort |
| `Arrays.sort(Object[])` / `Collections.sort` | O(n log n) worst | O(n) | Timsort, stable |
| `Arrays.binarySearch` (sorted array required) | O(log n) | O(1) | undefined result if not sorted |
| `Arrays.copyOf` / `System.arraycopy` | O(n) | O(n) new array | `arraycopy` is a JVM intrinsic, very fast |
| `Collections.unmodifiableList` (wrap) | O(1) | O(1) | view, not a copy |
| `List.copyOf(list)` (defensive copy) | O(n) | O(n) | true immutable copy |
| `Collections.binarySearch` | O(log n) for `RandomAccess` lists (e.g. ArrayList), O(n log n) for `LinkedList` | O(1) | falls back to a linear-ish scan strategy for non-RandomAccess lists |
| `Collections.rotate` | O(n) | O(1) | in-place via reversal-style algorithm |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/foundations/examples/ArraysCollectionsUtilsDemo.java`

```java
int[] arr = {5, 3, 8, 1};
Arrays.sort(arr);
int idx = Arrays.binarySearch(arr, 8);

List<Integer> fixed = Arrays.asList(1, 2, 3);
fixed.set(0, 99);       // OK - writes through to the backing array
// fixed.add(4);        // would throw UnsupportedOperationException

List<Integer> readOnly = Collections.unmodifiableList(new ArrayList<>(List.of(1, 2, 3)));
```

Expected console output:
```
Sorted array: [1, 3, 5, 8]
binarySearch(8) index = 3
Arrays.asList set(0,99): [99, 2, 3]
Arrays.asList add(4) blocked: UnsupportedOperationException
Collections.unmodifiableList mutation blocked: UnsupportedOperationException
Collections.max/min of [5, 3, 8, 1]: max=8, min=1
Collections.rotate([1,2,3,4,5], 2): [4, 5, 1, 2, 3]
```

## 5. When to use / when NOT to use

- **Use** raw arrays when size is fixed and known up front, performance/cache
  locality matters most (numeric/matrix code), or you're implementing a
  lower-level data structure yourself (that's literally what `ArrayList`
  does internally).
- **Use** `List`/`Collections` for everything else — dynamic sizing, rich
  API (`removeIf`, `stream()`, generics without boxing headaches beyond the
  usual autoboxing cost).
- **Don't** call `.add()`/`.remove()` on the list returned by
  `Arrays.asList(...)` — it's fixed-size by design; wrap it in `new
  ArrayList<>(Arrays.asList(...))` if you need a growable copy.
- **Don't** assume `Collections.unmodifiableList` gives you a deep, mutation
  -proof snapshot — it's a live view; use `List.copyOf(list)` (Java 10+) or
  `new ArrayList<>(list)` + wrap, when you need true isolation from the
  source list's later mutations.

## 6. Common pitfalls & gotchas

**Pitfall 1 — `Arrays.asList` is fixed-size.**

```java
List<Integer> list = Arrays.asList(1, 2, 3);
list.add(4); // BUG: throws UnsupportedOperationException at runtime
```
```java
List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3)); // FIX: wrap in a real ArrayList
list.add(4); // works fine now
```

**Pitfall 2 — `List.remove(int)` vs `List.remove(Object)` ambiguity with
`Integer`/boxed types.**

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
list.remove(1);    // BUG (probably): removes by INDEX 1 -> removes value 20, not the value 1!
```
```java
list.remove(Integer.valueOf(1)); // FIX: forces the Object overload -> removes the VALUE 1 (if present)
list.remove((Integer) 1);         // equivalent cast-based fix
```

**Pitfall 3 — mutating an array/list while a caller holds an
"unmodifiable" view of it, expecting isolation.**

```java
List<Integer> backing = new ArrayList<>(List.of(1, 2, 3));
List<Integer> readOnly = Collections.unmodifiableList(backing);
backing.add(4); // NOT blocked - backing is still a normal, mutable list
System.out.println(readOnly); // BUG (if isolation was expected): prints [1, 2, 3, 4]
```
```java
List<Integer> readOnly = List.copyOf(backing); // FIX: true immutable snapshot, Java 10+
backing.add(4);
System.out.println(readOnly); // prints [1, 2, 3] - unaffected
```

## 7. Interview questions

- **[Basic]** What's the difference between an array and an `ArrayList`? →
  *An array has a fixed size decided at creation and stores primitives or
  references directly with O(1) index access; `ArrayList` is a resizable
  wrapper around an array internally, offering dynamic growth (amortized
  O(1) add), a richer API, but with autoboxing overhead for primitives
  since generics can't hold primitive types directly.* → Follow-up: *When
  would raw arrays clearly outperform ArrayList<Integer>?* (Numeric-heavy
  code like matrix math — avoiding autoboxing of every int into an Integer
  object saves both memory and CPU (boxing/unboxing + extra pointer
  indirection + cache misses from scattered heap objects instead of a
  contiguous int[] block).)

- **[Basic]** What does `Arrays.sort` use for primitives vs objects, and
  why the difference? → *Primitives use a dual-pivot quicksort (in-place,
  no stability needed since primitive values have no identity beyond their
  value — two equal ints are indistinguishable). Objects use a Timsort
  variant which is guaranteed O(n log n) worst case AND stable, because
  object equality doesn't imply "no observable difference" — two objects
  that compare equal might still carry other state, so preserving their
  relative order matters.* → Follow-up: *What does "stable" mean and give an
  example where it matters.* (Equal elements retain their original relative
  order; it matters when you sort by one key after already sorting by
  another — e.g. sort a list of orders by customer name, then sort THAT
  result by order date - stability keeps same-date orders in name order.)

- **[Basic]** What does `Arrays.asList()` return, and what's the #1 gotcha?
  → *A `List` view backed DIRECTLY by the given array (or varargs array) —
  it's fixed-size: `set()` works and writes through to the array, but
  `add()`/`remove()` throw `UnsupportedOperationException` since the list
  can't resize the underlying array.* → Follow-up: *How do you get a
  resizable list from it?* (`new ArrayList<>(Arrays.asList(...))`, or
  simply `new ArrayList<>(List.of(...))` in modern code.)

- **[Intermediate]** How does `Collections.unmodifiableList` differ from
  `List.of(...)`/`List.copyOf(...)`? → *`unmodifiableList` wraps the given
  list in a read-only VIEW — it blocks mutation attempts made through the
  wrapper, but the wrapper still reflects live changes made to the original
  backing list through any OTHER reference. `List.of(...)`/`List.copyOf(...)`
  (Java 9/10+) create a genuinely immutable, independent snapshot — no
  reference to it can ever see a later mutation, because there IS no mutable
  backing list involved.* → Follow-up: *Does List.of() allow null elements?*
  (No — `List.of()` throws `NullPointerException` if you pass null,
  unlike `Arrays.asList()` which permits null elements.)

- **[Intermediate]** Why does `list.remove(1)` behave differently for
  `List<Integer>` depending on how the argument is typed? → *`List<E>`
  declares two overloads: `remove(int index)` and `remove(Object o)`. A
  literal `1` is an `int`, so the compiler resolves it to the `int index`
  overload (removes the element AT position 1) rather than the `Object`
  overload, even though `Integer` autoboxing exists — Java prefers the exact
  primitive-matching overload over one that requires boxing. To remove the
  Integer VALUE 1, you must force boxing explicitly:
  `list.remove(Integer.valueOf(1))` or `list.remove((Integer) 1)`.* →
  Follow-up: *Does this ambiguity exist for List<String>?* (No — `String`
  has no `int` overload conflict; `list.remove("someValue")` unambiguously
  resolves to `remove(Object)`.)

- **[Intermediate]** How does `Collections.binarySearch` perform differently
  on an `ArrayList` vs a `LinkedList`? → *`Collections.binarySearch` checks
  if the list implements `RandomAccess` (which `ArrayList` does and
  `LinkedList` does not); for `RandomAccess` lists it does a true O(log n)
  index-based binary search, but for non-`RandomAccess` lists (like
  `LinkedList`) it falls back to an iterator-based approach that's
  effectively O(n log n) or worse in practice (since even "finding the
  midpoint" requires walking node links), to avoid the O(n) cost of random
  index access on every comparison step.* → Follow-up: *Why does
  `LinkedList.get(i)` even need O(n)?* (No random access — it must walk
  node-to-node from whichever end (head/tail) is closer to index i.)

- **[Advanced]** Why is `Arrays.asList()` sometimes described as "the
  bridge between arrays and collections", and what implementation detail
  makes it memory-efficient despite that? → *It returns a lightweight
  wrapper object (`Arrays.ArrayList`, an internal nested class distinct from
  `java.util.ArrayList`) that holds a direct reference to the SAME backing
  array — no element copying occurs. This is intentional: it's meant for
  quick interop (e.g. passing a `T...` varargs array to an API expecting a
  `List`) without the cost of a full defensive copy, at the cost of losing
  resizability since resizing would require reallocating the array the
  caller might still be using directly.* → Follow-up: *Is it safe to mutate
  the original array after calling Arrays.asList on it?* (Technically yes,
  syntactically — but any element change is instantly visible through the
  List view too since they share the same backing array; this is usually
  surprising/unwanted, so treat the returned list as tied to that array's
  lifecycle, not an independent snapshot.)

- **[Advanced]** `Collections.synchronizedList` wraps each method call in a
  `synchronized` block — why is manual synchronization still required
  around iteration, and what exception can result if you forget? → *Each
  individual method call (`get`, `add`, `size`, ...) is atomically
  synchronized, but ITERATION is a SEQUENCE of `hasNext()`/`next()` calls —
  synchronizing each one individually doesn't prevent another thread from
  mutating the list BETWEEN two of those calls. If that happens, the
  underlying (still fail-fast) iterator detects the structural change and
  throws `ConcurrentModificationException`, exactly as it would on a plain
  unsynchronized list. The Javadoc explicitly instructs callers to wrap the
  entire iteration block in `synchronized(list) { for (...) {...} }` to get
  true iteration safety.* → Follow-up: *Would a concurrent collection like
  CopyOnWriteArrayList avoid this problem?* (Yes — its fail-safe,
  weakly-consistent iterator never throws `ConcurrentModificationException`
  regardless of concurrent mutation, trading that safety for O(n) writes and
  potentially stale reads, as covered in the Iterable/Iterator topic.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Merge two sorted int arrays into one sorted array | two-pointer merge | `exercises/MergeSortedArrays.java` |
| E02 | Easy | Sorted, duplicate-free intersection of two int arrays | sort + two pointers | `exercises/ArrayIntersection.java` |
| E03 | Medium | Rotate an int array right by k, in place | reversal algorithm | `exercises/RotateArrayInPlace.java` |
| E04 | Hard | Recursively deep-compare two int[][] matrices without `Arrays.deepEquals` | recursive structural equality | `exercises/DeepEqualsMatrix.java` |

- **E01 hint:**
  <details><summary>hint</summary>Two pointers `i` (into a) and `j` (into b); at each step append the smaller of `a[i]`/`b[j]` to the result and advance that pointer; then append any remaining tail from whichever array isn't exhausted.</details>
- **E02 hint:**
  <details><summary>hint</summary>`Arrays.sort` both copies first (don't mutate the caller's arrays), then walk two pointers; when values match, record it ONCE and advance both pointers past all duplicates of that value.</details>
- **E03 hint:**
  <details><summary>hint</summary>Normalize `k = k % arr.length` (handle k larger than length, and k possibly 0); reverse the WHOLE array, then reverse the first `k` elements, then reverse the remaining `n-k` elements.</details>
- **E04 hint:**
  <details><summary>hint</summary>First compare `a.length == b.length` (and handle either being null); then for each row index, recursively compare `a[row]` and `b[row]` the same way you'd compare two 1-D arrays (length check + element-by-element), being careful that a row itself can be null.</details>

Target complexity — E01: O(n+m) time, O(n+m) space. E02: O((n+m) log(n+m))
time (dominated by sorting), O(n+m) space. E03: O(n) time, O(1) extra space.
E04: O(rows × cols) time, O(1) extra space (excluding recursion stack).
Solutions are in `src/main/java/com/gk/study/foundations/solutions/` — not
shown here; attempt the exercises first.

## 9. Quick recap

- Arrays are fixed-size, contiguous, O(1)-index-access memory — the fastest structure for known-size, cache-sensitive workloads, and what `ArrayList` is built on top of internally.
- `Arrays.asList(...)` is a fixed-size VIEW over the given array — `add`/`remove` throw `UnsupportedOperationException`; wrap in `new ArrayList<>(...)` to get resizability.
- `list.remove(1)` on `List<Integer>` removes by INDEX, not value — force the `Object` overload with `Integer.valueOf(1)` to remove by value.
- `Collections.unmodifiableList` is a live read-only VIEW (mutations to the backing list are still visible through it); `List.copyOf(...)`/`List.of(...)` (Java 9/10+) are true immutable, isolated snapshots.
- `Collections.synchronizedList` synchronizes individual calls but NOT a whole iteration — wrap iteration in `synchronized(list) { ... }` yourself or you can still get a `ConcurrentModificationException`.
