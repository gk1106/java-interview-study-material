# Parallel streams — when they help and when they hurt

## 1. What it is

`.parallelStream()` (or `.stream().parallel()`) splits a stream's source into chunks and processes
them concurrently across multiple threads from the shared JVM-wide `ForkJoinPool.commonPool()`,
then merges the partial results back together. It can speed up large, CPU-bound, stateless
computations — but it introduces real correctness hazards (shared mutable state, ordering) and
overhead that can make it *slower* than sequential for small or I/O-bound workloads.

## 2. How it works internally

### Fork/join under the hood

A parallel stream uses the **fork/join framework**: the source is recursively split (via its
`Spliterator`) into smaller chunks until each chunk is small enough to process directly; each leaf
chunk is processed on a worker thread; partial results are combined pairwise back up the
recursion tree (using the `Collector`'s `combiner`, or the reduction's `combiner` function). All
of this happens on `ForkJoinPool.commonPool()` — a single pool **shared by every parallel stream
in the entire JVM**, sized by default to `Runtime.getRuntime().availableProcessors() - 1` worker
threads.

```
data.parallelStream().mapToLong(ParallelStreamsDemo::costlyTransform).sum();

           split source into chunks (recursive, via Spliterator)
                      /              |              \
              chunk1(sum)      chunk2(sum)      chunk3(sum)     <- each on a commonPool thread
                      \              |              /
                       combine partial sums (associative reduction)
                                     |
                              final total
```

### Why "shared with every parallel stream in the JVM" matters

Because the common pool is shared, one long-running parallel stream (or a badly-behaved blocking
task submitted to it, e.g. via `CompletableFuture` without a custom executor) can starve *every
other* concurrently-running parallel stream in the same JVM of worker threads — this is a
frequently-missed production gotcha: parallel streams do not each get their own dedicated thread
pool by default.

### The core promise: correctness requires the same rules as `reduce`/`collect`

Parallel execution only produces a **correct** result if:
- the accumulator/combiner functions are **associative** (grouping order must not change the
  result), and
- there is **no shared mutable state** being written from multiple threads without synchronization
  (or, better, no shared mutable state at all — the whole point of `collect`'s per-thread
  accumulator + combiner design is to avoid needing any).

`ParallelStreamsDemo` verifies exactly this: `sumSeq == sumPar` is asserted (correctness), while
timings are printed for interest only and never asserted on (wall-clock speed depends on machine,
core count, and JIT warm-up — not something a portable unit test should assert on).

### Pitfall 1 — shared mutable accumulator, demonstrated

```java
List<Integer> unsafeAccumulator = new ArrayList<>();
data.stream().limit(50_000).parallel().forEach(unsafeAccumulator::add); // racy structural mutation
// size is often < 50000, and can even throw ArrayIndexOutOfBoundsException/other corruption —
// ArrayList is not thread-safe, and forEach on a parallel stream calls the lambda from MULTIPLE
// threads with no synchronization between them
```
Fix options, both shown in the demo:
```java
// (a) let the framework do the accumulation correctly via a proper collector
List<Integer> safeResult = data.stream().limit(50_000).parallel().collect(Collectors.toList());

// (b) use a concurrency-aware accumulator designed for exactly this
LongAdder counter = new LongAdder();
data.parallelStream().limit(50_000).forEach(n -> counter.increment());
```
`Collectors.toList()`'s internal accumulator/combiner protocol handles the parallel merge
correctly and safely (each thread gets its own partial container, merged via the combiner — no
shared mutation across threads at all). `LongAdder` (`java.util.concurrent.atomic`) is purpose-
built for high-contention counting from many threads, cheaper under contention than
`AtomicLong` because it internally stripes the counter across multiple cells to reduce CAS
contention.

### Pitfall 2 — `forEach` does not preserve encounter order on a parallel stream

