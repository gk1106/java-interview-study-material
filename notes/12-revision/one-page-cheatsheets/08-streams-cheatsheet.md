# Cheat Sheet — 08: Streams

One-page pre-interview skim. Full notes: `notes/08-streams/`.

## Complexity table

| Operation | Time | Space | Notes |
|---|---|---|---|
| `map`/`filter`/`flatMap`/`peek` | O(1)/element | O(1) | stateless, single-pass, parallelize trivially |
| `distinct()` | O(n) | O(n) | hash-based dedup, must buffer |
| `sorted()` | O(n log n) | O(n) | must see whole stream before emitting anything |
| `limit(n)` | O(min(n, upstream)) | O(1) | short-circuiting |
| `reduce` | O(n) | O(1) extra | immutable fold each step |
| `collect(toList/toSet)` | O(n) amortized | O(n) | one mutable accumulator |
| `collect(toMap)` | O(n) avg | O(n) | throws on dup key w/o merge fn |
| `groupingBy`/`partitioningBy` | O(n) | O(n) | downstream collector runs per group |
| primitive stream ops (`sum`/`average`/`summaryStatistics`) | O(n) | O(1) | no boxing |
| parallel: split/merge | O(log n) levels | — | helps only for large, CPU-bound, cheaply-splittable (array-backed) sources |

## Most-likely-asked facts

1. Pipeline = source → intermediate ops (lazy, return a new stream) → terminal op (the ONLY thing that runs anything). No terminal op = silent no-op.
2. Processing is **depth-first per element** through every stage, not stage-by-stage over the whole collection — this is what enables short-circuiting.
3. A stream can be consumed exactly once — reuse throws `IllegalStateException`.
4. `map`/`filter`/`flatMap`/`peek` are stateless; `distinct()`/`sorted()` are stateful (must buffer the whole stream).
5. `flatMap` maps each element to a sub-stream and flattens all sub-streams into one output — use whenever the mapping produces a collection/stream per input.
6. `Collectors.toMap` throws `IllegalStateException` on a duplicate key unless you supply a 3rd merge-function argument.
7. `reduce` folds into a **new immutable** value each step; `collect` mutates **one shared accumulator** — prefer `collect` for building collections (avoids an O(n²) immutable-rebuild trap).
8. `orElse(x)` **always evaluates `x` eagerly** (even when present); `orElseGet(supplier)` is lazy — use `orElseGet` for expensive defaults.
9. `Optional.of(null)` throws NPE immediately; `Optional.ofNullable(null)` is the null-safe constructor.
10. `IntStream.range` excludes the upper bound; `rangeClosed` includes it — classic off-by-one.
11. Parallel streams fork via `Spliterator`, run on the shared JVM-wide `ForkJoinPool.commonPool()`, then join — correctness requires an associative combiner and zero unsynchronized shared mutable state.
12. `forEach` on a parallel stream doesn't preserve encounter order; `forEachOrdered` restores it at the cost of re-synchronization overhead.

## Top pitfalls

- **Forgetting the terminal op** — pipeline built, never executes.
- **Reusing a consumed stream** → `IllegalStateException`.
- **`sorted()` before `limit()`** for top-k on a huge stream — sorts everything instead of using a bounded heap.
- **`map` where `flatMap` was needed** (mapping fn itself returns `Optional`/`Stream`) → nested wrapper type, awkward double-unwrap.
- **Racy shared mutable accumulator in `parallelStream().forEach(list::add)`** — non-thread-safe collection mutated by multiple threads; use `collect()` instead.
- **`.parallel()` on a small collection** — coordination overhead makes it *slower* than sequential.
- **Blocking I/O inside a parallel stream lambda** — starves the shared common pool for *every other* parallel stream/default-executor `CompletableFuture` in the process.
- **`BigDecimal.equals()` instead of `compareTo()`** for money comparisons in a stream pipeline — `equals` is scale-sensitive (`8000` ≠ `8000.00`).
- **Converting money to `double`** for stream arithmetic — introduces rounding error; stay in `BigDecimal` (an *average* via `averagingDouble` is the one accepted exception).

## When to use / not use

- Use parallel streams for large, CPU-bound, cheaply-splittable (array/ArrayList-backed) workloads only.
- Use `Optional` as a **return type**; avoid it as a field or parameter type.
- Use `flatMap` whenever a mapping function itself returns a stream/Optional; `map` otherwise.
