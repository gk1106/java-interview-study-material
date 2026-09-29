# Intermediate operations: map / filter / flatMap / distinct / sorted / limit / skip / peek

## 1. What it is

Intermediate operations are the transformation stages of a stream pipeline — each takes a stream
and returns a *new* stream with the transformation described, but (per topic 1) does no work
until a terminal operation runs. This topic covers the eight you'll use constantly: `map`,
`filter`, `flatMap`, `distinct`, `sorted`, `limit`, `skip`, and `peek`.

## 2. How it works internally

| Operation | What it does | Stateless or stateful? |
|-----------|---------------|--------------------------|
| `map(Function)` | transforms each element 1:1 | stateless |
| `filter(Predicate)` | keeps elements matching a predicate, drops the rest | stateless |
| `flatMap(Function<T, Stream<R>>)` | maps each element to a *stream*, then flattens all of those streams into one | stateless |
| `distinct()` | removes duplicates (via `equals`/`hashCode`) | **stateful** — must remember every element seen so far |
| `sorted()` / `sorted(Comparator)` | orders elements | **stateful** — must buffer the whole stream to sort it |
| `limit(n)` | truncates to the first n elements | stateful but short-circuiting |
| `skip(n)` | discards the first n elements, keeps the rest | stateful (must count) |
| `peek(Consumer)` | runs a side-effecting action per element, passes the element through unchanged | stateless |

**Stateless vs. stateful matters for laziness and parallelism**: a stateless op (`map`,
`filter`) can process element 1 completely independently of element 2 — it fits the
one-element-at-a-time depth-first model from topic 1 perfectly, and parallelizes trivially. A
stateful op (`distinct`, `sorted`) *must* see elements it can't yet act on — `sorted()` cannot
emit its first output element until every input element has arrived (there might be a smaller one
still coming), so it internally buffers the entire stream into an array, sorts it, and only then
starts emitting — this breaks the "process depth-first, one at a time" pipeline model for
everything downstream of it, and adds an O(n) space cost, plus O(n log n) time.

### `flatMap` — flattening a stream of streams

```java
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
nested.stream().flatMap(List::stream).toList(); // [1, 2, 3, 4, 5]
```
`flatMap` differs from `map` in return type: `map(Function<T,R>)` gives a `Stream<R>` where each
input produces exactly one output; `flatMap(Function<T, Stream<R>>)` lets each input produce
*zero, one, or many* outputs (a sub-stream per input), and internally those sub-streams are
concatenated into a single flat output stream, lazily, as elements are pulled.

### ASCII diagram — `distinct` + `sorted` breaking single-pass streaming

```
source: [banana, apple, cherry, apple, date, banana, fig]

distinct()  ->  must track every element seen in a Set-like structure internally
                buffer: {banana, apple, cherry, date, fig}   (dedup happens incrementally
                                                                as each element arrives)

sorted()    ->  cannot emit ANYTHING until the whole upstream has been pulled and buffered,
                because element N might sort before element 1
                [apple, banana, cherry, date, fig]   (sorted only after full buffering)

limit(3)    ->  short-circuits: once 3 elements have been emitted downstream, upstream is
                never pulled again — but note limit() AFTER sorted() still had to wait for
                sorted's full buffering first; limit() BEFORE a stateful op can short-circuit
                the whole upstream, limit() AFTER one cannot save the stateful op's cost
```
This is why `words.stream().distinct().sorted().limit(3)` still processes and sorts *every*
distinct word even though only 3 are ultimately kept — `limit` runs after `sorted`, so it can't
prevent `sorted`'s full buffering.

### `peek` — debugging only, not business logic

`peek` exists to run a side-effecting action (typically logging/tracing) *between* two stages
without altering what flows through — every element it sees passes through unchanged. It is
**not** meant to drive real computation (mutating external state, populating a collection): the
JDK explicitly documents this as an anti-pattern, and — crucially — if a downstream operation is
short-circuiting or the pipeline is never fully consumed, `peek` may run on *fewer* elements than
you expect, making it unreliable for anything beyond debugging visibility.

## 3. Complexity

