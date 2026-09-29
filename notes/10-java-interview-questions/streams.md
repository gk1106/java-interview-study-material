# Streams interview questions

Cross-cutting drill on the Streams API — full mechanics live in `notes/08-streams/` (7 files:
pipeline/laziness, intermediate ops, collect/reduce/Collectors, Optional, primitive streams,
parallel streams, banking-dataset exercises). This file is the rapid interview version, tags
`[Basic]` / `[Intermediate]` / `[Advanced]`.

Sections: [Pipeline & laziness](#1-pipeline--laziness) · [map vs flatMap and intermediate ops](#2-map-vs-flatmap-and-intermediate-operations)
· [Collectors deep dive](#3-collectors-deep-dive) · [reduce](#4-reduce) · [Primitive streams](#4b-primitive-streams)
· [Optional](#5-optional-done-right-and-wrong) · [Parallel streams](#6-parallel-streams)
· [Predict-the-output puzzles](#7-predict-the-output-puzzles)

---

## 1. Pipeline & laziness

**[Basic] What are the three parts of every stream pipeline, and which part actually "does
something"?**
**Source** (a `Collection`, an array via `Arrays.stream`, `Stream.of`, `IntStream.range`, an I/O
channel, etc.) → zero or more **intermediate operations** (`map`, `filter`, `sorted`, `distinct`,
`limit`, `peek`, ...) → exactly one **terminal operation** (`collect`, `forEach`, `reduce`,
`count`, `anyMatch`, ...). Nothing actually executes until the **terminal** operation is invoked —
intermediate operations merely build up a description of the pipeline (a chain of `Spliterator`-
wrapping stages); this is stream **laziness**.
*Follow-up: what happens if a stream pipeline has no terminal operation at all?* Nothing — it never
runs. This is a real, silent bug class: building a stream, chaining `.filter()`/`.map()`, and
forgetting to call `.collect()`/`.forEach()`/etc. compiles fine and simply does nothing at runtime,
with no error or warning.

**[Basic] Can a stream be reused/iterated twice?**
No — a stream can only be consumed by **one** terminal operation. Calling a second terminal
operation (or even another intermediate operation) on an already-consumed stream throws
`IllegalStateException: stream has already been operated upon or closed`. If you need to run the
same logic twice, re-create the stream from the source (`list.stream()` again) rather than trying to
reuse a `Stream` reference.
*Follow-up: does this mean streams are stateful objects, contradicting the "streams don't store
data" idea?* Not contradictory — a `Stream` holds no data of its own (it's a pipeline description
over a source), but it does track whether it's already been "walked" once, purely to enforce
single-use and prevent nonsensical re-traversal semantics.

**[Intermediate] Explain stream laziness with a concrete example showing operations interleaving
per-element, not stage-by-stage.**
```java
Stream.of(1, 2, 3, 4, 5)
    .filter(n -> { System.out.println("filter " + n); return n % 2 == 0; })
    .map(n -> { System.out.println("map " + n); return n * 10; })
    .forEach(n -> System.out.println("forEach " + n));
```
Output interleaves per element (not "run filter on everything, then map on everything"):
```
filter 1
filter 2
map 2
forEach 20
filter 3
filter 4
map 4
forEach 40
filter 5
```
Each element flows through the **entire pipeline** one at a time before the next element starts —
this is what allows short-circuiting operations like `findFirst()`/`limit()`/`anyMatch()` to avoid
processing the whole source at all (see puzzle below), and it's a direct consequence of streams
being pull-based internally (the terminal operation pulls elements one at a time through the whole
stage chain), not a series of separate full passes.
*Follow-up: does this per-element interleaving still hold for a parallel stream?* No — parallel
streams split the source into chunks processed by different threads, so no single, predictable
interleaving order is guaranteed at all (see the parallel streams section).

**[Intermediate] What is `peek()` actually meant for, and why is using it for side-effecting
business logic considered bad practice?**
`peek()` is an intermediate operation meant strictly for **debugging/observing** elements as they
flow through the pipeline, without altering the stream — it exists to let you log/inspect
in-flight values. Because it's intermediate and lazy, it's a common trap: `peek()` calls that aren't
followed by a terminal operation never execute at all (no error, just silent no-op), and even when a
terminal operation follows, the exact number of times `peek` runs per element is not strictly
specified for parallel/optimized pipelines (some JIT optimizations, or short-circuiting terminal
ops, can skip or reorder calls). Using `peek()` to perform actual business-logic side effects
(mutating external state, saving to a database) is fragile and explicitly discouraged by the
Javadoc — use `forEach()` (a terminal operation) for intentional side effects instead.
*Follow-up: give a case where `peek()`'s side effect might not run even though the stream is
consumed.* If a short-circuiting terminal operation like `findFirst()` or `anyMatch()` is satisfied
before reaching later elements, `peek()` (and any upstream operation) simply never executes for the
remaining, un-visited elements.

---

## 2. map vs flatMap and intermediate operations

**[Basic] `map()` vs `flatMap()` — what's the structural difference?**
`map(Function<T,R>)` transforms each element **one-to-one**: a `Stream<T>` becomes a `Stream<R>`,
same element count. `flatMap(Function<T, Stream<R>>)` transforms each element into its **own
stream**, then flattens all of those sub-streams into a single, combined stream — used whenever
each input element logically produces **zero, one, or many** output elements (e.g. flattening a
`List<List<Integer>>` into a single `Stream<Integer>`, or splitting each sentence into words).
```java
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4, 5));
List<Integer> flat = nested.stream()
    .flatMap(List::stream)     // each inner List<Integer> -> Stream<Integer>, then flattened
    .collect(Collectors.toList());
// flat = [1, 2, 3, 4, 5]
```
*Follow-up: what would `.map(List::stream)` produce instead of `.flatMap(List::stream)` on the same
input?* A `Stream<Stream<Integer>>` — a stream of streams, not flattened — almost never what you
actually want, and you'd typically need to then manually flatten it anyway.

**[Basic] What does `distinct()` use to decide two elements are duplicates?**
`equals()`/`hashCode()` (implemented internally with a `HashSet`-like structure for uniqueness
tracking as elements are pulled through) — the same contract that backs `HashSet`/`HashMap`
uniqueness. For custom objects, `distinct()` only works correctly if `equals()`/`hashCode()` are
properly overridden; otherwise it falls back to identity comparison, likely not what's intended.
*Follow-up: is `distinct()` guaranteed to be stable (keep the first occurrence's relative order) for
an ordered stream?* Yes, for an **ordered** stream (the default for most sources like `List`), the
JDK guarantees `distinct()` preserves encounter order, keeping the first occurrence.

**[Intermediate] Why does the order of `filter()` and `sorted()`/`map()` in a pipeline sometimes
matter for performance (not just correctness)?**
Because streams are lazy and process elements one at a time through the whole chain (see laziness
above), placing a cheap, aggressively-reducing operation like `filter()` **before** an expensive
operation like `map()` or a stateful op like `sorted()` means the expensive/stateful op only runs on
the already-reduced subset, not the full source.
```java
// worse: maps every element (expensive), then throws most of them away
list.stream().map(this::expensiveTransform).filter(x -> x.isValid()).toList();
// better: filters first (cheap), only transforms the survivors
list.stream().filter(this::isValidRaw).map(this::expensiveTransform).toList();
```
*Follow-up: is `sorted()` itself lazy in the same per-element sense as `filter`/`map`?* No —
`sorted()` (and `distinct()`, in the general case) is a **stateful** intermediate operation: it must
consume the **entire** upstream before it can emit even the first element (it needs to see
everything to know what's smallest), unlike stateless ops like `filter`/`map` that process one
element at a time. This also means `sorted()` can't meaningfully participate in short-circuiting the
way `filter` can.

**[Advanced] What's the difference between a "stateless" and "stateful" intermediate operation, and
why does it matter for both correctness under parallelism and short-circuiting?**
Stateless operations (`map`, `filter`, `peek`, `flatMap`) process each element independently, with
no dependency on other elements — trivially parallelizable, each element's processing can happen on
any thread with no coordination. Stateful operations (`sorted`, `distinct`, `limit`, `skip`) require
information from **other** elements (the whole stream for `sorted`/`distinct`, or a running count for
`limit`/`skip`) to correctly process any single element — this makes them inherently more expensive
in a parallel pipeline (often requiring a merge/buffering step), and `sorted`/`distinct` in
particular defeat the ability to short-circuit early the way `filter` followed by `findFirst()` can.
*Follow-up: can `limit(n)` short-circuit an infinite stream, e.g. from `Stream.iterate`?* Yes —
`limit` is stateful but still short-circuiting: `Stream.iterate(1, x -> x + 1).limit(5)` correctly
terminates after producing 5 elements even though the source is conceptually infinite, because
`limit` knows exactly when its condition (element count) is satisfied.

---

## 3. Collectors deep dive

**[Basic] `Collectors.toList()` vs `Collectors.toUnmodifiableList()` vs `.toList()` (Java 16+) —
what's the difference?**
`Collectors.toList()` gives no guarantee about mutability, serializability, or thread-safety of the
returned list (in practice, historically an `ArrayList`, but that's an implementation detail, not a
contract). `Collectors.toUnmodifiableList()` (Java 10+) explicitly guarantees an immutable result
(throws on mutation attempts). `.toList()` (the `Stream` instance method, Java 16+) is a convenient
shorthand that also returns an **unmodifiable** list — effectively replacing
`.collect(Collectors.toList())` for the common case with less boilerplate.
*Follow-up: is `Stream.toList()`'s result exactly the same type as
`Collectors.toUnmodifiableList()`'s?* Not guaranteed to be the identical concrete class, but both
are unmodifiable — the practical contract (throws `UnsupportedOperationException` on mutation) is
the same.

**[Intermediate] `groupingBy` vs `partitioningBy` — when do you use each, and what's the return type
difference?**
`Collectors.groupingBy(classifier)` groups elements by an arbitrary key function into a
`Map<K, List<T>>` (or a different downstream collection, with a second `Collectors` argument) — the
number of distinct groups (keys) is whatever the classifier produces, unbounded. `Collectors.
partitioningBy(predicate)` is a specialized two-way split: it always returns a
`Map<Boolean, List<T>>` with **exactly** two keys (`true` and `false`), even if one partition ends
up empty (the map always has both keys present, unlike `groupingBy`, which only creates keys that
actually occurred). Use `partitioningBy` when the split is a genuine yes/no predicate; use
`groupingBy` for anything with more than two possible groups, or grouping by a non-boolean key.
```java
Map<Department, List<Employee>> byDept =
    employees.stream().collect(Collectors.groupingBy(Employee::getDepartment));
Map<Boolean, List<Employee>> highVsLow =
    employees.stream().collect(Collectors.partitioningBy(e -> e.getSalary() > 100_000));
```
*Follow-up: how do you get counts per group instead of the actual grouped lists?* Pass a downstream
collector as the second arg: `Collectors.groupingBy(Employee::getDepartment,
Collectors.counting())` → `Map<Department, Long>`.

**[Intermediate] What's the merge-function argument for in `Collectors.toMap(...)`, and what
happens without it?**
`Collectors.toMap(keyMapper, valueMapper)` (2-arg form) throws `IllegalStateException: Duplicate
key` if two stream elements map to the same key — a very common runtime surprise when the "key" isn't
actually guaranteed unique across the source data. The 3-arg form,
`Collectors.toMap(keyMapper, valueMapper, mergeFunction)`, supplies a `BinaryOperator<V>` telling the
collector what to do when a key collision occurs (keep the first, keep the last, sum them, combine
into a list, etc.) instead of throwing.
```java
Map<String, Integer> totalByDept = employees.stream()
    .collect(Collectors.toMap(Employee::getDepartment, Employee::getSalary, Integer::sum));
// merges salaries for employees sharing the same department key instead of throwing
```
*Follow-up: how would you control the resulting Map implementation (e.g. get a sorted `TreeMap`
result instead of a `HashMap`)?* Use the 4-arg overload, adding a `Supplier<M>` map factory:
`Collectors.toMap(keyMapper, valueMapper, mergeFunction, TreeMap::new)`.

**[Intermediate] What does `Collectors.mapping(...)` do, and why is it usually paired with
`groupingBy`?**
`Collectors.mapping(mapperFunction, downstreamCollector)` applies a transformation to each element
**before** it reaches a downstream collector — it's a way to compose collectors, most commonly
nested inside `groupingBy` to say "group by X, but collect only Y (a projection) per group" instead
of collecting the whole original element.
```java
// group employees by department, but collect only their NAMES, not the whole Employee object
Map<Department, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment,
             Collectors.mapping(Employee::getName, Collectors.toList())));
```
*Follow-up: what's the analogous problem `Collectors.filtering` solves, and why was it added
separately in Java 9 rather than just filtering before groupingBy?* `Collectors.filtering(predicate,
downstream)` filters **within** each group after grouping, as opposed to `stream().filter(...)`
before `groupingBy`, which would filter out elements (and potentially entire keys) *before*
grouping — meaning a group that would exist with zero matching elements simply wouldn't appear as a
key at all. `filtering` inside `groupingBy` lets a key still appear in the result map (with an empty
downstream collection) even if no elements in that group pass the predicate.

**[Advanced] What does `Collectors.teeing()` (Java 12+) do, and when would you reach for it instead
of two separate stream passes?**
`Collectors.teeing(downstream1, downstream2, merger)` runs **two** independent downstream
collectors over the **same** single pass of the stream, then combines their two results with a
merge function into one final result — letting you compute, say, both an average and a count (or
min and max) in a **single** traversal instead of either iterating the source twice or writing a
manual custom collector.
```java
record MinMax(int min, int max) {}
MinMax result = numbers.stream().collect(Collectors.teeing(
    Collectors.minBy(Integer::compareTo),
    Collectors.maxBy(Integer::compareTo),
    (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())
));
```
*Follow-up: could you achieve the same "two aggregates in one pass" result with
`Collectors.summarizingInt`/`summarizingDouble` instead, for numeric stats?* Yes, for the common
numeric-statistics case (count, sum, min, max, average all at once) —
`Collectors.summarizingInt(ToIntFunction)` is a purpose-built single-pass collector for exactly that;
`teeing` is the more general tool for combining two *arbitrary*, possibly non-numeric collectors.

**[Basic] What does `Collectors.joining(delimiter, prefix, suffix)` give you over manually
concatenating strings in a loop?**
It builds a single delimited string efficiently (internally using a `StringJoiner`/`StringBuilder`,
not repeated immutable `String` concatenation) directly from a `Stream<String>` (or any stream mapped
to `String` first), with optional prefix/suffix wrapping — the natural collector for producing
things like a comma-separated list, a SQL `IN (...)` clause, or a CSV row without hand-rolling
delimiter/edge-case logic (specifically, correctly omitting the delimiter around the first/last
element, which a naive loop-based concatenation often gets wrong on the first iteration).
```java
String csv = names.stream().collect(Collectors.joining(", ", "[", "]"));  // "[Alice, Bob, Carol]"
```
*Follow-up: does `joining()` work directly on a `Stream<Employee>`, or does it require mapping to
`String` first?* It requires `Stream<CharSequence>` (so `String`, `StringBuilder`, etc.) — you must
`.map(Employee::getName)` (or similar) before `.collect(Collectors.joining(...))`; `joining` has no
implicit `toString()` conversion built in.

---

## 4. reduce

**[Basic] What are the three overloads of `Stream.reduce(...)`, and what does each give you?**
`reduce(BinaryOperator<T> accumulator)` — returns `Optional<T>` (empty if the stream is empty,
since there's no identity to fall back to). `reduce(T identity, BinaryOperator<T> accumulator)` —
returns `T` directly (never empty; `identity` is the starting value and the fallback for an empty
stream). `reduce(U identity, BiFunction<U,T,U> accumulator, BinaryOperator<U> combiner)` — supports
reducing to a **different type** `U` than the stream's element type `T`; the extra `combiner` is
needed to merge partial results when the reduction runs in **parallel** (each thread's partial `U`
accumulations need to be combined into one).
*Follow-up: why does the 2-arg form need an `identity` value at all — couldn't it just use the first
element as the seed?* The identity value must be a true identity for the operation (e.g. `0` for
sum, `1` for product, `""` for string concatenation) specifically so that reducing an **empty**
stream still returns a sensible, well-defined result (the identity itself) rather than needing
special-casing — and so parallel reduction can freely combine partial results starting from the
same identity on each chunk.

**[Intermediate] Why does the 3-arg `reduce` need a separate `combiner` function, and when is it
actually invoked?**
The `combiner` is only ever invoked when the stream runs **in parallel** — the accumulator function
processes each chunk of the (parallel-split) source independently into a partial `U` result per
chunk/thread; the `combiner` then merges those partial `U` results pairwise into the final single
`U`. For a sequential stream, the combiner is simply never called at all (there's only ever one
accumulation in progress, nothing to merge). This is a very commonly missed detail — people write a
`combiner` that behaves differently from the `accumulator` (or that mutates shared state
unsafely), which works fine sequentially and breaks silently/nondeterministically the moment the
same reduction runs on a parallel stream.
*Follow-up: what specifically goes wrong if the accumulator mutates a shared, non-thread-safe
collection passed as the identity, under `parallelStream()`?* Race conditions on the shared mutable
object (e.g. lost `ArrayList` elements, or `ArrayIndexOutOfBoundsException` from a resize racing
with another thread's write) — this is exactly why `Collectors.toList()`/`.collect(...)` (which uses
a proper per-thread-then-merge strategy via `Collector`'s own combiner) is the correct tool for
building a result collection, not a naive `reduce` with a shared mutable seed.

**[Advanced] `reduce` vs `collect` — why does the JDK provide both, and when is one clearly the
right tool over the other?**
`reduce` is designed for **immutable** functional folding — combining elements into a new value each
step, ideal for primitives and immutable results (sums, products, string concatenation via a proper
identity/accumulator/combiner). `collect` is designed for **mutable reduction** — accumulating
results into a mutable container (a `List`, `Map`, `StringBuilder`) more efficiently, using a
`Supplier` (create the container), `BiConsumer` accumulator (add one element into the container, in
place, no copying), and `BinaryOperator` combiner (merge two containers, e.g. `addAll`) — critically,
avoiding the O(n²) cost that repeatedly producing new immutable collections via `reduce` would
incur (imagine `reduce` building a `List` by creating a new list with one more element added, every
single step). Prefer `collect(Collectors...)` for building any collection; prefer `reduce` for
folding to a single scalar/immutable value.
*Follow-up: could you technically implement `Collectors.toList()`'s behavior using `reduce`
instead?* Technically yes, using the 3-arg overload with a mutable `ArrayList` accumulator, but
you'd be manually reimplementing what `collect`'s specialized combiner-based mutable-reduction
strategy already does more efficiently and idiomatically — there's no good reason to.

---

## 4b. Primitive streams

**[Basic] Why do `IntStream`/`LongStream`/`DoubleStream` exist separately from `Stream<Integer>`
etc.?**
To avoid **autoboxing overhead**. A `Stream<Integer>` stores boxed `Integer` objects — every element
is a separate heap allocation, and every arithmetic operation involves unbox-compute-rebox. The
primitive stream specializations store actual primitive values internally, avoiding that boxing
entirely, which matters a lot for numeric-heavy pipelines (`IntStream.range(0, 1_000_000).sum()`
never allocates a million `Integer` objects). They also expose numeric-specific terminal operations
plain object streams don't have out of the box: `sum()`, `average()`, `max()`, `min()`,
`summaryStatistics()`.
*Follow-up: how do you go from a `Stream<Integer>` to an `IntStream`, and back?*
`.mapToInt(Integer::intValue)` (unboxing map) to go to `IntStream`; `.boxed()` to go from `IntStream`
back to `Stream<Integer>` (re-boxing).

**[Intermediate] `Stream.iterate` vs `Stream.generate` — what's the structural difference, and how
do you bound either safely?**
`Stream.iterate(seed, unaryOperator)` produces an ordered, **deterministic** infinite sequence where
each element is derived from the previous one (`seed, f(seed), f(f(seed)), ...`) — inherently
sequential in nature (each step depends on the last). `Stream.generate(supplier)` produces an
infinite stream from a `Supplier<T>` with **no** dependency between elements (e.g.
`Math::random`, or a fixed constant supplier) — elements are independent, so it parallelizes more
naturally than `iterate`. Both are infinite by default and **must** be bounded with `.limit(n)` (or
a short-circuiting operation) or the pipeline never terminates.
```java
Stream.iterate(1, n -> n * 2).limit(5).forEach(System.out::println); // 1, 2, 4, 8, 16
```
*Follow-up: does Java 9's 3-arg `Stream.iterate(seed, predicate, unaryOperator)` overload remove the
need for `.limit()`?* Yes for a bounded, condition-based stop (it takes a `hasNext`-style predicate
and stops once it fails, similar to a classic `for` loop) — genuinely useful when the natural
stopping condition is a value check, not a fixed count.

**[Advanced] Why does calling `.parallel()` on a `Stream.iterate(...)`-based pipeline typically NOT
actually run in parallel, in practice?**
Because each element in an `iterate`-based stream is defined as a function of the **immediately
preceding** element — there's a genuine sequential data dependency the JVM cannot break without
computing every prior element first, so a naive parallel split can't meaningfully divide the work
among threads ahead of time (it would have to walk the whole chain sequentially to even know what
element N is, defeating the purpose of splitting). `Stream.generate` with an independent
`Supplier` (no inter-element dependency) parallelizes far more naturally since chunks genuinely can
be computed independently by different threads with no ordering dependency between them.
*Follow-up: does this mean iterate-based streams should never be parallelized?* Effectively yes for
the classic 2-arg form — it's a case where reaching for `.parallel()` provides no real benefit
(possibly net-negative, given coordination overhead) and is a good interview example of "parallel
streams aren't magic — the algorithm's data-dependency shape has to actually support it."

**[Basic] `IntStream.range(a, b)` vs `IntStream.rangeClosed(a, b)` — which bound is inclusive?**
`range(a, b)` is a **half-open** interval — `a` inclusive, `b` **exclusive** (matches typical
0-indexed loop semantics: `IntStream.range(0, list.size())` mirrors `for (int i = 0; i < size; i++)`
exactly). `rangeClosed(a, b)` is **fully inclusive** on both ends — `a` and `b` both included
(matches a `for (int i = 1; i <= n; i++)`-style 1-to-n loop). Picking the wrong one is a classic
off-by-one source when translating an existing loop into a stream.
*Follow-up: what does `IntStream.range(5, 5)` produce?* An empty stream — `range` with equal bounds
(or `from > to`) simply produces zero elements, not an error, same as an ordinary loop that never
executes its body.

---

## 5. Optional done right (and wrong)

**[Basic] What is `Optional<T>` for, and what is it explicitly NOT meant for?**
It's meant as a **return type** signaling "this method might legitimately have no result" — forcing
callers to consciously handle the absent case instead of silently risking a `NullPointerException`
on an unchecked `null`. It is explicitly **not** meant as: a field type (adds serialization
complications and object overhead for no real benefit over just allowing `null` fields internally),
a method parameter type (forces every caller to wrap arguments, awkward and not idiomatic — overload
or use `null`/a sentinel instead), or a general all-purpose null-replacement wrapped around
everything reflexively. The JDK's own Javadoc explicitly scopes `Optional` to method return values.
*Follow-up: why specifically is `Optional` a poor field type?* `Optional` doesn't implement
`Serializable`, adding a wrapper object per field costs extra memory/indirection for something a
plain nullable reference already expresses, and it doesn't compose well with common frameworks (JPA
entities, JSON (de)serialization) that expect plain nullable fields.

**[Basic] What's wrong with `optional.isPresent()` followed by `optional.get()`?**
It reintroduces exactly the imperative null-check pattern `Optional` was meant to replace —
verbose, and easy to get wrong (forgetting the `isPresent()` guard entirely, or a race in
concurrent code between the check and the get, though that's less common for `Optional` specifically
since it's typically a local value, not shared mutable state). The idiomatic alternative is the
functional style: `map`/`filter`/`ifPresent`/`ifPresentOrElse`/`orElse`/`orElseGet`/`orElseThrow`,
which express "do X if present, otherwise Y" declaratively without ever needing to manually call
`.get()`.
```java
// avoid:
if (opt.isPresent()) { System.out.println(opt.get().toUpperCase()); }
// prefer:
opt.map(String::toUpperCase).ifPresent(System.out::println);
```
*Follow-up: when, if ever, is `isPresent()`+`get()` acceptable?* Rare cases needing multiple
independent branches of logic on the same value that don't map cleanly onto `map`/`ifPresentOrElse`
— even then, `ifPresentOrElse` usually covers the two-branch case better; genuinely justified uses
are uncommon.

**[Intermediate] `orElse(x)` vs `orElseGet(Supplier<X>)` — what's the subtle performance trap?**
`orElse(x)` **always evaluates its argument eagerly**, even when the `Optional` is present and the
fallback won't be used — if `x` is an expensive call (a database query, a heavy computation, a new
object allocation), that cost is paid on every invocation regardless of whether the `Optional` was
actually empty. `orElseGet(Supplier<X>)` only invokes the supplier **lazily**, exactly when the
`Optional` is empty — no wasted work when a value was already present.
```java
// BUG: buildExpensiveDefault() runs every single time, even when opt is present
String result = opt.orElse(buildExpensiveDefault());
// FIX: only runs when opt is actually empty
String result = opt.orElseGet(() -> buildExpensiveDefault());
```
*Follow-up: does the same eager-vs-lazy distinction apply to `orElseThrow(x)` vs
`orElseThrow(Supplier<X>)`?* Yes, identically — a plain exception instance argument (if such an
overload existed) would be eagerly constructed regardless; the actual `orElseThrow(Supplier<?
extends X>)` overload lazily constructs the exception only when the `Optional` is empty, which also
avoids paying the (non-trivial, per Core Java stack-trace-capture cost) of constructing an exception
object that's usually never thrown.

**[Advanced] Why does `Optional.of(null)` throw immediately, while `Optional.ofNullable(null)`
doesn't — and what does this say about intended usage?**
`Optional.of(value)` asserts the caller is certain `value` is non-null — it throws
`NullPointerException` immediately if that assertion is violated, functioning as a **fail-fast
sanity check**, not a null-tolerant wrapper. `Optional.ofNullable(value)` is the actual "this might
be null, wrap it safely" factory, returning `Optional.empty()` for a `null` input instead of
throwing. The existence of both signals the JDK's intended discipline: use `of()` when you've
already established non-null-ness (documenting that certainty to readers and catching a violated
assumption immediately), and `ofNullable()` specifically at the boundary where a value's
null-ness is genuinely unknown/expected (e.g. wrapping the result of a possibly-`null`-returning
legacy API or `Map.get()`).
*Follow-up: is there ever a legitimate reason to store a "present but conceptually null" value in an
Optional?* No — `Optional<T>` cannot itself hold a `null` internally at all (both `of` and the
internal storage forbid it); "present" always means a genuinely non-null value is held, by design.

---

## 6. Parallel streams

**[Basic] How do you get a parallel stream, and what actually executes it?**
Either `collection.parallelStream()` (from the source) or `.parallel()` on an existing sequential
stream. Execution happens on the JVM's shared **common `ForkJoinPool`** (`ForkJoinPool.commonPool()`),
sized by default to `Runtime.getRuntime().availableProcessors() - 1` worker threads — **not** a
pool you control per-call by default, which has real consequences (see below).
*Follow-up: can you make a stream sequential again after calling `.parallel()`?* Yes — `.sequential()`
flips it back; the **last** call to `.parallel()`/`.sequential()` in the pipeline chain wins for the
whole pipeline (it's a pipeline-wide flag, not something toggled per-stage).

**[Intermediate] When does a parallel stream actually help, and when does it hurt?**
Helps when: the source is large (thousands+ elements — the fork/join splitting and thread
coordination overhead needs enough work to amortize against), the per-element work is genuinely
CPU-bound and substantial (not trivial), the source splits efficiently (arrays and `ArrayList`
split cheaply via index ranges; `LinkedList` and I/O-based sources split poorly, often serializing
most of the work anyway), and the operations are stateless/associative (no shared mutable state,
no operation order dependency). Hurts when: the collection is small (coordination overhead exceeds
any parallelism benefit), the work per element is I/O-bound or blocks a thread (blocking calls
starve the shared common pool of worker threads other unrelated code in the same JVM might also be
relying on — a classic cross-cutting production bug when *unrelated* parts of an application both
use `parallelStream()` and one starves the other), or the operations involve shared mutable state
(introducing race conditions the sequential version never had).
*Follow-up: give a concrete example of the "shared pool starvation" problem across unrelated code.*
Service A uses `.parallelStream()` with slow I/O-bound per-element work (e.g. blocking HTTP calls)
inside its stream operation; this ties up common-pool worker threads for a long time. Service B,
completely unrelated, also calls `.parallelStream()` elsewhere in the same JVM for genuinely
CPU-bound work — it now has to compete for (or wait behind) the same starved common pool, degrading
B's latency for a reason entirely invisible in B's own code.

**[Advanced] What thread-safety guarantee, if any, does `parallelStream().forEach(...)` give you
regarding the order side effects are applied, and how does `forEachOrdered` differ?**
Plain `forEach` on a parallel stream makes **no guarantee whatsoever** about the order in which the
action is applied across elements — different chunks run on different threads with no coordinated
ordering, so side effects (e.g. printing) can and will interleave unpredictably run to run.
`forEachOrdered` forces the action to run in the stream's **encounter order** (the order defined by
the source, if the stream is ordered) even when parallel — but doing so largely **defeats the
performance benefit** of parallelism for that stage, since it reintroduces a global ordering
constraint the parallel workers must respect, usually serializing much of the work again.
*Follow-up: if you don't care about output order but the action itself mutates a shared,
non-thread-safe `List`, is plain `forEach` safe?* No — regardless of ordering guarantees, mutating a
shared non-thread-safe collection from multiple threads inside `forEach`'s action is a race
condition (lost updates, `ArrayIndexOutOfBoundsException`) independent of the ordering question
entirely; use `.collect(Collectors.toList())` (a proper concurrent-safe mutable reduction) instead of
manually accumulating into a shared list from inside `forEach`.

---

## 7. Predict-the-output puzzles

**Puzzle 1 — laziness means `peek()` on a stream with no terminal operation never runs**
```java
Stream.of("a", "b", "c").peek(s -> System.out.println("peek: " + s));
System.out.println("done");
```
**Output:**
```
done
```
**Why:** `peek` is an intermediate operation — it only *describes* a pipeline stage. With no
terminal operation (`.forEach()`, `.collect()`, `.count()`, etc.) ever called, the pipeline never
executes at all; the `peek` lambda never runs once. This is a genuinely common silent bug — the code
compiles cleanly and produces no error, it simply does nothing.

**Puzzle 2 — short-circuiting means not every element gets processed**
```java
List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8);
Optional<Integer> result = nums.stream()
    .peek(n -> System.out.println("checking " + n))
    .filter(n -> n % 3 == 0)
    .findFirst();
System.out.println("result: " + result.get());
```
**Output:**
```
checking 1
checking 2
checking 3
result: 3
```
**Why:** `findFirst()` is a **short-circuiting** terminal operation — once it finds one element that
satisfies the whole upstream pipeline (`3` is the first multiple of 3), it stops pulling further
elements entirely. Elements `4` through `8` are never even visited by `peek`, because streams process
one element through the *entire* chain at a time (laziness) rather than running each stage to
completion over the whole source before moving to the next stage.
