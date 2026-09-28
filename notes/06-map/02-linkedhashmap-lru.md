# LinkedHashMap and building an LRU cache

## 1. What it is

`LinkedHashMap<K,V>` extends `HashMap` and adds a **doubly linked list** threading through all
entries, giving predictable iteration order — either **insertion order** (default) or **access
order** (opt-in). It also exposes a protected hook, `removeEldestEntry`, purpose-built for
implementing bounded LRU (least-recently-used) caches with almost no extra code.

## 2. How it works internally

### Extra fields beyond `HashMap`

- Each entry is a `LinkedHashMap.Entry<K,V>` (subclass of `HashMap.Node<K,V>`) with two extra
  pointers, `before` and `after`, threading every entry into one global doubly linked list —
  independent of which hash bucket it lives in.
- `Entry<K,V> head`, `Entry<K,V> tail` — the ends of that linked list.
- `boolean accessOrder` — constructor flag. `false` (default) = iteration follows **insertion
  order**. `true` = iteration follows **access order**: every successful `get()` (and every
  `put()` that updates an existing key) moves that entry to the **tail** of the linked list, so
  the list is always ordered least-recently-used (head) → most-recently-used (tail).

### How it reuses HashMap's machinery

`LinkedHashMap` does **not** reimplement hashing, buckets, resize, or treeification — it inherits
all of `HashMap`'s bucket-array logic entirely. It only overrides a few internal hooks:
- `newNode(...)` — creates a `LinkedHashMap.Entry` instead of a plain `HashMap.Node`, and links it
  at the tail of the doubly linked list (`linkNodeLast`).
- `afterNodeAccess(node)` — called after every successful `get`/`put`-on-existing-key; if
  `accessOrder` is true, unlinks the node and relinks it at the tail (bumping recency).
  A no-op when `accessOrder` is false.
- `afterNodeInsertion(node)` — called after every successful insert; calls
  `removeEldestEntry(eldest)` (the head of the linked list — the least-recently-inserted or
  least-recently-used entry, depending on mode) and, if it returns `true`, removes that entry.
  Default implementation always returns `false` (unbounded map); **overriding this method is the
  entire mechanism for a self-evicting bounded cache.**

So the bucket array still does the O(1) hashing/lookup work; the linked list is purely an
ordering overlay maintained alongside it — two data structures, one map.

### ASCII diagram — bucket array + overlay linked list

```
Buckets (hash-indexed, same as HashMap):
  table[2] -> [C]
  table[5] -> [A] -> [D]     (collision chain, same as plain HashMap)
  table[9] -> [B]

Overlay doubly linked list (insertion order: A, B, C, D):
  head                                                  tail
   |                                                      |
   v                                                      v
  [A] <-> [B] <-> [C] <-> [D]

get("A") on an accessOrder=true map relinks A to the tail:
  head                                                  tail
   |                                                      |
   v                                                      v
  [B] <-> [C] <-> [D] <-> [A]      // A is now "most recently used"
```
Note the bucket array positions of A/B/C/D never change — only the overlay list pointers move.

### Building an LRU cache with `removeEldestEntry`

```java
class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    LruCache(int capacity) {
        super(16, 0.75f, true);   // accessOrder = true is essential
        this.capacity = capacity;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;   // evict the head (least recently used) once over capacity
    }
}
```
Every `get`/`put` runs in O(1) (bucket lookup + O(1) linked-list relink), and the eviction check
after each insert is O(1) — `removeEldestEntry` just compares `size()` to a constant and, if
true, the framework removes `head` for you. This is the simplest correct LRU cache in Java, but
`src/main/java/com/gk/study/map/solutions/LRUCache.java` in this module builds the *same*
mechanism completely by hand (own `HashMap<K, Node>` + own doubly linked list) to make the O(1)
get/put mechanics fully explicit rather than delegated to `LinkedHashMap`.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `get(key)` | O(1) average | O(1) | bucket lookup + (access-order only) O(1) relink |
| `put(key, value)` | O(1) average amortized | O(1) amortized | bucket insert + O(1) list append/relink + O(1) eviction check |
| iteration | O(size) | O(1) | walks the linked list, **not** the bucket array — no empty-slot overhead unlike plain `HashMap` |
| eviction (`removeEldestEntry` true) | O(1) | O(1) | removes `head` of the linked list directly, no scan |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/map/examples/LinkedHashMapLruDemo.java`

```java
LinkedHashMap<Integer, String> insertionOrder = new LinkedHashMap<>();
insertionOrder.put(3, "c"); insertionOrder.put(1, "a"); insertionOrder.put(2, "b");
System.out.println(insertionOrder.keySet());   // [3, 1, 2] -> insertion order preserved

