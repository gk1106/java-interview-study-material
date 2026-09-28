# Build it yourself: MyArrayList&lt;T&gt; and MySinglyLinkedList&lt;T&gt;

## 1. What it is

The best way to prove you actually understand `ArrayList`/`LinkedList` internals (topics 2 and 3)
is to implement minimal, working versions yourself: `MyArrayList<T>` (array-backed, manual
growth) and `MySinglyLinkedList<T>` (singly linked `Node<T>` chain — deliberately singly linked,
not doubly, to keep the head/tail bookkeeping instructive without duplicating `LinkedList`).

## 2. How it works internally

### MyArrayList&lt;T&gt; — what to implement and why

Required API surface: `add(T)`, `add(int, T)`, `get(int)`, `set(int, T)`, `remove(int)`,
`size()`, `isEmpty()`, `contains(T)`, `indexOf(T)`, `iterator()` (implements `Iterable<T>`).

Design points to get right (these are exactly what the interview questions in topics 2 and 7
probe):
- **Generic array creation**: Java forbids `new T[capacity]` directly (type erasure — the JVM
  doesn't know `T` at runtime). The standard workaround, used by the JDK itself, is to allocate
  `Object[]` and **cast** on read:
  ```java
  private Object[] elements = new Object[DEFAULT_CAPACITY];
  @SuppressWarnings("unchecked")
  private T at(int index) { return (T) elements[index]; }
  ```
  The cast is unchecked but safe **by construction**, because only `add`/`set` (which are
  generic-typed at compile time) ever write into the array — document this with
  `@SuppressWarnings("unchecked")` plus a comment explaining why it's safe.
- **Resizing strategy**: grow by ~1.5x (mirroring `ArrayList`) when `size == elements.length`,
  using `Arrays.copyOf` — implement `grow()` as its own private method so the growth policy is
  isolated and testable (a growth-boundary test should assert the list still behaves correctly
  right at capacity, one past capacity, and after several growth cycles).
- **`modCount` for fail-fast iteration**: increment it on every structural change (`add`,
  `remove`, but not `set`), and have your `Iterator` capture `expectedModCount` at creation,
  checking it on every `next()` — this is what makes your `Iterator.remove()` (if you implement
  it) safe and what makes a plain external structural mutation during iteration throw
  `ConcurrentModificationException`, matching real `ArrayList` behaviour.
- **Bounds checking**: every index-taking method must validate `0 <= index < size` (or `<= size`
  for insertion) and throw `IndexOutOfBoundsException` with a useful message — don't rely on the
  underlying array's own bounds check, since `elements.length` (capacity) is usually larger than
  `size` (logical length) and would let out-of-bounds reads silently return `null` instead of
  throwing.

### MySinglyLinkedList&lt;T&gt; — what to implement and why

Required API surface: `addFirst(T)`, `addLast(T)`, `removeFirst()`, `get(int)`, `size()`,
`isEmpty()`, `contains(T)`, `iterator()`.

Design points:
- **Singly linked `Node<T>`** (`item` + `next` only, no `prev`) — keep both `head` and `tail`
  references so `addLast` is O(1) (without a `tail` reference, `addLast` would require an O(n)
  walk to find the last node every time — a very common beginner mistake worth deliberately
  avoiding here).
- **`removeFirst()` tail edge case**: when removing the only remaining node, both `head` and
  `tail` must be reset to `null` — forgetting to clear `tail` leaves a **dangling reference**
  that both leaks memory (the old node can't be GC'd if something still points at it as `tail`)
  and corrupts the next `addLast` (which would wrongly link after a node no longer reachable from
  `head`).
- **Why singly, not doubly, here**: implementing doubly linked bookkeeping (`prev` maintenance on
  every insert/remove) is mechanical repetition of the same idea; singly linked plus a `tail`
  pointer is enough to internalize the core trade-off (O(1) head/tail ops, O(n) indexed access)
  without the exercise becoming pure typing.

ASCII diagram — `MySinglyLinkedList` state after `addLast(A); addLast(B); addFirst(Z)`:
```
head                              tail
 |                                  |
 v                                  v
[Z|next]->[A|next]->[B|next=null]
```

## 3. Complexity

| Operation | MyArrayList | MySinglyLinkedList |
|-----------|-------------|----------------------|
| `add`/`addLast` (append) | O(1) amortized | O(1) (tail ref) |
| `addFirst` | O(n) (shift) | O(1) |
| `get(index)` | O(1) | O(n) |
| `remove(index)` / `removeFirst` | O(n) | O(1) for `removeFirst`, O(n) for arbitrary index |
| `contains` | O(n) | O(n) |
| growth event | O(n) copy, amortized O(1) per add | n/a (no array) |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/BuildYourOwnListDemo.java`
- Reference implementations (real, working code — not stubs):
  `src/main/java/com/gk/study/list/solutions/MyArrayList.java`,
  `src/main/java/com/gk/study/list/solutions/MySinglyLinkedList.java`
- TODO stubs for you to attempt first:
  `src/main/java/com/gk/study/list/exercises/MyArrayListExercise.java`,
  `src/main/java/com/gk/study/list/exercises/MySinglyLinkedListExercise.java`

```java
MyArrayList<String> list = new MyArrayList<>();
list.add("A"); list.add("B"); list.add(1, "X");
System.out.println(list);          // [A, X, B]
System.out.println(list.get(2));   // B

MySinglyLinkedList<Integer> ll = new MySinglyLinkedList<>();
ll.addLast(1); ll.addLast(2); ll.addFirst(0);
System.out.println(ll);            // [0, 1, 2]
```
Expected console output:
```
[A, X, B]
B
[0, 1, 2]
```

## 5. When to use / when NOT to use

- Never use these in real production code — the JDK's `ArrayList`/`LinkedList` are more complete,
  more optimized, and battle-tested. This exercise exists purely to force you to reason about
  growth math, generic-array casting, pointer rewiring and off-by-one bounds checking with your
  own hands, which is exactly what "explain ArrayList internals" interview questions probe.
- Do build and run these when preparing for interviews at companies known to ask "implement your
  own X" as a live-coding round (common for backend/infra-heavy roles).

## 6. Common pitfalls & gotchas

**Forgetting `@SuppressWarnings("unchecked")` reasoning** — an unchecked cast from `Object[]` to
`T` is *always* a compiler warning; understand *why* it's safe here (you control every write into
the array through generically-typed methods) rather than reflexively suppressing every warning
you see elsewhere.

**Off-by-one on insertion bounds** — `add(int index, T e)` should accept `index == size`
(append at the end via the indexed API) but reject `index > size`; `get`/`remove`/`set` should
reject `index == size` (no element exists there). Mixing these two bound rules up is the single
most common bug when implementing this from scratch.

**Not nulling out vacated slots on `MyArrayList.remove`** — like the real `ArrayList`, failing to
set the last (now-unused) slot to `null` after shifting keeps a "loitering" reference alive,
which the JVM's GC cannot reclaim even though the list no longer logically contains it.

**`MySinglyLinkedList` losing `tail` on last-element removal** — see internals section above;
write a dedicated test for "remove the only element, then addLast again" to catch this.

## 7. Interview questions

- [Basic] Why can't you write `new T[capacity]` directly in a generic class? → Generics are
  erased at compile time — at runtime there is no `T` to allocate an array of; the JVM needs a
  concrete component type for array creation, which erasure removes, so the language disallows it
  entirely (a compile error, not just a warning). → Follow-up: *What's the standard workaround?*
  Allocate `Object[]` internally and cast to `T` (unchecked) on read, exactly as the real
  `ArrayList` does internally.
- [Basic] Why does `MyArrayList` need both a `size` field and an `elements.length`? → `length` is
  the array's physical **capacity** (including unused spare slots reserved for future growth);
  `size` is the **logical** element count actually stored — they diverge intentionally so `add`
  doesn't need to reallocate on every call. → Follow-up: *What would break if you used
  elements.length as the logical size instead?* Every single `add` would force a resize
  (allocate + copy), destroying the whole amortized-O(1) benefit of pre-allocated spare capacity.
- [Intermediate] Walk through implementing `add(int index, T e)` for `MyArrayList` from scratch.
  → Validate `0 <= index <= size`; ensure capacity (grow if `size == elements.length`); shift
  elements from `index` to `size-1` one slot right (via `System.arraycopy` or a manual loop from
  the end backward to avoid overwriting data before it's copied); write `e` at `index`; increment
  `size` and `modCount`. → Follow-up: *Why shift from the end backward if doing it manually
  (without arraycopy)?* Shifting left-to-right would overwrite `elements[index+1]` with
  `elements[index]`'s new value before its original value has been copied further right — you'd
  lose data; `System.arraycopy` handles overlapping ranges correctly regardless of direction, but
  a hand-written loop must go right-to-left.
- [Intermediate] Why does `MySinglyLinkedList` keep a `tail` reference instead of always walking
  from `head`? → Without `tail`, `addLast` would require an O(n) walk to find the last node on
  every call, turning what should be an O(1) append into O(n) — exactly the mistake this exercise
  is designed to make you avoid deliberately, by comparing it against the "naive" version's cost.
  → Follow-up: *Does adding a tail reference complicate removeFirst?* Only at the single-element
  edge case — when removing the last remaining node, both `head` and `tail` must be reset to
  `null`, not just `head`.
- [Intermediate] How would you add fail-fast iteration (`ConcurrentModificationException`) to
  `MyArrayList`? → Add a `modCount` field incremented on every structural change; your
  `Iterator` implementation captures `expectedModCount = modCount` in its constructor and checks
  `modCount == expectedModCount` at the start of every `next()`, throwing
  `ConcurrentModificationException` on mismatch. → Follow-up: *Does set(index, e) need to bump
  modCount?* No — it doesn't change the list's structure/size, matching the real `ArrayList`'s
  behaviour (an iterator survives a concurrent `set` without throwing).
- [Advanced] What's the amortized cost analysis for your own `grow()` implementation, and how
  would you write a test to verify amortized O(1) behaviour empirically? → Same accounting as
  real `ArrayList` — copies happen on a geometric schedule (every ~1.5x calls), so total copy
  work across N appends sums to a converging geometric series bounded by O(N); to test it
  empirically, time N appends for increasing N (e.g. 10k, 100k, 1M) and confirm total time scales
  roughly linearly, not quadratically, with N. → Follow-up: *Would a growth factor of exactly 1.0
  (grow by a fixed constant like +10 each time) still be amortized O(1)?* No — fixed-increment
  growth makes total copy work O(N²/increment), i.e., still quadratic in N (just with a smaller
  constant), because the array is resized O(N) times instead of O(log N) times; the growth factor
  must be a multiplicative (not additive) function of current capacity for true amortized O(1).
- [Advanced] Why does the JDK's real `ArrayList` avoid exposing its backing array directly, and
  should your `MyArrayList` do the same? → Exposing the raw `Object[]` (even via a getter) would
  let external code mutate slots beyond `size` (corrupting the size/capacity invariant) or hold a
  reference to an array that gets replaced by the next `grow()` call (becoming silently stale) —
  keep `elements` `private` and only expose data through `get`/`set`/`iterator`, exactly like the
  real class. → Follow-up: *Is toArray() safe to expose then?* Yes, as long as it returns a
  **copy** (`Arrays.copyOf(elements, size)`), not the live backing array.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| B1 | Build-it-yourself | Implement a resizable array-backed list from scratch | Manual array growth, generic array cast | `exercises/MyArrayListExercise.java` |
| B2 | Build-it-yourself | Implement a singly linked list from scratch | Node chain, head/tail bookkeeping | `exercises/MySinglyLinkedListExercise.java` |

**B1 — MyArrayList&lt;T&gt;**
- Implement: `add(T)`, `add(int,T)`, `get(int)`, `set(int,T)`, `remove(int)`, `size()`,
  `isEmpty()`, `contains(T)`, `indexOf(T)`, `iterator()`.
- Constraint: `add`/`get` amortized O(1) / O(1); growth factor ~1.5x; proper bounds checking;
  `Iterable<T>` with fail-fast `Iterator`.
- <details><summary>Hint</summary>Store `Object[] elements`; write a private
  <code>ensureCapacity()</code> that grows via <code>Arrays.copyOf(elements, newCap)</code> using
  <code>oldCap + (oldCap &gt;&gt; 1)</code>; cast to <code>T</code> only inside a single private
  accessor method.</details>

**B2 — MySinglyLinkedList&lt;T&gt;**
- Implement: `addFirst(T)`, `addLast(T)`, `removeFirst()`, `get(int)`, `size()`, `isEmpty()`,
  `contains(T)`, `iterator()`.
- Constraint: `addFirst`/`addLast`/`removeFirst` O(1); `get(index)` O(n).
- <details><summary>Hint</summary>Keep both `head` and `tail` node references; remember to null
  out `tail` (not just `head`) when removing the last remaining node.</details>

Solutions are the real implementations in the `solutions` package
(`solutions/MyArrayList.java`, `solutions/MySinglyLinkedList.java`) — attempt the stubs in
`exercises/` first.

## 9. Quick recap

- Generic arrays can't be created directly (`new T[n]` doesn't compile) — allocate `Object[]` and
  cast on read, documented with `@SuppressWarnings("unchecked")`.
- Growth must be multiplicative (~1.5x, matching real `ArrayList`), not additive, to keep
  amortized O(1) append.
- A singly linked list needs an explicit `tail` reference for O(1) `addLast`, and must null out
  `tail` (not just `head`) when the list becomes empty.
- Bounds checks must compare against logical `size`, not the backing array's physical capacity.
- Building these from scratch is the fastest way to make "explain ArrayList/LinkedList
  internals" interview answers concrete instead of memorized.
