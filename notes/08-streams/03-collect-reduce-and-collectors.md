# reduce, collect, and Collectors

## 1. What it is

`reduce` and `collect` are the two general-purpose ways to turn a stream into a single result.
`reduce` folds elements into one value via an associative combining function (sum, max, string
concatenation of immutable values). `collect` is more general: it accumulates elements into a
**mutable** result container (a list, map, `StringBuilder`, custom object) using a `Collector` —
and `java.util.stream.Collectors` ships dozens of ready-made ones: `toList`, `toMap` (with a merge
function), `groupingBy`, `partitioningBy`, `joining`, `counting`, `mapping`, and `teeing`.

## 2. How it works internally

### `reduce` — three overloads

```java
T reduce(T identity, BinaryOperator<T> accumulator);
Optional<T> reduce(BinaryOperator<T> accumulator);                 // no identity -> may be empty
<U> U reduce(U identity, BiFunction<U,T,U> accumulator, BinaryOperator<U> combiner); // parallel-safe
```
- **identity**: the starting value, and (crucially) the correct *no-op* result for an empty
  stream — for sum it's `0`, for a product it's `1`, for string concat it's `""`.
- **accumulator**: combines the running result with the next element — `(acc, e) -> acc + e`.
- **combiner** (3-arg form only): merges two partial results computed by different threads when
  the stream runs in parallel — must be consistent with the accumulator (`combiner(identity,
  accumulator(identity, x)) == accumulator(identity, x)`), otherwise parallel results diverge from
  sequential ones.
- The accumulator function **must be associative** — `(a op b) op c == a op (b op c)` — because
  the JDK is free to apply it in whatever grouping is convenient (especially in parallel, where
  the combiner merges results computed in arbitrary sub-orders).

```java
int totalSalary = employees.stream().reduce(0, (acc, e) -> acc + e.salary(), Integer::sum);
```
This works, but `Collectors.summingInt(Employee::salary)` (a purpose-built `Collector`) is
idiomatically preferred for this exact "sum a numeric field" case — `reduce` shown here mainly to
illustrate the mechanics.

### `collect` — the mutable-reduction protocol

`collect(Collector<T,A,R>)` uses four functions bundled into a `Collector`:
```
supplier:    () -> A            create a fresh, empty mutable accumulator (e.g. new ArrayList<>())
accumulator: (A, T) -> void      fold one element into the accumulator, in place
combiner:    (A, A) -> A         merge two accumulators (parallel sub-results) into one
finisher:    (A) -> R            convert the accumulator into the final public result type
```
Unlike `reduce`'s immutable fold (a new `T` each step), `collect`'s accumulator *mutates* a shared
container in place per element — this is what lets `Collectors.toList()` build one `ArrayList`
instead of allocating a new immutable list on every element (which `reduce` with an immutable
`List` would otherwise do, an O(n^2) trap — see pitfalls).

### `Collectors` factory tour

| Collector | Produces | Key gotcha |
|-----------|----------|------------|
| `toList()` / `toSet()` | `List<T>` / `Set<T>` | unspecified mutability/implementation unless you need a guarantee — prefer `Stream.toList()` (Java 16+) for a plain unmodifiable list |
| `toMap(keyFn, valueFn)` | `Map<K,V>` | **throws `IllegalStateException` on a duplicate key** unless you pass a 3rd merge-function argument |
| `toMap(keyFn, valueFn, mergeFn)` | `Map<K,V>` | merge function resolves collisions, e.g. `Integer::sum` |
| `groupingBy(classifier)` | `Map<K, List<T>>` | default downstream is `toList()` |
| `groupingBy(classifier, downstream)` | `Map<K, D>` | downstream can be `counting()`, `averagingInt()`, another `groupingBy` (nested), `mapping()`, etc. |
| `partitioningBy(predicate)` | `Map<Boolean, List<T>>` | **always has exactly two keys**, `true` and `false`, even if one group is empty |
| `joining(delim, prefix, suffix)` | `String` | only for `Stream<CharSequence>`/`Stream<String>` |
| `counting()` | `Long` | typically used as a `groupingBy` downstream for frequency counts |
| `mapping(fn, downstream)` | whatever `downstream` produces | lets you transform elements *before* they reach a downstream collector, e.g. extract names before grouping |
| `reducing(identity, mapper, op)` | same type as identity | a `Collector`-flavored `reduce`, usable as a `groupingBy` downstream |
| `teeing(c1, c2, merger)` (Java 12+) | whatever `merger` produces | fans the *same* stream to two collectors in one pass, then combines their results |

