# TreeMap and NavigableMap

## 1. What it is

`TreeMap<K,V>` is a `Map` backed by a **red-black tree**, keeping entries sorted by key (natural
order or a supplied `Comparator`) at all times. It trades `HashMap`'s O(1) average operations for
O(log n) guaranteed operations in exchange for sorted iteration and rich range-query methods via
the `NavigableMap` interface.

## 2. How it works internally

### Red-black tree — the balancing invariant, conceptually

A red-black tree is a self-balancing binary search tree where every node is colored red or black,
maintaining four invariants:
1. Every node is red or black.
2. The root is always black.
3. Red nodes cannot have a red child (no two reds in a row on any path).
4. Every path from a given node to any of its descendant `null` leaves passes through the **same
   number** of black nodes (the node's "black-height").

These invariants together guarantee the longest possible root-to-leaf path is at most **twice**
the shortest one (worst case alternates red/black; best case is all black), which bounds tree
height at `O(log n)` — this is *why* search/insert/delete are guaranteed O(log n), unlike a plain
unbalanced BST which degrades to O(n) on sorted/adversarial input.

**Why this matters without needing the full proof**: an unbalanced BST built from already-sorted
input becomes a straight line (effectively a linked list) — O(n) lookups. A red-black tree
*rebalances itself* after every insert/delete via **rotations** (a local restructuring that swaps
a node with one of its children, updating a handful of pointers in O(1)) and **recoloring**, so
it never gets worse than roughly balanced, no matter the insertion order.

- **Insert**: standard BST insert (walk down comparing keys, attach as a new red leaf), then
  fix up any invariant-3 violation (a red node with a red parent) by walking back up performing
  recolors and/or a rotation (at most a constant number of rotations, though recoloring can
  propagate multiple levels up).
- **Delete**: standard BST delete, then — if a black node was removed (which would shrink that
  path's black-height) — fix up via rotations/recolors so invariant 4 holds again. Deletion
  fix-up is the more intricate of the two (several symmetric cases), but conceptually it's the
  same idea: local rotations restore balance in O(log n) worst case.

You do not need to reproduce every CLRS rotation case in an interview; you *do* need to be able to
say: *"red-black trees bound height to O(log n) via the black-height + no-double-red invariants,
and restore that bound after every mutation using O(1)-per-step rotations plus recoloring, which
is what guarantees `TreeMap`'s O(log n) worst case, unlike a plain unbalanced BST."*

### ASCII diagram — a small red-black tree (B = black, R = red)

```
                 20(B)
                /      \
            10(R)      35(B)
           /    \           \
        5(B)   15(B)       40(R)
```
Every root-to-null-leaf path here passes through the same count of black nodes (root 20 is black,
counted once per path; 5/15/40's null children add one more black "virtual" leaf each) — that's
invariant 4 holding. No red node (10, 40) has a red child — invariant 3 holding.

### Node fields (paraphrased)

Each `TreeMap.Entry<K,V>` holds `key`, `value`, `left`, `right`, `parent`, and a `boolean color`
(red/black). `TreeMap` also stores `Comparator<? super K> comparator` (`null` means natural
ordering via `Comparable`) and `int size`.

### `NavigableMap` — range and neighbor queries

`TreeMap` implements `NavigableMap<K,V>` (which extends `SortedMap<K,V>`), adding O(log n)
neighbor/range operations that a `HashMap` fundamentally cannot offer efficiently:

| Method | Meaning |
|--------|---------|
| `firstKey()` / `lastKey()` | smallest / largest key |
| `firstEntry()` / `lastEntry()` | smallest / largest full entry (or `null` if empty) |
| `floorKey(k)` | largest key `<= k` |
| `ceilingKey(k)` | smallest key `>= k` |
| `lowerKey(k)` | largest key `< k` (strict) |
| `higherKey(k)` | smallest key `> k` (strict) |
| `pollFirstEntry()` / `pollLastEntry()` | remove and return the smallest / largest entry |
| `subMap(from, fromInclusive, to, toInclusive)` | a **view** over a key range, backed by the same tree — mutating the view mutates the original |
| `headMap(to)` / `tailMap(from)` | view of all keys `< to` / `>= from` |
| `descendingMap()` | a reverse-order view |

All of these are O(log n) — a red-black tree makes "find the nearest key" and "give me everything
between X and Y" cheap operations, which a hash table structurally cannot support without a full
O(n) scan.

### ASCII diagram — `floorKey`/`ceilingKey` on the tree above, `find(23)`

```
floorKey(23):   walk down comparing to 23: 20 < 23 (candidate=20), go right to 35;
                35 > 23, go left to null -> best floor candidate remains 20 -> floorKey(23) = 20
ceilingKey(23): same walk, tracking smallest key >= 23 seen -> 35 -> ceilingKey(23) = 35
```

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `get(key)` / `containsKey` | O(log n) guaranteed | O(1) | BST search, always balanced |
| `put(key, value)` | O(log n) guaranteed | O(1) amortized | insert + rebalance fix-up |
| `remove(key)` | O(log n) guaranteed | O(1) | delete + rebalance fix-up |
| `firstKey`/`lastKey`/`floorKey`/`ceilingKey` | O(log n) | O(1) | tree-walk toward an extreme or nearest key |
| `subMap`/`headMap`/`tailMap` | O(log n) to construct the view | O(1) (view, not a copy) | mutations through the view affect the backing tree |
| iteration (in sorted order) | O(n) | O(1) | in-order traversal |

Compare to `HashMap`: `TreeMap` trades average O(1) for guaranteed O(log n) — no worst-case
cliff, no dependency on `hashCode()` quality, but strictly slower on average for plain
get/put/remove.

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/map/examples/TreeMapNavigableDemo.java`

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(20, "twenty"); map.put(10, "ten"); map.put(35, "thirty-five"); map.put(15, "fifteen");

System.out.println(map.firstKey());          // 10
System.out.println(map.floorKey(23));        // 20
System.out.println(map.ceilingKey(23));      // 35
System.out.println(map.subMap(10, true, 20, true).keySet()); // [10, 15, 20]
```
Expected console output:
```
10
20
35
[10, 15, 20]
```

## 5. When to use / when NOT to use

- Use when you need keys in sorted order, range queries (`subMap`), or nearest-key lookups
  (`floorKey`/`ceilingKey`) — e.g. a banking use case: find the most recent transaction at or
  before a given timestamp, or the interest-rate tier boundary just below a balance.
- Avoid when you only need plain key lookup with no ordering requirement — `HashMap`'s average
  O(1) beats `TreeMap`'s O(log n) for pure get/put workloads.
- Keys **must** be mutually comparable (implement `Comparable` consistently, or supply a
  `Comparator` covering every key you'll insert) — inconsistent comparison logic (or a
  `Comparator` that doesn't agree with `equals`) silently breaks lookups the same way a broken
  `hashCode()`/`equals()` pair breaks `HashMap`.

## 6. Common pitfalls & gotchas

**`Comparator` inconsistent with `equals`** — `TreeMap` uses `compareTo`/`Comparator.compare`,
**not** `equals()`, to decide key identity. Two "equal" keys per `equals()` but non-zero per
`compareTo` are treated as different entries by a `HashMap` but as duplicates (later overwrites
earlier) by `TreeMap`, or vice versa — a frequent source of subtle bugs when switching a codebase
from `HashMap` to `TreeMap` without auditing the key type's ordering.

**`NullPointerException` on natural ordering with `null` keys** — unlike `HashMap`, `TreeMap`
(with no `Comparator`, i.e. natural ordering) throws `NullPointerException` on `put(null, v)`,
because there's no way to compare `null` to anything with `compareTo`.

**Mutating a view (`subMap`/`headMap`/`tailMap`) after structural changes to the backing map** —
these are *live views*, not copies; inserting a key outside the view's range through the view
throws `IllegalArgumentException`, and concurrent structural modification of the backing map
while iterating a view throws `ConcurrentModificationException` just like the full map would.

**Forgetting `subMap`'s inclusive/exclusive boundary defaults** — the two-arg
`subMap(from, to)` is `from`-inclusive, `to`-exclusive (matching general Java range convention);
the four-arg overload lets you control both ends explicitly — mixing these up off-by-ones the
boundary.

## 7. Interview questions

- [Basic] What data structure backs `TreeMap`, and what ordering guarantee does it give? → A
  red-black tree; keys are always kept in sorted order (natural order via `Comparable`, or a
  supplied `Comparator`), and iteration reflects that sorted order. → Follow-up: *What's the time
  complexity of get/put on a TreeMap?* O(log n) guaranteed, not average — the tree stays balanced.
- [Basic] `HashMap` vs `TreeMap` — when would you pick each? → `HashMap` for pure key-value lookup
  with no ordering need (faster, O(1) average); `TreeMap` when you need sorted iteration, range
  queries, or nearest-key lookups (`floorKey`/`ceilingKey`), accepting O(log n). → Follow-up:
  *What about LinkedHashMap versus TreeMap?* `LinkedHashMap` gives insertion/access order (still
  O(1) average, no sorting by key value); `TreeMap` gives true sorted-by-key order at O(log n).
- [Intermediate] What invariant does a red-black tree maintain, and how does that bound its
  height? → No red node has a red child, and every root-to-leaf path has the same black-node
  count (black-height); together these force the longest path to be at most twice the shortest,
  bounding height at O(log n) regardless of insertion order. → Follow-up: *What happens to a
  plain (unbalanced) BST if you insert already-sorted data?* It degenerates into a straight chain
  (effectively a linked list), giving O(n) search — exactly what red-black balancing prevents.
- [Intermediate] What are `floorKey`, `ceilingKey`, `lowerKey`, `higherKey` and how do they
  differ? → All four find a "neighbor" key relative to a given key: `floorKey` = largest key
  `<=` given key (inclusive), `ceilingKey` = smallest key `>=` given key (inclusive), `lowerKey` =
  largest key strictly `<`, `higherKey` = smallest key strictly `>`. All run in O(log n). →
  Follow-up: *Give a banking example where floorKey is the right tool.* Given a
  `TreeMap<LocalDate, ExchangeRate>`, find the rate in effect on an arbitrary date by calling
  `floorKey(date)` to get the most recent rate published on or before that date.
- [Intermediate] What does `subMap(from, to)` return, and is it a copy? → A live, sorted view of
  the portion of the map with keys in `[from, to)` (two-arg form), backed by the same underlying
  tree — not a copy. Insertions/removals through the view affect the backing map and vice versa,
  as long as the mutation stays within the view's range. → Follow-up: *What happens if you try to
  insert a key outside the view's range through the subMap view?* `IllegalArgumentException`.
- [Advanced] How does insertion fix-up work at a high level when a new red node's parent is also
  red? → The new node is inserted as a red leaf first, then a fix-up walk looks at the new
  red-red violation: if the "uncle" node is also red, recolor parent/uncle to black and
  grandparent to red, then continue the fix-up from the grandparent (violation may propagate
  upward); if the uncle is black (or absent), one or two rotations around the grandparent restore
  the invariants in O(1), terminating the fix-up. Either way, the total fix-up work is bounded by
  O(log n) (tree height) with at most O(1) rotations actually performed (only recoloring can
  chain further up). → Follow-up: *Why is the root always forced black after fix-up?* Invariant 2
  requires it; if recoloring propagated red all the way to the root, it's simply repainted black
  as the final fix-up step (this can never violate invariant 4 since it only increases every
  path's black-height by the same amount).
- [Advanced] `TreeMap` vs a balanced BST you'd write from scratch (e.g. AVL) — why did the JDK
  choose red-black over AVL for `TreeMap` (and for `HashMap`'s treeified buckets)? → AVL trees
  enforce a stricter balance factor (height difference of subtrees ≤ 1), giving slightly faster
  lookups but more frequent/expensive rotations on insert/delete to maintain that tighter bound;
  red-black trees allow a looser balance (height ratio up to 2x) in exchange for cheaper, less
  frequent rebalancing — a better trade-off for a general-purpose structure expected to see a mix
  of reads and writes, which is exactly `TreeMap`'s (and a treeified `HashMap` bucket's) typical
  usage profile. → Follow-up: *Would you ever prefer AVL in practice?* When lookups vastly
  outnumber insertions/deletions and the tighter balance's lookup speedup outweighs the extra
  rebalancing cost paid rarely.
- [Advanced] Can two keys that are `.equals()` but not "equal" per `compareTo`/`Comparator` both
  exist in a `TreeMap`? → Yes — `TreeMap` uses `compareTo`/`Comparator.compare` exclusively for
  both ordering and identity; if your `Comparator` returns non-zero for two objects that
  `.equals()` says are equal, `TreeMap` treats them as distinct keys (both retained), which is
  inconsistent with `HashMap`'s `equals()`-based identity and inconsistent with the general
  `Map` contract's expectation that "equal" keys collapse to one entry — the JDK docs explicitly
  warn `TreeMap`'s ordering should be "consistent with equals" to avoid this trap. → Follow-up:
  *What's a concrete example?* A `Comparator<Person>` ordering by `age` only: two different
  `Person` objects with the same age but different names/ids would be treated as the same key by
  `TreeMap`, silently overwriting one with the other on `put` — surprising if `Person.equals()`
  compares by id.

## 8. Exercises

`TreeMap`-based exercises are grouped with the module's DSA pattern set in `notes/06-map/06-dsa-
patterns-map.md` (see the time-based key-value store, Hard tier, which uses binary search over a
per-key `TreeMap<Long, V>` of timestamped values).

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| H2 | Hard | Design a time-based key-value store: `set(key, value, timestamp)`, `get(key, timestamp)` returns the value set at the largest timestamp `<= timestamp` | `TreeMap` `floorKey` binary search | `exercises/TimeBasedKeyValueStore.java` |

Solution is `solutions/TimeBasedKeyValueStoreSolution.java` — attempt the stub first.

## 9. Quick recap

- `TreeMap` is backed by a red-black tree: always sorted by key, O(log n) guaranteed for
  get/put/remove — no worst-case cliff like an unbalanced BST or a badly-hashed `HashMap`.
- The core invariant: no red node has a red child, and every path has equal black-height, which
  bounds height at O(log n); rotations + recoloring restore this after every insert/delete.
- `NavigableMap` adds O(log n) neighbor/range queries `HashMap` cannot offer:
  `floorKey`/`ceilingKey`/`lowerKey`/`higherKey`, `firstEntry`/`lastEntry`, `subMap`/`headMap`/
  `tailMap` (live views, not copies).
- Keys must be mutually comparable and that ordering should be consistent with `equals` — a
  `Comparator` that treats `equals()`-unequal objects as ordering-equal silently collapses them
  into one entry.
- Pick `TreeMap` for sorted iteration/range queries/nearest-key lookups; pick `HashMap` for pure
  lookup speed when order doesn't matter.
