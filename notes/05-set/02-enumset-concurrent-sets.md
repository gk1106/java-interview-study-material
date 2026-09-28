# EnumSet and concurrent sets

## 1. What it is

`EnumSet<E extends Enum<E>>` is a specialized `Set` for enum constants, represented internally as
a **bit vector** instead of hashed buckets — every operation is a single bitwise instruction.
Concurrent sets (`CopyOnWriteArraySet`, `ConcurrentSkipListSet`, and `Collections.newSetFromMap`)
give thread-safe `Set` behaviour with different trade-offs, filling the gap left by the fact that
the JDK has **no** `ConcurrentHashSet` class.

## 2. How it works internally

### EnumSet — a bit vector, chosen at factory-method time

`EnumSet` is abstract; you never call `new`. Static factories (`EnumSet.of`, `.allOf`, `.noneOf`,
`.range`, `.complementOf`) pick one of two package-private concrete implementations based on how
many constants the target enum declares:

- **`RegularEnumSet`** — used when the enum has ≤ 64 constants (the overwhelming majority of real
  enums). The whole set is a **single `long` bit mask**: bit `i` set means the enum constant with
  `ordinal() == i` is a member.
- **`JumboEnumSet`** — used when the enum has > 64 constants. The mask becomes a `long[]` array,
  one `long` per 64 constants, otherwise identical logic.

```
enum Status { PENDING, APPROVED, REJECTED, SETTLED, CANCELLED }   // 5 constants, fits in one long

EnumSet.of(APPROVED, SETTLED):
  ordinal:   0        1         2         3        4
  bit:       0        1         0         1        0
  elements = 0b01010   (bit 1 = APPROVED, bit 3 = SETTLED)

add(REJECTED)   -> elements |= (1L << 2)   -> 0b01110
contains(SETTLED) -> (elements & (1L << 3)) != 0  -> true
remove(APPROVED)  -> elements &= ~(1L << 1)
```
Because every op is a shift + bitwise AND/OR/NOT on one (or a few, for `JumboEnumSet`) machine
words, `add`/`remove`/`contains` are effectively O(1) with an extremely small constant — no
hashing, no object allocation per element, no boxing. Iteration order is always **declaration
(ordinal) order**, unlike `HashSet`. Bulk set operations (`union` via `addAll`, `intersection` via
`retainAll`, `complement` via `EnumSet.complementOf`) become single word-level bitwise ops across
the whole set at once — this is why `EnumSet` massively outperforms `HashSet<SomeEnum>` for
flag-like enum combinations (e.g. a set of active `Permission` or `DayOfWeek` values).

### Concurrent sets

**`CopyOnWriteArraySet<E>`** wraps a `CopyOnWriteArrayList<E>` internally (composition, not
inheritance) and enforces uniqueness by linearly scanning the backing array with `equals()` before
every `add`. Every mutation (`add`, `remove`) copies the **entire** backing array, mutates the
copy, then swaps the volatile array reference — readers never block and iterators are **weakly
consistent** (a snapshot of the array at iterator-creation time; never throws
`ConcurrentModificationException`, may not reflect concurrent mutations).
```
add(X) on CopyOnWriteArraySet backed by array [A, B]:
  1. scan [A, B] for X via equals() -> not found
  2. copy: new array [A, B, X]   (full O(n) copy, not append-in-place)
  3. atomically swap the volatile array reference
  readers already iterating the OLD [A, B] array see it unchanged -- no CME, no lock
```
`contains`/`add` are both **O(n)** — there is no hashing at all, just a linear scan — which is the
opposite of `HashSet`'s O(1) average. It only pays off when the set is small and read/iterate
operations vastly outnumber writes (the canonical use case is a small, rarely-changed listener/
observer registry iterated frequently and modified rarely).

**`ConcurrentSkipListSet<E>`** wraps a `ConcurrentSkipListMap<E, Object>` (same `PRESENT` dummy
trick as `HashSet`/`TreeSet`), which is a **skip list** — a probabilistically balanced, multi-level
linked structure — instead of a red-black tree. Each node has a random "tower height," and search
starts at the highest level, dropping down a level whenever the next node would overshoot the
target, giving `O(log n)` expected search/insert/delete **without any locking**: updates use CAS
(compare-and-swap) on individual node pointers, so multiple threads can mutate different parts of
the structure concurrently without a global lock. It implements `NavigableSet`, so it's the
concurrent, thread-safe analogue of `TreeSet` — sorted iteration, `floor`/`ceiling`/`higher`/
`lower` all included.
```
level 3:  H ------------------------> 30 -------------------------> NIL
level 2:  H ---------> 10 ----------> 30 -------------> 50 -------> NIL
level 1:  H ---> 5 --> 10 ---> 20 --> 30 ---> 40 -----> 50 -------> NIL
level 0:  H -> 5 -> 10 -> 15 -> 20 -> 25 -> 30 -> 40 -> 45 -> 50 -> NIL
search(40): start top level, skip past 30 (< 40), drop a level, skip past 30 again,
            drop to level 0, walk to 40 -- O(log n) expected hops
```