### `toMap` without a merge function — the classic trap

```java
Map<String, Integer> byFirstLetter = employees.stream()
        .collect(Collectors.toMap(e -> e.name().substring(0, 1), Employee::salary));
// IllegalStateException: Duplicate key A (attempted merging values 95000 and 88000)
// if two employees' names start with the same letter
```
`toMap`'s 2-arg form has no way to know what you want done with a colliding key — it deliberately
fails fast rather than silently dropping data. The 3-arg form's merge function
(`(existing, incoming) -> resolved`) tells it exactly how to combine.

### `groupingBy` with a downstream collector

```java
Map<String, Long> countByDept =
        employees.stream().collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
Map<String, List<String>> namesByDept = employees.stream()
        .collect(Collectors.groupingBy(
                Employee::department, Collectors.mapping(Employee::name, Collectors.toList())));
```
`groupingBy(classifier)` alone is shorthand for `groupingBy(classifier, toList())`. The downstream
collector runs *within each group* — so `groupingBy(dept, counting())` gives a per-department
count, `groupingBy(dept, averagingInt(salary))` gives a per-department average, and nesting
`groupingBy(accountId, groupingBy(month, ...))` produces a two-level map (used heavily in topic 7).

### `Collectors.teeing` — one pass, two answers

```java
record MinMax(Employee min, Employee max) {}
MinMax minMax = employees.stream()
        .collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingInt(Employee::salary)),
                Collectors.maxBy(Comparator.comparingInt(Employee::salary)),
                (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())));
```
Without `teeing`, computing "both the min and the max" would naively require streaming the source
twice (`stream().min(...)` then a *second* `stream().max(...)`) — impossible anyway if the source
is a genuinely single-pass stream. `teeing` fans every element to *both* downstream collectors in
the same single pass, then combines their two final results with the merge `BiFunction`.

### A hand-built `Collector` via `Collector.of`

```java
Collector.of(Box::new, Box::accept, Box::combine, Box::toStats);
//           supplier   accumulator  combiner     finisher
```
This is exactly the four-function protocol `Collectors.toList()` etc. are built from — see
`CustomStatsCollectorSolution` for a full count/sum/min/max collector built by hand, whose
`combine` must correctly merge two independently-accumulated `Box`es so the collector gives the
same answer whether the source stream is sequential or parallel.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `reduce` | O(n) | O(1) extra (immutable fold) | each step produces a new value, no shared mutable state |
| `collect(toList/toSet)` | O(n) amortized | O(n) | mutable accumulator grows like `ArrayList`/`HashSet` |
| `collect(toMap)` | O(n) average | O(n) | hash-based; a colliding key without a merge fn throws mid-collection |
| `groupingBy` | O(n) | O(n) | one map entry per distinct classifier key, downstream cost is per-group |
| `partitioningBy` | O(n) | O(n) | always exactly 2 output groups regardless of data |
| `joining` | O(total chars) | O(total chars) | backed by a `StringBuilder` internally, not repeated `String` concatenation |
| `teeing` | O(n) single pass | O(1) extra beyond the two downstream collectors | avoids a second pass over the source |
| custom `Collector.of` | O(n) sequential, O(n/p + combines) parallel | depends on the accumulator | correctness requires an associative, thread-safe-per-partition `combiner` |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/CollectorsDemo.java`

```java
Map<String, Long> countByDept = employees.stream()
        .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
