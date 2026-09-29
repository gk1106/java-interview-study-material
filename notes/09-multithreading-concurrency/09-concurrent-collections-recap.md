# Concurrent collections recap

## 1. What it is

This topic doesn't introduce new data structures — it ties together the thread-safe collections
already covered in earlier modules (`ConcurrentHashMap` in module 06, `CopyOnWriteArrayList` in
module 03, the `BlockingQueue` family in module 04) into one concurrency-focused decision
framework, alongside `ConcurrentSkipListMap`/`ConcurrentSkipListSet` (concurrent sorted
alternatives to `TreeMap`/`TreeSet`, not covered elsewhere) and the "synchronized wrapper" legacy
approach, so you can pick the right thread-safe collection under interview pressure without
re-deriving everything from scratch.

## 2. How it works internally (cross-reference, not re-derivation)

### `ConcurrentHashMap` — already covered in depth

See `notes/06-map/04-concurrenthashmap.md` for the full internals: Java 8+ uses per-bin CAS for
empty-bin insertion and `synchronized` on the bin's first node only for collision chains (not a
single lock over the whole map, unlike the legacy `Hashtable`/`Collections.synchronizedMap`), lazy
resizing with multiple threads able to help transfer entries concurrently, and a `LongAdder`-style
striped counter for `size()`. The headline fact worth repeating here: **no null keys or values**
are allowed (unlike `HashMap`) — this is a deliberate design choice, because in a concurrent map
`map.get(key) == null` is ambiguous between "no mapping" and "mapping to null," and that ambiguity
is actively dangerous when another thread could be concurrently inserting.

### `CopyOnWriteArrayList` — already covered in depth

See `notes/03-list/06-copyonwritearraylist.md`. The core idea worth repeating: every mutating
operation (`add`, `remove`, `set`) copies the **entire backing array** and atomically swaps the
reference (`volatile` array field) — reads never block and never throw
`ConcurrentModificationException`, because iterators work off a fixed snapshot of the array at the
time the iterator was created. This makes it excellent for **read-heavy, write-rare** collections
(e.g., a list of registered event listeners) and actively harmful for write-heavy ones (every
single write is an O(n) full-array copy).

### `BlockingQueue` family — already covered in depth

See `notes/04-queue-deque/04-blockingqueue-family.md` for `ArrayBlockingQueue`,
`LinkedBlockingQueue`, `PriorityBlockingQueue`, `DelayQueue`, `SynchronousQueue`. The reason it's
relevant here: `BlockingQueue` is the **idiomatic, highest-level building block for
producer-consumer coordination** — before reaching for hand-rolled `wait`/`notify` (topic 2) or
`Lock`+`Condition` (topic 4), ask whether a `BlockingQueue` already solves the problem; it usually
does (see topic 10's producer-consumer exercises, done both ways specifically to contrast the
hand-rolled version against this ready-made one).

### `ConcurrentSkipListMap` / `ConcurrentSkipListSet` — new here

