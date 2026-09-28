# WeakHashMap, IdentityHashMap, EnumMap

## 1. What it is

Three specialized `Map` implementations, each trading general-purpose behavior for one specific
property: `WeakHashMap` lets entries be garbage-collected when their keys are otherwise
unreachable (good for caches that must not leak memory); `IdentityHashMap` uses reference
identity (`==`) instead of `equals()`/`hashCode()` (good for identity-sensitive bookkeeping, e.g.
object-graph traversal); `EnumMap` is a compact, ordinal-array-backed map exclusively for `enum`
keys (fastest possible map when your key space is a fixed enum).

## 2. How it works internally

### WeakHashMap — weak keys + `ReferenceQueue`-driven cleanup

- Each key is wrapped in a `WeakReference<K>` (technically the map's internal `Entry` *extends*
  `WeakReference`) instead of being held by a strong reference. A weak reference does **not**
  prevent the garbage collector from reclaiming the referent — if nothing else in the application
  holds a strong reference to a key, the GC is free to collect it even while it's still "in" the
  map.
- Every `WeakReference` is registered against a shared `ReferenceQueue<K>`. When the GC clears a
  weak reference (because its key became unreachable elsewhere), the JVM enqueues that reference
  onto the queue.
- `WeakHashMap` does **not** proactively scan for dead entries. Instead, on essentially every
  public operation (`get`, `put`, `size`, etc.), it first calls a private `expungeStaleEntries()`
  that drains the `ReferenceQueue`, removing the corresponding (now-keyless) entries from the
  bucket table. So cleanup is lazy and piggybacks on normal usage — a `WeakHashMap` that's never
  touched again after its keys die will hold onto the empty `Entry` objects (and the stale
  bucket-array slots) until the next operation runs the expunge pass.
- Values are held **strongly** by default — only the key reference is weak. (A value can
  transitively keep its own key artificially alive if the value holds a reference back to the
  key, defeating the purpose — a common mistake.)

**Use case**: caches keyed by objects whose *lifecycle* you don't own or want to extend — e.g. a
per-request or per-session metadata cache keyed by the request/session object itself, where you
want the cache entry to disappear automatically once the request/session object is no longer
referenced anywhere else, without needing an explicit eviction call. (In practice, dedicated
caching libraries — Caffeine, Guava — are usually a better production choice since they offer
weak/soft *values* too, size-based eviction, and expiry; `WeakHashMap` is the lean JDK-only
building block.)

### ASCII diagram — key becomes unreachable, entry gets expunged lazily

```
WeakHashMap<SessionKey, Metadata> cache = new WeakHashMap<>();
SessionKey s = new SessionKey("abc");
cache.put(s, new Metadata(...));        // key held via WeakReference internally

s = null;                                // no more strong references to the SessionKey anywhere
// ... GC runs at some point, clears the WeakReference, enqueues it on the ReferenceQueue ...

cache.size();                            // triggers expungeStaleEntries() first -> entry removed
                                          // -> size() reflects the map WITHOUT the dead entry
```

### IdentityHashMap — `==` instead of `equals()`, open addressing