System.out.println("countByDept = " + countByDept);
```
Expected console output (computed from the demo's fixed employee list — Alice/Bob ENGINEERING,
Cara/Dan/Eve SALES, Finn HR):
```
total salary via reduce = 433000
names = [Alice, Bob, Cara, Dan, Eve, Finn]
countByDept = {HR=1, SALES=3, ENGINEERING=2}    (HashMap order not guaranteed, values are fixed)
namesByDept = {HR=[Finn], SALES=[Cara, Dan, Eve], ENGINEERING=[Alice, Bob]}
avgSalaryByDept = {HR=55000.0, SALES=65000.0, ENGINEERING=91500.0}
highEarners = [Alice, Bob, Eve]
otherEarners = [Cara, Dan, Finn]
csv = [Alice, Bob, Cara, Dan, Eve, Finn]
lowest paid = Finn, highest paid = Alice
```

## 5. When to use / when NOT to use

- Use `reduce` for simple associative folds over immutable values (sum, max of primitives/
  `Comparable`s, string concat with few elements) — prefer a dedicated `Collectors` factory
  (`summingInt`, `averagingDouble`, `joining`) when one exists, it's clearer and usually faster.
- Use `collect` with a `Collectors` factory for anything that builds a mutable result (list, map,
  grouped structure) — this is the vast majority of real aggregation code.
- Use `toMap`'s 3-arg (merge function) form whenever the key isn't provably unique — defaulting to
  the 2-arg form on real-world data is asking for a production `IllegalStateException`.
- Use `teeing` when you need two independent aggregates from one stream and re-streaming the
  source is wasteful or impossible (e.g. a genuinely one-shot I/O-backed stream).
- Don't hand-roll a custom `Collector` unless no `Collectors` factory (possibly composed via
  `groupingBy`/`mapping`/`teeing`) fits — it's more code to get the combiner correctly associative,
  which matters the moment someone parallelizes the stream later.

## 6. Common pitfalls & gotchas

**`toMap` without a merge function on non-unique keys:**
```java
// BUG: throws IllegalStateException the moment two employees share a first-letter key
Map<String, Integer> m = employees.stream()
        .collect(Collectors.toMap(e -> e.name().substring(0, 1), Employee::salary));

// FIX: supply a merge function
Map<String, Integer> m2 = employees.stream()
        .collect(Collectors.toMap(e -> e.name().substring(0, 1), Employee::salary, Integer::sum));
```

**`reduce` with an immutable accumulator type used like a mutable collection — accidental O(n^2):**
```java
// BUG: List.of(...) + one element creates a brand-new immutable list every step -> O(n^2) total
List<String> names = employees.stream()
        .reduce(List.<String>of(), (list, e) -> {
            List<String> next = new ArrayList<>(list);
            next.add(e.name());
            return next;
        }, (a, b) -> a);

// FIX: this is exactly what collect(toList()) exists for — one mutable accumulator, O(n)
List<String> names2 = employees.stream().map(Employee::name).collect(Collectors.toList());
```

**Nested generic `Collectors.teeing(...)` calls that javac can't fully infer — a real compile
error seen in this module:**
```java
// FAILS TO COMPILE: "inference variable T has incompatible upper bounds" — javac cannot unify T
// across minBy/maxBy/teeing when everything is nested in one expression and T is only
// target-type-driven (Comparator.naturalOrder() gives no concrete argument to anchor T on)
return numbers.stream()
        .collect(Collectors.teeing(
                Collectors.minBy(Comparator.naturalOrder()),
                Collectors.maxBy(Comparator.naturalOrder()),
                (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())));

