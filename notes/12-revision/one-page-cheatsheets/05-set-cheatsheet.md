# Cheat Sheet — 05: Set

One-page pre-interview skim. Full notes: `notes/05-set/`.

## Complexity table

| Operation | HashSet | LinkedHashSet | TreeSet | EnumSet | CopyOnWriteArraySet | ConcurrentSkipListSet |
|---|---|---|---|---|---|---|
| add/remove/contains | O(1) avg, O(log n) worst (treeified) | same | O(log n) always | O(1) bitwise | O(n) | O(log n) expected |
| iteration | O(capacity+size) | O(size) | O(size), sorted | O(universe/64 words) | O(n), weakly consistent | O(n), weakly consistent |
| ordering | none | insertion | sorted | declaration order | insertion | sorted |

## Most-likely-asked facts

1. `HashSet` = `HashMap<E, Object>` in disguise — every element is a key, a shared dummy `PRESENT` value; all HashMap internals (buckets, treeify @ 8, load factor 0.75, resize ×2) apply.
2. `LinkedHashSet` swaps the backing map for `LinkedHashMap` — same speed, deterministic insertion-order iteration via an extra doubly-linked list.
3. `TreeSet` = `TreeMap<E,Object>` (red-black tree) — `NavigableSet` methods `floor`/`ceiling`/`higher`/`lower`/`headSet`/`tailSet`/`subSet` are **live views**, not copies.
4. **`TreeSet` equality is `compareTo()==0`, NOT `equals()`** — an inconsistent comparator silently drops "duplicates" that `equals()` would call distinct.
5. `TreeSet.add(null)` throws NPE (can't compare null); `HashSet`/`LinkedHashSet` permit exactly one null element.
6. `EnumSet` = a bit vector (`RegularEnumSet`: one `long`; `JumboEnumSet`: `long[]`) indexed by `ordinal()` — every op O(1) bitwise, iteration always declaration order, **not thread-safe**.
7. There is **no JDK `ConcurrentHashSet`** — the idiomatic thread-safe hash set is `Collections.newSetFromMap(new ConcurrentHashMap<>())`.
8. `CopyOnWriteArraySet`: O(n) reads/writes, full-array-copy per mutation, weakly-consistent never-throws iterators — small/read-heavy/rarely-mutated only.
9. `ConcurrentSkipListSet`: lock-free (per-node CAS) sorted analogue of `TreeSet` — no thread-safe red-black tree exists in the JDK.
10. `Set.add()`'s boolean return value IS the duplicate-detection trick: `if (!seen.add(x))` = "already present," one hash lookup instead of `contains` + `add` (two lookups).

## Top pitfalls

- **Mutating a hash-relevant field** after an object sits in a `HashSet` — element is now in the wrong bucket; `contains()`/`remove()` silently fail (still physically present, unreachable by value).
- **Same bug in `TreeSet`, one level worse** — mutating a comparator-relevant field breaks the tree's structural invariant; even `iterator()` order can become inconsistent.
- **Mutating a set while iterating it** → `ConcurrentModificationException`; iterate a copy or the *other* collection for set algebra (`a.retainAll(b)` instead of a manual loop).
- **Not checking `n-1` before expanding a run** in longest-consecutive-sequence — without the guard it degrades from O(n) to O(n²).
- **`HashSet<SomeEnum>` out of habit** instead of `EnumSet` — works, but wastes memory/CPU for something a single `long` bitmask handles instantly.
- **Assuming `EnumSet` is thread-safe** — it has zero synchronization.

## When to use / not use

- `HashSet` default; `LinkedHashSet` only when callers need insertion order; `TreeSet` only for sorted iteration/range queries (`floor`/`ceiling`).
- `EnumSet` by default for any enum-flag combination.
- `newSetFromMap(ConcurrentHashMap)` for "just need a thread-safe set"; `ConcurrentSkipListSet` only when concurrent code also needs sorted/range queries.

## DSA patterns (module 05 §3)

| Pattern | Time/Space | Idea |
|---|---|---|
| Duplicate detection | O(n)/O(n) | `Set.add()` return value, one lookup |
| Union/intersection/difference | O(n+m) | `addAll`/`retainAll`/`removeAll` — work on a copy |
| Longest consecutive sequence | O(n)/O(n) | HashSet + only expand a run when `n-1` absent |
| Group anagrams | O(n·k log k) | canonical key (sorted chars / freq signature) → bucket map |
