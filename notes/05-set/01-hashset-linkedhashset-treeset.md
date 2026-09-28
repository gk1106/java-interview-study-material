# HashSet, LinkedHashSet, TreeSet

## 1. What it is

`Set<E>` is a `Collection` that forbids duplicate elements (at most one `null`, per `equals()`).
The three general-purpose implementations trade off **ordering guarantees** for **lookup cost**:
`HashSet` (no ordering, fastest), `LinkedHashSet` (insertion order, small overhead over
`HashSet`), and `TreeSet` (sorted order, `O(log n)` instead of `O(1)`, but adds range/navigation
queries no hash-based set can offer).

## 2. How it works internally

### HashSet — literally a HashMap wearing a trench coat

`HashSet<E>` does **not** reimplement hashing/bucketing. It holds a private
`HashMap<E, Object> map` field and a single shared sentinel value:
```java
private static final Object PRESENT = new Object();   // dummy value, same instance reused for every key
```
Every `Set` operation is a one-line delegation to the map:
```
add(e)      -> map.put(e, PRESENT) == null          // true if e was NOT already a key
contains(e) -> map.containsKey(e)
remove(e)   -> map.remove(e) == PRESENT
size()      -> map.size()
iterator()  -> map.keySet().iterator()
```
So every `HashMap` internal you already know applies unchanged: an `Object[] table` of buckets,
each bucket a linked list of `Node<K,V>` (treeified into a red-black tree once a single bucket
holds ≥8 entries **and** the table has ≥64 buckets), hash spreading via
`h ^ (h >>> 16)` before masking with `(capacity - 1)`, default capacity 16, load factor 0.75,
resize (capacity ×2) when `size > capacity * 0.75`.

```
HashSet elements {17, 33, 5} inserted into a HashSet backed by a HashMap, capacity 16:

table (buckets, length 16)
index: 0    1    ...   17&15=1
       [ ]  [17]->[33]  [ ]  ...  [5]->[ ]
             |PRESENT|                |PRESENT|
       17 and 33 collide (both hash to bucket 1) -> chained via `next`
       key = element, value = shared PRESENT constant (never actually read)
```
Iteration order is **whatever bucket order the table happens to have** — an artifact of hash
codes and capacity, not something to rely on. This is *the* thing every "why is my HashSet order
weird" bug traces back to.

### LinkedHashSet — same HashMap machinery, plus a threaded insertion-order list