// FIX: bind each Collector to an explicit, concrete local variable type first — this gives the
// compiler a fixed target type per call instead of one big nested inference problem
Collector<Integer, ?, Optional<Integer>> minCollector = Collectors.minBy(Comparator.naturalOrder());
Collector<Integer, ?, Optional<Integer>> maxCollector = Collectors.maxBy(Comparator.naturalOrder());
return numbers.stream()
        .collect(Collectors.teeing(minCollector, maxCollector,
                (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())));
```
This exact fix was applied to `src/main/java/com/gk/study/streams/solutions/MinMaxViaTeeingSolution.java`
in this module (see the module's exercise M03 below) — worth knowing this failure mode exists even
though `CollectorsDemo`'s own `teeing` call compiles fine, because there `Comparator.comparingInt
(Employee::salary)` gives the compiler a concrete method-reference argument to anchor `T = Employee`
on immediately, instead of relying purely on downstream target typing.

**A custom `Collector`'s `combiner` not being associative/consistent with the accumulator** —
correct sequentially, silently wrong only under `.parallelStream()` because the combiner is never
even invoked on a purely sequential stream; always test a custom `Collector` with a parallel
stream too (see `CustomStatsCollectorTest`'s `parallelStreamExercisesTheCombiner` test).

## 7. Interview questions

- [Basic] What's the difference between `reduce` and `collect`? → `reduce` folds elements into a
  new *immutable* result each step via an accumulator function; `collect` mutates a single shared
  *mutable* container (list, map, etc.) in place across all elements, using a 4-function
  `Collector` (supplier/accumulator/combiner/finisher). → Follow-up: *Why is `collect` generally
  preferred for building collections?* Because the mutable-container approach avoids repeatedly
  allocating a new immutable result at every step, which `reduce` would otherwise do — an O(n^2)
  trap for something like accumulating into a `List`.
- [Basic] What happens if `Collectors.toMap` finds two elements with the same key and you didn't
  supply a merge function? → It throws `IllegalStateException` at collection time, reporting the
  duplicate key and both colliding values — it deliberately fails fast rather than silently
  dropping one. → Follow-up: *How do you fix it?* Pass a third argument, a `BinaryOperator<V>`
  merge function, e.g. `Integer::sum` to add colliding values or `(a, b) -> a` to keep the first.
- [Basic] What does `Collectors.partitioningBy` guarantee about its result map that `groupingBy`
  doesn't? → The result always has exactly two keys, `Boolean.TRUE` and `Boolean.FALSE`, even if
  one partition is empty — `groupingBy` only produces keys that actually occurred in the data. →
  Follow-up: *When would you prefer `partitioningBy` over `groupingBy(predicate)`?* When you always
  want both branches present and addressable (e.g. `.get(true)`/`.get(false)` without a null
  check), such as a pass/fail split you'll always report both sides of.
- [Intermediate] Walk through the four functions a `Collector` is built from. → `supplier`
  (`() -> A`) creates a fresh empty mutable accumulator; `accumulator` (`(A, T) -> void`) folds one
  element into it in place; `combiner` (`(A, A) -> A`) merges two accumulators together (used when
  the stream is processed in parallel and partial results need merging); `finisher` (`A -> R`)
  converts the accumulator into the final public result type. → Follow-up: *When is the combiner
  actually invoked on a purely sequential stream?* Never — sequential collection uses a single
  accumulator throughout; the combiner only matters for `.parallelStream()`, which is exactly why
  a broken combiner can pass all your sequential tests and still be wrong.
- [Intermediate] How would you compute both the minimum and maximum of a stream in a single pass
  without collecting to a list first? → `Collectors.teeing(minBy(cmp), maxBy(cmp), (min, max) ->
  combine(min, max))` — it fans every element to both downstream collectors in one pass and merges
  their two final `Optional` results at the end. → Follow-up: *What problem does `teeing` solve
  that calling `.min()` then `.max()` separately would create?* Streaming the source twice — which
  is wasteful for an expensive-to-produce source, and outright impossible for a genuinely one-shot
  stream (e.g. backed by a network/file read that can't be replayed).
- [Advanced] Why can `Collectors.teeing(Collectors.minBy(Comparator.naturalOrder()), ...)` fail to
  compile with an "incompatible upper bounds" error when written as one nested expression, even
  though the logic is correct? → `Comparator.naturalOrder()` is a generic method whose type
  parameter `T` is resolved purely from target typing (there's no concrete argument to anchor it
  on); nesting it three levels deep inside `minBy` inside `teeing` inside `collect` gives javac
  several simultaneous inference variables it must unify from context alone, and its inference
  algorithm can fail to find a consistent solution even though a valid one exists. Extracting each
  `Collectors.minBy/maxBy(...)` call into its own explicitly-typed local variable
  (`Collector<Integer, ?, Optional<Integer>> minCollector = ...`) gives the compiler a concrete,
  already-resolved type to feed into `teeing`, sidestepping the nested-inference failure. →
  Follow-up: *Why does `CollectorsDemo`'s teeing call with `Comparator.comparingInt(Employee::
  salary)` compile fine as one expression?* Because `comparingInt`'s key-extractor argument
  (`Employee::salary`) is a concrete method reference that pins `T = Employee` immediately at that
  call site, rather than needing to be inferred purely from the surrounding expression's target
  type.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| M02 | Medium | Frequency map of department -> headcount | `groupingBy(identity, counting())` | `exercises/DepartmentHeadcount.java` |
| M03 | Medium | Min and max of a list in one pass | `Collectors.teeing(minBy, maxBy, combiner)` | `exercises/MinMaxViaTeeing.java` |
| H01 | Hard | Build-it-yourself: a custom `Collector` (count/sum/min/max in one pass) | `Collector.of(supplier, accumulator, combiner, finisher)` | `exercises/CustomStatsCollector.java` |

- <details><summary>Hint (M02)</summary>`employeeNameToDepartment.values().stream()` gives a
  `Stream<String>` of department names; `Collectors.groupingBy(Function.identity(),
  Collectors.counting())` is the textbook frequency-map idiom.</details>
- <details><summary>Hint (M03)</summary>Two `Collectors.minBy`/`maxBy` calls fed into
  `Collectors.teeing`, combined into the `MinMax` record — watch the nested-generics compile trap
  described in the pitfalls above if you inline everything into one expression.</details>
- <details><summary>Hint (H01)</summary>The mutable `Box` accumulator (accept/combine/toStats) is
  already provided — just wire it into `Collector.of(Box::new, Box::accept, Box::combine,
  Box::toStats)`.</details>

Solutions are in `src/main/java/com/gk/study/streams/solutions/` (`DepartmentHeadcountSolution`,
`MinMaxViaTeeingSolution`, `CustomStatsCollectorSolution`) — attempt the stubs in `exercises/`
first.

## 9. Quick recap

- `reduce` folds into a new immutable value each step; `collect` mutates one shared accumulator —
  prefer `collect` for building collections, it avoids `reduce`'s O(n^2) immutable-rebuild trap.
- `Collectors.toMap` throws `IllegalStateException` on a duplicate key unless you supply a 3rd
  merge-function argument — always consider whether your key is truly unique.
- `groupingBy(classifier, downstream)` runs the downstream collector per group — nest it for
  multi-level grouping, or pair with `mapping()`/`counting()`/`averagingInt()` for common shapes.
- `partitioningBy` always yields both `true` and `false` keys; `teeing` (Java 12+) computes two
  independent aggregates from one single pass over the source.
- A custom `Collector.of(supplier, accumulator, combiner, finisher)` must have a correct,
  associative combiner — it's never even exercised on a sequential stream, so test it in parallel
  too; and beware nested generic `teeing`/`minBy`/`maxBy` calls tripping up javac's inference when
  the comparator's type isn't pinned by a concrete argument.