| Operation | Time | Extra space | Notes |
|-----------|------|--------------|-------|
| `map` / `filter` / `peek` | O(1) per element | O(1) | stateless, fits the single-pass pipeline |
| `flatMap` | O(1) amortized per emitted element | O(1) (streams concatenated lazily) | total cost is proportional to total output size, not input size |
| `distinct()` | O(n) time | O(n) space | internally backed by a hash-based structure to detect duplicates |
| `sorted()` | O(n log n) time | O(n) space | must buffer and sort the entire stream before emitting anything |
| `limit(n)` | O(min(n, upstream cost)) | O(1) | short-circuiting — can stop pulling from upstream once n elements are produced |
| `skip(n)` | O(n) to discard + O(remaining) | O(1) | must still pull and discard the first n elements from upstream |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/IntermediateOpsDemo.java`

```java
List<String> words = List.of("banana", "apple", "cherry", "apple", "date", "banana", "fig");
System.out.println("distinct: " + words.stream().distinct().toList());
System.out.println("sorted (natural): " + words.stream().distinct().sorted().toList());
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
System.out.println("flatMap (flatten list-of-lists): " + nested.stream().flatMap(List::stream).toList());
```
Expected console output (abridged):
```
map (uppercase): [BANANA, APPLE, CHERRY, APPLE, DATE, BANANA, FIG]
filter (length > 4): [banana, apple, cherry, banana]
distinct: [banana, apple, cherry, date, fig]
sorted (natural): [apple, banana, cherry, date, fig]
sorted (by length desc): [banana, cherry, apple, date, fig]
limit(3) after sorted: [apple, banana, cherry]
skip(2) after sorted: [cherry, date, fig]
flatMap (flatten list-of-lists): [1, 2, 3, 4, 5]
```

## 5. When to use / when NOT to use

- `map`/`filter`/`flatMap` are the everyday transformation workhorses — chain them freely, they
  stay cheap and single-pass.
- Use `distinct()`/`sorted()` only when you actually need dedup/ordering — they're the two
  intermediate ops with real O(n) time and space cost and break single-pass streaming; putting
  `sorted()` before a `limit(k)` when you only need the top/bottom k is wasteful — prefer a
  bounded-heap approach (`PriorityQueue`, see the queue-deque module) for very large streams if
  k << n.
- Use `peek` only for debugging/tracing during development; never rely on it to perform business
  logic or populate external state — use `map`/`collect` instead.
- Put `limit`/short-circuiting predicates **as early as possible** in the pipeline (before
  `sorted`/`distinct` when semantics allow) so the stateful, expensive stages see fewer elements.

## 6. Common pitfalls & gotchas

**Using `peek` to drive real logic instead of `map`/`forEach`:**
```java
// BUG: relies on peek for a side effect that changes program state — fragile, may not
// even run on every element if the pipeline is short-circuited or optimized away
List<String> collected = new ArrayList<>();
words.stream().peek(collected::add).filter(w -> w.length() > 4).toList();

// FIX: use the right operation for the job
List<String> collected2 = words.stream().filter(w -> w.length() > 4).toList();
```

**`sorted()` before `limit()` when only a top-k is needed on a huge stream** — sorts the *entire*
stream (O(n log n), O(n) space) even though only k results are kept:
```java
// Works, but sorts everything even for k=3 on a million-element stream
words.stream().sorted().limit(3).toList();
// Better for large n, small k: a bounded max-heap (see 04-queue-deque module) gives O(n log k)
```

**Confusing `map` and `flatMap` on a nested collection:**
```java
// BUG: map produces Stream<Stream<Integer>> — wrong shape, and won't compile as List<Integer>
nested.stream().map(List::stream);

