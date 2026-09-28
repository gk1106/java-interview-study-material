# List interface & core operations

## 1. What it is

`List<E>` is the sub-interface of `Collection<E>` that models an **ordered, index-addressable
sequence** that allows duplicates. Every element has a position (0-based index), so besides the
usual add/remove/contains from `Collection`, `List` adds positional access (`get(int)`,
`set(int, E)`, `add(int, E)`, `indexOf`), range views (`subList`) and a richer iterator
(`ListIterator`) that can move backward and mutate in place.

## 2. How it works internally

`List` itself has **no fields or storage** — it is a contract. The behaviour (and therefore the
performance) comes entirely from the implementing class:

```
Collection<E>
   └── List<E>            (interface — order + index contract)
         ├── ArrayList<E>       -> backed by Object[] (contiguous array)
         ├── LinkedList<E>      -> backed by doubly-linked Node<E> chain
         ├── Vector<E>          -> like ArrayList, every method synchronized
         │      └── Stack<E>    -> Vector + push/pop/peek (LIFO)
         └── CopyOnWriteArrayList<E> -> array copied on every mutation
```

Key methods defined by the interface (not by any one implementation):

| Method | Contract |
|---|---|
| `boolean add(E e)` | append at the end, always returns `true` for `List` |
| `void add(int index, E e)` | insert, shifting subsequent elements right |
| `E get(int index)` | positional read |
| `E set(int index, E e)` | positional overwrite, returns old value |
| `E remove(int index)` | remove by **position** |
| `boolean remove(Object o)` | remove first element that `.equals(o)` |
| `int indexOf(Object o)` / `lastIndexOf` | linear scan using `equals` |
| `List<E> subList(int from, int to)` | live **view**, not a copy |
| `ListIterator<E> listIterator()` | bidirectional, supports `set`/`add`/`remove` during traversal |

`RandomAccess` is a **marker interface** (no methods) that `ArrayList`/`Vector` implement but
`LinkedList` does not. Algorithms in `Collections` (e.g. `Collections.binarySearch`) check
`instanceof RandomAccess` to decide whether to iterate with `get(i)` (O(1) per step, fine for
arrays) or switch to a `ListIterator` (because `get(i)` on a linked list would be O(n) per step,
turning the whole algorithm into O(n²)).

## 3. Complexity

Complexity is **implementation-dependent** — this is the whole reason the module studies each
implementation separately (topics 2, 3, 5, 6). As a sub-interface contract only:

