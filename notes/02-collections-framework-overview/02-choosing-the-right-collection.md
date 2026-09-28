# Choosing the Right Collection

## 1. What it is

Picking a collection is a design decision driven by four questions: **What
ordering do I need? Do I allow duplicates? Do I need thread-safety? What's
my dominant access pattern (index, key, priority, both ends)?** Getting this
right up front avoids expensive rewrites later (e.g. discovering in
production that a `HashMap` iteration order you silently relied on is
actually undefined).

## 2. How it works internally

This topic is a *decision* topic more than an *internals* topic — the
internals of each candidate are covered in depth in modules 03–06. Here we
build the decision table and explain *why* each recommendation follows from
the implementation's underlying data structure.

### 2.1 The decision table

| I need... | Recommended collection | Why |
|---|---|---|
| Index-based access, duplicates OK, mostly reads/appends | `ArrayList` | backing array -> O(1) `get(i)`, amortized O(1) append |
| Frequent inserts/removals at both ends, no index access needed | `ArrayDeque` | circular array, amortized O(1) both ends, better cache locality than linked nodes |
| Frequent inserts/removals in the *middle* via an iterator (rare) | `LinkedList` | O(1) `ListIterator.add/remove` once positioned, but O(n) to get there |
| Unique elements, insertion order must be preserved, no sorting | `LinkedHashSet` | HashSet's O(1) hashing + a doubly linked list threading insertion order |
| Unique elements, no order guarantee needed, max throughput | `HashSet` | pure hash bucket lookup, no linked-list bookkeeping overhead |
| Unique elements that must stay sorted, need range queries (floor/ceiling/subSet) | `TreeSet` | Red-Black tree, O(log n) ops, in-order traversal is sorted |
| Key -> value lookup, no order guarantee needed | `HashMap` | O(1) average hashing |
| Key -> value lookup, must preserve insertion (or access) order | `LinkedHashMap` | HashMap + linked list; access-order mode gives you LRU for free |
| Key -> value lookup, keys must stay sorted / need range queries | `TreeMap` | Red-Black tree keyed lookup |
| Priority-ordered retrieval (always get the min/max next) | `PriorityQueue` | binary heap, O(log n) insert/poll, O(1) peek |
| FIFO processing (classic queue / BFS) | `ArrayDeque` (via `offer`/`poll`) | same as above; `ArrayDeque` is the modern default queue |
| LIFO processing (stack) | `ArrayDeque` (via `push`/`pop`) | avoid legacy `Stack` (synchronized, extends `Vector`) |
| Thread-safe map, high read/write concurrency | `ConcurrentHashMap` | segmented/bucket-level locking + CAS, no global lock |
| Thread-safe list, reads vastly outnumber writes (e.g. listener lists) | `CopyOnWriteArrayList` | snapshot-on-write, lock-free reads, O(n) writes |
| Thread-safe queue for producer/consumer handoff | `BlockingQueue` family (`ArrayBlockingQueue`, `LinkedBlockingQueue`, `SynchronousQueue`) | blocking `put`/`take`, built for concurrency (module 04/09) |
| Truly immutable, fixed contents, safe to share across threads without any wrapper | `List.of` / `Set.of` / `Map.of` | compact, defensively unmodifiable, throws NPE on null elements (see topic 03) |
| Enum-keyed set/map with tiny memory footprint | `EnumSet` / `EnumMap` | backed by a bit vector / array indexed by enum ordinal |

### 2.2 A decision flow (ASCII)

```
                Do you need key -> value lookup?
                        /              \
                      yes                no
                       |                  |
        Need sorted keys?          Need element uniqueness?
          /        \                   /            \
        yes          no              yes              no
         |            |               |                |
      TreeMap   Need insertion    Need sorted       Need both-ends
                  order kept?       elements?          access?
                  /      \           /    \             /    \
               yes        no       yes     no          yes    no
                |          |        |       |            |     |
          LinkedHashMap  HashMap TreeSet  insertion   ArrayDeque ArrayList
                                          order kept?  (queue/     (index
                                           /     \      stack/     access,
                                         yes      no     deque)    dupes ok)
                                          |        |
                                   LinkedHashSet  HashSet
```

Thread-safety is an orthogonal axis: once you land on a shape (List/Set/
Map/Queue), ask "single-threaded, or shared across threads?" and if shared,
swap in the `java.util.concurrent` counterpart (`ConcurrentHashMap`,
`CopyOnWriteArrayList`, `BlockingQueue` impls) rather than manually
synchronizing the plain collection (module 09 covers why coarse
`Collections.synchronizedX` wrappers are usually the wrong answer under
contention).

