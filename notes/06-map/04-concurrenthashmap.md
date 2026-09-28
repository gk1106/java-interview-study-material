# ConcurrentHashMap

## 1. What it is

`ConcurrentHashMap<K,V>` is a thread-safe `Map` designed for high-concurrency reads and writes
without locking the entire map. Java 8 rewrote its internals from Java 7's **segment locking**
(16 independent locked stripes) to a much finer-grained scheme: **CAS for the common case, a
per-bin lock only when there's an actual collision**, giving near-lock-free performance under
typical workloads. It is the default choice for any `Map` shared across threads.

## 2. How it works internally

### Java 7 vs Java 8 — the architectural shift

- **Java 7**: the map was split into a fixed number of `Segment`s (default 16), each an
  independently `ReentrantLock`-guarded mini hash table. A `put` locked only the segment owning
  that key's hash (so up to 16 threads could write concurrently, one per segment), but concurrency
  was capped at the segment count, and reads still needed careful volatile-read handling to avoid
  torn reads during a segment's internal resize.
- **Java 8+**: segments are gone. The table is one flat `Node<K,V>[] table`, structurally similar
  to `HashMap`'s. Concurrency control moved to the **finest reasonable granularity: per bucket
  (bin)**, using two different techniques depending on the situation:
  1. **Inserting into an empty bin**: a lock-free **CAS** (compare-and-swap, via
     `Unsafe`/`VarHandle` intrinsics on the array slot) — no lock at all. If the CAS fails
     (another thread beat you to it), retry.
  2. **Inserting into a non-empty bin (collision)**: `synchronized` on the **head node of that
     bin only** — not the whole table, not even the whole bucket array, just that one bin's
     current first node. Two threads writing to *different* bins never block each other at all.
  Reads (`get`) are **entirely lock-free** — they read `table` and node fields through volatile
  (`VarHandle`) semantics, so a reader always sees either the old or the fully-published new
  state of a node, never a torn half-write, without ever taking a lock.

### Why this is safe without a table-wide lock

Every write to a bin's head slot, and every write of a `Node`'s `val`/`next` fields, happens
through **volatile** (`VarHandle`) writes, and reads use matching volatile reads. This gives
the Java Memory Model's happens-before guarantee: once a writer publishes a node via a
volatile/CAS write, any reader that subsequently reads that reference is guaranteed to see the
fully-constructed node (all its fields), not a partial object — the same principle behind the
classic "safe publication via a `volatile` field" pattern, applied per-bin instead of per-field.

### Resize / transfer, and `helpTransfer`

When the table needs to grow (tracked via a "base count" estimate crossing a threshold, computed
the same 0.75-load-factor way as `HashMap`), one thread initiates a **transfer** to a new,
double-sized table. Unlike `HashMap`'s single-threaded resize, `ConcurrentHashMap`'s resize is
**incremental and can be helped by other threads**:
- The table being resized is split into contiguous ranges of bins ("stripes"); each thread
  (the initiator, or another thread that calls `put`/`get`/`remove` mid-resize and detects a
  resize in progress via a special `ForwardingNode` marker) claims a stripe and moves its bins to
  the new table, using the same low/high-list split trick as `HashMap` (a bin's entries split
  into "stays at same index" / "moves to index + oldCapacity" based on one hash bit).
- A bin already moved is marked with a sentinel `ForwardingNode` whose `next` effectively
  redirects any thread that lands on it to look in the *new* table instead — this is how a `get()`
  running concurrently with a resize still finds the right answer without blocking.
- This is `helpTransfer`: any thread performing a `put` that notices a resize in progress
  volunteers to migrate a chunk of bins itself instead of just waiting, spreading the O(n) resize
  cost across multiple threads rather than making one thread (or, worse, blocking everyone) pay
  for it alone.

### `size()` — why it needs `LongAdder`-style striping

Maintaining one shared `AtomicLong` counter, incremented by every `put`/`remove` under CAS, would
become a **contention bottleneck** under heavy concurrent write load — every thread's CAS retries
against the same memory location, effectively serializing writers on that single counter even
though their actual bin writes are independent. `ConcurrentHashMap` instead uses a
**`CounterCell[]` array**, conceptually the same idea as `LongAdder`: when contention is detected
on the base counter, each thread's updates spread out across different `CounterCell` slots
(chosen via a thread-local hash), so concurrent increments mostly hit *different* memory
locations and don't contend. `size()` (and `mappingCount()`) then **sums all cells plus the
base counter** on demand — an O(number of cells) operation, done rarely (size queries), in
exchange for O(1) uncontended increments on the hot write path. This trades a slightly more
expensive read (`size()`) for dramatically cheaper, non-contending writes — the right trade-off
since writes vastly outnumber size queries in most concurrent workloads.

### Why no null keys or null values