**`Collections.newSetFromMap(Map<E, Boolean> map)`** is the generic escape hatch: wrap *any*
`Map` implementation as a `Set` view, backed by the same `PRESENT`-style trick (value is always
`Boolean.TRUE`, never read meaningfully). This is how you get a genuinely concurrent,
`HashMap`-speed (O(1) average) `Set` — since the JDK ships no `ConcurrentHashSet` class:
```java
Set<String> concurrentSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
```
The returned `Set` delegates every call to the wrapped map (`add` → `map.put(e, TRUE) == null`,
etc.), so it inherits whatever concurrency guarantees the underlying map provides — with
`ConcurrentHashMap`, that means lock-striped/CAS-based O(1) average operations, safe for
concurrent reads and writes without external synchronization.

## 3. Complexity

| Structure | add/remove/contains | iteration | ordering | concurrency mechanism |
|-----------|---------------------|-----------|----------|------------------------|
| `EnumSet` | O(1) (bitwise op) | O(universe size / 64) words scanned | declaration (ordinal) order | not thread-safe (no sync at all) |
| `CopyOnWriteArraySet` | O(n) (linear scan + full array copy) | O(n), weakly consistent, never throws CME | insertion order (list-backed) | copy-on-write + volatile array swap |
| `ConcurrentSkipListSet` | O(log n) expected | O(n), weakly consistent | sorted order | lock-free, per-node CAS |
| `Collections.newSetFromMap(new ConcurrentHashMap<>())` | O(1) average (inherits the map's cost) | O(capacity + size), weakly consistent | none | inherits `ConcurrentHashMap`'s lock striping / CAS |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/set/examples/EnumSetConcurrentSetsDemo.java` —
  builds `EnumSet`s with `of`/`range`/`complementOf`, times a bulk `retainAll`
  (intersection) against an equivalent `HashSet<Enum>`, then demonstrates
  `CopyOnWriteArraySet`'s safe-during-iteration mutation and `ConcurrentSkipListSet`'s sorted,
  concurrent `NavigableSet` behaviour.

```java
enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }
EnumSet<Day> weekdays = EnumSet.range(Day.MON, Day.FRI);
EnumSet<Day> weekend = EnumSet.complementOf(weekdays);
System.out.println(weekdays);  // [MON, TUE, WED, THU, FRI]  -- always declaration order
System.out.println(weekend);   // [SAT, SUN]

