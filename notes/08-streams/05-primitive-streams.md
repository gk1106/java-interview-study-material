# Primitive streams and Stream.iterate/generate

## 1. What it is

`IntStream`, `LongStream`, and `DoubleStream` are specialized stream types that operate on
primitive values directly, avoiding the per-element boxing cost of a `Stream<Integer>`/
`Stream<Long>`/`Stream<Double>`. `Stream.iterate` and `Stream.generate` are two ways to build a
stream from a formula rather than an existing collection — both can be infinite, and both need
careful handling to terminate.

## 2. How it works internally

### Why primitive streams exist: boxing cost

A `Stream<Integer>` stores `Integer` objects — every `int` value that enters it is autoboxed into
a heap-allocated `Integer` wrapper (unless it's a small cached value, `-128..127`, per the
`Integer` cache). Summing a million elements via `Stream<Integer>.reduce(Integer::sum)` boxes and
unboxes repeatedly at every step. `IntStream` stores raw `int`s in its internal pipeline — no
wrapper objects, no boxing, better cache locality, and it exposes primitive-specialized terminal
ops (`sum()`, `average()`, `min()`, `max()`, `summaryStatistics()`) that a generic `Stream<T>`
simply doesn't have (there's no generic notion of "sum" over an arbitrary `T`).

```java
int sum = IntStream.rangeClosed(1, 100).sum();       // no boxing at all
long viaBoxedReduce = Stream.iterate(1, n -> n + 1)   // Stream<Integer>: every element boxed
        .limit(1_000_000)
        .mapToInt(Integer::intValue)                   // converts back down when actually needed
        .sum();
```

### `boxed()` — converting back to an object stream only when needed

