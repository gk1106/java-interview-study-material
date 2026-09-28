# ArrayList vs LinkedList — benchmark

## 1. What it is

A direct, measured comparison of `ArrayList` and `LinkedList` on the four operations where their
Big-O either differs or, despite matching Big-O, real timings diverge sharply because of memory
layout: **add at end, add at front, get by index, and full iteration**.

## 2. How it works internally

This topic doesn't introduce new internals — it exercises the internals from topics 2 and 3
under a stopwatch (`System.nanoTime()`), on a JIT-warmed JVM, to turn "Big-O theory" into
concrete numbers a candidate can quote and reason about in an interview.

Benchmark design (see the demo class for the exact code):
- **Add at end**: `list.add(x)` in a loop, N times, for both implementations.
- **Add at front**: `list.add(0, x)` for `ArrayList` vs `list.addFirst(x)` for `LinkedList`, N
  times (smaller N for `ArrayList` since this is intentionally its worst case).
- **Get by index**: iterate `for (i = 0; i < n; i++) list.get(i)` for both — deliberately
  including `LinkedList` to demonstrate the O(n²) trap.
- **Iterate**: `for (E e : list)` (uses `Iterator` for both) — the fair comparison, since neither
  pays an indexing tax here.

A **JIT warm-up phase** (run the workload once, throw away the timing, then time again) is
included because the JIT compiler needs a few thousand iterations to compile hot loops to native
code; skipping warm-up produces noisy, pessimistic numbers dominated by interpreter overhead.

## 3. Complexity

