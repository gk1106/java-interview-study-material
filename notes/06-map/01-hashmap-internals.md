# HashMap internals

## 1. What it is

`HashMap<K,V>` stores key-value pairs in a **bucket array** (`Node<K,V>[] table`), locating a
key's bucket by its `hashCode()`. It gives average O(1) `get`/`put`/`remove` at the cost of no
ordering guarantee. It is the default general-purpose `Map` implementation and, arguably, the
single most-asked data structure in Java interviews — because "explain what happens when you
call `put()`" touches hashing, arrays, linked lists, trees, resizing, and concurrency all at once.

## 2. How it works internally

### Fields (paraphrased, not copied from JDK source)

- `Node<K,V>[] table` — the bucket array. Each slot holds either `null`, a single `Node`, a short
  **linked list** of `Node`s (collision chain), or (from Java 8) the root of a **red-black tree**
  once a bucket gets crowded enough.
- `int size` — number of key-value mappings currently stored.
- `int threshold` — `capacity * loadFactor`; when `size` exceeds this, the table resizes.
- `float loadFactor` — default **0.75**. Controls the space/time trade-off: lower = fewer
  collisions but more wasted array slots and more frequent resizes; higher = denser table, more
  collisions, slower lookups.
- `int modCount` — structural-change counter for fail-fast iterators (same idea as `ArrayList`).
- A `Node<K,V>` holds `hash`, `key`, `value`, `next` (the collision-chain pointer). A treeified
  bucket instead stores `TreeNode<K,V>` objects (a `Node` subclass) linked both as a red-black
  tree (`parent`/`left`/`right`) **and** kept as a backup linked list via `next`, so iteration and
  untreeify are still possible without a separate structure.
- Table capacity is **always a power of two** (default 16). This is deliberate — see index
  computation below.
- Like `ArrayList`, a `new HashMap<>()` does **not** allocate the table eagerly; it's allocated
  lazily (size 16) on the first `put`.

### Hash spreading — why `hash ^ (hash >>> 16)`

`HashMap` does not use `key.hashCode()` directly. It applies a supplemental "spreading" function:
```
static int hash(Object key) {
    int h = key.hashCode();
    return h ^ (h >>> 16);   // XOR the high 16 bits into the low 16 bits
}
```
Why: the bucket index is computed from only the **low bits** of the hash (see below), because
`capacity` is small (e.g. 16 = 4 bits) compared to a full 32-bit `hashCode()`. If two keys'
hash codes differ only in high bits (common for hash codes derived from object identity or
poor custom `hashCode()` implementations, e.g. `Long.hashCode()` which XORs high/low halves but
many custom classes hash mostly through low-order fields), they'd collide on every bucket despite
having "different" hash codes. XOR-folding the top 16 bits into the bottom 16 lets high-bit
entropy influence the bucket choice too, spreading collisions more evenly across the table without
the cost of a full, expensive hash (like a cryptographic mix) on every operation — cheap,
one-shift, one-XOR.

### Index computation — why capacity must be a power of two

```
index = (n - 1) & hash          // n = table.length (a power of two)
```
When `n` is a power of two, `n - 1` is a bitmask of all 1s in the low bits (e.g. `n=16` →
`n-1 = 0b1111`). `(n-1) & hash` is exactly equivalent to `hash % n` for a power-of-two `n`, but a
bitwise AND is far cheaper than a modulo/division instruction. This is *why* the JDK insists on
power-of-two capacities (even rounding a user-supplied initial capacity up to the next power of
two) rather than allowing arbitrary sizes.

### `put(K key, V value)` — full step-by-step

1. Compute `hash = spread(key.hashCode())` (null key is special-cased to `hash = 0`, always
   bucket 0).
2. If `table` is `null` or empty, allocate it (default capacity 16, `threshold = 16 * 0.75 = 12`).
3. Compute `index = (n - 1) & hash`.
4. If `table[index]` is `null`, insert a new `Node` directly — O(1), no collision.
5. Otherwise, walk the bucket:
   - If a `Node` already has the same `hash` **and** (`key == existingKey` **or**
     `key.equals(existingKey)`), overwrite its `value` and return the old value (`put` replaces,
     it does not duplicate).
   - If the bucket is currently a tree (`TreeNode`), delegate to the tree's `putTreeVal`
     (O(log n) insert/lookup by comparing hash, then `Comparable` if the keys implement it, then
     a tie-break).
   - Otherwise walk the linked list to the end, appending a new `Node`. While walking, count the
     chain length; if it reaches `TREEIFY_THRESHOLD` (8) **and** the table capacity is at least
     `MIN_TREEIFY_CAPACITY` (64), treeify that bucket (convert the linked list to a red-black
     tree). If capacity is below 64, the table is **resized instead** — treeification is a
     last resort for genuinely bad hash distributions, not the first response to a crowded small
     table.
