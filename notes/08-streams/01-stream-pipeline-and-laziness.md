# Stream pipeline: source -> intermediate -> terminal, and laziness

## 1. What it is

A `Stream<T>` is a one-shot, declarative pipeline over a data source: exactly one **source**
(a collection, an array, `Stream.of`, an I/O channel, etc.), zero or more **intermediate
operations** (`map`, `filter`, ...) that each return a *new* stream and are never executed on
their own, and exactly one **terminal operation** (`collect`, `forEach`, `count`, ...) that
actually pulls elements through the whole pipeline and produces a result or a side effect. Until
the terminal operation runs, nothing happens — streams are **lazy**.

## 2. How it works internally

### The three pieces

```
source            intermediate ops (0..n)              terminal op (exactly 1)
--------          ------------------------              -----------------------
List/array   ->    map / filter / flatMap / ...    ->    collect / forEach / reduce / count / ...
Stream.of()         each returns a NEW Stream              consumes the pipeline, produces
                    (pipeline stages, not executed)        a result or triggers side effects
```

- **Source**: adapts the underlying data into a `Spliterator` (a splittable iterator that also
  knows characteristics like `SIZED`, `ORDERED`, `SORTED` — used to optimize operations such as
  `count()` or a later `parallel()` split).
- **Intermediate operations** are *lazy* and merely record a stage — internally each call wraps
  the previous stream in a new pipeline stage object describing "what to do to each element when
  it eventually flows through," without touching a single element yet. Calling `.map(fn)` returns
  a brand-new `Stream` object; it does not mutate or consume the one it was called on.
- **Terminal operation** is the only thing that actually drives iteration. Internally, the JDK
  builds one composed function chain from source to terminal and pulls elements through it
  **one at a time, depth-first** through every stage — not stage-by-stage over the whole
  collection. This is why `peek` calls interleave: element 1 goes through map, then filter, then
  the terminal consumer, *before* element 2 is even touched.

### Why laziness matters: nothing runs until a terminal op

```java
Stream<Integer> pipeline = source.stream()
        .peek(n -> System.out.println("map stage source: " + n))
        .map(n -> n * 2)
        .peek(n -> System.out.println("filter stage input: " + n))
        .filter(n -> n > 4);
// nothing has printed yet — pipeline is just a description, not a computation
pipeline.findFirst(); // NOW it runs, element by element, until findFirst can answer
```
This is exactly what `StreamPipelineLazinessDemo` proves: building the pipeline above prints
nothing at all; only calling the terminal op (`findFirst()`) triggers any `peek` output.

### Short-circuiting

Some operations (`anyMatch`, `allMatch`, `noneMatch`, `findFirst`, `findAny`, `limit`) are
**short-circuiting** — they can produce an answer without visiting every element, and a terminal
short-circuiting op can stop pulling from an *infinite* source entirely:
```java
boolean found = source.stream()
        .peek(n -> System.out.println("evaluating: " + n))
        .anyMatch(n -> n == 3);
// peek only prints 1, 2, 3 — evaluation stops the instant a match is found, elements 4..8
// on the source are never even pulled through the pipeline
```
```java
List<Integer> firstFive = Stream.iterate(1, n -> n + 1) // infinite stream, no natural end
        .peek(n -> System.out.println("generated: " + n))
        .limit(5)                                        // short-circuits after 5 elements
        .toList();
// without limit(), this would hang forever
```

### A stream is single-use

Every intermediate op call *returns a new object*, but the underlying pipeline as a whole can
only be walked by a terminal operation **once**. After a terminal op runs, calling any other
operation on the *same stream reference* throws `IllegalStateException: stream has already been
operated upon or closed` — you must build a fresh stream (e.g. call `.stream()` on the source
collection again) to run a second query.

## 3. Complexity