```java
data.stream().limit(10).parallel().forEach(n -> System.out.print(n + " "));
// output order is NOT guaranteed to be 1..10 -- may print in any interleaving

data.stream().limit(10).parallel().forEachOrdered(n -> System.out.print(n + " "));
// ALWAYS prints "1 2 3 4 5 6 7 8 9 10" -- forces re-synchronization to encounter order
```
`forEachOrdered` restores ordering, but at a real cost: it forces the framework to re-synchronize
results back into encounter order, which largely negates the concurrency benefit you were trying
to get from `.parallel()` in the first place — use it only when order genuinely matters for the
side effect (e.g. writing lines to a file that must stay in order).

## 3. Complexity

| Aspect | Cost | Notes |
|--------|------|-------|
| Splitting the source (fork) | O(log n) levels of recursive split | cost depends on the source's `Spliterator` — array/`ArrayList`-backed sources split cheaply (`SIZED`), a `LinkedList` or I/O-backed source splits poorly or not at all |
| Per-chunk processing | parallelized across up to `availableProcessors() - 1` worker threads | true speedup only if per-element work is CPU-bound and non-trivial |
| Merge/combine (join) | O(log n) pairwise combines | must be an associative, cheap combiner or this overhead dominates |
| Thread coordination overhead | fixed cost per parallel stream invocation | for small n or cheap per-element work, this overhead alone can make parallel *slower* than sequential |
| `forEachOrdered` on a parallel stream | extra re-synchronization cost | largely negates the parallelism benefit — use only when order matters |
| Shared mutable state, unsynchronized | **undefined / wrong result**, not just "slow" | lost updates, `ArrayIndexOutOfBoundsException`, or an incorrect count/size — a correctness bug, not a performance one |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/ParallelStreamsDemo.java`

```java
List<Integer> safeResult = data.stream().limit(50_000).parallel().collect(Collectors.toList());
System.out.println("safe collect size (always exactly 50000): " + safeResult.size());
```
Expected console output (abridged — timings vary by machine and are illustrative only):
```
sequential sum=... took=...ms
parallel   sum=... took=...ms
both sums equal (correctness, not speed, is what we assert in tests): true
common pool parallelism (shared by ALL parallel streams in this JVM): 7   (example: 8-core machine)
unsafe accumulator size (often < 50000, sometimes throws): 49837
safe collect size (always exactly 50000): 50000
LongAdder count (always exactly 50000): 50000
parallel forEach order (will likely look shuffled):
3 7 1 9 2 5 8 4 6 10
parallel forEachOrdered (always 1..10 in order):
1 2 3 4 5 6 7 8 9 10
```

## 5. When to use / when NOT to use

- Use `.parallelStream()` for large (typically tens/hundreds of thousands+ elements or more),
  CPU-bound, stateless, source-splits-cheaply (array/`ArrayList`-backed) workloads where the
  per-element work genuinely dominates the coordination overhead — `ParallelStreamsDemo`'s
  `costlyTransform` (a small CPU-bound loop per element) is exactly this shape.
- Don't parallelize small collections — the fork/join coordination overhead alone can exceed the
  entire sequential runtime.
- Don't parallelize I/O-bound work (network calls, file reads, blocking DB queries) — the common
  pool's worker threads block waiting on I/O instead of doing CPU work, and since the pool is
  shared JVM-wide, this starves every other parallel stream in the process too. Use a dedicated
  executor / `CompletableFuture` with a custom `Executor` for I/O-bound concurrency instead (see
  the multithreading module).
- Don't parallelize a source that splits poorly (`LinkedList`, `Stream.iterate` without a known
  size, most I/O-backed streams) — poor splitting means most of the work still runs on one thread
  anyway, with all the coordination overhead and none of the benefit.
- Never introduce shared mutable state (a plain field, `ArrayList`, `HashMap`) written from a
  parallel stream's lambda — always use the stream's own accumulation (`collect`, `reduce`) or a
  concurrency-safe accumulator (`LongAdder`, `ConcurrentHashMap`) if some form of shared state is
  unavoidable.
- Measure before committing to parallel in production — "looks like it should be faster" is not
  proof; benchmark with a realistic dataset size on realistic hardware.

## 6. Common pitfalls & gotchas

**Racy shared mutable accumulator (the classic parallel-stream bug):**
```java
// BUG: multiple threads call add() on the same non-thread-safe ArrayList concurrently
List<Integer> results = new ArrayList<>();
numbers.parallelStream().forEach(results::add);

