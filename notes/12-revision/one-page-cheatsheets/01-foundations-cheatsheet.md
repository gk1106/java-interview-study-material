# Cheat Sheet — 01: Java Foundations for DSA

One-page pre-interview skim. Full notes: `notes/01-java-foundations-for-dsa/`.

## Big-O quick table

| Class | Example |
|---|---|
| O(1) | array index, HashMap get (avg) |
| O(log n) | binary search, TreeMap ops |
| O(n) | linear scan, ArrayList.add (amortized) |
| O(n log n) | Arrays.sort(Object[]) (Timsort), merge sort |
| O(n²) | nested loops, insertion sort |
| O(2ⁿ) | naive recursive Fibonacci, all subsets |
| O(n!) | all permutations |

**Amortized O(1) append** works because capacity *doubles* (or ×1.5): total copy work across n
inserts sums to a geometric series < 2n → O(n) total → O(1) per insert. A *fixed* growth
increment degrades this to amortized O(n) per insert (O(n²) total).

## Most-likely-asked facts

1. Big-O drops constants/lower-order terms; it describes growth trend, not wall-clock time.
2. A single call that triggers a resize is genuinely O(n) for *that* call — amortized ≠ worst-case per call.
3. Generics use **type erasure** — no runtime cost, but no `new T[]`, no `instanceof T`, no overload-on-erased-signature.
4. **PECS**: `? extends T` = producer (read-only, can't add except null); `? super T` = consumer (write-only-ish, reads give Object).
5. `equals()`/`hashCode()` contract: `a.equals(b)==true` ⟹ `a.hashCode()==b.hashCode()` (reverse not required — collisions are legal).
6. Forgetting `hashCode()` after overriding `equals()` is the #1 collections bug — objects "disappear" from `HashSet`/`HashMap`.
7. `Comparable` = one natural order, inside the class (`compareTo`); `Comparator` = any number of orderings, outside the class (`compare`).
8. `Collections.sort`/`Arrays.sort(Object[])` = **Timsort** — O(n log n) worst case, *stable*, exploits sorted runs.
9. Fail-fast iterators (ArrayList, HashMap) track `modCount` and throw CME on unexpected structural change — best-effort, not a concurrency guarantee.
10. `HashMap` spreads hash bits (`h ^ (h>>>16)`) and treeifies a bucket at 8 entries (table ≥ 64) → worst case O(log n) instead of O(n).

## Top pitfalls

- **Amortized-as-worst-case**: pre-size `new ArrayList<>(n)` for latency-sensitive loops to avoid any resize spike.
- **String += in a loop** → O(n²); use `StringBuilder` for O(n).
- **`? extends T` list**: compiles but `.add()` fails except `null` — read-only by design.
- **Raw type + generic mixing**: `List raw = strings; raw.add(42);` compiles with a warning, blows up with `ClassCastException` later, at the *read* site not the write site.
- **Mutating a field used in `hashCode()`** after the object is a `HashSet`/`HashMap` key — the entry becomes unreachable (still present, wrong bucket).
- **`compareTo` inconsistent with `equals`** — a `TreeSet`/`TreeMap` silently drops "different" (by equals) elements if `compareTo` says 0.
- **Comparator overflow**: `(a,b) -> a - b` overflows for extreme ints — use `Integer.compare`.
- **Removing during for-each** via the collection's own `remove()` → `ConcurrentModificationException`; use `Iterator.remove()`/`removeIf`.

## When to use / not use

- Use `Comparable` for one obvious natural order; `Comparator` for multiple/situational orderings or third-party classes you can't edit.
- Use bounded wildcards in *parameter* positions (max API flexibility); never in return types.
- Never mix raw types with parameterized types in new code.