`ConcurrentHashMap.put(key, null)` and `.put(null, value)` both throw `NullPointerException` —
this is deliberate, not an oversight. In a **single-threaded** `HashMap`, `map.get(key) == null`
is ambiguous (key absent, vs key present mapped to `null`), but you can always disambiguate with a
follow-up `containsKey(key)` check, and between the `get` and the `containsKey` nothing else can
have changed the map (single thread). In a **concurrent** map, that two-step
`get`-then-`containsKey` check is **not atomic** — another thread could insert or remove the key
between your two calls, making the disambiguation meaningless/racy. Doug Lea (the JDK's
concurrency architect) made the design call to simply disallow `null` values entirely, removing
the ambiguity at the source rather than trying to paper over a race that can't be fixed by
querying twice. Null keys are disallowed too, for consistency and because `ConcurrentHashMap`
offers no "one special null-key bucket" the way `HashMap` does.

### `compute` / `computeIfAbsent` / `merge` — atomicity

These are the idiomatic way to do atomic "read-modify-write" on a single key in
`ConcurrentHashMap` without external locking:
- `computeIfAbsent(key, mappingFunction)` — atomically inserts a computed value only if the key
  is absent; the whole check-and-insert happens while holding that bin's lock (or via CAS on an
  empty bin), so two threads racing to initialize the same key never both compute and overwrite
  each other — exactly one wins, and the other sees the already-computed value.
- `compute(key, remappingFunction)` / `merge(key, value, remappingFunction)` — atomically read the
  current value (or `null`), apply the function, and write the result (or remove the entry if the
  function returns `null`), all under that bin's lock.
- **Caveat**: the supplied function runs **while holding the bin's lock**, so it must be fast and
  must **not** attempt another map operation on the same map (even a `get` on a different key can
  deadlock in pathological cases, and recursively invoking `compute` on the *same* key is
  documented as undefined behavior/can deadlock) — keep the lambda side-effect-free and quick.

### ASCII diagram — CAS on empty bin vs synchronized on collision

```
put(K1, V1) where table[3] is currently null:
  CAS(table[3], null -> new Node(K1,V1))      // lock-free; retry the CAS if it fails
  success -> done, no lock ever taken

put(K2, V2) where table[3] already holds Node(K1,V1) (collision):
  synchronized(table[3]) {                     // lock ONLY this bin's head node
      walk chain, append/overwrite
  }
  // a concurrent put(K9,V9) hashing to table[7] proceeds fully in parallel, no contention at all
```

### ASCII diagram — resize stripe with ForwardingNode

```
Old table (capacity 16) mid-resize to capacity 32:
  table[0]  -> already migrated -> [ForwardingNode] (points readers to newTable)
  table[1]  -> being migrated by thread T1 right now
  table[2]  -> not yet migrated -> normal Node chain, untouched

get(key) landing on table[0] during the resize:
  sees ForwardingNode -> follows it into newTable, searches there instead -> correct result,
  no blocking
```

## 3. Complexity