| Operation | ArrayList | LinkedList | Winner in practice |
|-----------|-----------|------------|---------------------|
| add at end (N times) | O(1) amortized each, O(N) total | O(1) each, O(N) total | ArrayList (better constants — array write beats node alloc + 2 pointer writes) |
| add at front (N times) | O(n) each → O(N²) total | O(1) each → O(N) total | LinkedList, by a wide and growing margin as N grows |
| get(i) in a loop (N times) | O(1) each → O(N) total | O(n) each → O(N²) total | ArrayList, by a wide and growing margin |
| full iteration (N elements) | O(N) | O(N) | ArrayList usually wins on wall-clock (cache locality) despite equal Big-O |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/ArrayListVsLinkedListBenchmark.java`

```java
long t0 = System.nanoTime();
for (int i = 0; i < n; i++) list.add(0, i);   // front insert
long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
```
Representative results on a typical developer laptop, N = 50,000 (your numbers will vary — the
point is the *shape* of the difference, not exact millisecond values):
```
Operation            ArrayList        LinkedList
add at end (50k)     ~2 ms            ~6 ms
add at front (50k)   ~650 ms          ~3 ms
get by index (50k)   ~1 ms            ~1800 ms
iterate (50k)        ~1 ms            ~4 ms
```
Two numbers to internalize for interviews: front-insert on `ArrayList` and indexed-get on
`LinkedList` are both **quadratic-shaped** (they get dramatically worse, not just linearly worse,
as N grows) — doubling N roughly **quadruples** their time, while the "good" cells only double.

## 5. When to use / when NOT to use

- If the workload profile is "mostly append + mostly random read" → `ArrayList`, no contest.
- If the workload profile is "mostly push/pop at one or both ends, rarely by index" → `ArrayDeque`
  first (topic 5), `LinkedList` second.
- If the workload mixes indexed reads with front/middle inserts, neither is great — consider
  restructuring (e.g. process in a single pass, use a different data structure like a balanced
  tree/skip list for order-statistics, or batch inserts and rebuild).
- Never benchmark and never decide based on a single unwarmed run — JIT warm-up alone can be a
  10-100x difference for tight loops; always run multiple trials.

## 6. Common pitfalls & gotchas

**Benchmarking without JIT warm-up gives misleading numbers:**
```java
long t0 = System.nanoTime();
runWorkload();               // BUG: first run includes interpreter + JIT compilation overhead
System.out.println(System.nanoTime() - t0);
// fix: run once to warm up (discard the timing), then time a second run
runWorkload();                // warm-up, discarded
long t0 = System.nanoTime();
runWorkload();                // timed
```

**Silently swapping `ArrayList` for `LinkedList` (or vice versa) breaks a hidden performance
assumption** — code reviewed and approved for `ArrayList`-shaped access patterns (heavy `get(i)`
loops) becomes an O(n²) production incident if the field type changes to `LinkedList` without
re-auditing access patterns. Declare fields as `List<E>` but *think* about which concrete type is
injected/constructed, and comment non-obvious choices.

**Comparing `System.currentTimeMillis()` instead of `System.nanoTime()`** for sub-millisecond
timings — `currentTimeMillis()` has coarser resolution and is affected by system clock
adjustments; `nanoTime()` is monotonic and meant for elapsed-time measurement.

## 7. Interview questions

- [Basic] For appending N elements, which is generally faster in practice, `ArrayList` or
  `LinkedList`, and why? → `ArrayList` — both are amortized O(1) per append, but `ArrayList`
  writes into a pre-allocated contiguous slot while `LinkedList` allocates a new `Node` object and
  writes two pointers per element, which costs more per operation and hurts cache locality. →
  Follow-up: *Does that mean LinkedList is never worth using?* No — it wins decisively for
  front/middle-heavy workloads.
- [Basic] Why is `LinkedList.get(i)` in a loop dangerous for large lists? → Each call is O(n), so
  an N-iteration loop of `get(i)` calls costs O(n²) total — for N=50,000 that's ~2.5 billion
  pointer hops versus ~50,000 for the equivalent `ArrayList` loop. → Follow-up: *How do you fix
  code that does this?* Replace the indexed loop with a for-each loop (uses `Iterator`, O(1) per
  step) or a `ListIterator`.
- [Intermediate] Why does `ArrayList.add(0, x)` in a loop degrade quadratically? → Every insert at
  index 0 shifts all existing elements one slot right via `System.arraycopy`, an O(current size)
  operation; summed over N inserts that's `0+1+2+...+N ≈ N²/2`. → Follow-up: *What's the
  LinkedList equivalent cost?* `addFirst` is O(1) regardless of size, so N calls cost O(N) total —
  linear, not quadratic.
- [Intermediate] Why might `LinkedList` still lose a "fair" full-iteration benchmark against
  `ArrayList` even though both are O(n)? → CPU cache lines and hardware prefetching favor
  sequential memory access; `ArrayList`'s backing array is contiguous, so iterating strides through
  memory predictably, while `LinkedList` nodes can be scattered across the heap, causing more
  cache misses per element visited. → Follow-up: *Does object pooling/compact node layout fix
  this?* Partially — it's an active JVM research area (e.g. Project Valhalla's flattened/value
  types aim to reduce this exact overhead), but stock `LinkedList` doesn't do it.
- [Intermediate] What's a realistic banking-domain scenario where the "add at front" difference
  matters? → Building a running transaction ledger where the newest transaction must appear
  first — prepending N transactions into an `ArrayList` is O(N²); using `LinkedList.addFirst` (or
  `ArrayDeque.addFirst`) keeps it O(N), which matters once N reaches the tens of thousands (e.g.
  a full account history export). → Follow-up: *What if you need indexed access afterward too?*
  Build with `addFirst` into a `LinkedList`/`ArrayDeque`, then copy once into an `ArrayList` if
  random access is needed downstream — one O(N) copy beats O(N²) inserts.
- [Advanced] How would you design a fair micro-benchmark for these two structures beyond just
  wrapping `nanoTime()` around a loop? → Warm up the JIT with representative iterations first;
  run multiple trials and report median/percentiles, not a single sample; avoid dead-code
  elimination by consuming results (e.g. summing into a value you print); isolate GC noise by
  running with a large enough heap or using a proper benchmarking harness (JMH) for anything
  that needs to be trustworthy at the microsecond level. → Follow-up: *Why is JMH preferred over
  hand-rolled nanoTime benchmarks for serious work?* JMH controls for JIT warm-up, dead-code
  elimination, loop unrolling artifacts, and provides statistically sound multi-fork
  measurements — hand-rolled loops are fine for an interview-level demonstration but not for
  production capacity planning.
- [Advanced] If both structures are O(n) for iteration, why does the interview answer still
  usually favor `ArrayList` "in practice"? → Because Big-O ignores constant factors and memory
  layout, and real hardware heavily rewards sequential access; an interviewer asking this wants
  you to distinguish **asymptotic** complexity from **wall-clock** performance — a mature answer
  cites both.

## 8. Exercises

This topic is a measurement exercise, not a new coding pattern — run
`ArrayListVsLinkedListBenchmark` yourself with different N and record your own numbers; no
separate stub/solution pair is provided here (pattern exercises continue in topics 2, 3, 7, 8).

## 9. Quick recap

- Add-at-end: ArrayList usually wins (better constants), both amortized O(1).
- Add-at-front: LinkedList wins decisively — ArrayList degrades to O(n²) over N inserts.
- Get-by-index: ArrayList wins decisively — LinkedList degrades to O(n²) over N gets.
- Full iteration: both O(n) Big-O, ArrayList usually faster in wall-clock due to cache locality.
- Always warm up the JIT and run multiple trials before trusting a micro-benchmark.