6. Increment `size`. If `size > threshold`, resize (see below).
7. Bump `modCount`.

### `get(Object key)`

Compute `hash`, `index`, then: if `table[index]` is `null` → not found (`null`). Else check the
first node; if it doesn't match, either walk the linked list (`equals` on each) or, if it's a
tree bucket, do an O(log n) tree search. Average O(1), worst case (all keys collide into one
untreeified bucket, or forced through `equals` chains) degrades toward O(n) — this is exactly
what treeification protects against.

### Resize (doubling)

When `size > threshold`, `resize()`:
1. `newCapacity = oldCapacity * 2` (still a power of two), `newThreshold = newCapacity * 0.75`.
2. Allocate a new `Node[] newTable` of `newCapacity`.
3. **Rehash every existing node into the new table.** Java 8's key optimization: because capacity
   only ever *doubles*, and index = `(n-1) & hash`, each existing bucket's entries split into
   exactly two groups relative to the new table — see the diagram below. No entry needs its hash
   recomputed; only one extra high bit of the existing hash needs to be checked
   (`hash & oldCapacity`).

### ASCII diagram — bucket array with a collision chain

```
capacity = 16, threshold = 12 (16 * 0.75)

index:   0     1     2     3    ...   7                    15
table: [null][null][ K1 ]-[K5][null]...[ K2 ]-[K7]-[K9]  [null]
                     (Node)(Node)          (chain of 3 Nodes, same bucket)
```
`K1` and `K5` both hashed (after spreading) to `(n-1)&hash = 2`; they chain via `next`. Looking
up `K5` costs: compute index (O(1)) → compare `K1` (miss) → compare `K5` (hit) → O(chain length).

### ASCII diagram — bucket growing past 8 entries: treeify

```
Before (bucket 7, capacity >= 64): linked list of 8 nodes after latest collision
table[7] -> N1 -> N2 -> N3 -> N4 -> N5 -> N6 -> N7 -> N8 -> null   (chain length reaches 8)

treeifyBin(table, 7):
  if table.length < MIN_TREEIFY_CAPACITY (64):
      resize() instead of treeifying   <-- small tables resize first, don't treeify yet
  else:
      convert the 8 Nodes into TreeNodes, build a red-black tree ordered by hash
      (ties broken by Comparable, then a deterministic tie-breaker), root replaces table[7]

After (capacity >= 64):
table[7] -> [TreeNode root]
                /        \
          [TreeNode]   [TreeNode]
           /     \         \
        [TN]   [TN]       [TN]        (red-black tree, O(log n) get/put within this bucket)
```
Lookups inside this bucket become O(log n) instead of O(n) — this defends against pathological
inputs (e.g. an attacker submitting many keys engineered to collide, or simply bad luck) where
one bucket would otherwise degrade to a long linked list scanned linearly. If the bucket later
shrinks (via removals, or a resize splits it) down to **`UNTREEIFY_THRESHOLD` (6)** entries, it is
converted back to a plain linked list — the tree bookkeeping overhead isn't worth it for a short
chain.

### ASCII diagram — resize/rehash split into low/high lists (the Java 8 optimization)

Doubling capacity means exactly one more bit of the hash now participates in the index
computation. For old capacity 16 (`0b10000`, index mask `0b01111`) growing to 32
(`0b100000`, index mask `0b011111`), the *new* bit examined is bit 4 (`oldCapacity` itself as a
bitmask, `hash & oldCapacity`):