// FIX: let collect() do the accumulation correctly (or use a concurrent collection deliberately)
List<Integer> results2 = numbers.parallelStream().collect(Collectors.toList());
```

**A non-thread-safe read-modify-write on a shared primitive holder — this exact bug is the H02
exercise in this module (`FixParallelStreamSideEffectBug`):**
```java
// BUG: total[0] += ... is a racy read-modify-write from many threads, loses updates
long[] total = {0};
numbers.parallelStream().filter(n -> n % 2 == 0).forEach(n -> total[0] += (long) n * n);
return total[0];

// FIX: use the stream's own parallel-safe reduction instead of any shared mutable accumulator
return numbers.parallelStream()
        .filter(n -> n % 2 == 0)
        .mapToLong(n -> (long) n * n)
        .sum();
```

**Assuming `.parallel()` always makes code faster:**
```java
// On a small list, this is typically SLOWER than the sequential version — coordination
// overhead (splitting, thread handoff, merging) dwarfs the tiny amount of actual work
List.of(1, 2, 3, 4, 5).parallelStream().map(n -> n * 2).toList();
```

**Blocking I/O inside a parallel stream lambda, starving the shared common pool:**
```java
// BUG: every worker thread blocks on network I/O; since ForkJoinPool.commonPool() is shared
// JVM-wide, this can stall completely unrelated parallel streams elsewhere in the same process
urls.parallelStream().map(this::blockingHttpGet).toList();