| Operation | Guarantee from the interface |
|-----------|-------------------------------|
| `get`/`set` | none — could be O(1) (array) or O(n) (linked) |
| `add`/`remove` at arbitrary index | none — could be O(n) amortized shift or O(1) splice |
| `contains`/`indexOf` | O(n) linear scan for all standard implementations (no index/hash) |
| `subList` | O(1) — always a view, never a copy |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/ListCoreOperationsDemo.java`

```java
List<String> accounts = new ArrayList<>(List.of("A100", "A200", "A300"));
accounts.add(1, "A150");          // [A100, A150, A200, A300]
accounts.set(0, "A101");          // [A101, A150, A200, A300]
System.out.println(accounts.indexOf("A200")); // 2
List<String> view = accounts.subList(1, 3);   // live view [A150, A200]
view.clear();                                  // mutates accounts too!
System.out.println(accounts);     // [A101, A300]
```
Expected console output:
```
2
[A101, A300]
```

## 5. When to use / when NOT to use

- Use `List` as the **declared type** of a variable/field/parameter whenever order matters and
  duplicates are allowed (transaction history, ordered account statements, form field order).
- Pick the concrete implementation based on the *access pattern*, not habit:
  - Mostly random reads, rare structural changes → `ArrayList`.
  - Frequent insert/remove at the head/middle with sequential access → `LinkedList`
    (or better, `ArrayDeque` if you don't need `List`'s index API).
  - Read-heavy, write-rare, concurrent → `CopyOnWriteArrayList`.
- Do NOT use `List` when you need uniqueness (`Set`) or key lookup (`Map`) — using
  `list.contains()` in a hot loop is a classic banking-codebase perf bug (O(n) per call).

## 6. Common pitfalls & gotchas

**`Arrays.asList` is fixed-size** — it's backed by the array itself, so `add`/`remove` throw:
```java
List<Integer> l = Arrays.asList(1, 2, 3);
l.add(4); // UnsupportedOperationException
// fix:
List<Integer> mutable = new ArrayList<>(Arrays.asList(1, 2, 3));
```

**`List.of(...)` is truly immutable** — even `set()` throws, unlike `Arrays.asList` which allows
`set()` but not resize.

**`remove(int)` vs `remove(Object)` ambiguity with `Integer`:**
```java
List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
nums.remove(1);          // removes INDEX 1 -> [10, 30]  (int overload wins)
nums.remove(Integer.valueOf(1)); // removes the OBJECT 1 -> no-op, not present
```
This bites banking code that stores account numbers as `List<Integer>` and tries
`list.remove(accountNumber)` expecting object removal.

**`equals`-based `indexOf`/`contains` need a correct `equals()`** on custom objects, otherwise
lookups silently return `-1`/`false` even when a "logically equal" element is present.

## 7. Interview questions

- [Basic] What's the difference between `Collection` and `List`? → `List` adds ordering and
  index-based access (`get`, `set`, positional `add`/`remove`), and permits duplicates by
  contract; plain `Collection` makes no ordering promise. → Follow-up: *Does `Set` extend
  `List`?* No — `Set` and `List` are siblings under `Collection`, mutually exclusive contracts.
- [Basic] Why does `list.remove(1)` behave differently for `List<Integer>` vs `List<String>`? →
  For `Integer` there are two overloads, `remove(int index)` and `remove(Object o)`; the compiler
  picks the most specific applicable one, and an `int` literal always resolves to the `int`
  overload. For `String` only `remove(Object)` applies since there's no `int`→`String`
  conversion. → Follow-up: *How do you force object removal for a List<Integer>?* Box it:
  `list.remove(Integer.valueOf(x))`.
- [Basic] Is `Arrays.asList()` mutable? → You can `set()` elements (it writes through to the
  backing array) but not `add`/`remove` — it's fixed-size. → Follow-up: *How do you get a fully
  mutable copy?* Wrap it in `new ArrayList<>(...)`.
- [Intermediate] What is `RandomAccess` and why does it matter? → A marker interface with no
  methods; `ArrayList`/`Vector` implement it, `LinkedList` doesn't. Library code (e.g.
  `Collections.binarySearch`) checks it via `instanceof` to choose an O(1)-index-lookup loop vs an
  iterator-based loop, avoiding O(n²) traversal on linked lists. → Follow-up: *What happens if you
  binary-search a LinkedList directly with a manual for-loop using get(i)?* O(n) per `get`, so the
  whole binary search degrades to O(n log n) → still bad, actually **O(n²)** in the worst
  reasoning since each `get` is O(n) and there are O(log n) steps = O(n log n); either way far
  worse than the intended O(log n).
- [Intermediate] Is `subList` a copy or a view? → A **live view** backed by the same array/nodes;
  structural changes to the sublist are reflected in the parent and vice versa, and structural
  changes to the parent through anything other than the sublist invalidate the sublist
  (`ConcurrentModificationException` on next use). → Follow-up: *How do you get an independent
  copy instead?* `new ArrayList<>(list.subList(a, b))`.
- [Intermediate] Why does `List` not extend `RandomAccess`? → Because not every `List` supports
  fast random access (`LinkedList` doesn't); making it part of the base contract would force
  linked structures to lie about their performance characteristics.
- [Advanced] Why is `indexOf` O(n) even though the list may be sorted? → `List` has no ordering
  comparator baked into the interface — it only guarantees insertion order, not sort order, so
  `indexOf` can't assume monotonicity and must scan linearly using `equals`. `Collections.
  binarySearch` is a separate opt-in utility you must call explicitly on a list you know is sorted.
  → Follow-up: *What breaks if you binary-search an unsorted list?* Undefined result — no
  exception, just a wrong index, because binary search relies on the sortedness invariant it
  cannot verify.
- [Advanced] Why does `List<E>` not provide a `sort` that returns a new list? → `List.sort
  (Comparator)` (default method since Java 8) sorts **in place** to avoid an extra O(n) allocation
  for the common case; if you want a new sorted list you explicitly copy first
  (`list.stream().sorted().toList()` or `new ArrayList<>(list); Collections.sort(copy)`).

## 8. Exercises

Pattern exercises that operate on `List<Integer>`/arrays are concentrated in topic 8
(`08-dsa-patterns-lists-arrays.md`) and spread into topics 2, 3 and 7 where the implementation
detail is the point (e.g. in-place array ops live with `ArrayList` internals, linked-list
reversal lives with `LinkedList` internals). See those files' tables for the full list; this
topic is foundational and has no exercise of its own.

## 9. Quick recap

- `List` is a contract (order + index access), not a data structure — performance comes from the
  concrete class.
- `RandomAccess` marker lets generic algorithms pick an O(1)-index loop vs iterator loop.
- `subList` is a live view, not a copy — mutate carefully.
- `remove(int)` vs `remove(Object)` is a classic `List<Integer>` trap.
- `Arrays.asList` = fixed-size, writable; `List.of` = fully immutable.