## 3. Complexity

| Collection | get/contains by index | get/contains by key or value | insert (typical position) | ordered iteration |
|---|---|---|---|---|
| `ArrayList` | O(1) index | O(n) value scan | O(1) amortized append, O(n) middle | insertion order |
| `LinkedList` (as List) | O(n) | O(n) | O(1) at known position, O(n) to find it | insertion order |
| `ArrayDeque` | n/a | O(n) scan | O(1) amortized both ends | insertion order |
| `HashSet` | n/a | O(1) average | O(1) average | none |
| `LinkedHashSet` | n/a | O(1) average | O(1) average | insertion order |
| `TreeSet` | n/a | O(log n) | O(log n) | sorted order |
| `HashMap` | n/a | O(1) average by key | O(1) average | none |
| `LinkedHashMap` | n/a | O(1) average by key | O(1) average | insertion (or access) order |
| `TreeMap` | n/a | O(log n) by key | O(log n) | sorted by key |
| `PriorityQueue` | n/a | O(n) contains | O(log n) offer | none (heap order); `poll()` gives sorted sequence |
| `ConcurrentHashMap` | n/a | O(1) average, thread-safe | O(1) average, thread-safe | none, weakly consistent iterator |
| `CopyOnWriteArrayList` | O(1) index | O(n) value scan | O(n) (copies whole array) | insertion order, snapshot iterator |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/collectionsoverview/examples/ChoosingCollectionDemo.java`

```java
// Scenario: dedupe a stream of account IDs but keep first-seen order
Set<String> seen = new LinkedHashSet<>();
for (String id : List.of("acc-3", "acc-1", "acc-3", "acc-2", "acc-1")) {
    seen.add(id);
}
System.out.println(seen); // [acc-3, acc-1, acc-2]  <- insertion order, deduped
```

Expected console output (abridged, full output documented in the class):
```
Deduped, insertion order: [acc-3, acc-1, acc-2]
Bounded LRU cache after inserts+touch (capacity 3): {c=3, a=1, d=4}
Transaction amounts in [100, 400]: [150, 200, 400]
```

## 5. When to use / when NOT to use

- Use `ArrayList` by default for lists; reach for `LinkedList` only when
  profiling shows you genuinely do many middle-insertions via an
  already-positioned `ListIterator` — otherwise it's usually a net loss
  (module 03 has the benchmark).
- Use `HashMap`/`HashSet` by default; upgrade to `LinkedHashMap`/
  `LinkedHashSet` only when you've observed callers relying on order (don't
  pay the linked-list overhead speculatively).
- Use `TreeMap`/`TreeSet` only when you need sorted iteration or range
  queries (`floor`, `ceiling`, `headMap`, `tailMap`) — O(log n) is strictly
  worse than O(1) hashing if you don't need the ordering.
- Do NOT reach for `Vector`/`Stack`/`Hashtable` in new code — they're
  synchronized on every call (throughput hit even single-threaded) and long
  superseded by `ArrayDeque`, `ArrayList`, and `ConcurrentHashMap`/
  `Collections.synchronizedMap`.
- Do NOT default to `ConcurrentHashMap` "just in case" — if a map never
  escapes a single thread, the extra CAS/locking overhead buys you nothing.
- Do NOT use `CopyOnWriteArrayList` for write-heavy workloads — every
  mutation copies the entire backing array (O(n)); it's a great fit for
  rarely-changed listener/observer lists, a poor fit for a work queue.

## 6. Common pitfalls & gotchas

**Pitfall 1 — picking `HashMap` when order matters, then "fixing" it later
with a sort at read time everywhere.**

```java
Map<String, Integer> counts = new HashMap<>(); // iteration order unspecified
// ... code elsewhere relies on insertion order by accident ...
```

Fix: if insertion order is actually part of the contract, say so in the
type up front.

```java
Map<String, Integer> counts = new LinkedHashMap<>(); // order guaranteed, documented in the type
```

**Pitfall 2 — using `ArrayList.contains()`/`remove(Object)` in a hot loop
over large data, expecting `Set`-like speed.**

```java
List<String> ids = new ArrayList<>(millionIds);
if (ids.contains(target)) { ... } // O(n) linear scan, easy to miss at scale
```

Fix: use a `HashSet` (or `HashMap` if you need an associated value) for
membership tests.

```java
Set<String> idSet = new HashSet<>(millionIds);
if (idSet.contains(target)) { ... } // O(1) average
```

**Pitfall 3 — reaching for `TreeMap` "to be safe" when you never actually
need sorted/range access.**

```java
Map<Long, Customer> byId = new TreeMap<>(); // pays O(log n) forever
```

Fix: use `HashMap` unless you call `firstKey()`, `ceilingEntry()`,
`headMap()`, etc.

```java
Map<Long, Customer> byId = new HashMap<>(); // O(1) average, same functionality if unused
```

## 7. Interview questions

- [Basic] Q: You need to store unique usernames with fast membership checks
  and don't care about order — what do you pick and why?
  A: `HashSet` — O(1) average `add`/`contains`/`remove` via hashing, and no
  ordering overhead since none is required.
  Follow-up: What if usernames must print in the order they were registered?
  (`LinkedHashSet` — same O(1) average operations plus an insertion-order
  linked list at a small constant memory cost.)

- [Basic] Q: When would you choose `ArrayDeque` over `LinkedList` for a
  stack or queue?
  A: Almost always — `ArrayDeque` has no per-element node allocation, better
  cache locality (contiguous circular array), and amortized O(1) operations
  at both ends, whereas `LinkedList` pays per-node allocation/GC overhead
  and pointer-chasing for the same asymptotic complexity.
  Follow-up: Is there any case `LinkedList` still wins? (Only if you need
  simultaneous `List` random-position `ListIterator` mutation *and* deque
  behavior on the same instance — rare in practice.)

- [Basic] Q: You need to look up customer records by account number with no
  ordering requirement — `HashMap` or `TreeMap`?
  A: `HashMap` — O(1) average lookup beats `TreeMap`'s O(log n) when you
  don't need sorted iteration or range queries.
  Follow-up: At what point would you reconsider? (If you later need
  "all accounts between X and Y" or "the next account number after N" —
  that's exactly what `TreeMap`'s `subMap`/`ceilingKey` give you.)

- [Intermediate] Q: Design a data structure to always retrieve the
  highest-value pending transaction next — what do you use?
  A: `PriorityQueue` with a comparator on transaction value (or a max-heap
  via `Comparator.reverseOrder()`, since `PriorityQueue` is a min-heap by
  default) — O(log n) insert, O(1) peek, O(log n) poll.
  Follow-up: How would you support "cancel an arbitrary pending
  transaction" efficiently? (Plain `PriorityQueue.remove(Object)` is O(n);
  a common fix is an *indexed* heap you build yourself, or lazy deletion —
  mark cancelled and skip on poll.)

- [Intermediate] Q: You're building an LRU cache. Which JCF class gets you
  90% of the way there for free?
  A: `LinkedHashMap` constructed with `accessOrder = true`, overriding
  `removeEldestEntry(Map.Entry)` to evict once size exceeds capacity — O(1)
  average get/put, and the linked list already tracks recency.
  Follow-up: What would you reach for if you needed this to be thread-safe
  under concurrent access? (`LinkedHashMap` isn't thread-safe; you'd wrap it
  with external synchronization, or implement a concurrent LRU using
  `ConcurrentHashMap` + a concurrent eviction structure — module 06/09.)

- [Intermediate] Q: A shared list of event listeners is read on every event
  (thousands of times a second) but modified rarely (listeners
  added/removed a few times at startup). What collection?
  A: `CopyOnWriteArrayList` — reads need no locking/synchronization at all
  (iterating a stable snapshot array), and the O(n) copy cost on
  add/remove is irrelevant given how rarely it happens.
  Follow-up: What would go wrong if writes were actually frequent (say,
  thousands/sec)? (Every write copies the entire backing array — O(n) per
  write — so throughput collapses; you'd want `ConcurrentHashMap`-backed
  keys/a concurrent set, or a `synchronized` list, or a `CopyOnWriteArraySet`
  redesign depending on the access pattern.)

- [Intermediate] Q: Why is `Hashtable`/`Vector`/`Stack` discouraged even
  though they're thread-safe out of the box?
  A: They synchronize on *every single method call*, including reads, which
  serializes access even when no contention exists, and provides no
  atomicity across multi-step operations anyway (e.g. check-then-act races
  are still possible). Modern alternatives (`ConcurrentHashMap`,
  `Collections.synchronizedList` when you truly need a drop-in, or explicit
  `java.util.concurrent` classes) give better throughput and clearer
  atomicity guarantees.
  Follow-up: Give an example of a check-then-act race that `Hashtable`
  doesn't protect against. (`if (!map.containsKey(k)) map.put(k, v)` is two
  separate synchronized calls — another thread can interleave between them;
  `ConcurrentHashMap.putIfAbsent` fixes this atomically.)

- [Advanced] Q: You need a `Set` of `BigDecimal` transaction amounts with
  fast membership checks, but `BigDecimal.equals()` treats `2.0` and `2.00`
  as unequal (scale-sensitive) while `compareTo` treats them as equal. How
  does the collection you choose change the semantics?
  A: `HashSet<BigDecimal>` uses `equals()`/`hashCode()`, so `2.0` and `2.00`
  are treated as *distinct* elements — both would be stored. `TreeSet<BigDecimal>`
  uses `compareTo()` for both ordering and uniqueness, so it would treat them
  as duplicates and keep only one. This is a case where the "same" data
  produces different de-duplication behavior purely based on which
  collection you picked — worth calling out explicitly in code review.
  Follow-up: How would you force `HashSet` to treat them as equal? (Normalize
  before inserting, e.g. `stripTrailingZeros()`, or store a canonical
  `String`/`long`-cents representation as the actual set element.)

- [Advanced] Q: Under heavy concurrent read/write load, would you pick
  `Collections.synchronizedMap(new HashMap<>())` or `ConcurrentHashMap`, and
  why?
  A: `ConcurrentHashMap` — `synchronizedMap` wraps every call in one global
  lock, so reads block writes and each other, while `ConcurrentHashMap`
  (Java 8+) uses per-bin locking plus CAS for many operations, giving much
  higher read/write throughput under contention; it also offers atomic
  compound operations (`computeIfAbsent`, `merge`) that `synchronizedMap`
  doesn't make atomic across the check+act boundary unless you manually
  synchronize on the map object for the whole sequence.
  Follow-up: Does `ConcurrentHashMap` allow null keys or values? (No — for
  either — because a `null` return from `get(k)` would be ambiguous between
  "key absent" and "key mapped to null" under concurrent mutation, which
  `HashMap` tolerates only because it isn't making concurrency guarantees.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Deduplicate a list of account IDs while preserving first-seen order | insertion-order dedupe | `DedupePreservingOrder.java` |
| E02 | Easy | Count word frequency across many threads safely and correctly | concurrent map choice | `ConcurrentWordCounter.java` |
| E03 | Medium | Build a bounded LRU cache using `LinkedHashMap` access-order mode | LRU via LinkedHashMap | `LruCache.java` |
| E04 | Medium | Given a set of transaction amounts, answer range queries (`between(lo, hi)`) efficiently | sorted set / NavigableSet range query | `TransactionAmountIndex.java` |

Solutions live only in `src/main/java/com/gk/study/collectionsoverview/solutions/`.

<details>
<summary>E01 hint</summary>
`LinkedHashSet` gives you O(1) average membership checks AND insertion
order in one data structure — no need to combine a `HashSet` with a
separate `List`.
</details>

<details>
<summary>E02 hint</summary>
Use <code>ConcurrentHashMap&lt;String, LongAdder&gt;</code> or
<code>ConcurrentHashMap.merge(word, 1, Integer::sum)</code> — both give
atomic increments without an external lock. Run the increments from
multiple threads via an <code>ExecutorService</code> and assert the final
total equals the expected count.
</details>

<details>
<summary>E03 hint</summary>
Extend <code>LinkedHashMap&lt;K,V&gt;</code>, pass
<code>accessOrder=true</code> to the 3-arg constructor, and override
<code>removeEldestEntry(Map.Entry&lt;K,V&gt; eldest)</code> to return true
once <code>size() &gt; capacity</code>.
</details>

<details>
<summary>E04 hint</summary>
Back the index with a <code>TreeSet&lt;Integer&gt;</code> (or
<code>NavigableSet</code>) and use <code>subSet(lo, true, hi, true)</code>
for an inclusive range query in O(log n + k) where k is the result size.
</details>

## 9. Quick recap

- Ask four questions first: ordering needed? duplicates allowed? thread
  safety required? what's the dominant access pattern (index/key/priority/
  both-ends)?
- Default to `ArrayList`, `HashSet`, `HashMap`, `ArrayDeque` — upgrade to
  `LinkedHash*`/`Tree*`/concurrent variants only when you have a concrete
  requirement for order, sorting, or thread-safety.
- `LinkedHashMap` with `accessOrder=true` + `removeEldestEntry` = an LRU
  cache almost for free.
- `TreeSet`/`TreeMap` cost O(log n) instead of O(1) — only pay for that when
  you actually use sorted iteration or range queries.
- Avoid legacy synchronized classes (`Vector`, `Stack`, `Hashtable`) and
  naive `Collections.synchronizedX` under real contention — prefer
  `java.util.concurrent` (`ConcurrentHashMap`, `CopyOnWriteArrayList`,
  `BlockingQueue`).