LruCache<Integer, String> lru = new LruCache<>(2);
lru.put(1, "a"); lru.put(2, "b");
lru.get(1);          // touches 1, making 2 the least-recently-used
lru.put(3, "c");      // evicts 2 (LRU), keeps 1 and 3
System.out.println(lru.keySet());   // [1, 3]
```
Expected console output:
```
[3, 1, 2]
[1, 3]
```

## 5. When to use / when NOT to use

- Use when you need predictable iteration order without the O(log n) overhead of a `TreeMap` —
  insertion order is useful for reproducible output/logging/serialization; access order is
  purpose-built for LRU caches.
- Use `removeEldestEntry` for a quick, correct, self-evicting bounded cache when you don't need
  custom eviction policies (size-based, weight-based, TTL-based) — for those, reach for a real
  caching library (Caffeine, Guava `CacheBuilder`) in production code instead of hand-rolling.
- Avoid `accessOrder = true` mode if you iterate the map from multiple threads while also calling
  `get()` — even a *read* (`get`) mutates the linked list under the hood in access-order mode,
  which is a structural change for fail-fast purposes, so `LinkedHashMap` is **not** safer for
  concurrent access-order reads than a plain `HashMap`; still needs external synchronization.

## 6. Common pitfalls & gotchas

**Forgetting `accessOrder = true`** — the three-arg constructor is required; the no-arg/two-arg
constructors default to insertion order, so `get()` calls silently do nothing to reordering and
an LRU eviction policy built on the default constructor evicts the *oldest inserted* entry, not
the *least recently used* one:
```java
new LinkedHashMap<K, V>();                    // insertion order — NOT LRU
new LinkedHashMap<K, V>(16, 0.75f, true);      // access order — required for LRU
```

**`get()` counts as a structural mutation in access-order mode** — iterating a
`LinkedHashMap(accessOrder=true)` with a for-each while also calling `get()` on it (even from the
same thread, e.g. inside the loop body) throws `ConcurrentModificationException`, because
`afterNodeAccess` bumps `modCount`-adjacent bookkeeping the same way structural changes do in
access-order mode.

**Overriding `removeEldestEntry` but forgetting it operates on the linked list's head, not
"smallest key"** — `eldest` is the least-recently-inserted-or-accessed entry, unrelated to key
ordering; don't confuse this with `TreeMap`'s sorted semantics.

## 7. Interview questions

- [Basic] What's the difference between `HashMap` and `LinkedHashMap`? → `LinkedHashMap` extends
  `HashMap` and adds a doubly linked list threading all entries to give predictable iteration
  order (insertion or access order); all hashing/bucket/resize/treeify behavior is otherwise
  identical since it's inherited. → Follow-up: *Does LinkedHashMap use more memory?* Yes — each
  entry carries two extra object references (`before`, `after`) compared to a plain `HashMap`
  node.
- [Basic] What are the two ordering modes and how do you choose between them? → Insertion order
  (default, `false`) preserves the order keys were first inserted; access order (`true`, passed to
  the 3-arg constructor) reorders on every `get`/update so the most-recently-used entry is always
  last — access order is what you need to implement LRU. → Follow-up: *Does a `put()` that
  updates an existing key's value count as an access?* Yes, in access-order mode it moves that
  entry to the tail just like a `get()` would.
- [Intermediate] How would you implement a fixed-capacity LRU cache in five lines using
  `LinkedHashMap`? → Extend `LinkedHashMap`, call `super(initialCapacity, 0.75f, true)` for
  access order, and override `removeEldestEntry(Map.Entry eldest)` to `return size() >
  capacity;` — the framework then auto-evicts the head entry after every insert that pushes the
  map over capacity. → Follow-up: *What's the time complexity of every operation in this
  implementation?* O(1) average for `get`/`put`, including the eviction check, because the
  eldest entry is always the list head — no scan needed.
- [Intermediate] Why does `LinkedHashMap` iterate faster in practice than a `HashMap` with many
  empty buckets? → `LinkedHashMap` iteration walks the overlay doubly linked list, which has
  exactly `size` nodes and no gaps; plain `HashMap` iteration must walk the entire bucket array
  (`capacity` slots), skipping over `null` slots, which wastes time when the map is sparse (low
  load factor relative to capacity, e.g. right after a resize or after many removals). →
  Follow-up: *Does that mean LinkedHashMap is always faster overall?* No — it does more work per
  insert (maintaining the linked list pointers) and uses more memory per entry; the iteration win
  only matters for iteration-heavy, sparse-map workloads.
- [Intermediate] What does the `eldest` parameter of `removeEldestEntry` actually represent? → The
  current head of the linked list — in insertion-order mode, the entry inserted longest ago that
  hasn't been removed; in access-order mode, the entry least recently read or written — i.e.,
  exactly the LRU candidate. → Follow-up: *Can removeEldestEntry evict more than one entry per
  call?* No, it's checked once per insert and only ever removes the current head if it returns
  true; a single oversized batch insert (not applicable to `put`, but conceptually via
  `putAll`) still evicts one entry per `afterNodeInsertion` call, called once per inserted node.
- [Advanced] Is a `LinkedHashMap` in access-order mode safe for concurrent reads from multiple
  threads (no writes)? → No — a plain `get()` in access-order mode mutates the internal linked
  list (relinking the accessed node to the tail), which is a write to shared mutable state; two
  threads calling `get()` concurrently without synchronization can corrupt the linked list
  pointers even though neither thread ever calls `put()`. This is a common trap: developers assume
  "read-only access" is inherently thread-safe, but access-order `LinkedHashMap` breaks that
  assumption. → Follow-up: *What's a thread-safe alternative for an LRU cache?* Wrap it with
  `Collections.synchronizedMap(...)` (coarse locking) or use a dedicated concurrent LRU
  implementation / caching library (Caffeine) designed for concurrent access.
- [Advanced] Why did the JDK choose to make `LinkedHashMap` extend `HashMap` rather than compose
  it internally? → Inheritance lets `LinkedHashMap` reuse the entire bucket/hashing/resize/
  treeify implementation via a handful of protected override hooks
  (`newNode`, `afterNodeAccess`, `afterNodeInsertion`, `afterNodeRemoval`) without duplicating any
  of that logic, and it lets `LinkedHashMap` instances be used anywhere a `HashMap` is expected
  (Liskov-substitutable) while adding ordering as a pure overlay. → Follow-up: *Does that design
  make LinkedHashMap fragile to future HashMap internal changes?* Somewhat — it depends on
  `HashMap` calling those hooks at exactly the right points, which is why they're `protected`,
  documented extension points rather than incidental implementation details.

## 8. Exercises

Exercises for this topic (LRU cache, both via the hook and fully hand-built) are grouped with the
rest of this module's build-it-yourself and DSA exercises — see `notes/06-map/06-dsa-patterns-
map.md`, exercise B2 (`LRUCacheExercise`).

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| B2 | Build-it-yourself | LRU cache, O(1) get/put, built from your own doubly linked list + HashMap (not a `LinkedHashMap` wrapper) | HashMap + intrusive doubly linked list | `exercises/LRUCacheExercise.java` |

- <details><summary>Hint</summary>Store `Map<K, Node> lookup` for O(1) node access plus a hand-
  rolled doubly linked list with sentinel `head`/`tail` nodes; on `get`, unlink and re-append to
  the tail; on `put` over capacity, remove the node just after `head`.</details>

Solution is the real implementation in `solutions/LRUCache.java` — attempt the stub in
`exercises/LRUCacheExercise.java` first.

## 9. Quick recap

- `LinkedHashMap` = `HashMap`'s buckets + an overlay doubly linked list for predictable iteration
  order; it reuses all of `HashMap`'s hashing/resize/treeify logic unchanged.
- Two modes: insertion order (default) and access order (`accessOrder=true`, required for LRU) —
  access order relinks the touched entry to the tail on every `get`/update.
- `removeEldestEntry(eldest)` is the hook for self-evicting bounded caches; override it to
  `return size() > capacity` and pass `accessOrder=true` for a correct O(1) LRU cache in a few
  lines.
- Iteration walks the linked list (O(size)), not the sparse bucket array, so it can beat plain
  `HashMap` iteration when the map is sparse.
- Access-order `get()` mutates shared state (the linked list) — it is not safe to call
  concurrently from multiple threads even with no `put()`s involved.