| Operation | Time | Notes |
|-----------|------|-------|
| `get(key)` | O(1) average, lock-free | volatile reads only, never blocks |
| `put`/`putIfAbsent` (empty bin) | O(1), lock-free (CAS) | retries on CAS failure |
| `put` (collision) | O(1) average | `synchronized` on that bin's head node only |
| `compute`/`merge`/`computeIfAbsent` | O(1) average | atomic per-key, function runs under that bin's lock |
| `size()` / `mappingCount()` | O(number of CounterCells), amortized cheap | sums striped counters on demand |
| resize | O(n) total, parallelizable via `helpTransfer` | multiple threads can migrate different stripes concurrently |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/map/examples/ConcurrentHashMapDemo.java` — spins up
  a small fixed `ExecutorService`, has multiple threads call `merge`/`compute` on a shared
  `ConcurrentHashMap<String, Integer>` a bounded number of times each, awaits termination with a
  timeout, then prints the final counts to prove no updates were lost (deterministic totals).

```java
ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
ExecutorService pool = Executors.newFixedThreadPool(4);
for (int t = 0; t < 4; t++) {
    pool.submit(() -> {
        for (int i = 0; i < 1000; i++) {
            counts.merge("key", 1, Integer::sum);   // atomic increment, no lost updates
        }
    });
}
pool.shutdown();
pool.awaitTermination(5, TimeUnit.SECONDS);
System.out.println(counts.get("key"));   // always 4000 — merge is atomic per key
```
Expected console output:
```
4000
```

## 5. When to use / when NOT to use

- Use whenever a `Map` is shared across threads and mutated concurrently — it should be the
  default over `Collections.synchronizedMap(new HashMap<>())` for anything beyond trivial/rare
  contention, because it scales far better under load.
- Use `compute`/`merge`/`computeIfAbsent` for atomic per-key read-modify-write instead of manual
  `get` + check + `put` (which is racy even on a `ConcurrentHashMap` — individual operations are
  atomic, but a sequence of them is not, unless composed via `compute`).
- Avoid if you need `null` values/keys semantically — model "absence" with `Optional` or a
  sentinel object instead, or use a different map with external synchronization if you truly can't
  avoid `null`.
- Avoid running slow or blocking logic inside `compute`/`merge`/`computeIfAbsent` lambdas — they
  execute while holding a per-bin lock, so a slow lambda serializes every other thread trying to
  touch a key in that same bin (and can deadlock if it re-enters the same map).

## 6. Common pitfalls & gotchas

**Racy check-then-act even on a thread-safe map**:
```java
if (!map.containsKey(key)) {
    map.put(key, computeExpensiveValue());   // BUG: two threads can both pass the check
}
// fix: map.computeIfAbsent(key, k -> computeExpensiveValue());  // atomic
```

**Re-entering the same map inside `compute`/`merge`** — calling `map.get(otherKey)` or, worse,
`map.compute(key, ...)` recursively on the *same* key from within the remapping function is
documented as risking deadlock or `IllegalStateException` (the JDK explicitly warns against this)
— keep the lambda pure and limited to the value being computed.

**Assuming `size()` is instantaneous/exact under concurrent mutation** — like any concurrent
collection's `size()`, it's a best-effort estimate the moment it's computed; by the time the
caller reads the returned value, concurrent writers may have already changed the true count — fine
for monitoring/logging, not for logic that must be exact (use `compute`-based atomic counters for
that).

**Iterating and expecting a consistent snapshot** — `ConcurrentHashMap`'s iterators are
**weakly consistent**: they reflect the state at some point during (or after) iteration began,
may or may not reflect later concurrent modifications, and never throw
`ConcurrentModificationException` — different from `HashMap`'s fail-fast iterators. Don't assume
you're iterating a frozen snapshot.

## 7. Interview questions

- [Basic] What replaced segment locking in Java 8's `ConcurrentHashMap`? → Per-bin (bucket)
  concurrency control: CAS for inserting into an empty bin (lock-free), and `synchronized` on just
  that bin's head node when there's a collision — instead of Java 7's fixed 16-segment locking. →
  Follow-up: *Does that mean the whole map is never locked?* Correct — there is no table-wide
  lock for normal reads/writes; at most one bin is briefly locked, and reads never lock at all.
- [Basic] Why doesn't `ConcurrentHashMap` allow `null` keys or values? → Because `get(key) ==
  null` would be ambiguous between "key absent" and "key mapped to null," and in a concurrent
  map you cannot safely disambiguate with a follow-up `containsKey` check — another thread could
  mutate the map between the two calls, so the check-then-act pattern that "fixes" this ambiguity
  in single-threaded `HashMap` code isn't reliable here; the JDK design simply forbids `null` to
  remove the ambiguity at the source. → Follow-up: *What should you use instead of a null
  value?* A sentinel object, `Optional<V>` as the value type, or simply omitting the key to mean
  "absent."
- [Basic] Is `get()` on a `ConcurrentHashMap` blocking? → No — reads are entirely lock-free,
  implemented via volatile/`VarHandle` reads of the table and node fields, so a `get()` never
  waits on a writer's lock. → Follow-up: *Can a get() ever see a partially-constructed Node?* No —
  nodes are published via volatile/CAS writes, giving happens-before visibility: a reader either
  sees the node fully constructed or doesn't see it at all, never a torn write.
- [Intermediate] Walk through what happens when two threads call `put()` with keys that hash to
  the same empty bin at the same time. → Both attempt a CAS on that bin slot from `null` to their
  new `Node`. Exactly one CAS succeeds (atomic hardware-level compare-and-swap); the other thread's
  CAS fails because the slot is no longer `null`, so it retries — on retry it finds the bin
  occupied and falls into the `synchronized`-on-head-node collision path instead, appending
  behind the winner's node. → Follow-up: *What if they hash to different bins?* Both CASes
  succeed independently and concurrently — no contention, no blocking, both threads proceed in
  parallel.
- [Intermediate] How does `size()` avoid being a contention bottleneck under heavy concurrent
  writes? → Instead of one shared counter every writer CASes against (which would serialize
  writers on that single memory location under contention), `ConcurrentHashMap` uses a striped
  array of `CounterCell`s (the same idea as `LongAdder`): when contention is detected, different
  threads' increments land on different cells, so writes mostly don't contend; `size()` sums the
  base counter plus every cell on demand, which is more expensive per call but called far less
  often than `put`. → Follow-up: *Is the size() result exact?* It's a best-effort snapshot —
  concurrent mutations during the summation can make it stale by the time it's returned, which is
  acceptable for a highly concurrent structure.
- [Intermediate] What is `helpTransfer` and why does it exist? → During a resize, other threads
  calling `put`/`get`/`remove` that detect a resize in progress (via a `ForwardingNode` marker)
  don't just wait — they volunteer to migrate a stripe of bins themselves, parallelizing what
  would otherwise be one thread's O(n) resize cost across every thread that happens to touch the
  map during that window, keeping the map responsive under load instead of stalling every caller
  behind one resizing thread. → Follow-up: *How does a reader find the right entry for a bin
  that's already been migrated?* It follows the `ForwardingNode` left in the old table's slot,
  which redirects the lookup into the new table.
- [Intermediate] Are `compute`/`computeIfAbsent`/`merge` atomic on `ConcurrentHashMap`? → Yes,
  per key — the whole read-apply-write sequence for a given key happens while holding that key's
  bin lock (or via the empty-bin CAS path for a first insert), so concurrent callers touching the
  *same* key are serialized correctly with no lost updates and no torn reads; callers touching
  *different* keys (even in the same bin, after appending) proceed independently. → Follow-up:
  *Is `map.get(k) + 1` then `map.put(k, ...)` equivalent to `map.merge(k, 1, Integer::sum)`?* No —
  the two-step version is not atomic (a lost-update race between threads), while `merge` is.
- [Advanced] Contrast the Java 7 infinite-loop-on-resize failure mode of plain `HashMap` (topic 1)
  with how `ConcurrentHashMap` avoids equivalent bugs during its own resize. → The Java 7
  `HashMap` bug came from *unsynchronized* concurrent resizes corrupting a shared linked list via
  interleaved, non-atomic pointer rewrites (no locking at all, since `HashMap` was never meant for
  concurrent use). `ConcurrentHashMap` is explicitly designed for concurrent resize: it uses
  `ForwardingNode` markers to make a bin's "already migrated, look in the new table" state visible
  and safe to read concurrently, `synchronized` locks the specific bin being actively transferred
  so no two threads rewrite the same bin's pointers simultaneously, and `helpTransfer` coordinates
  multiple threads to migrate disjoint stripes rather than racing over the same bins — concurrency
  during resize is a first-class, engineered scenario here, not an accident nobody guarded
  against. → Follow-up: *Could you safely run a plain HashMap resize concurrently by just adding
  `synchronized` around every method?* You'd fix the corruption (that's essentially what
  `Collections.synchronizedMap` does, minus the fine-grained bin-level locking), but you'd lose
  all the fine-grained concurrency `ConcurrentHashMap` specifically engineers for — every
  operation would serialize on one table-wide lock.
- [Advanced] Why must the lambda passed to `compute`/`merge`/`computeIfAbsent` avoid calling back
  into the same map? → The remapping function executes **while the bin's lock is held** (or
  during the CAS-protected empty-bin path); calling another map method on the same key
  re-enters a lock that's not designed to be reentrant-safe in this context and is documented to
  potentially deadlock or throw `IllegalStateException` (detected recursive update); calling into
  a *different* key can also deadlock in edge cases involving resize coordination. Keep the
  function a pure computation over its inputs. → Follow-up: *Is this specific to
  ConcurrentHashMap, or true of compute() on any Map?* Plain `HashMap`'s `compute` doesn't
  document a deadlock risk (no locks involved), but modifying the map structurally from within the
  function is still explicitly disallowed there too (throws `ConcurrentModificationException` via
  `modCount` checks) — the underlying lesson (keep the remapping function side-effect-free
  regarding the map itself) applies to both, for different underlying reasons.

## 8. Exercises

This topic has no dedicated exercise files (the atomicity concepts are demonstrated in
`ConcurrentHashMapDemo`); the module's build-it-yourself and DSA-pattern exercises live in
`notes/06-map/06-dsa-patterns-map.md`.

## 9. Quick recap

- Java 8+ replaced 16-segment locking with per-bin concurrency: CAS on an empty bin (lock-free),
  `synchronized` on just that bin's head node on collision — reads never lock.
- Resize is incremental and cooperative (`helpTransfer`): multiple threads can migrate different
  bin stripes in parallel, using `ForwardingNode` markers so concurrent readers still find moved
  entries correctly.
- `size()` uses `LongAdder`-style striped `CounterCell`s to avoid a single hot-counter contention
  point; it's a best-effort estimate, summed on demand.
- No `null` keys/values — `get()==null` would be ambiguous between "absent" and "mapped to null,"
  and that ambiguity can't be safely resolved with a second call in a concurrent world.
- `compute`/`computeIfAbsent`/`merge` are atomic per key (run under that bin's lock) — use them
  instead of manual get-check-put, but keep the lambda fast and never re-enter the same map from
  inside it.