// FIX: use a dedicated executor / CompletableFuture.supplyAsync(fn, customExecutor) for I/O-bound
// concurrency instead of a parallel stream (see notes/09-multithreading-concurrency)
```

**Using `forEach` when order matters:**
```java
// BUG: order of printed lines is unspecified on a parallel stream
orderedLines.parallelStream().forEach(this::writeLine);
// FIX: forEachOrdered restores encounter order (at the cost of re-synchronization overhead)
orderedLines.parallelStream().forEachOrdered(this::writeLine);
```

## 7. Interview questions

- [Basic] What thread pool does `.parallelStream()` use by default? → `ForkJoinPool.commonPool()`
  — a single pool shared by every parallel stream (and every `CompletableFuture.*Async` call
  without a custom executor) across the entire JVM, sized by default to
  `availableProcessors() - 1` worker threads. → Follow-up: *Can you point a parallel stream at a
  different pool?* Yes, by wrapping the terminal operation inside a call submitted to a custom
  `ForkJoinPool` (`customPool.submit(() -> stream.parallel()...).get()`) — not the common, simple
  path, and rarely needed outside specific isolation requirements.
- [Basic] Name two conditions under which `.parallelStream()` is likely to be *slower* than
  `.stream()`. → (1) Small collections, where fork/join coordination overhead exceeds the actual
  work; (2) sources that split poorly (`LinkedList`, unsized/I/O-backed streams), where most work
  still ends up serialized on effectively one thread with added overhead on top. → Follow-up:
  *What source characteristic makes splitting cheap?* Being `SIZED` and random-access (array-
  backed / `ArrayList`-backed), so the `Spliterator` can divide the range by index cheaply and
  evenly, unlike a linked structure that must be walked to find a split point.
- [Basic] Why does `parallelStream().forEach(list::add)` on a plain `ArrayList` produce an
  incorrect or inconsistent result? → `ArrayList` is not thread-safe; multiple worker threads call
  `add()` concurrently with no synchronization between them, causing lost updates (or worse,
  internal array corruption) — a shared mutable structural write from multiple threads is exactly
  what `ArrayList` was never designed to handle. → Follow-up: *What's the correct alternative?*
  Use `.collect(Collectors.toList())` (its accumulator/combiner protocol handles the parallel
  merge safely, per-thread) instead of manually pushing into a shared list.
- [Intermediate] What correctness properties must a `reduce`/`collect` accumulator and combiner
  satisfy for a parallel stream to give the same result as sequential? → Associativity — the
  result must not depend on how the elements are grouped for combination — and the combiner must
  be consistent with the accumulator (merging two partial results computed independently must
  give the same answer as if they'd been accumulated together sequentially in some valid order).
  → Follow-up: *Give an example of a non-associative operation that would break under
  parallelization.* Subtraction: `((a - b) - c) != (a - (b - c))` in general — a `reduce` using
  subtraction as the combiner can give a different, wrong answer under parallel splitting than
  sequential, even though it "looks fine" on a small sequential test.
- [Intermediate] Why does `forEachOrdered` largely negate the benefit of `.parallel()`? → It forces
  the runtime to re-synchronize each sub-task's results back into the stream's original encounter
  order before invoking the action, which reintroduces a serialization point — worker threads
  still do their chunk of work in parallel, but the *action* itself effectively runs in order,
  losing most of the concurrency benefit for that step, so it should only be used when true
  ordering is a hard requirement (e.g. writing sequential log lines). → Follow-up: *Does plain
  `forEach` guarantee anything at all about ordering for a sequential (non-parallel) stream?*
  Yes — for a sequential stream over an ordered source, `forEach` does process elements in
  encounter order; the lack of ordering guarantee is specific to running in parallel.
- [Advanced] Why is the shared, JVM-wide nature of `ForkJoinPool.commonPool()` a production
  hazard, and how would you diagnose it? → Because every parallel stream (and default-executor
  `CompletableFuture` async calls) in the whole JVM shares the same fixed-size worker pool, one
  component doing long-running or blocking work inside a parallel stream can starve the pool for
  every other unrelated component — a seemingly unrelated feature (e.g. a report-generation
  parallel stream) can suddenly slow down because another part of the application submitted a
  blocking task to the same common pool. Diagnosis typically involves thread-dumping the JVM and
  noticing `ForkJoinPool.commonPool-worker-N` threads blocked on I/O or long computations instead
  of actively splitting/combining stream work. → Follow-up: *What's the standard mitigation?*
  Never do blocking I/O inside a parallel stream or a default-executor `CompletableFuture`; use a
  dedicated, appropriately-sized `ExecutorService` for I/O-bound or long-running async work, and
  reserve the common pool for genuinely short, CPU-bound parallel computations.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| H02 | Hard | Fix a racy shared-mutable-accumulator bug in a parallel-stream sum-of-squares | parallel-safe reduction (primitive stream `sum()` / `Collectors.summingLong`) | `exercises/FixParallelStreamSideEffectBug.java` |

- <details><summary>Hint</summary>Replace the shared `long[] total` read-modify-write entirely —
  `filter` the evens, `mapToLong` to squares, and call the primitive stream's own `sum()`; no
  external synchronization, no shared mutable state at all.</details>

Solution is in
`src/main/java/com/gk/study/streams/solutions/FixParallelStreamSideEffectBugSolution.java` —
attempt the stub in `exercises/FixParallelStreamSideEffectBug.java` first.

## 9. Quick recap

- Parallel streams fork the source (via `Spliterator`), process chunks concurrently on the shared
  JVM-wide `ForkJoinPool.commonPool()`, then join partial results — correctness requires
  associative accumulator/combiner functions and zero unsynchronized shared mutable state.
- They help for large, CPU-bound, cheaply-splittable (array-backed) workloads; they hurt (both
  correctness and performance) for small collections, poorly-splitting sources, and I/O-bound
  work.
- `forEach` on a parallel stream does not preserve encounter order; `forEachOrdered` restores it
  at the cost of re-synchronization overhead that largely cancels the parallelism benefit.
- Never mutate shared external state (a plain list/map/counter) from a parallel stream's lambda —
  use `collect`/`reduce`'s own accumulation, or a concurrency-safe accumulator like `LongAdder`.
- The common pool is shared JVM-wide — blocking I/O inside a parallel stream can starve every
  other parallel stream and default-executor `CompletableFuture` in the same process.