A **skip list** is a probabilistic, multi-level linked-list structure: each node has a random
number of "levels" of forward pointers (roughly like an express lane system — most nodes are only
in the base level, fewer nodes are in level 2, even fewer in level 3, etc.), giving expected
O(log n) search/insert/delete without needing tree-rebalancing operations at all. This makes it
naturally lock-free-friendly: unlike a balanced tree (`TreeMap`'s red-black tree), where a single
rotation can touch/relock a chain of ancestor nodes, a skip list insertion only needs to splice in
one new node's forward pointers at each level via CAS — no cascading rebalancing, no need for a
single lock covering large parts of the structure.

```
Level 3: HEAD ---------------------------------> 9 -------------------> END
Level 2: HEAD -----------> 3 -------------------> 9 -------> 15 ------> END
Level 1: HEAD -> 1 -> 3 -> 5 -> 7 -----> 9 -----> 12 -> 15 -> 18 -----> END
```
`ConcurrentSkipListMap`/`Set` are the concurrent, thread-safe, lock-free-read counterparts to
`TreeMap`/`TreeSet` — sorted, support the same `NavigableMap`/`NavigableSet` operations
(`firstKey`, `ceilingKey`, `headMap`, etc.), and allow safe concurrent reads and writes without
external synchronization, unlike `TreeMap` which has no concurrent-safe variant at all (you'd have
to wrap it or use `Collections.synchronizedSortedMap`, serializing all access).

### The synchronized-wrapper approach (legacy, still worth knowing)

`Collections.synchronizedList(list)` / `synchronizedMap(map)` / `synchronizedSet(set)` wrap any
collection so every method is `synchronized` on a single shared lock. This is simple and correct
for **individual** operations, but has two well-known gaps:
1. **Iteration is not automatically safe** — you must manually `synchronized` the entire iteration
   block yourself, or you'll get `ConcurrentModificationException` from a concurrent structural
   modification during iteration (the wrapper's own internal locking doesn't span a whole `for`
   loop, only each individual call).
2. **Compound actions aren't atomic** — `if (!list.contains(x)) list.add(x)` is still a
   check-then-act race even though `contains` and `add` are each individually synchronized.

```java
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
synchronized (syncList) {                 // REQUIRED for safe iteration -- easy to forget
    for (String s : syncList) { ... }
}
```
Modern code almost always prefers a genuinely concurrent collection (`CopyOnWriteArrayList`,
`ConcurrentHashMap`) over a synchronized wrapper, precisely to avoid these footguns — but the
wrapper is still common in legacy code and worth recognizing.

### Decision table

| Need | Use | Why not the alternatives |
|------|-----|---------------------------|
| Thread-safe map, general purpose | `ConcurrentHashMap` | far less contention than `Hashtable`/`synchronizedMap`'s single lock |
| Thread-safe sorted map/set | `ConcurrentSkipListMap`/`Set` | `TreeMap`/`TreeSet` have no thread-safe variant at all |
| Read-heavy, write-rare list (listeners, config snapshots) | `CopyOnWriteArrayList` | every write on a plain synchronized list still serializes readers too |
| Producer-consumer hand-off, bounded backpressure | `BlockingQueue` family | purpose-built; avoids hand-rolled wait/notify bugs entirely |
| Need `ConcurrentModificationException`-free iteration on a general list, write-heavy | none of the above fit well | consider external locking with `ReentrantReadWriteLock` (topic 4) around a plain list instead |
| Legacy code already using synchronized wrappers | `Collections.synchronizedXxx` | fine for simple cases, but manually synchronize the whole block for iteration/compound actions |

## 3. Complexity

| Collection | Read | Write | Notes |
|------------|------|-------|-------|
| `ConcurrentHashMap` | O(1) avg, lock-free | O(1) avg, per-bin lock/CAS | see module 06 for full treeification/resize detail |
| `CopyOnWriteArrayList` | O(1) get, lock-free | O(n) — full array copy per write | reads never block, iterators never throw CME |
| `ArrayBlockingQueue`/`LinkedBlockingQueue` | O(1) `peek`/`poll` | O(1) `offer`/`put` | blocking variants park the caller when empty/full |
| `ConcurrentSkipListMap`/`Set` | O(log n) expected | O(log n) expected | probabilistic balancing, no rebalancing locks needed |
| `Collections.synchronizedXxx` wrapper | O(1)/O(log n) + lock overhead | same + lock overhead | single shared lock — no read/write concurrency at all |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/ConcurrentCollectionsRecapDemo.java`
  — runs concurrent writers against `ConcurrentHashMap` and `CopyOnWriteArrayList` (proving no data
  loss/no `ConcurrentModificationException`), demonstrates `ConcurrentSkipListMap`'s sorted-order
  guarantee under concurrent inserts, and contrasts a `Collections.synchronizedList` compound-action
  race against a correctly externally-locked version.

```java
ConcurrentSkipListMap<Integer, String> sorted = new ConcurrentSkipListMap<>();
// N threads insert concurrently; afterward, keySet() is verified to be in ascending order
System.out.println(sorted.firstKey() + " .. " + sorted.lastKey());
```
Expected console output (abbreviated):
```
ConcurrentHashMap: 8 threads x 500 puts each -> size=4000 (no lost updates)
CopyOnWriteArrayList: concurrent iteration during writes completed with zero ConcurrentModificationException
ConcurrentSkipListMap: keys after concurrent insert from 6 threads are in ascending sorted order = true
synchronized wrapper compound-action race: final size WITHOUT external lock > expected (duplicate adds slipped through)
synchronized wrapper compound-action fixed: final size WITH synchronized(list) block == expected
```

## 5. When to use / when NOT to use

- Default to `ConcurrentHashMap` for any shared mutable map — there's rarely a good reason to use
  `Hashtable` or `Collections.synchronizedMap` in new code.
- Use `CopyOnWriteArrayList` only when reads dominate writes by a wide margin — profile before
  assuming this fits; a write-heavy workload makes its O(n)-copy-per-write cost worse than a
  simple `synchronized`/`ReadWriteLock`-guarded `ArrayList`.
- Use a `BlockingQueue` implementation as the default answer to "how do I hand work off between
  threads safely" before reaching for lower-level primitives.
- Use `ConcurrentSkipListMap`/`Set` specifically when you need both thread safety AND sorted-order
  navigation (`NavigableMap` operations) concurrently — for unsorted thread-safe access,
  `ConcurrentHashMap`/a concurrent `Set` view of it is simpler and faster.
- Avoid `Collections.synchronizedXxx` wrappers in new code; when you must use legacy code that
  already relies on one, remember to manually synchronize on the wrapper for iteration and any
  compound (check-then-act) operation.

## 6. Common pitfalls & gotchas

**Iterating a `Collections.synchronizedList` without an explicit `synchronized` block**:
```java
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
for (String s : syncList) { ... }   // BUG: each individual next()/hasNext() call IS synchronized,
                                     // but a concurrent structural modification between calls can
                                     // still throw ConcurrentModificationException
// fix:
synchronized (syncList) {
    for (String s : syncList) { ... }
}
```

**Assuming `ConcurrentHashMap.size()` is a perfectly consistent snapshot under concurrent writes**
— it's a fast, striped-counter estimate (module 06), not a value obtained by locking out all
writers; treat it as approximately correct at any given instant under contention, not exact.

**Storing `null` in a `ConcurrentHashMap`**:
```java
map.put("key", null);   // BUG: throws NullPointerException immediately -- unlike HashMap
// fix: use a sentinel value, or Optional.empty() as the stored value, or a separate "absent" check
```

**Using `CopyOnWriteArrayList` for a write-heavy shared list**:
```java
CopyOnWriteArrayList<Event> events = new CopyOnWriteArrayList<>();
for (...) { events.add(newEvent); }   // BUG: O(n) full-array copy on EVERY add -- O(n^2) total
// fix: use a synchronized/ReadWriteLock-guarded ArrayList, or a BlockingQueue if it's really a
// producer-consumer pattern rather than "a list many threads append to"
```

## 7. Interview questions

- [Basic] Why can't `ConcurrentHashMap` store `null` keys or values, unlike `HashMap`? →
  `map.get(key)` returning `null` would be ambiguous between "no such mapping exists" and "the
  mapping exists and its value is null" — in a single-threaded `HashMap` you can resolve that
  ambiguity with a follow-up `containsKey()` check, but in a concurrent map another thread could
  insert/remove between your `get()` and your `containsKey()`, making that check-then-act
  unreliable; disallowing null entirely sidesteps the ambiguity. → Follow-up: *What's the
  workaround if you genuinely need to represent "no value" in a ConcurrentHashMap?* Use a sentinel
  object, or wrap values in `Optional`, or use `computeIfAbsent`-style atomic methods that avoid
  the ambiguous check-then-act pattern altogether.
- [Basic] When is `CopyOnWriteArrayList` a good fit, and when is it a bad fit? → Good fit:
  read-heavy, write-rare collections, like a list of registered listeners iterated frequently but
  modified rarely — reads never block and iterators never throw
  `ConcurrentModificationException`. Bad fit: write-heavy lists — every single mutation copies the
  entire backing array, making it O(n) per write. → Follow-up: *How does its iterator avoid
  ConcurrentModificationException?* It iterates over a fixed snapshot of the array captured when the
  iterator was created — concurrent writes create a new array entirely, never touching the one the
  iterator is walking.
- [Basic] What's wrong with `if (!syncMap.containsKey(k)) syncMap.put(k, v);` even though both
  calls are individually thread-safe? → It's a classic check-then-act race: two threads can both
  observe `containsKey(k) == false` before either calls `put`, so both proceed to `put`, with the
  second silently overwriting the first's value — individual method thread-safety doesn't compose
  into atomicity of a multi-step sequence. → Follow-up: *What's the correct fix on a
  ConcurrentHashMap?* Use `putIfAbsent(k, v)`, which performs the check-and-insert atomically as a
  single operation.
- [Intermediate] Compare `ConcurrentHashMap`'s locking strategy to `Collections.synchronizedMap`'s.
  → `Collections.synchronizedMap` wraps every method call with the *same single lock*, so all
  reads and writes across the entire map fully serialize against each other, regardless of which
  keys/buckets are actually involved; `ConcurrentHashMap` (Java 8+) uses fine-grained per-bin
  locking (`synchronized` on just that bin's first node) plus CAS for lock-free empty-bin
  insertion, so operations on different bins run fully concurrently, and even most reads require no
  locking at all. → Follow-up: *Does that mean ConcurrentHashMap read operations never block?*
  Correct — reads are largely lock-free (`Node` fields are `volatile`), so a read essentially never
  blocks on a write in progress to a different bin, and even reads on the same bin during a
  concurrent write usually still succeed without blocking, seeing either the old or new state
  consistently.
- [Intermediate] Why does `ConcurrentSkipListMap` not need the rebalancing that `TreeMap` (a
  red-black tree) requires, and why does that matter for concurrency? → A skip list achieves
  expected O(log n) operations through randomized multi-level linked lists rather than strict
  tree-balance invariants — inserting a node just requires splicing its forward pointers in at
  each of its randomly chosen levels, a localized operation; a red-black tree insertion/deletion
  can require rotations that touch and must lock a chain of ancestor nodes to maintain balance
  invariants, which is much harder to do correctly and efficiently under concurrent access without
  either a single coarse lock or a very intricate fine-grained locking scheme. → Follow-up: *Is
  there a thread-safe TreeMap equivalent in the JDK at all?* No — there's no lock-free or
  fine-grained-locking concurrent red-black tree in the standard library; `ConcurrentSkipListMap`
  is specifically the sorted, concurrent alternative precisely because rebalancing trees don't
  parallelize well.
- [Intermediate] Why is `Collections.synchronizedList(list)` iteration required to be wrapped in an
  explicit `synchronized (list) { ... }` block, when every individual method on the wrapper is
  already synchronized? → The wrapper synchronizes each *individual* method call (`get(i)`,
  `size()`, `add()`, the iterator's `next()`) but a `for-each` loop is many separate calls across
  time; a structural modification from another thread between two of those calls (e.g., between
  `hasNext()` and `next()`) is not prevented by per-call synchronization alone, and will still
  surface as `ConcurrentModificationException` — only explicitly holding the lock for the entire
  iteration prevents that. → Follow-up: *Does CopyOnWriteArrayList have this same iteration
  problem?* No — because its iterator works off an immutable snapshot array rather than the live
  backing structure, so no concurrent modification can ever invalidate an in-progress iteration.
- [Advanced] You're choosing between `ConcurrentHashMap<K, List<V>>` (list per key, synchronized
  externally) versus `ConcurrentHashMap<K, CopyOnWriteArrayList<V>>` for a multimap-like structure
  under concurrent writes across many keys. Walk through the trade-off. → With a plain `List<V>`
  per key, you need external synchronization (or `computeIfAbsent` + synchronized block) around
  every mutation of that key's list, and different threads mutating *different* keys' lists can
  still contend if you synchronize too coarsely (e.g., on the whole map); with
  `CopyOnWriteArrayList<V>` per key, each key's list is independently thread-safe with no external
  locking needed, and different keys never contend with each other at all — but if any individual
  key's list is written to frequently, that key pays the O(n)-copy-per-write cost repeatedly. The
  right choice depends on the write frequency *per key*, not just overall: many keys each written
  rarely favors `CopyOnWriteArrayList` per key (simplicity, no external locking, negligible copy
  cost since lists are small/write-rare); a few hot keys written very frequently favor a
  `ReentrantReadWriteLock`-guarded plain `List` for those keys, or restructuring the data model
  entirely (e.g., a `ConcurrentLinkedQueue` per key if order/insertion is all that matters, since
  it needs no locking and no O(n) copy). → Follow-up: *What would ConcurrentHashMap.compute() add
  here?* It lets you atomically create-or-update a key's list-wrapping value (e.g., "if absent,
  create a new CopyOnWriteArrayList with this element; if present, add to the existing one") as one
  atomic operation on the map itself, closing a check-then-act gap in "get-list-or-create,
  then add" done as two separate map operations.

## 8. Exercises

No dedicated exercises for this topic — it is a decision-framework recap over collections already
exercised in modules 03, 04, and 06 (`CopyOnWriteArrayList`, `BlockingQueue` family,
`ConcurrentHashMap` respectively). The producer-consumer exercises in
`notes/09-multithreading-concurrency/10-classic-concurrency-problems.md` apply the `BlockingQueue`
choice from this recap directly.

## 9. Quick recap

- `ConcurrentHashMap` (module 06): fine-grained per-bin locking + CAS, no null keys/values —
  default choice for any shared map.
- `CopyOnWriteArrayList` (module 03): O(n) copy-on-write, snapshot iterators never throw CME — only
  for read-heavy, write-rare lists.
- `BlockingQueue` family (module 04): the idiomatic producer-consumer building block — reach for it
  before hand-rolling `wait`/`notify` or `Lock`/`Condition` coordination.
- `ConcurrentSkipListMap`/`Set`: the thread-safe, sorted alternative to `TreeMap`/`TreeSet` — no
  thread-safe red-black tree exists in the JDK; skip lists parallelize far more easily.
- `Collections.synchronizedXxx` wrappers: correct for individual calls, but iteration and compound
  (check-then-act) operations need an explicit external `synchronized` block — modern code prefers
  genuinely concurrent collections instead.
