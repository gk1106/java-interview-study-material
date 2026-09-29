# Cheat Sheet — 06: Map

One-page pre-interview skim. Full notes: `notes/06-map/`.

## Complexity table

| Operation | HashMap | LinkedHashMap | TreeMap | ConcurrentHashMap |
|---|---|---|---|---|
| get/put (average) | O(1) | O(1) | O(log n) guaranteed | O(1) avg, lock-free reads |
| get/put (worst case) | O(log n) treeified / O(n) untreeified | same as HashMap | O(log n) always | O(1) avg |
| iteration | O(capacity+size) | O(size), walks linked list | O(n), sorted | O(capacity+size), weakly consistent |
| resize | O(n) total, amortized O(1)/put | inherits HashMap's | O(log n) rebalance per put/remove | incremental, cooperative (`helpTransfer`) |

## Most-likely-asked facts

1. `index = (capacity-1) & spread(hashCode())` — capacity always a power of two, so indexing is a cheap bitwise AND, not modulo.
2. Spreading: `h ^ (h >>> 16)` folds high hash bits into low bits before indexing (only low bits matter when capacity is small).
3. **Load factor 0.75** triggers a doubling resize; Java 8 splits each old bucket into "low"/"high" lists in one pass (no full rehash).
4. A bucket **treeifies** into a red-black tree at **8 entries** (if table capacity ≥ 64) for O(log n) worst case, and **untreeifies** back to a list at **6 entries**.
5. Iteration order is an unspecified artifact of bucket layout — never rely on it; `HashMap` is not thread-safe.
6. The **Java 7 concurrent-resize infinite loop**: old resize() prepended nodes (reversing chain order); two threads resizing concurrently could create a cycle → `get()` spins forever. Java 8's split-resize no longer produces that exact bug, but a plain `HashMap` is still never safe to share across threads.
7. `LinkedHashMap` needs the **3-arg constructor** (`16, 0.75f, true`) for access-order/LRU — the no-arg ctor defaults to insertion order, silently breaking an LRU built on it.
8. `removeEldestEntry(eldest)` override + `accessOrder=true` = a correct O(1) LRU cache in a few lines.
9. `TreeMap` uses `compareTo`/`Comparator`, **not** `equals()`, for key identity — `put(null,v)` throws NPE under natural ordering (unlike HashMap's one allowed null key).
10. `ConcurrentHashMap` (Java 8+): CAS on an empty bin (lock-free), `synchronized` on just that bin's head node on collision; **no null keys/values** (ambiguous "absent" vs "mapped to null" under concurrency); `size()` uses striped `CounterCell`s, a best-effort estimate.
11. `WeakHashMap`: keys via `WeakReference`, cleanup lazy (piggy-backed on next map operation after GC) — not a precise TTL. `EnumMap`: array-backed by ordinal, O(1) worst case, no hashing possible.

## Top pitfalls

- **Mutable keys**: mutating a field used in `hashCode()`/`equals()` after `put()` strands the entry in the old bucket — `get()` on an "equal" key returns null.
- **Racy check-then-act even on `ConcurrentHashMap`**: `if(!map.containsKey(k)) map.put(k,v)` — use `computeIfAbsent` instead (atomic).
- **Re-entering the same map inside `compute`/`merge`** — JDK-documented deadlock/`IllegalStateException` risk; keep the lambda pure.
- **Iterating and modifying at the same time**: `for (k : map.keySet()) map.remove(k)` → CME; use `keySet().removeIf(...)`.
- **`get()` in access-order `LinkedHashMap` counts as structural mutation** — iterating while calling `get()` throws CME.
- **Off-by-one on prefix-sum seed**: forgetting `map.put(0,1)` before scanning undercounts subarrays starting at index 0 for "subarray sum = K".
- **Autoboxed `Integer` keys compared with `==`** instead of `.equals()` — breaks silently outside the `[-128,127]` cache range.

## When to use / not use

- `HashMap` default; `LinkedHashMap` when order (or LRU) matters; `TreeMap` only for sorted iteration/range/nearest-key queries.
- `ConcurrentHashMap` default for any shared map; never `Hashtable`/`Collections.synchronizedMap` under real contention.
- `TreeMap.floorEntry`/`floorKey` is the right tool for "nearest value at or before X" — plain HashMap can't do this.

## DSA patterns (module 06 §6)

| Pattern | Time/Space | Idea |
|---|---|---|
| Frequency counting | O(n) | one pass, `merge`/`getOrDefault` |
| Two-sum | O(n) | complement lookup — check seen, THEN record, in that order |
| Subarray sum = K | O(n) | prefix-sum counts, seed `{0:1}`, works with negatives |
| Top-K frequent | O(n log K) | frequency map + size-K heap |
| Longest substring ≤ K distinct | O(n) | sliding window, map bounded to K+1 distinct |
| Time-based KV store | O(log n) get | `TreeMap` per key, `floorEntry` |