| Aspect | Cost | Notes |
|--------|------|-------|
| Building the pipeline (chaining intermediate ops) | O(1) per stage | just wraps a description, no element visited |
| Running a non-short-circuiting terminal op | O(n) single pass | every element flows through every stage once, depth-first |
| Short-circuiting terminal op (`anyMatch`, `findFirst`, `limit`-bounded pipeline) | O(k), k <= n | stops as soon as the answer is known / the limit is reached |
| Re-using a consumed stream | N/A — throws `IllegalStateException` | must re-derive a new stream from the source |
| `peek` for debugging | O(1) per element, same pass | not a separate pass — runs inline as each element flows through |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/StreamPipelineLazinessDemo.java`

```java
List<Integer> firstFive = Stream.iterate(1, n -> n + 1)
        .peek(n -> System.out.println("generated: " + n))
        .limit(5)
        .toList();
System.out.println("firstFive = " + firstFive);
```
Expected console output (abridged):
```
-- building the pipeline (no terminal op yet) --
pipeline built, nothing printed above this line — that's laziness
-- now invoking a terminal op: findFirst() --
map stage source: 1
filter stage input: 2
map stage source: 2
filter stage input: 4
map stage source: 3
filter stage input: 6
result = 6
-- short-circuit proof: anyMatch stops as soon as it can answer --
evaluating: 1
evaluating: 2
evaluating: 3
anyMatch(== 3) = true — notice peek never printed past 3
-- short-circuit proof: limit() truncates an infinite stream --
generated: 1
generated: 2
generated: 3
generated: 4
generated: 5
firstFive = [1, 2, 3, 4, 5]
```

## 5. When to use / when NOT to use

- Use streams for declarative, read-oriented transformations of a data source (filter/map/
  aggregate) where the pipeline reads clearly top-to-bottom — this is most of what the rest of
  this module covers.
- Don't build a pipeline purely for its side effects one stage at a time expecting eager
  execution — nothing runs until you call a terminal op, and forgetting the terminal op entirely
  is a silent no-op bug (see pitfalls).
- Don't try to reuse a `Stream` variable across two queries — treat every stream as consumed the
  moment a terminal op runs.
- For anything genuinely unbounded/infinite (`Stream.iterate`/`Stream.generate` with no natural
  end), you *must* pair it with a short-circuiting op (`limit`, or the Java 9+ `iterate` overload
  with a `hasNext` predicate) or the terminal op will never return.

## 6. Common pitfalls & gotchas

**Forgetting the terminal operation — a pipeline that silently does nothing:**
```java
// BUG: builds a pipeline, throws it away — no terminal op, nothing ever executes
words.stream().filter(w -> w.length() > 4).map(String::toUpperCase);

// FIX: always end with a terminal op
List<String> result = words.stream().filter(w -> w.length() > 4).map(String::toUpperCase).toList();
```

**Reusing a stream after a terminal op has run:**
```java
Stream<Integer> s = List.of(1, 2, 3).stream();
long count = s.count();      // terminal op #1 — consumes s
s.forEach(System.out::println); // BUG: IllegalStateException: stream has already been operated upon or closed