// FIX: flatMap flattens the inner streams into one
nested.stream().flatMap(List::stream).toList();
```

**Calling `distinct()`/`sorted()` on a stream of mutable objects whose `equals`/`hashCode` or
natural ordering change mid-pipeline** — same class of bug as mutating a `HashMap` key (see
`notes/06-map/01-hashmap-internals.md`): dedup/ordering are computed against the object's state
*as `distinct`/`sorted` observes it*, so mutating an element after it's been buffered but before
sort/compare finishes gives inconsistent results.

## 7. Interview questions

- [Basic] What's the difference between `map` and `filter`? → `map` transforms every element 1:1
  into a (possibly different type of) output element; `filter` keeps or discards each element
  based on a `Predicate`, never changing the ones it keeps. → Follow-up: *Can `filter` change the
  stream's element type?* No — `Predicate<T>` only tests, it can't produce a different type;
  that's exactly what `map` is for.
- [Basic] Why do you need `flatMap` instead of `map` when each element maps to a collection? →
  `map` would produce a `Stream<Stream<R>>` (or `Stream<List<R>>`) — a nested shape; `flatMap`
  flattens the per-element sub-streams into one single-level output stream. → Follow-up: *What
  does `flatMap` return for an element that maps to an empty stream?* Nothing — zero elements
  contributed to the output, which is exactly how you'd filter-and-map in one step for
  collection-valued fields.
- [Basic] Is `peek` allowed to be relied on for business logic? → No — it's documented and
  intended purely for debugging/tracing; the JDK doesn't guarantee `peek`'s action runs on every
  element in every circumstance (e.g. under short-circuiting or certain optimizations), so
  functional correctness must never depend on it. → Follow-up: *What should you use instead if you
  need a side effect per element as part of the actual result?* `map` (if it also transforms) or
  the terminal `forEach`, whichever matches what you're actually trying to compute.
- [Intermediate] Why is `sorted()` called a "stateful" intermediate operation, and what does that
  cost? → It cannot emit any output element until it has seen the entire upstream, because any
  later element might need to sort before an earlier one already emitted — so internally it
  buffers everything into an array and sorts (O(n log n) time, O(n) space) before emitting a
  single result. → Follow-up: *Does that mean `sorted()` defeats short-circuiting entirely for the
  whole pipeline?* Not entirely — a `limit()` placed *before* `sorted()` still limits what reaches
  it; but a `limit()` placed *after* `sorted()` cannot prevent `sorted()` from consuming and
  buffering the full upstream first.
- [Intermediate] What's the practical performance difference between `words.stream().sorted()
  .limit(3)` and a bounded-heap top-k approach for a huge input? → `sorted().limit(3)` is O(n log
  n) time and O(n) space regardless of k, because it fully sorts before truncating; a bounded
  max-heap of size k (via `PriorityQueue`) is O(n log k) time and O(k) space — for k << n this is
  a large practical win, especially on very large streams. → Follow-up: *When would you still
  prefer `sorted().limit(3)` despite the cost?* When n is small, or when the pipeline needs the
  full sorted order for other reasons anyway (not just the top k), or simply for code clarity when
  performance isn't a concern.
- [Advanced] How does `distinct()` decide two elements are duplicates, and what's the risk with a
  custom class? → It relies on the elements' `equals()`/`hashCode()` contract, internally using a
  hash-based structure similar to `HashSet` to track what's been seen. If a custom class has a
  broken or missing `equals`/`hashCode` override (default identity-based), `distinct()` silently
  treats every instance as unique even if they represent the same logical value — the classic
  "duplicates weren't removed" bug traces straight back to the `equals`/`hashCode` contract (see
  `notes/01-java-foundations-for-dsa`). → Follow-up: *Does `distinct()` preserve encounter order?*
  Yes for ordered streams — the JDK spec guarantees `distinct()` keeps the first occurrence's
  position and drops later duplicates, for streams with a defined encounter order.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Filter+uppercase first names starting with a vowel, sorted | filter + map + collect | `exercises/FilterAndMapNames.java` |
| E02 | Easy | Count words in a sentence longer than a threshold | split + stream + filter + count | `exercises/CountLongWords.java` |
| M01 | Medium | Flatten a list of lists of integers, distinct + sorted | flatMap + distinct + sorted | `exercises/FlattenListOfLists.java` |

- <details><summary>Hint (E01)</summary>Split each `"first last"` entry on the first space,
  `filter` by whether the first character (case-insensitively) is a vowel, `map` to uppercase, then
  `sorted()` before collecting.</details>
- <details><summary>Hint (E02)</summary>`sentence.trim().split("\\s+")` handles repeated/leading/
  trailing spaces; filter blanks defensively, then filter by `length() > minLength`, then
  `count()`.</details>
- <details><summary>Hint (M01)</summary>`lists.stream().flatMap(List::stream)` gives one flat
  `Stream<Integer>`; chain `.distinct().sorted()` before collecting to a `List`.</details>

Solutions are in `src/main/java/com/gk/study/streams/solutions/` (`FilterAndMapNamesSolution`,
`CountLongWordsSolution`, `FlattenListOfListsSolution`) — attempt the stubs in `exercises/` first.

## 9. Quick recap

- `map`/`filter`/`flatMap`/`peek` are stateless — one element in, immediately available out, no
  buffering, parallelize trivially.
- `distinct()`/`sorted()` are stateful — they must see the whole stream before emitting anything,
  costing O(n) extra space (and O(n log n) time for `sorted`).
- `flatMap` maps each element to a sub-stream and flattens all sub-streams into one flat output —
  use it whenever a mapping produces a collection/stream per input, not a single value.
- Put cheap filters and `limit()` as early as possible in the pipeline, before expensive stateful
  ops, so they see fewer elements.
- `peek` is for debugging only — never rely on it for real computation or side effects that affect
  program correctness.