```
Old bucket index 5 (capacity 16), chain: A -> B -> C -> D   (all had (hash & 15) == 5)

resize to capacity 32:
  for each node, check (hash & oldCapacity)   // oldCapacity = 16 = 0b10000, i.e. bit 4
  bit 4 == 0  -> stays at the SAME index (5) in the new table       ("low" list)
  bit 4 == 1  -> moves to index (5 + oldCapacity) = 21              ("high" list)

  A: bit4=0 -> low        B: bit4=1 -> high
  C: bit4=0 -> low        D: bit4=1 -> high

new table[5]  -> A -> C -> null    (low list, same index)
new table[21] -> B -> D -> null    (high list, index = oldIndex + oldCapacity)
```
This is far cheaper than the pre-Java-8 approach (recomputing `hash % newCapacity` and
re-inserting every node from scratch, which could also reverse chain order and is the root cause
of the classic Java 7 concurrent-resize infinite loop — see pitfalls below). Java 8 instead
**splits each old bucket's list into two lists in one linear pass**, preserving relative order,
and simply hangs the "low" list back at the same index and the "high" list at `index +
oldCapacity`. Tree buckets get the analogous `split()` treatment (splitting into two trees, or
untreeifying back to a list if a split half drops to ≤ `UNTREEIFY_THRESHOLD`).

### Iteration order is NOT guaranteed

Iteration walks the bucket array index 0..n-1, and within each bucket, the chain/tree order. This
order is an implementation artifact of hash values and resize history — it is **not** insertion
order, **not** sorted order, and can change across JVM versions, across resizes of the *same* map,
or even because two runs with different insertion sequences land in the same buckets differently.
Never write code (or tests!) that depends on `HashMap` iteration order; use `LinkedHashMap` (topic
2) or `TreeMap` (topic 3) if order matters.

## 3. Complexity