// FIX: derive a fresh stream per query
List<Integer> data = List.of(1, 2, 3);
long count2 = data.stream().count();
data.stream().forEach(System.out::println); // fresh stream, fine
```

**Assuming intermediate ops run "one stage over the whole collection at a time" (batch style)** —
in reality processing is depth-first per element, so a `peek` placed between `map` and `filter`
interleaves with map/filter calls for *each individual element*, not "map everything, then filter
everything." This matters when reasoning about ordering of side effects or debugging with `peek`.

**Relying on an infinite `Stream.iterate`/`generate` without `limit`** — the terminal op simply
never returns (or, with a non-short-circuiting terminal op like `.toList()` on an unlimited
stream, it runs forever / eventually OOMs). Always pair unbounded sources with `limit(n)` or the
bounded 3-arg `Stream.iterate(seed, hasNext, next)`.

## 7. Interview questions

- [Basic] What are the three parts of every stream pipeline? → Exactly one source, zero or more
  intermediate operations, and exactly one terminal operation. → Follow-up: *What happens if you
  never call a terminal operation?* Nothing — the pipeline is a description only; no element is
  ever visited, no side effect ever fires.
- [Basic] Why are streams described as "lazy"? → Intermediate operations don't execute when
  called — they just record a pipeline stage and return a new `Stream` object. Only invoking a
  terminal operation drives elements through the whole chain. → Follow-up: *Give one practical
  consequence of laziness.* You can build up a pipeline conditionally across several lines/method
  calls with zero execution cost until you finally decide to consume it.
- [Basic] Can you reuse the same `Stream` object for two different terminal operations? → No —
  once a terminal op runs, that stream instance is considered closed; any further operation on it
  throws `IllegalStateException`. You need to create a new stream from the source to run a second
  query. → Follow-up: *Does that include intermediate operations too?* Yes — calling `.filter()`
  on an already-consumed stream also throws, not just a second terminal op.
- [Intermediate] What does "short-circuiting" mean for a stream operation, and name three
  short-circuiting operations. → A short-circuiting operation can produce a result without
  processing every element of the (possibly infinite) source — `anyMatch`/`allMatch`/`noneMatch`
  stop as soon as the boolean answer is determined, `findFirst`/`findAny` stop at the first match,
  and `limit(n)` stops pulling once n elements have passed through. → Follow-up: *Why is
  short-circuiting essential for `Stream.iterate`/`Stream.generate`?* Those sources are infinite by
  default; without a short-circuiting op like `limit`, the terminal operation would never return.
- [Intermediate] Explain, with an example, why elements flow through a pipeline depth-first
  rather than stage-by-stage. → Given `.map(f).filter(p)` over `[1,2,3]`, the JDK does not compute
  `map` over all three elements and then `filter` over all three results; instead it pushes
  element 1 through `map` then `filter` then the terminal consumer, then does the same for element
  2, then 3 — one element at a time, all the way through the pipeline. You can prove this by
  placing `peek` calls between stages and observing the interleaved print order rather than two
  separate "batches" of output. → Follow-up: *Why did the JDK design it this way instead of
  materializing an intermediate list at each stage?* It avoids allocating a full intermediate
  collection per stage (better memory/cache behavior) and is exactly what makes short-circuiting
  operations able to stop early — a batch-per-stage design would have to fully compute each stage
  before the next, defeating short-circuiting.
- [Advanced] How does laziness interact with a source that is mutated between building the
  pipeline and running the terminal operation? → For most collection sources, `stream()` captures
  a `Spliterator` over the *current* backing structure lazily; if the source is structurally
  modified after the stream is created but before the terminal op runs (and the source isn't
  concurrency-safe), you risk a `ConcurrentModificationException` at terminal-op time, or
  undefined behavior — because nothing was actually snapshotted at `.stream()` call time, only a
  view was set up. → Follow-up: *Does putting a `.toList()` earlier in a chain fix this?* Yes for
  that portion — `.toList()`/`.collect(toList())` is itself a terminal op that fully materializes
  results into a new, independent list at that point; a fresh `.stream()` over that list is
  immune to later mutation of the *original* source.

## 8. Exercises

Exercise files for map/filter mechanics on top of this pipeline model are grouped with the
intermediate-operations topic — see `notes/08-streams/02-intermediate-operations.md`.

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| — | — | (no dedicated exercise for pipeline/laziness mechanics itself — study via `StreamPipelineLazinessDemo`) | — | — |

## 9. Quick recap

- Every pipeline is source -> intermediate ops (lazy, return a new stream each) -> terminal op
  (the only thing that actually runs anything).
- Nothing executes until a terminal op is called; forgetting one is a silent no-op bug.
- A stream can be walked by a terminal op exactly once; reusing a consumed stream throws
  `IllegalStateException`.
- Processing is depth-first per element through every stage, not stage-by-stage over the whole
  collection — this is what makes short-circuiting possible.
- Short-circuiting ops (`anyMatch`, `findFirst`, `limit`, ...) can stop before visiting every
  element, and are required to terminate an infinite `Stream.iterate`/`generate` source.