Set<String> listeners = new CopyOnWriteArraySet<>(List.of("audit", "email"));
for (String l : listeners) {
    listeners.add("sms");   // safe: iterator saw the OLD snapshot, no ConcurrentModificationException
}
System.out.println(listeners); // [audit, email, sms]
```
Expected console output matches the comments above.

## 5. When to use / when NOT to use

- **`EnumSet`**: always prefer it over `HashSet<SomeEnum>`/`EnumMap` for flag-like combinations of
  a *known enum type* — smaller memory footprint, faster ops, deterministic ordering. There is
  essentially no reason to use a general-purpose `Set` for enum constants.
- **`CopyOnWriteArraySet`**: small, read-heavy, write-rarely collections accessed concurrently —
  listener/observer lists, small configuration/whitelist sets read on every request but updated
  only occasionally (e.g. admin toggling a feature flag). Avoid for large sets or write-heavy
  workloads — every write is a full O(n) array copy.
- **`ConcurrentSkipListSet`**: concurrent code that needs both thread safety **and** sorted
  order/range queries — e.g. a concurrently-updated set of active transaction timestamps you need
  to query by range. If you don't need sorted order, prefer the `newSetFromMap` +
  `ConcurrentHashMap` combo (O(1) average beats O(log n)).
- **`Collections.newSetFromMap(new ConcurrentHashMap<>())`**: the default choice for "I need a
  thread-safe `HashSet`" — no ordering, best average-case throughput among the concurrent options.

## 6. Common pitfalls & gotchas

**Using `HashSet<SomeEnum>` out of habit instead of `EnumSet`** — works correctly but wastes
memory (a full hash table + boxed-ish enum references + node objects per element) and CPU (hashing
+ bucket lookup) for something a single `long` bit mask handles instantly:
```java
Set<Permission> perms = new HashSet<>();       // works, but heavyweight for a handful of enum flags
Set<Permission> perms = EnumSet.noneOf(Permission.class); // one long, bitwise ops, ordinal-order iteration
```

**Assuming `EnumSet` is thread-safe** — it is not synchronized at all; concurrent mutation from
multiple threads needs external synchronization (e.g. `Collections.synchronizedSet(enumSet)`) or
a different structure entirely.

**Iterating a `CopyOnWriteArraySet` expecting to see concurrent writes** — the iterator is a
snapshot of the array at creation time; writes from other threads (or even the same thread mid-loop)
are invisible to an in-progress iteration. This is a feature (no `ConcurrentModificationException`,
ever) but surprises people expecting "eventually visible" semantics mid-iteration.

**Defaulting to `CopyOnWriteArraySet` for a large or write-heavy set** — every `add`/`remove`
copies the entire backing array; at thousands of elements with frequent writes this is a severe
performance cliff (`O(n)` per write, with full array reallocation) compared to
`ConcurrentSkipListSet`'s `O(log n)` or `ConcurrentHashMap`-backed `O(1)` average.

**Forgetting there is no `ConcurrentHashSet` class** — reaching for a nonexistent type instead of
`Collections.newSetFromMap(new ConcurrentHashMap<>())` (or, when order matters,
`ConcurrentSkipListSet`) is a very common first-time mistake.

## 7. Interview questions

- [Basic] How is `EnumSet` represented internally? → As a bit vector — a single `long` (for enums
  with ≤ 64 constants, via `RegularEnumSet`) or a `long[]` array (for larger enums, via
  `JumboEnumSet`), where bit `i` represents whether the constant with `ordinal() == i` is present.
  → Follow-up: *Why does this make it faster than `HashSet<SomeEnum>`?* No hashing, no bucket
  lookup, no per-element node objects — `add`/`remove`/`contains` are single bitwise
  shift-and-mask operations on one machine word.
- [Basic] Why doesn't the JDK provide a `ConcurrentHashSet` class? → Because `Set` is easy to
  derive from `Map` via the `PRESENT`-dummy-value trick (exactly how `HashSet`/`TreeSet` are
  built), the JDK designers chose to expose `Collections.newSetFromMap(Map)` as a generic adapter
  instead of shipping a dedicated concurrent set class for every concurrent map variant. →
  Follow-up: *What do you pass it to get a thread-safe, hash-based set?*
  `Collections.newSetFromMap(new ConcurrentHashMap<>())`.
- [Basic] What ordering does `EnumSet` iterate in? → Always the enum's declaration order
  (`ordinal()` order), regardless of insertion order — a direct consequence of it being a bit
  vector indexed by ordinal, not a hash table or linked list. → Follow-up: *Does that match
  `TreeSet<SomeEnum>` using natural ordering?* Yes, since `Enum` implements `Comparable` by
  ordinal — but `EnumSet` gets that ordering far more cheaply (no tree structure needed).
- [Intermediate] How does `CopyOnWriteArraySet` avoid `ConcurrentModificationException`? → Its
  iterator captures a reference to the backing array at creation time; every mutation
  (`add`/`remove`) builds an entirely new array and atomically swaps the volatile reference, so an
  in-progress iterator keeps seeing its own immutable snapshot regardless of concurrent writes —
  there's no shared mutable state for the iterator to observe changing underneath it. → Follow-up:
  *What's the cost of that safety?* Every write is O(n) (full array copy), and iteration may not
  reflect the most recent writes — a "weakly consistent" iterator, not a live view.
- [Intermediate] What underlies `ConcurrentSkipListSet`, and why not a red-black tree like
  `TreeSet`? → A skip list — a linked structure with randomized multi-level "express lanes" giving
  expected O(log n) search — because concurrent, lock-free rebalancing of a red-black tree (which
  needs coordinated rotations touching multiple nodes atomically) is far harder to implement
  correctly without a global lock than concurrently splicing skip-list node pointers via CAS,
  which only ever touches one node's pointers at a time. → Follow-up: *Is skip-list O(log n) a
  guarantee or an expectation?* Expected/probabilistic, based on randomized level assignment per
  node — not a hard worst-case guarantee like a balanced tree's, though it holds with very high
  probability in practice.
- [Intermediate] When would you choose `ConcurrentSkipListSet` over
  `Collections.newSetFromMap(new ConcurrentHashMap<>())`? → Only when you need sorted iteration or
  range/navigation queries (`floor`/`ceiling`/`headSet`/`tailSet`) under concurrent access — the
  `ConcurrentHashMap`-backed set has no ordering at all and better average-case (O(1) vs O(log n))
  throughput otherwise. → Follow-up: *Does `ConcurrentHashMap` itself support any ordering?* No —
  it's hash-bucket based like `HashMap`, with no sorted-order guarantee or navigation methods.
- [Intermediate] Is `EnumSet.of(A, B)` mutable? → Yes — all the static factory methods return a
  mutable `EnumSet` instance (`RegularEnumSet`/`JumboEnumSet`); wrap with
  `Collections.unmodifiableSet(...)` if immutability is required, same as any other `Set`. →
  Follow-up: *What if you need an immutable, order-preserving snapshot of enum flags?* Either
  `Collections.unmodifiableSet(EnumSet.copyOf(...))` or `Set.copyOf(enumSet)` (which returns a
  generic immutable `Set`, losing the bit-vector representation and ordinal ordering in the
  process).
- [Advanced] Why is `EnumSet.complementOf` efficient, and what would the equivalent cost be with a
  `HashSet<SomeEnum>`? → `complementOf` is a single bitwise NOT (masked to the enum's universe
  size) on the underlying `long`/`long[]` — O(1) (or O(universe/64) words) regardless of set
  size; the `HashSet` equivalent requires iterating every constant of the enum, checking
  `contains()` on the original set for each, and building a brand-new hash table — O(universe
  size) with hashing overhead per element, plus new object/node allocation. → Follow-up: *Does
  `RegularEnumSet` vs `JumboEnumSet` choice affect `complementOf` cost meaningfully?* Only by a
  small constant factor (number of 64-bit words to NOT/mask) — both stay effectively O(1) relative
  to any hash-based alternative.
- [Advanced] Why is `CopyOnWriteArraySet.contains()` O(n) instead of O(1), and when does that
  actually matter in practice? → It's backed by `CopyOnWriteArrayList`, which has no hash index at
  all — membership is a linear `equals()` scan of the backing array; it only matters once the set
  grows into the hundreds/thousands of elements or `contains`/`add` is called in a hot path —
  for small (tens of elements) rarely-changing listener registries the constant factor is tiny and
  irrelevant, which is precisely the use case it's designed for. → Follow-up: *Would switching
  that same use case to `Collections.newSetFromMap(new ConcurrentHashMap<>())` ever be worse?*
  Only if you specifically relied on `CopyOnWriteArraySet`'s guarantee that iterators never throw
  `ConcurrentModificationException` and never observe partial in-flight mutations —
  `ConcurrentHashMap`'s iterator is also weakly consistent and CME-free, so in most cases the
  switch is a pure win once the set grows past "tiny."
- [Advanced] How does `ConcurrentSkipListSet` provide a NavigableSet's `floor`/`ceiling` without
  any locking? → Search for the target value the normal skip-list way (top-down, level by level);
  the last node visited whose value is `<=`/`>=` the target on the way down/at the base level is
  the answer — this is a pure read traversal over immutable-once-linked node references (or
  values read via volatile/CAS-consistent fields), so it needs no lock: a concurrent insert either
  hasn't linked its new node into the traversed path yet (search simply doesn't see it, weakly
  consistent) or has already fully linked it (search sees the final state) — there's no
  torn/partial state a reader can observe, by construction of how nodes are spliced in via CAS. →
  Follow-up: *Can two floor()/ceiling() calls on the same skip list, run concurrently with an
  insert, return mutually inconsistent results?* Yes, in principle, if the insert lands "between"
  them — each call independently sees whatever state existed at the moment it read node pointers
  ("weakly consistent"), which is a documented, accepted trade-off for lock-free performance.

## 8. Exercises

No dedicated coding exercises for this topic — `EnumSet`'s value is almost entirely in *knowing
when to reach for it* over `HashSet<SomeEnum>`, and the concurrent-set trade-offs are best
internalized by reading and running
`src/main/java/com/gk/study/set/examples/EnumSetConcurrentSetsDemo.java` and modifying it (try
swapping `CopyOnWriteArraySet` for `ConcurrentSkipListSet` in the demo and compare the printed
ordering). The DSA-pattern and TreeSet-backed exercises in topics 1 and 3 cover the hands-on
coding practice for this module.

## 9. Quick recap

- `EnumSet` = a bit vector (`RegularEnumSet`: one `long`; `JumboEnumSet`: `long[]`) indexed by
  `ordinal()` — every op is O(1) bitwise, iteration is always declaration order, and it is not
  thread-safe.
- There is no JDK `ConcurrentHashSet`; the idiomatic thread-safe hash-based set is
  `Collections.newSetFromMap(new ConcurrentHashMap<>())`.
- `CopyOnWriteArraySet`: O(n) reads/writes, full-array-copy-per-mutation, weakly consistent
  never-throws iterators — good only for small, read-heavy, rarely-mutated sets.
- `ConcurrentSkipListSet`: the concurrent, lock-free analogue of `TreeSet` — O(log n) expected,
  sorted order, full `NavigableSet` API, no global lock (per-node CAS).
- Reach for `EnumSet` by default for any enum-flag combination; reach for
  `newSetFromMap(ConcurrentHashMap)` by default for "just need a thread-safe set," and
  `ConcurrentSkipListSet` only when concurrent code also needs sorted/range queries.