- Uses `System.identityHashCode(key)` (or the object's actual identity hash) instead of
  `key.hashCode()`, and compares keys with `==` instead of `.equals()` for both lookup and
  collision resolution. Two keys that are `.equals()` but not the same object are treated as
  **distinct** keys — the opposite trade-off of every other `Map` in the JDK.
- Backed by a **single flat `Object[] table`** using **open addressing** (linear probing), not
  separate chaining — keys and values are interleaved in the same array (`table[2*i]` = key,
  `table[2*i+1]` = value) rather than using `Node` objects with `next` pointers. On a collision
  (slot occupied by a *different* object, per `==`), it linearly probes to the next slot instead
  of chaining a linked list, avoiding a per-entry object allocation.
- No treeification (open addressing doesn't have "buckets" that grow into chains the same way).

**Use case**: object-graph algorithms where you must track "have I visited this exact object
instance before" regardless of its `equals()` definition — e.g. a deep-clone/serialization
visited-set, cycle detection in object graphs, or proxy/interceptor frameworks keying off exact
instances. Also useful for defending against classes with expensive or buggy `equals()`/
`hashCode()` when only identity matters.

### ASCII diagram — open addressing with linear probing

```
table (interleaved key/value slots), capacity 8:
index: 0    2    4    6    8    10   12   14
       [K1] [V1] [ ]  [ ]  [K2] [V2] [ ]  [ ]

put(K3, V3) where identityHashCode(K3) maps to the same slot as K1 (index 0):
  slot 0 occupied by a DIFFERENT object (K1 != K3 by reference) -> probe next slot (index 2, 4...)
  first free slot found -> K3/V3 stored there (linear probing, no chaining, no extra Node objects)
```

### EnumMap — array-backed by ordinal

- `EnumMap<K extends Enum<K>, V>` is backed internally by a plain `Object[]` sized to
  `keyUniverse.length` (the total number of constants in the enum, obtained once via
  reflection/`Class.getEnumConstants()`), indexed directly by `key.ordinal()`. No hashing at all.
- This makes every operation a direct array index — `get`/`put` are O(1) with an extremely small,
  branch-free constant factor, and iteration is automatically in **enum declaration (ordinal)
  order**, not an arbitrary hash-bucket order.
- Extremely memory-compact compared to a `HashMap<EnumKey, V>` — no `Node` objects, no hash
  buckets, no load-factor headroom; just one array sized exactly to the enum's constant count.

**Use case**: any map keyed by a fixed, known `enum` — e.g. `EnumMap<DayOfWeek, Schedule>`,
`EnumMap<TransactionStatus, Handler>` in a banking workflow — always prefer this over
`HashMap<SomeEnum, V>` when the key type is an enum; there's no downside.

## 3. Complexity

| Map | get/put | Notes |
|-----|---------|-------|
| `WeakHashMap` | O(1) average | same bucket-array/chaining mechanics as `HashMap`, plus O(stale entries) amortized cleanup cost on each call |
| `IdentityHashMap` | O(1) average | open addressing; degrades toward O(n) under heavy identity-hash collisions (rare in practice) |
| `EnumMap` | O(1) worst case | direct array index by ordinal, no hashing, no collisions possible |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/map/examples/WeakIdentityEnumMapDemo.java` —
  demonstrates a `WeakHashMap` entry disappearing after `System.gc()` + a nudge operation,
  `IdentityHashMap` treating two `.equals()`-but-distinct `String` objects (built via `new
  String(...)` to defeat interning) as separate keys where a plain `HashMap` would collapse them,
  and an `EnumMap` iterating in declaration order.

```java
IdentityHashMap<String, Integer> idMap = new IdentityHashMap<>();
String a = new String("key");
String b = new String("key");           // .equals(a) is true, but a != b
idMap.put(a, 1);
idMap.put(b, 2);
System.out.println(idMap.size());        // 2 -- treated as distinct keys (== identity)

HashMap<String, Integer> normal = new HashMap<>();
normal.put(a, 1);
normal.put(b, 2);
System.out.println(normal.size());       // 1 -- collapsed via equals()
```
Expected console output:
```
2
1
```

## 5. When to use / when NOT to use

- `WeakHashMap`: use for lightweight, GC-aware caches keyed by objects you don't control the
  lifecycle of; avoid for general-purpose maps (surprising, non-deterministic entry disappearance
  is a footgun if you didn't intend it) and avoid if values hold strong references back to their
  keys (defeats the weak-key purpose entirely).
- `IdentityHashMap`: use narrowly for identity-based bookkeeping (visited-sets, serialization
  cycle detection); avoid as a general map — violating the `equals()` contract expectation
  surprises almost every caller who doesn't know it's identity-based.
- `EnumMap`: always prefer over `HashMap<EnumType, V>` for enum-keyed maps — faster, more
  compact, deterministic ordinal-order iteration, no reason not to.

## 6. Common pitfalls & gotchas

**Expecting `WeakHashMap` entries to disappear immediately** — cleanup only happens lazily, piggy-
backed on the next map operation after the GC has actually collected the key; a `WeakHashMap` you
stop touching after keys die will keep stale `Entry` objects around until something calls a method
on the map again. Don't rely on it as a precise, timely eviction mechanism — it's advisory memory
pressure relief, not a TTL cache.

**A value holding a strong reference back to its own key** silently defeats `WeakHashMap`'s whole
point — the value keeps the key reachable, so the weak reference never clears:
```java
class CacheEntry { SessionKey key; Data data; }   // holds the key strongly
WeakHashMap<SessionKey, CacheEntry> cache = ...;   // key never becomes unreachable while its
                                                    // own value object is still referenced
```

**Using `IdentityHashMap` by accident** where `equals()`-based semantics were expected (e.g.
passing it somewhere a `Map<String, V>` is generically expected) silently produces "duplicate"
entries for logically-equal keys — always be explicit about choosing it, never a drop-in
substitute for `HashMap`.

**`EnumMap` requires a non-null enum type at construction** (or an existing `EnumMap`/enum-keyed
map to copy from) since it needs to know the key universe up front — you can't create an empty,
untyped `EnumMap<MyEnum, V>` via a no-arg constructor the way you can with `HashMap`; you must
pass `MyEnum.class` or copy from another `EnumMap`.

## 7. Interview questions

- [Basic] What makes `WeakHashMap` different from a regular `HashMap`? → Its keys are held via
  `WeakReference`, so the garbage collector can reclaim a key (and its entry gets lazily removed)
  once nothing else in the application strongly references that key — regular `HashMap` keys are
  held strongly and never disappear on their own. → Follow-up: *Are values also weak by default?*
  No, only keys — values are held strongly, which means a value referencing its own key back can
  defeat the mechanism.
- [Basic] What does `IdentityHashMap` use for key comparison instead of `equals()`/`hashCode()`?
  → Reference identity: `==` for equality and `System.identityHashCode()` (roughly) for hashing,
  so two distinct objects that are `.equals()` are still treated as different keys. → Follow-up:
  *Name a real use case.* Cycle detection / visited-tracking during deep object-graph traversal
  (e.g. serialization), where you specifically want "is this the same object instance," not
  "is this an equal-value object."
- [Basic] Why is `EnumMap` faster than `HashMap<SomeEnum, V>`? → It's backed by a plain array
  indexed directly by the enum constant's `ordinal()` — no hashing, no bucket lookup, no
  collision handling at all; every operation is a direct O(1) array access with minimal constant
  factor, and iteration order matches enum declaration order for free. → Follow-up: *Is there any
  downside to always using EnumMap for enum keys?* Essentially no downside for correctness or
  performance; the only requirement is knowing the enum type up front (constructor needs the
  `Class` or an existing map to copy from).
- [Intermediate] Explain how `WeakHashMap` cleans up entries whose keys have been garbage
  collected. → Every key is wrapped in a `WeakReference` registered against a shared
  `ReferenceQueue`; when the GC clears a weak reference because its referent (the key) became
  unreachable, the JVM enqueues that reference. `WeakHashMap` doesn't poll this queue on a
  background thread — instead, essentially every public method call first drains the queue and
  removes the corresponding entries from the bucket table, so cleanup is lazy and piggybacks on
  normal usage rather than running proactively. → Follow-up: *What happens if you never call any
  method on the map again after the keys die?* The stale entries simply remain in the table
  indefinitely — cleanup never runs without a trigger call.
- [Intermediate] How does `IdentityHashMap`'s internal storage differ structurally from
  `HashMap`'s? → `HashMap` uses separate chaining: an array of bucket "heads," each potentially a
  linked list (or tree) of `Node` objects holding key/value/next. `IdentityHashMap` uses open
  addressing with linear probing over one flat `Object[]` where keys and values are interleaved
  directly in array slots (no `Node` wrapper objects, no `next` pointers) — a collision moves to
  the next slot in the array instead of extending a chain. → Follow-up: *Why does open addressing
  suit IdentityHashMap specifically?* Identity hash codes are already well-spread (effectively
  memory-address-derived), so pathological clustering is less of a concern than with
  user-controlled `hashCode()` implementations, and avoiding per-entry `Node` allocations is a
  meaningful memory/speed win for a structure often used in tight object-graph traversal loops.
- [Intermediate] Give a concrete banking-adjacent example of when `WeakHashMap` is the right
  tool. → A per-transaction-context metadata side-table: `WeakHashMap<TransactionContext,
  AuditMetadata>` where `TransactionContext` objects are created per request and would otherwise
  need explicit cleanup when the request finishes — using a `WeakHashMap` means the metadata
  entry disappears automatically once the `TransactionContext` itself is no longer referenced
  anywhere (request completed, garbage collected), with no explicit `remove()` call needed
  anywhere in the request lifecycle code. → Follow-up: *Why not just use a HashMap and remove()
  explicitly at the end of the request?* You could, and in most production code you should
  (explicit lifecycle management is more predictable than GC timing) — `WeakHashMap` mainly helps
  when there's no single clear "end of lifecycle" hook to call `remove()` from, or as a safety net
  against forgetting to.
- [Advanced] Why can't you reliably use `WeakHashMap` as a precise, timely cache-expiration
  mechanism? → Garbage collection timing is not deterministic or immediately triggered by
  reachability changes — the JVM decides when (and whether, under sufficient headroom) to run a
  collection cycle, and even after collection, `WeakHashMap` only *notices* via its lazy expunge
  pass on the next method call; there's no guaranteed bound on how long a "dead" entry lingers
  before actually being removed from the visible map, and `size()`/iteration can both
  transiently show stale entries. For an actual TTL or precise-eviction cache, use an explicit
  eviction policy (a `LinkedHashMap`-based LRU, a scheduled sweep, or a caching library with
  real expiry semantics). → Follow-up: *So what is WeakHashMap actually good for?* Memory-
  pressure-driven cleanup as a safety net, not correctness-critical or latency-sensitive eviction
  — it prevents a specific class of memory leak, it doesn't implement a cache policy.
- [Advanced] Could `EnumMap`'s array-backed approach be reproduced manually with a `HashMap`, and
  what would you lose by doing so? → You could use `HashMap<SomeEnum, V>` and get functionally
  correct results (since enums have well-defined `hashCode()`/`equals()`), but you'd lose:
  guaranteed O(1) *worst case* (vs. average) access since there's no hashing/collision path to
  degrade, the compact array-only memory layout (no `Node` object overhead, no load-factor spare
  capacity), and free deterministic ordinal-order iteration (a `HashMap` iterates in unspecified
  bucket order) — `EnumMap` is strictly better with zero trade-off for enum-keyed maps, which is
  why it's always the right choice when the key type is a fixed enum. → Follow-up: *Does EnumMap
  support null keys?* No — like `TreeMap`, it throws `NullPointerException` on a `null` key,
  since there's no ordinal to index by.

## 8. Exercises

This topic has no dedicated exercise files — its concepts are demonstrated directly in
`WeakIdentityEnumMapDemo`. The module's DSA-pattern and build-it-yourself exercises are in
`notes/06-map/06-dsa-patterns-map.md`.

## 9. Quick recap

- `WeakHashMap`: keys held via `WeakReference`; entries for GC'd keys are removed lazily, drained
  from a `ReferenceQueue` on the next map operation — good for GC-friendly caches, not a precise
  TTL mechanism.
- `IdentityHashMap`: `==` and `System.identityHashCode()` instead of `equals()`/`hashCode()`,
  backed by open addressing (linear probing) over one flat interleaved array — good for
  identity-based bookkeeping like visited-sets.
- `EnumMap`: array-backed, indexed directly by `ordinal()` — O(1) worst case, no hashing, no
  collisions, free ordinal-order iteration; always preferred over `HashMap` for enum keys.
- A value that holds a strong reference back to its own key silently defeats `WeakHashMap`'s
  purpose — the entry never becomes eligible for cleanup.
- None of these three are general-purpose drop-in `HashMap` replacements — each is a deliberate,
  narrow trade-off; choose them only when their specific property (weak keys, identity semantics,
  enum-key compactness) is actually what you need.