```java
List<Integer> boxedList = IntStream.range(0, 5).boxed().toList();
```
`boxed()` converts `IntStream` -> `Stream<Integer>`. You need this the moment you must put values
into a generic `List<Integer>`, pass them to a `Collectors` method that expects reference types,
or otherwise interact with generics (primitives can't be type parameters in Java). The rule of
thumb: stay in the primitive stream as long as possible, `boxed()` only at the boundary where a
generic API demands it.

### `summaryStatistics()` — five aggregates in one pass

```java
IntSummaryStatistics stats = IntStream.of(4, 1, 7, 3).summaryStatistics();
stats.getMin(); stats.getMax(); stats.getAverage(); stats.getSum(); stats.getCount();
```
Computes count, sum, min, max, and average together in a single traversal — cheaper than calling
`min()`, `max()`, `average()` as three separate terminal operations on three separate streams (or,
worse, three re-materializations of the same source).

### `Stream.iterate` — seed + function, and the Java 9+ bounded overload

```java
// classic 2-arg form: infinite, MUST be paired with limit() or it never terminates
List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2).limit(8).toList();
// [1, 2, 4, 8, 16, 32, 64, 128]

// Java 9+ 3-arg form: bounded by a hasNext predicate, self-terminating, like a for-loop
List<Integer> under50 = Stream.iterate(1, n -> n < 50, n -> n * 2).toList();
// [1, 2, 4, 8, 16, 32]  (64 would exceed 50, so iteration stops there)
```
The 3-arg form is directly equivalent to `for (int n = 1; n < 50; n = n * 2)` — it's the
declarative-stream way to express a bounded, formula-driven sequence without a separate `limit()`
call and without the risk of forgetting one.

### `Stream.generate` — a `Supplier`, no relationship between elements

```java
List<Double> fixedNoise = Stream.generate(() -> 0.5).limit(4).toList(); // [0.5, 0.5, 0.5, 0.5]
```
Unlike `iterate`, `generate`'s `Supplier<T>` has *no* access to the previous element — each call
is independent (useful for constant streams, random number streams via `Math::random`, or reading
from an external source element by element). It has no natural stopping point at all — it is
**always** infinite and **must** be paired with `limit()` (or another short-circuiting op).

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `IntStream.rangeClosed/of/range` | O(n) to traverse | O(1) extra | no boxing, primitive array/range under the hood |
| `.sum()` / `.average()` / `.min()` / `.max()` | O(n) single pass | O(1) | primitive-specialized, no wrapper allocation |
| `.summaryStatistics()` | O(n) single pass | O(1) | 5 aggregates computed together, one traversal |
| `.boxed()` | O(n), one allocation per element | O(n) | pay the boxing cost only at this point, not earlier |
| `Stream<Integer>.reduce(Integer::sum)` | O(n), boxing every step | O(1) extra beyond boxed elements | strictly more allocation than the `IntStream` equivalent |
| `Stream.iterate(seed, next).limit(n)` | O(n) (short-circuits at limit) | O(1) streaming | infinite source, safe only because of `limit` |
| `Stream.iterate(seed, hasNext, next)` | O(k), k = iterations until hasNext fails | O(1) | self-terminating, no `limit()` needed |
| `Stream.generate(supplier)` | must be bounded externally | O(1) | always infinite on its own — `limit()` or similar is mandatory |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/PrimitiveStreamsDemo.java`

```java
var stats = IntStream.of(4, 1, 7, 3).summaryStatistics();
System.out.println("stats: min=" + stats.getMin() + " max=" + stats.getMax()
        + " avg=" + stats.getAverage() + " count=" + stats.getCount());
```
Expected console output:
```
sum 1..100 via IntStream = 5050
stats: min=1 max=7 avg=3.75 count=4
boxed list: [0, 1, 2, 3, 4]
powers of two: [1, 2, 4, 8, 16, 32, 64, 128]
iterate with predicate (< 50): [1, 2, 4, 8, 16, 32]
generate (constant supplier) x4: [0.5, 0.5, 0.5, 0.5]
viaIntStream=500000500000 viaBoxedReduce=500000500000
```

## 5. When to use / when NOT to use

- Use `IntStream`/`LongStream`/`DoubleStream` whenever you're working with numeric data and doing
  numeric aggregation (`sum`, `average`, `summaryStatistics`) — it's both faster (no boxing) and
  gives you the right vocabulary of terminal ops directly.
- Use `boxed()` only at the point where a generic API genuinely requires reference types (`List
  <Integer>`, a `Collectors` method, a method expecting `Stream<T>`) — don't box earlier than
  necessary.
- Use `Stream.iterate`'s 3-arg (bounded) form whenever the stopping condition is a natural
  predicate on the generated values themselves — it's clearer and safer than the 2-arg form plus a
  separately-reasoned `limit(n)`.
- Use `Stream.generate` for independent-per-element sources (constants, random values, polling an
  external supplier) — never for a sequence where each element depends on the previous one (that's
  what `iterate` is for).
- Don't reach for `IntStream` when you need a `Stream<Integer>` anyway for downstream generic
  processing (custom objects, non-numeric transforms) — the boxing cost is irrelevant next to the
  complexity of forcing everything through a primitive-only API.

## 6. Common pitfalls & gotchas

**Using `Stream.generate`/2-arg `Stream.iterate` without a `limit()` — hangs forever:**
```java
// BUG: no natural stopping point, terminal op (toList here) never returns
List<Double> noise = Stream.generate(Math::random).toList();

// FIX: always bound generate() and 2-arg iterate() with limit() (or an equivalent short-circuit)
List<Double> noise2 = Stream.generate(Math::random).limit(10).toList();
```

**Boxing overhead from `Stream<Integer>.reduce(Integer::sum)` on a hot numeric path:**
```java
// Works, but boxes every intermediate sum into a new Integer object
int sum = numbers.stream().reduce(0, Integer::sum);

// FIX: mapToInt (or start from an IntStream directly) avoids per-step boxing
int sum2 = numbers.stream().mapToInt(Integer::intValue).sum();
```

**Forgetting that `IntStream` is a *different type* from `Stream<Integer>` — `Collectors` methods
built for `Stream<T>` don't directly accept an `IntStream`:**
```java
// COMPILE ERROR: Collectors.toList() expects a Stream<T>, IntStream.collect has a different signature
List<Integer> list = IntStream.range(0, 5).collect(Collectors.toList());

// FIX: box first, or use IntStream's own collect(supplier, accumulator, combiner) 3-arg overload
List<Integer> list2 = IntStream.range(0, 5).boxed().collect(Collectors.toList());
```

**Off-by-one between `range` (exclusive end) and `rangeClosed` (inclusive end):**
```java
IntStream.range(1, 100).sum();        // sums 1..99  (100 excluded) — easy to get wrong
IntStream.rangeClosed(1, 100).sum();  // sums 1..100 (100 included) — matches "1 to 100 inclusive"
```

## 7. Interview questions

- [Basic] Why do `IntStream`/`LongStream`/`DoubleStream` exist when `Stream<Integer>` etc. already
  work? → To avoid autoboxing every element into a wrapper object — primitive streams store raw
  primitive values internally, which is both faster and lower-memory for numeric-heavy pipelines,
  and they expose numeric-only terminal ops (`sum`, `average`, `summaryStatistics`) that a generic
  `Stream<T>` has no way to provide. → Follow-up: *Is there a `Stream<Boolean>` or `Stream<Byte>`
  specialization?* No — only `int`/`long`/`double` get dedicated stream types; `boolean`, `byte`,
  `short`, `char`, `float` all have to go through the generic `Stream<T>` (usually widened to
  `int`/`double` first if numeric operations are needed).
- [Basic] What's the difference between `IntStream.range(a, b)` and `IntStream.rangeClosed(a, b)`?
  → `range` excludes the upper bound `b`; `rangeClosed` includes it — same as `[a, b)` vs `[a, b]`.
  → Follow-up: *Which would you use to sum 1 through 100 inclusive?* `rangeClosed(1, 100)` — using
  `range(1, 100)` would miss 100 and give the wrong sum.
- [Basic] What does `.boxed()` do and when do you need it? → Converts a primitive stream
  (`IntStream` etc.) into the corresponding object stream (`Stream<Integer>`), boxing each
  primitive into its wrapper type; you need it whenever downstream code requires a reference type
  — putting results into a `List<Integer>`, passing to a generic `Collectors` method, etc. →
  Follow-up: *Should you call `.boxed()` as early as possible in a pipeline?* No — the opposite:
  stay in the primitive stream as long as possible and box only at the last moment a generic API
  actually demands it, to minimize boxing cost.
- [Intermediate] What's the difference between `Stream.iterate` and `Stream.generate`, and when
  would you pick one over the other? → `iterate(seed, next)` produces each element from the
  *previous* one via a `UnaryOperator` — a genuine sequence with dependency between elements (like
  powers of two); `generate(supplier)` produces each element independently via a `Supplier` with
  no relationship to prior elements (like random numbers or polling a source). → Follow-up: *Does
  either one terminate on its own?* `generate` never does — always needs `limit()`. The 2-arg
  `iterate` also never does. Only the Java 9+ 3-arg `iterate(seed, hasNext, next)` is
  self-terminating, stopping once the predicate fails, equivalent to a bounded for-loop.
- [Intermediate] Why might `IntStream.of(numbers).min()` and `.max()` called as two separate
  statements be less efficient than needed, and what's the fix? → Each call re-traverses the
  stream from scratch (in `MaxMinViaPrimitiveStreamSolution`, two separate `IntStream.of(numbers)`
  calls are used deliberately, since a single primitive stream instance is one-shot); for a truly
  single-pass computation of multiple aggregates over the *same* stream instance, use
  `summaryStatistics()` (five aggregates, one pass) or `Collectors.teeing` for a `Stream<T>`
  equivalent. → Follow-up: *Is re-deriving `IntStream.of(numbers)` twice from an array actually
  wasteful here?* Not asymptotically — both calls are O(n); the real single-pass win from
  `summaryStatistics()` matters more when the source can't be cheaply re-derived (e.g. a network
  or file stream) rather than a plain in-memory array.
- [Advanced] Why can't primitives be generic type parameters in Java, and how does that shape the
  primitive-stream API design? → Java generics are erased to `Object` at the bytecode level (type
  erasure), and primitives aren't subtypes of `Object` — there's no way to instantiate
  `Stream<int>`. The JDK's answer is a parallel, hand-duplicated set of stream types (`IntStream`,
  `LongStream`, `DoubleStream`) with their own primitive-specialized functional interfaces
  (`IntFunction`, `IntPredicate`, `IntUnaryOperator`, ...) rather than a single generic `Stream<T>`
  that could somehow specialize itself. → Follow-up: *Does this duplication show up anywhere else
  in the JDK?* Yes — `java.util.function` has an entire family of primitive-specialized functional
  interfaces for exactly this reason, and collections like `ArrayList<Integer>` pay the same
  boxing cost `IntStream` was designed to avoid (there's no primitive `ArrayList<int>` in the JDK
  itself).

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E03 | Easy | Min and max of an `int[]` without boxing into `Integer` | `IntStream.min()`/`max()` | `exercises/MaxMinViaPrimitiveStream.java` |

- <details><summary>Hint</summary>`IntStream.of(numbers).min().orElseThrow()` and a second
  `IntStream.of(numbers).max().orElseThrow()` call (a fresh primitive stream each time, since a
  stream instance is single-use); throw `IllegalArgumentException` first if the array is
  empty.</details>

Solution is in `src/main/java/com/gk/study/streams/solutions/MaxMinViaPrimitiveStreamSolution.java`
— attempt the stub in `exercises/MaxMinViaPrimitiveStream.java` first.

## 9. Quick recap

- `IntStream`/`LongStream`/`DoubleStream` avoid boxing and add numeric terminal ops (`sum`,
  `average`, `summaryStatistics`) that generic `Stream<T>` can't offer.
- `boxed()` converts back to an object stream — call it only at the last moment a generic API
  demands reference types.
- `Stream.iterate(seed, next)` and `Stream.generate(supplier)` are both infinite by default and
  need `limit()`; the Java 9+ 3-arg `iterate(seed, hasNext, next)` self-terminates like a
  for-loop.
- `range` excludes the upper bound, `rangeClosed` includes it — a classic off-by-one trap.
- `summaryStatistics()` computes count/sum/min/max/average in a single pass, cheaper than calling
  several separate terminal ops (or re-deriving the stream) when the source can't be cheaply
  re-traversed.
