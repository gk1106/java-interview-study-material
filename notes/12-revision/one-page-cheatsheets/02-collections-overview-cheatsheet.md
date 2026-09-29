# Cheat Sheet — 02: Collections Framework Overview

One-page pre-interview skim. Full notes: `notes/02-collections-framework-overview/`.

## Hierarchy (memorize the shape)

```
Iterable -> Collection -> {List, Set, Queue}   (Deque extends Queue)
Map<K,V>  is a SEPARATE root — NOT a Collection (exposes keySet()/values()/entrySet() views)
```

- `LinkedList` uniquely implements both `List` and `Deque`.
- `PriorityQueue` is a `Queue` but **not** a `Deque`; its iterator order is heap-array order, NOT sorted — only `poll()` gives sorted order.
- `SortedSet`/`NavigableSet` (and `SortedMap`/`NavigableMap`) are the only places ordering is part of the *interface contract*, not just an impl detail like `LinkedHashSet`.

## Decision table (the interview favorite)

| Need | Pick | Why |
|---|---|---|
| Index access, dupes ok | `ArrayList` | O(1) get, amortized O(1) append |
| Both-ends inserts/removes | `ArrayDeque` | circular array, amortized O(1) both ends |
| Unique + insertion order | `LinkedHashSet` | HashSet O(1) + linked list for order |
| Unique, no order needed | `HashSet` | pure hashing, no bookkeeping |
| Unique + sorted + range queries | `TreeSet` | Red-Black tree, O(log n) |
| Key→value, no order | `HashMap` | O(1) average |
| Key→value, order matters | `LinkedHashMap` | HashMap + linked list; `accessOrder=true` → LRU almost free |
| Key→value, sorted/range | `TreeMap` | Red-Black tree |
| Priority retrieval | `PriorityQueue` | binary heap, O(log n) |
| Thread-safe map, high concurrency | `ConcurrentHashMap` | per-bin lock + CAS |
| Thread-safe list, read-heavy | `CopyOnWriteArrayList` | snapshot iterators, O(n) writes |
| Enum-keyed, tiny footprint | `EnumSet`/`EnumMap` | bit vector / ordinal-indexed array |

## Complexity snapshot

| Collection | contains/get | insert | ordered iteration |
|---|---|---|---|
| ArrayList | O(1) index / O(n) value | O(1) amortized append | insertion |
| HashSet/HashMap | O(1) avg | O(1) avg | none |
| LinkedHashSet/Map | O(1) avg | O(1) avg | insertion (or access) |
| TreeSet/TreeMap | O(log n) | O(log n) | sorted |
| PriorityQueue | O(n) contains | O(log n) offer | none (heap order) |
| CopyOnWriteArrayList | O(1) index / O(n) value | O(n) (full copy) | insertion, snapshot |

## Most-likely-asked facts

1. Why isn't `Map` a `Collection`? — no single unambiguous "element" (key vs value vs pair); exposed via 3 collection views instead.
2. `Collections.unmodifiableList` = a **view** (mutations to the backing list are still visible through it); `List.of`/`List.copyOf` = **truly immutable**, no backing collection anyone can mutate.
3. `List.of`/`Set.of`/`Map.of` are null-hostile (NPE at creation) and `Set.of`/`Map.of` reject duplicate elements/keys (`IllegalArgumentException` at creation) — unlike `HashSet`/`ArrayList`.
4. `Arrays.asList` = fixed-**size**, mutable elements (`set()` works, `add()`/`remove()` throw), live view over the array. `List.of` = fixed size AND fixed content.
5. Iteration order of `Set.of`/`Map.of` is intentionally unspecified/randomized per JVM run — never depend on it.
6. `ConcurrentHashMap` forbids null keys/values (ambiguous "absent" vs "mapped to null" under concurrency); plain `HashMap` allows one null key, many null values.
7. Legacy `Vector`/`Stack`/`Hashtable` synchronize every method call (throughput hit even single-threaded) and don't make compound (check-then-act) operations safe — avoid in new code.

## Top pitfalls

- **"Leaked mutable reference"**: storing `Collections.unmodifiableList(callerList)` as a field — caller's later mutation still shows through. Fix: `List.copyOf(callerList)` for a true defensive snapshot.
- **Treating a Map as Iterable directly** — compile error; iterate `keySet()`/`values()`/`entrySet()`.
- **Assuming `PriorityQueue`'s iterator gives sorted order** — it doesn't; drain with `poll()`.
- **`Arrays.asList(1,2,3).add(4)`** throws but `.set(0,99)` works — fixed size ≠ fixed content.
- **Reaching for `TreeMap`/`ConcurrentHashMap`/`CopyOnWriteArrayList` "just in case"** without needing sorted/thread-safe/read-heavy semantics — pays O(log n) or lock/copy overhead for nothing.

## When to use / not use

- Program to the interface (`List`, `Map`, `Collection`) in signatures, never the concrete class.
- Default to `ArrayList`/`HashSet`/`HashMap`/`ArrayDeque`; upgrade only on a concrete requirement for order, sorting, or thread-safety.