`LinkedHashSet` extends `HashSet` but its constructors call a package-private `HashSet`
constructor that swaps the backing map for a `LinkedHashMap` instead of a plain `HashMap`:
```java
public LinkedHashSet(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor, true);   // dummy boolean picks the LinkedHashMap ctor
}
```
`LinkedHashMap.Entry<K,V>` extends `HashMap.Node<K,V>` and adds two extra pointers, `before` and
`after`, threading **every** entry into one doubly linked list in insertion order — completely
independent of which hash bucket the entry lives in. Iteration walks that list (`header.after...`)
instead of scanning buckets, so it's `O(size)` regardless of table capacity, and the order is
always insertion order (`LinkedHashSet` never uses `LinkedHashMap`'s access-order mode).
```
buckets (for hashing/lookup):        [ ]->[C]  [A]  [ ]->[B]
                                            (bucket index depends on hashCode)

insertion-order list (for iteration), independent of bucket layout:
        head <-> A <-> B <-> C <-> tail       (order: add(A), add(B), add(C))
```
`contains`/`add`/`remove` cost is identical to `HashSet` (same bucket lookup); the linked list
only adds a constant amount of pointer bookkeeping per insert/remove and is what iteration walks.

### TreeSet — a NavigableMap (red-black tree) underneath

`TreeSet<E>` holds a `private transient NavigableMap<E, Object> m`, defaulting to
`new TreeMap<>()`, and reuses the same `PRESENT` dummy-value trick as `HashSet`. `TreeMap` is a
**red-black tree** — a self-balancing binary search tree where every node is colored red or
black, and four invariants (root is black, red nodes never have red children, every root-to-null
path has the same number of black nodes, etc.) keep the tree height at `O(log n)` even after
adversarial insert/delete sequences, via rotations + recoloring on structural change.
```
                (10,B)
               /      \
           (5,R)      (20,R)
           /   \       /    \
        (2,B) (7,B) (15,B) (30,B)

insert/delete only ever touch O(log n) nodes on the path from root to the change point, then at
most a constant number of rotations to restore red-black balance — height stays O(log n).
```
Ordering comes from `Comparable.compareTo` (natural ordering) or a `Comparator` passed to the
constructor — **never** from `equals()`/`hashCode()`. `NavigableSet` methods
(`floor`/`ceiling`/`higher`/`lower`/`headSet`/`tailSet`/`subSet`/`pollFirst`/`pollLast`) are thin
delegations to the identically-named `NavigableMap` methods on the backing tree, each `O(log n)`.
`headSet`/`tailSet`/`subSet` return **live views**, not copies — mutating the view mutates the
original set, and vice versa, as long as inserted elements stay within the view's bounds.

## 3. Complexity

| Operation | HashSet | LinkedHashSet | TreeSet |
|-----------|---------|---------------|---------|
| `add` / `remove` / `contains` | O(1) avg, O(log n) worst case (treeified bucket) | O(1) avg, O(log n) worst case | O(log n) always |
| `first()` / `last()` | n/a | n/a | O(log n) (or O(1) with a cached leftmost/rightmost pointer, impl-dependent) |
| `floor` / `ceiling` / `higher` / `lower` | n/a | n/a | O(log n) |
| `headSet` / `tailSet` / `subSet` | n/a | n/a | O(log n) to create the view; iterating it is O(view size) |
| iteration | O(capacity + size) | O(size) | O(size), in sorted order |
| ordering guarantee | none | insertion order | sorted order (natural or Comparator) |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/set/examples/HashSetLinkedHashSetTreeSetDemo.java`
  — inserts the same elements into all three, prints their differing iteration order, then
  exercises `floor/ceiling/higher/lower/headSet/tailSet/pollFirst/pollLast` on a `TreeSet`.

```java
Set<Integer> hash = new HashSet<>(List.of(50, 20, 90, 10, 30));
Set<Integer> linked = new LinkedHashSet<>(List.of(50, 20, 90, 10, 30));
Set<Integer> tree = new TreeSet<>(List.of(50, 20, 90, 10, 30));
System.out.println(hash);    // e.g. [50, 20, 90, 10, 30] bucket order -- NOT guaranteed, don't rely on it
System.out.println(linked);  // [50, 20, 90, 10, 30]  -- always insertion order
System.out.println(tree);    // [10, 20, 30, 50, 90]  -- always sorted order

NavigableSet<Integer> nav = (NavigableSet<Integer>) tree;
System.out.println(nav.floor(25));    // 20  (largest <= 25)
System.out.println(nav.ceiling(25));  // 30  (smallest >= 25)
System.out.println(nav.higher(30));   // 50  (strictly > 30)
System.out.println(nav.lower(30));    // 20  (strictly < 30)
```
Expected console output matches the comments above (HashSet's line is illustrative — its exact
order depends on `hashCode()`/capacity and should never be asserted on in real code).

## 5. When to use / when NOT to use

- **HashSet**: default choice for "just need membership/uniqueness, don't care about order" —
  fastest average-case `add`/`contains`/`remove`. Don't use it when you need reproducible
  iteration order (tests comparing printed output, UI display order) or sorted output.
- **LinkedHashSet**: same speed as `HashSet` for lookups, plus predictable insertion-order
  iteration — good for de-duplicating a stream while preserving first-seen order (e.g. dedup a
  list of account IDs from a batch file, preserving file order), or as a simple LRU-adjacent
  structure. Slightly more memory per entry (two extra pointers) than plain `HashSet`.
- **TreeSet**: use when you need sorted iteration, range queries (`headSet`/`tailSet`/`subSet`),
  or "closest value" queries (`floor`/`ceiling`) — e.g. finding the nearest transaction timestamp
  at or before a cutoff. Don't use it just for uniqueness checking; `O(log n)` per op and a
  heavier per-node memory footprint (tree pointers + color bit) are wasted if you never need order.

## 6. Common pitfalls & gotchas

**Mutating an object's hash-relevant fields while it sits in a `HashSet`** — the element is
stored in the bucket computed from its `hashCode()` **at insertion time**; if a field used by
`hashCode()`/`equals()` changes afterward, the element is now in the "wrong" bucket and
`contains()`/`remove()` (which recompute the current hash to pick a bucket) will look in the
wrong place and silently fail to find it — the object is still in the set, just unreachable by
value:
```java
Set<Account> seen = new HashSet<>();
Account a = new Account("AC-1"); // hashCode/equals based on accountId
seen.add(a);
a.setAccountId("AC-2");          // BUG: mutates the field hashCode() depends on
seen.contains(a);                // false! -- computed a NEW hash, looked in the wrong bucket
```
Fix: treat elements placed in a `HashSet`/`HashMap` key position as effectively immutable for
their hash-relevant fields, or use an immutable value object / record for anything stored in a set.

**The identical bug in a `TreeSet`, one level worse** — if a `Comparator`/`compareTo` result for
an element changes after insertion, the tree's structural invariant (left < node < right) is
silently violated; subsequent lookups can fail to find an element that's still physically in the
tree, and even `iterator()` traversal order can become inconsistent.

**`TreeSet` treats "equal" as `compareTo() == 0`, not `equals()`** — if a `Comparator` considers
two distinct objects equal, only one survives an `add()`, even though `equals()` says they're
different:
```java
TreeSet<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
set.add("Bank");
set.add("BANK");   // NOT added -- comparator says compareTo == 0, even though !"Bank".equals("BANK")
System.out.println(set.size()); // 1, not 2
```

**`TreeSet` throws on `null`** — `add(null)` throws `NullPointerException` (natural ordering can't
compare against `null`), whereas `HashSet`/`LinkedHashSet` both permit exactly one `null` element.

**Forgetting `HashSet` iteration order is unspecified and JVM/version dependent** — never assert
on it in tests, never rely on it for output that must be stable; use `LinkedHashSet` or `TreeSet`
when order matters at all.

## 7. Interview questions

- [Basic] How is `HashSet` implemented internally? → It wraps a `HashMap<E, Object>`; every
  element becomes a map key and all elements share one dummy `PRESENT` value as the map value —
  `add`/`contains`/`remove`/`size` all delegate straight to the map's equivalent key operations.
  → Follow-up: *Why not store elements in a plain array or list internally?* Then `contains` would
  be O(n); reusing `HashMap` gives O(1) average lookup via hashing "for free," with zero
  duplicated engineering effort.
- [Basic] Why must an object's `hashCode()` and `equals()` be stable while it's stored as a
  `HashSet` element (or `HashMap` key)? → The element's bucket is chosen from its hash code at
  insertion time; if a hash-relevant field mutates afterward, later lookups recompute a *different*
  hash, search the wrong bucket, and silently fail to find an element that is still physically
  present in the set — it becomes "lost" without being removed. → Follow-up: *How would you catch
  this bug in code review?* Look for mutable setters on a class used as a `Set`/`Map` key, or
  fields used in `hashCode()`/`equals()` being reassigned after the object is stored — prefer
  immutable key/element types (records, final fields).
- [Basic] `HashSet` vs `TreeSet` vs `LinkedHashSet` — when do you use which? → `HashSet` for pure
  membership/uniqueness with no ordering need (fastest, O(1) avg); `LinkedHashSet` when you need
  the *insertion* order preserved during iteration at nearly the same speed; `TreeSet` when you
  need *sorted* order, range queries, or "closest element" lookups, accepting O(log n) ops. →
  Follow-up: *What does each cost in extra memory per element versus a plain `HashSet`?*
  `LinkedHashSet` adds two pointers (`before`/`after`) per entry; `TreeSet` adds left/right/parent
  tree-node pointers plus a color bit per entry — both are heavier per-element than `HashSet`.
- [Basic] Can a `HashSet` contain `null`? Can a `TreeSet`? → `HashSet`/`LinkedHashSet` allow
  exactly one `null` element (hashed to bucket 0 conceptually); `TreeSet` throws
  `NullPointerException` on `add(null)` because natural ordering/`compareTo` can't compare against
  `null` (a custom comparator that special-cases `null` is the only way around it, and is
  unusual). → Follow-up: *Does `Set.of(...)` (immutable set factory) allow null?* No — it throws
  `NullPointerException` eagerly, even before any comparison logic, as part of its fail-fast
  design.
- [Intermediate] How does `LinkedHashSet` maintain insertion order without re-implementing
  hashing? → It's a thin `HashSet` subclass whose constructors request the package-private
  `HashSet(int, float, boolean)` constructor, which builds a `LinkedHashMap` instead of a plain
  `HashMap` as the backing map; `LinkedHashMap.Entry` extends the normal hash node with extra
  `before`/`after` pointers threading all entries into one doubly linked list in insertion order,
  independent of bucket placement — iteration walks that list, not the bucket table. → Follow-up:
  *Does this make `add`/`contains` slower than plain `HashSet`?* No — bucket lookup cost is
  identical; the linked list only adds O(1) pointer bookkeeping on insert/remove, paid once, not
  per lookup.
- [Intermediate] Why is `TreeSet.add`/`contains`/`remove` O(log n) instead of O(1)? → It's backed
  by a red-black tree (via `TreeMap`), a balanced binary search tree — every operation walks a
  root-to-leaf path bounded by the tree's height, which self-balancing keeps at O(log n) even
  under adversarial insert/delete order, unlike a hash table's O(1) average bucket lookup. →
  Follow-up: *What does TreeSet give you in exchange for that slower average case?* Sorted
  iteration for free, plus `O(log n)` range/navigation queries (`floor`, `ceiling`, `headSet`,
  `subSet`) that a hash table structurally cannot support at all (a hash table has no notion of
  "between" or "nearest").
- [Intermediate] What's the difference between `headSet`/`tailSet`/`subSet` and copying a filtered
  subset? → They return **live views** backed by the same tree — no copying, O(log n) to
  construct, and mutations through the view (add/remove, as long as the element stays within
  bounds) are reflected in the original set and vice versa; a manual filter-and-collect produces
  an independent copy that costs O(n) and has no further relationship to the source. → Follow-up:
  *What happens if you try to add an out-of-range element through a `subSet` view?* It throws
  `IllegalArgumentException` — views enforce their bounds on every mutation.
- [Intermediate] Why does `TreeSet` use `compareTo`/`Comparator` for equality instead of
  `equals()`? → A sorted tree's structural invariant only understands "less than / equal /
  greater than," so it must have a single total ordering to decide placement; using `equals()`
  in addition to `compareTo` would let the tree simultaneously "believe" two elements are unequal
  (structurally distinct nodes) yet be unable to order them consistently, breaking the BST
  invariant — so the JDK explicitly documents that `TreeSet`'s ordering **must** be consistent
  with `equals` for well-defined `Set` semantics, but if you supply an inconsistent comparator,
  `TreeSet` behaves according to `compareTo`, silently violating `Set`'s `equals`-based contract.
  → Follow-up: *Give an example where this causes a real bug.* A case-insensitive
  `Comparator<String>` used to sort account codes will silently collapse `"AC-1"` and `"ac-1"`
  into one element even though `String.equals` says they differ — a classic source of "why did my
  set lose an entry" bugs.
- [Advanced] Walk through what happens internally when a `HashSet`'s backing `HashMap` resizes. →
  When `size` exceeds `capacity * loadFactor` (default 0.75), a new bucket array of double the
  capacity is allocated and every existing node is rehashed into it; because capacity is always a
  power of two, each old bucket's entries split into exactly two new buckets (old index, or old
  index + old capacity) based on one extra significant bit of the spread hash — no full rehash
  computation is needed, just a bit test, which is a JDK 8+ optimization over naively recomputing
  every hash from scratch. → Follow-up: *Does this affect `LinkedHashSet`'s iteration order?* No
  — resize only rearranges the bucket table; the separate insertion-order linked list is untouched,
  so iteration order is stable across resizes.
- [Advanced] How would you implement a `Set` that stays sorted **and** offers O(1) `contains`,
  and is that actually possible with one structure? → Not with a single balanced structure — a
  hash table gives O(1) average lookup by giving up any notion of order; a balanced tree gives
  order at the cost of O(log n) lookup; you *can* combine them (a `HashMap` for O(1) membership
  plus a separate `TreeSet`/skip list kept in sync for order, à la `LinkedHashMap` combining a
  hash table with a linked list), paying extra memory and bookkeeping on every mutation to keep
  both structures consistent — which is exactly the trade-off `LinkedHashSet` makes for
  *insertion* order (cheap to maintain) but not for *sorted* order (expensive to maintain
  incrementally on arbitrary insert values). → Follow-up: *Does `ConcurrentSkipListSet` change
  this trade-off?* No — same fundamental trade-off, just concurrent; still O(log n) average, not
  O(1), because it's still order-preserving.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E1 | Easy | Find all duplicate elements in an array | HashSet membership scan | `exercises/FindDuplicatesInArray.java` |
| H1 | Hard | Smallest range covering at least one element from each of K sorted lists | TreeSet of pointers, sliding min/max | `exercises/SmallestRangeKLists.java` |
| H2 | Hard | Kth largest element in a live data stream | TreeMap-backed bounded multiset (TreeSet's own backing structure) | `exercises/KthLargestStream.java` |
| B1 | Build it yourself | Implement your own hash set from scratch | Separate-chaining bucket array, resize at load factor 0.75 | `exercises/MyHashSetExercise.java` |

**E1 — Find duplicates in an array**
- Input: `[4, 3, 2, 7, 8, 2, 3, 1]` → Output: `{2, 3}` (order not significant)
- Constraint: O(n) time, O(n) extra space.
- <details><summary>Hint</summary>Scan once; try to `add` each element to a "seen" `HashSet` — if
  `add` returns `false`, the element was already seen, so add it to a second "duplicates"
  set.</details>

**H1 — Smallest range covering K sorted lists**
- Input: `[[4,10,15,24,26], [0,9,12,20], [5,18,22,30]]` → Output: `[20, 24]`
- Constraint: O(N log K) time where N is the total element count across all lists, using a
  `TreeSet` of size ≤ K.
- <details><summary>Hint</summary>Keep one "current pointer" element per list in a `TreeSet<int[]>`
  ordered by value, plus a running max of all current pointers. The range's minimum is always the
  `TreeSet`'s smallest element (`first()`); repeatedly remove it, advance that list's pointer, add
  the new value, update the running max, and track the best `(max - min)` seen — stop when any
  list is exhausted.</details>

**H2 — Kth largest element in a stream**
- Input: `k = 3`, initial stream `[4, 5, 8, 2]`, then `add(3)`, `add(5)`, `add(10)`, `add(9)`,
  `add(4)` → Output of each `add`: `4, 5, 5, 8, 8` (the current 3rd-largest after each addition)
- Constraint: each `add` should run in O(log k) time, keeping only k elements tracked at once.
- <details><summary>Hint</summary>A plain `TreeSet` can't hold duplicate values. Use a
  `TreeMap<Integer, Integer>` as a counted multiset (value → occurrence count) bounded to k total
  elements: insert the new value, increment total count, and if total count exceeds k, decrement
  (and possibly remove) the smallest key via `firstKey()`. The answer after each `add` is
  `firstKey()`.</details>

**B1 — MyHashSet&lt;T&gt;**
- Implement: `add(T)`, `remove(T)`, `contains(T)`, `size()`, `isEmpty()`.
- Constraint: separate-chaining bucket array (`Object[]` of linked nodes), initial capacity 16,
  resize (double capacity, rehash all entries) once `size > capacity * 0.75`; average O(1)
  `add`/`remove`/`contains`.
- <details><summary>Hint</summary>Spread each element's `hashCode()` the same way `HashMap` does
  (`h ^ (h >>> 16)`) before masking with `(capacity - 1)` to pick a bucket index — this keeps high
  bits from being ignored when capacity is small. Walk the bucket's chain using `equals()` to find
  an existing entry before inserting a new node.</details>

Solutions are in the `solutions` package (`FindDuplicatesInArraySolution`,
`SmallestRangeKListsSolution`, `KthLargestStreamSolution`, `MyHashSet`) — attempt the stubs first.

## 9. Quick recap

- `HashSet` = `HashMap<E, Object>` in disguise — every element is a key, `PRESENT` is a shared
  dummy value; all `HashMap` internals (buckets, treeification, load factor 0.75, resize ×2) apply.
- `LinkedHashSet` swaps the backing map for `LinkedHashMap`, which threads every entry into an
  extra doubly linked list in insertion order — same lookup speed, deterministic iteration order.
- `TreeSet` = `TreeMap<E, Object>` (a red-black tree) — O(log n) ops, sorted iteration,
  `NavigableSet` methods (`floor`/`ceiling`/`higher`/`lower`/`headSet`/`tailSet`) for range and
  nearest-value queries; `headSet`/`tailSet`/`subSet` are live views, not copies.
- Never mutate a hash-relevant field of an object already stored in a `HashSet`/`HashMap`, or a
  comparator-relevant field of one stored in a `TreeSet` — both make the element unreachable by
  value even though it's still physically present.
- `TreeSet` equality is `compareTo() == 0`, not `equals()` — an inconsistent comparator silently
  drops "duplicates" that `equals()` would say are distinct.