| Operation | Time (average) | Time (worst case) | Space | Notes |
|-----------|-----------------|--------------------|-------|-------|
| `get(key)` | O(1) | O(log n) per bucket (treeified) / O(n) (untreeified, pathological hash) | O(1) | worst case only with terrible `hashCode()` distribution |
| `put(key, value)` | O(1) amortized | O(log n) treeified / O(n) untreeified | O(1) amortized | amortized over resize copies |
| `remove(key)` | O(1) average | same as get | O(1) | |
| `containsKey` | O(1) average | same as get | O(1) | |
| resize (doubling) | O(n) total, amortized O(1) per put | O(n) | O(n) | same amortization argument as `ArrayList` growth |
| iteration | O(capacity + size) | — | O(1) | must walk every bucket slot, even empty ones |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/map/examples/HashMapInternalsDemo.java`

```java
Map<Integer, String> map = new HashMap<>();
for (int i = 0; i < 13; i++) {
    map.put(i, "v" + i);
    System.out.println("size=" + map.size());
}
// size crosses threshold (12) on the 13th put -> triggers a resize to capacity 32
```
Expected console output (abridged): sizes 1..12 print with no resize note; on the 13th put the
demo detects and prints a resize event (capacity 16 -> 32, new threshold 24), inferred the same
way `ArrayListInternalsDemo` infers growth (no reflection, using documented constants).

## 5. When to use / when NOT to use

- Use as the default `Map` when you don't need ordering, sorting, or thread safety — it's the
  fastest general-purpose key-value store in the JDK.
- Avoid when you need predictable iteration order (`LinkedHashMap`), sorted keys / range queries
  (`TreeMap`), or safe concurrent mutation without external locking (`ConcurrentHashMap`).
- Avoid using mutable objects as keys unless you never mutate the fields involved in
  `hashCode()`/`equals()` after insertion — mutating a key in place silently makes it
  unfindable (see pitfalls).
- In banking-style code: fine for request-scoped lookup caches, DTO field maps, grouping results
  by account id within a single thread; never share a plain `HashMap` across threads without
  external synchronization or switching to `ConcurrentHashMap`.

## 6. Common pitfalls & gotchas

**Mutable keys** — mutating a field used in `hashCode()`/`equals()` after the object is used as a
key strands the entry in the wrong bucket:
```java
Map<List<Integer>, String> map = new HashMap<>();
List<Integer> key = new ArrayList<>(List.of(1, 2));
map.put(key, "value");
key.add(3);                 // mutates the key in place
map.get(List.of(1, 2, 3));  // returns null! entry is still in the OLD bucket for hash of [1,2]
```
Fix: use immutable keys (records, `List.copyOf`, wrapper value objects), or never mutate a key
while it's a map key.

**Broken `hashCode()`/`equals()` contract** — if `equals()` says two objects are equal but
`hashCode()` differs, they can land in different buckets and the map will store "duplicate"
entries that should have collapsed into one; if `hashCode()` is *constant* (e.g. `return 1;`),
every key collides into one bucket, degrading every operation toward O(n) (or O(log n) once
treeified) — a classic denial-of-service vector if hash codes are attacker-controlled (this is
exactly why `String.hashCode()` isn't naively predictable-collision-friendly by design, and why
treeification exists as a defense-in-depth).

**Iterating and modifying at the same time**:
```java
for (String key : map.keySet()) {
    if (condition) map.remove(key); // ConcurrentModificationException on next()
}
// fix: map.keySet().removeIf(condition), or use an explicit Iterator.remove()
```

**Relying on iteration order** — never assume insertion order or any stable order from a plain
`HashMap`; a resize can completely reshuffle bucket contents.

**The Java 7 infinite-loop-on-concurrent-resize story** (concrete, often asked) — see interview
Q&A below; it's important enough to also call out here: in Java 7, `resize()` rehashed each
bucket's chain by prepending each node to the new bucket, which **reverses** the chain's order.
If two threads both trigger a resize on the same map concurrently (no synchronization — `HashMap`
was never meant for concurrent use, but people did this anyway), one thread's reversed partial
chain could end up pointing back at a node that eventually points at itself, forming a **cycle**.
A subsequent `get()` on that bucket then loops forever, spinning a CPU core at 100% — a real,
repeatedly-reported production incident pattern. Java 8 does not fix concurrent-use safety in
general (a plain `HashMap` is still not thread-safe), but the low/high-list split resize algorithm
happens to no longer produce this specific reversed-chain cycle; corruption under concurrent
mutation can still occur in other ways (lost updates, `NullPointerException`s, or an incorrect
`size`), so the fix is still: never share a plain `HashMap` across threads — use
`ConcurrentHashMap` (topic 4).

## 7. Interview questions

- [Basic] What is the default capacity and load factor of `HashMap`? → Capacity 16, load factor
  0.75, so the default resize threshold is 12. → Follow-up: *Why 0.75 specifically?* It's the
  JDK's tuned balance between memory waste (a low load factor wastes array slots) and collision
  probability (a high load factor packs buckets, growing chain length) — 0.75 keeps average chain
  length short (~0.5 per bucket at capacity) while not wasting too much space.
- [Basic] Why must `HashMap`'s capacity be a power of two? → So that `(capacity - 1) & hash` is
  equivalent to `hash % capacity` but computable with one fast bitwise AND instead of a division/
  modulo instruction, and so the low/high-list resize split (checking one extra bit) works
  correctly. → Follow-up: *What happens if you pass a non-power-of-two initial capacity to the
  constructor?* The JDK silently rounds it up to the next power of two (`tableSizeFor`).
- [Basic] What does `key.hashCode()` alone determine, and what does `HashMap` do to it before use?
  → It determines the raw 32-bit hash; `HashMap` applies a spreading function,
  `h ^ (h >>> 16)`, XOR-folding the high 16 bits into the low 16 bits before computing the bucket
  index. → Follow-up: *Why not use `hashCode()` directly?* Because only the low bits of the hash
  are used for indexing (since capacity is small), so without spreading, hash codes that only
  differ in high bits would all collide.
- [Basic] Is `HashMap` thread-safe? → No — concurrent `put`s (especially ones triggering a resize)
  can corrupt internal structure or lose updates; use `ConcurrentHashMap`, or wrap with
  `Collections.synchronizedMap`, for concurrent access. → Follow-up: *What's the practical
  difference between `Collections.synchronizedMap(new HashMap<>())` and `ConcurrentHashMap`?* The
  former locks the entire map on every operation (coarse-grained, serializes all access, and you
  must externally synchronize compound actions like check-then-act); `ConcurrentHashMap` uses
  finer-grained synchronization (per-bin) and lock-free reads, giving much better throughput under
  contention.
- [Intermediate] Walk me through exactly what happens when you call `map.put(key, value)` on a
  `HashMap` with an existing entry for that key. → (1) Compute `hash = spread(key.hashCode())`.
  (2) If the table is uninitialized, allocate it at default capacity 16. (3) Compute
  `index = (capacity-1) & hash`. (4) If the bucket is empty, insert directly. (5) Otherwise walk
  the bucket (list or tree): if a node with the same `hash` and (`==` or `.equals()`) key is
  found, its value is overwritten and the old value returned — no new entry is created, `size`
  does not change. (6) If no match is found, append a new node at the end of the chain (or insert
  into the tree); increment `size`; if `size > threshold`, resize. → Follow-up: *What if the key
  is `null`?* `HashMap` allows exactly one `null` key, always hashed to bucket 0 (`hash` forced to
  0), handled as a special case in the lookup/insert path.
- [Intermediate] What triggers a resize, and what happens during it? → `size > threshold`
  (`capacity * loadFactor`) after an insert triggers `resize()`: capacity doubles, threshold
  recalculates, and every existing node is redistributed into the new table. Java 8's optimization
  splits each old bucket's chain into a "low" list (stays at the same index) and a "high" list
  (moves to `index + oldCapacity`), determined by checking one bit of each node's stored hash
  (`hash & oldCapacity`) — no hash recomputation needed. → Follow-up: *Is resize O(1) or O(n)?*
  A single resize is O(n) (every entry touched once), but amortized over the n puts that led to
  it, it's O(1) per put — the same amortized-cost argument as `ArrayList` growth.
- [Intermediate] What is treeification and when does it kick in? → When a single bucket's chain
  length reaches `TREEIFY_THRESHOLD` (8) during an insert, **and** the table's total capacity is
  at least `MIN_TREEIFY_CAPACITY` (64), that bucket's linked list is converted into a red-black
  tree ordered primarily by hash (with `Comparable`/tie-break fallback), turning worst-case
  lookup in that bucket from O(n) to O(log n). If capacity is below 64, the table resizes instead
  of treeifying — a small table with one crowded bucket is more likely helped by spreading entries
  across more buckets than by paying tree overhead. → Follow-up: *When does it untreeify?* When
  removals shrink a treeified bucket to `UNTREEIFY_THRESHOLD` (6) entries, or a resize split
  leaves one of the two resulting groups at or below that size, it's converted back to a plain
  linked list.
- [Intermediate] Why is `HashMap` iteration order not guaranteed, and what does that break in
  practice? → Iteration order follows the physical bucket array layout (index order, then
  chain/tree order within a bucket), which is a function of hash values and resize history, not
  insertion order — it can visibly change after a resize even with no removals. It breaks: tests
  that assert on `toString()` output or iteration order, `equals()` comparisons between two
  differently-built maps that happen to rely on order (they don't, `Map.equals` is order-
  independent, but naive manual comparisons might), and any code that assumes "first put = first
  iterated." → Follow-up: *Which Map gives insertion order, and which gives sorted order?*
  `LinkedHashMap` (insertion or access order) and `TreeMap` (sorted by key) respectively.
- [Advanced] Why is HashMap not thread-safe — what specifically can go wrong with concurrent
  `put()`s? → Several failure modes, without any synchronization: (1) **lost updates** — two
  threads both read "bucket is empty," both write a `Node`, one overwrites the other, losing an
  entry; (2) **corrupted `size`** — `size++` is not atomic, so concurrent increments can lose
  counts; (3) in Java 7 specifically, a **resize race could create a cycle** in a bucket's linked
  list — Java 7's resize rehashes by prepending each moved node to the head of its new bucket,
  reversing the chain's order; if thread A is mid-resize (has read a node's `next` pointer,
  building a partial new chain) while thread B also triggers a resize on the same underlying table
  and finishes first, thread A can end up linking a node's `next` back to a node that (through
  thread B's already-completed reversal) points back at the first node — a genuine circular linked
  list. Any subsequent `get()` that lands on that bucket then loops forever, pegging a CPU core;
  this was a real, repeatedly-hit production issue with pre-Java-8 `HashMap`s shared across
  threads. Java 8's low/high-list split resize (which doesn't reverse chain order while splitting)
  eliminates that *specific* cycle bug, but does not make `HashMap` safe for concurrent use in
  general — lost updates and inconsistent reads during resize are still possible. → Follow-up:
  *Does making the `Map` reference `volatile` fix any of this?* No — `volatile` only guarantees
  visibility of the reference itself, not the atomicity or visibility of the internal mutations
  happening inside a `put()` call; you need real concurrency control (`ConcurrentHashMap`,
  external locking, or `Collections.synchronizedMap`).
- [Advanced] Why does treeification use a red-black tree instead of, say, a plain sorted array or
  an AVL tree for the bucket? → A red-black tree gives guaranteed O(log n) insert/delete/search
  with at most O(log n) rotations to rebalance after a mutation (see `notes/06-map/03-treemap-
  navigablemap.md` for the balancing invariant), which is cheaper to maintain under frequent
  insert/remove than an AVL tree's stricter balance (AVL trees are more strictly balanced, giving
  slightly faster lookups but more expensive rebalancing on writes) — a reasonable trade-off for a
  structure expected to still see occasional inserts/removes. A sorted array would need O(n)
  shifting on insert/remove, defeating the purpose. → Follow-up: *What do tree nodes order by,
  since keys might not implement `Comparable`?* Primarily by the stored `hash` value; if hashes
  tie and both keys implement `Comparable` with the same runtime class, `compareTo` breaks the
  tie; if that's still not enough (or keys aren't `Comparable`), the JDK falls back to a
  deterministic identity-based tie-breaker so tree ordering is always well-defined.
- [Advanced] What is the relationship between `HashMap` and `HashSet`? → `HashSet<E>` is backed
  internally by a `HashMap<E, Object>`, storing each set element as a key mapped to a shared dummy
  sentinel value (`PRESENT`); every `HashSet` operation delegates to the underlying map's key
  operations, so `HashSet` inherits all of `HashMap`'s internals (hashing, buckets, treeification,
  resize) directly. → Follow-up: *Does that mean HashSet iteration order is also unspecified?*
  Yes, for exactly the same reason.
- [Advanced] Given `Objects.hash(a, b, c)` as a `hashCode()` implementation, what's a subtle
  performance issue for very hot code paths? → `Objects.hash(...)` boxes its `Object...` varargs
  into an array on every call, then loops multiplying — perfectly correct but allocates an array
  each time, which matters in very hot key-generation code (e.g. constructing millions of
  composite cache keys). A hand-written `hashCode()` (`31*31*a + 31*b + c` style, matching what
  IDEs/records generate) avoids the array allocation. → Follow-up: *Why is 31 traditionally used
  as the multiplier?* It's odd and prime (reduces systematic collisions from multiplication), and
  `31 * i` can be replaced by the JIT with `(i << 5) - i`, a cheap shift-and-subtract instead of
  a real multiply.

## 8. Exercises

See `notes/06-map/06-dsa-patterns-map.md` for the map-specific DSA exercise set (two-sum, group
anagrams, subarray sum = K, etc.) — this topic's own exercises are folded into that patterns file
plus the build-it-yourself `MyHashMap<K,V>` covered there and in the recap below.

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| B1 | Build-it-yourself | Implement a HashMap from scratch (separate chaining, resize at 0.75 load factor) | hashing, dynamic array of buckets | `exercises/MyHashMapExercise.java` |

- <details><summary>Hint</summary>Back it with `Node<K,V>[] table` where each `Node` has
  `key`, `value`, `next`; spread the hash the same way the JDK does; resize (double capacity,
  rehash every node) once `size > capacity * 0.75`.</details>

Solution is the real implementation in `solutions/MyHashMap.java` — attempt the stub in
`exercises/MyHashMapExercise.java` first.

## 9. Quick recap

- `index = (capacity - 1) & spread(hashCode())`, capacity always a power of two, so index
  computation is a cheap bitwise AND, not a modulo.
- Spreading (`h ^ (h >>> 16)`) folds high hash bits into low bits before indexing, since only low
  bits are used when capacity is small.
- Load factor 0.75 triggers a doubling resize; Java 8 splits each old bucket into "low" (same
  index) and "high" (`index + oldCapacity`) lists in one pass instead of full rehashing.
- A bucket treeifies into a red-black tree at 8 entries (if capacity >= 64) for guaranteed
  O(log n) worst case, and untreeifies back to a list at 6 entries.
- Iteration order is an unspecified artifact of bucket layout — never depend on it; `HashMap` is
  not thread-safe, and the classic Java 7 concurrent-resize infinite loop (chain reversal creating
  a cycle) is the canonical cautionary story, even though Java 8's split-resize no longer produces
  that exact bug.
