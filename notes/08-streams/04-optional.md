# Optional done right

## 1. What it is

`Optional<T>` is a container that either holds a non-null value or is empty — a type-level
alternative to returning `null` from a method, forcing the caller to explicitly handle the "no
value" case instead of risking a `NullPointerException` from an unchecked assumption. It shines
when combined into `map`/`filter`/`flatMap` chains that read like a pipeline instead of a tower of
nested null checks.

## 2. How it works internally

`Optional<T>` is a small immutable wrapper around either a non-null reference or nothing — think
of it as a stream of zero or one element (indeed several of its methods intentionally mirror
`Stream`'s vocabulary: `map`, `filter`, `flatMap`). It is a **value-based class**: don't use it as
a `synchronized` lock target, don't rely on `==` identity, and never use it as a `Map` key or a
field you serialize casually (see when-not-to-use).

### Creation

```java
Optional<String> present = Optional.of("hello");          // throws NPE immediately if arg is null
Optional<String> maybeAbsent = Optional.ofNullable(lookup("missing-key")); // null-safe wrap
Optional<String> empty = Optional.empty();
```
`Optional.of` is a deliberate fail-fast: if you're certain the value can't be null, `of` catches
a violated assumption immediately at the construction site rather than letting a silent empty
`Optional` propagate confusion downstream. `ofNullable` is the one to reach for when the source
(a map lookup, a nullable field) might legitimately be absent.

### Chaining instead of nested null checks

```java
int timeout = Optional.ofNullable(CONFIG.get("timeoutMs"))
        .filter(s -> !s.isBlank())
        .map(Integer::parseInt)
        .orElse(1000);
```
Compare to the imperative equivalent:
```java
String raw = CONFIG.get("timeoutMs");
int timeout;
if (raw != null && !raw.isBlank()) {
    timeout = Integer.parseInt(raw);
} else {
    timeout = 1000;
}
```
Both are correct; the `Optional` chain scales better as more steps (more possible absences) are
added — each step is one more `.filter`/`.map`, not one more nested `if`.

### `flatMap` — avoiding `Optional<Optional<T>>`

```java
Optional<String> city = findCustomer("C1").flatMap(OptionalDemo::findCity);
// findCustomer: String -> Optional<String>, findCity: String -> Optional<String>
// map(...findCity) would give Optional<Optional<String>> — flatMap flattens one level, same
// idea as Stream.flatMap
```

### `orElse` vs `orElseGet` — eager vs lazy default

```java
present.orElse(expensiveDefault());        // expensiveDefault() ALWAYS runs, even though present
present.orElseGet(OptionalDemo::expensiveDefault); // supplier only invoked if actually empty
```
`orElse(T)` takes a plain value — Java must evaluate that argument expression *before* calling
`orElse`, exactly like any other method call, regardless of whether the `Optional` is present.
`orElseGet(Supplier<T>)` takes a lazy supplier that's only invoked if the `Optional` turns out to
be empty. For a cheap constant this distinction is invisible; for an expensive computation (a DB
call, object construction) it's a real, easy-to-miss performance bug.

### `orElseThrow` with a specific exception, and `ifPresentOrElse`

```java
empty.orElseThrow(() -> new IllegalStateException("no value configured")); // throws with a clear message
empty.ifPresentOrElse(
        v -> System.out.println("has value: " + v),
        () -> System.out.println("nothing present, ran the else branch"));
```
`orElseThrow()` (no-arg) throws a generic `NoSuchElementException` — barely better than a bare
`get()`. The 1-arg overload lets you throw a specific, informative exception, which is what
production code should almost always do. `ifPresentOrElse` is the "do X if present, else do Y"
pattern in one call, without a separate `isPresent()` branch.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `of` / `ofNullable` / `empty` | O(1) | O(1) | trivial wrapper allocation |
| `map` / `filter` / `flatMap` | O(1) + cost of the function | O(1) | short-circuits immediately if already empty — the function is never invoked |
| `orElse(T)` | O(1) + cost of evaluating the argument | O(1) | **argument is always evaluated**, present or not |
| `orElseGet(Supplier)` | O(1), supplier cost only if empty | O(1) | lazy — the whole point |
| `get()` / `orElseThrow()` | O(1) | O(1) | throws if empty — `get()` throws generic `NoSuchElementException` with no context |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/streams/examples/OptionalDemo.java`

```java
System.out.println("orElse (eager arg always built): " + present.orElse(expensiveDefault()));
System.out.println("orElseGet (lazy, only runs if empty): " + present.orElseGet(OptionalDemo::expensiveDefault));
```
Expected console output:
```
present=Optional[hello] maybeAbsent=Optional.empty empty=Optional.empty
timeout = 3000
city = Mumbai
  (expensiveDefault() was actually invoked)
orElse (eager arg always built): computed-default
orElseGet (lazy, only runs if empty): hello
caught expected: no value configured
ifPresentOrElse: nothing present, ran the else branch
firstOdd present? false -> -1
```
Note `expensiveDefault()`'s print appears even for `orElse` on a *present* `Optional` — proof the
argument is evaluated eagerly regardless of outcome; it does **not** print a second time for
`orElseGet`, because the supplier is never invoked when the `Optional` is present.

## 5. When to use / when NOT to use

- Use `Optional<T>` as a **method return type** for "may legitimately have no result" (a lookup, a
  search, a parse) — this is its intended, primary use case.
- Prefer chaining `map`/`filter`/`flatMap`/`orElse` over manual `isPresent()`/`get()` pairs — the
  chain form makes the "what if absent" handling impossible to forget.
- Avoid `Optional` as a **field type** (serialization frameworks, JPA entities, and Java's own
  `Serializable` don't handle it well; it adds an extra wrapper allocation per field for no real
  benefit over just allowing `null` on internal fields).
- Avoid `Optional` as a **method parameter type** — it makes call sites more awkward
  (`method(Optional.of(x))`) than simply overloading or accepting `@Nullable`-style plain
  arguments; the JDK team has said this was never the intended use.
- Avoid `Optional<Collection<T>>` — return an empty collection instead of an empty `Optional`
  wrapping a collection; callers can iterate an empty list for free without an `isPresent` check.
- Never use `Optional.of(x)` when `x` might be null — use `ofNullable` instead, or you've just
  converted a potential `NullPointerException` at the call site into one thrown one line earlier.

## 6. Common pitfalls & gotchas

**Calling `get()` (or the old-style `isPresent()` + `get()`) without checking — reintroduces the
exact NPE-style crash `Optional` exists to prevent:**
```java
List<Integer> numbers = List.of(2, 4, 6);
Optional<Integer> firstOdd = numbers.stream().filter(n -> n % 2 != 0).findFirst();
int value = firstOdd.get(); // BUG: throws NoSuchElementException, no odd number in this list

// FIX: check, or supply a default / specific exception
int value2 = firstOdd.orElse(-1);
int value3 = firstOdd.orElseThrow(() -> new IllegalStateException("no odd number found"));
```

**`orElse` with an expensive eagerly-evaluated argument:**
```java
// BUG: buildExpensiveFallback() runs on EVERY call, even when cache.get(key) is present
return cache.get(key).orElse(buildExpensiveFallback());

// FIX: orElseGet defers the supplier until actually needed
return cache.get(key).orElseGet(this::buildExpensiveFallback);
```

**`map` instead of `flatMap` when the mapping function itself returns `Optional`:**
```java
// BUG: produces Optional<Optional<String>> — usually a compile error, or an awkward double-unwrap
Optional<Optional<String>> nested = findCustomer("C1").map(OptionalDemo::findCity);

// FIX: flatMap flattens the extra layer
Optional<String> city = findCustomer("C1").flatMap(OptionalDemo::findCity);
```

**Using `Optional.of(possiblyNullValue)`:**
```java
// BUG: if lookup(key) returns null, Optional.of throws NullPointerException immediately
Optional<String> maybe = Optional.of(lookup(key));

// FIX: ofNullable is the null-safe constructor
Optional<String> maybe2 = Optional.ofNullable(lookup(key));
```

## 7. Interview questions

- [Basic] Why was `Optional` added to Java, and what problem does it solve? → To make "this method
  might not return a value" explicit in the type system, forcing callers to handle absence rather
  than silently risking a `NullPointerException` from an unchecked `null` return. → Follow-up: *Does
  `Optional` eliminate `NullPointerException` from your codebase?* No — you can still call `.get()`
  blind on an empty `Optional` (throwing `NoSuchElementException` instead) or pass `null` as an
  `Optional` reference itself; it reduces but doesn't eliminate null-related bugs, and only helps
  if used idiomatically.
- [Basic] What's the difference between `Optional.of` and `Optional.ofNullable`? → `of(value)`
  throws `NullPointerException` immediately if `value` is null (a fail-fast assertion that it
  can't be); `ofNullable(value)` accepts a possibly-null value and produces an empty `Optional` if
  it's null, present otherwise. → Follow-up: *When would you deliberately choose `of` over
  `ofNullable`?* When null reaching that point would itself indicate a bug — `of` turns that bug
  into an immediate, loud failure instead of a silently-empty `Optional` propagating downstream.
- [Basic] Why is `orElse(x)` sometimes a performance trap compared to `orElseGet(supplier)`? →
  `orElse`'s argument is a plain value, so Java evaluates it eagerly every time `orElse` is called
  — even when the `Optional` is present and the value is discarded; `orElseGet`'s supplier is only
  invoked if the `Optional` turns out empty. → Follow-up: *Does this matter for `orElse(0)` or
  `orElse("default")`?* No — evaluating a constant is free; it only matters when the argument
  expression itself does real work (a method call, object construction, DB/network access).
- [Intermediate] Why does `Optional` provide `map`/`filter`/`flatMap` instead of just `get()`/
  `isPresent()`? → It's deliberately modeled after `Stream`'s "container with 0 or 1 elements" idea
  so absence-handling composes: `.filter(predicate).map(transform).orElse(default)` chains
  multiple possibly-absent steps without ever needing a manual null/presence check at each step,
  reading declaratively top to bottom instead of as nested conditionals. → Follow-up: *What
  happens when you call `.map()` on an empty `Optional`?* The mapping function is never invoked at
  all — the result is simply another empty `Optional`, short-circuiting the rest of the chain.
- [Intermediate] What's wrong with declaring a class field as `Optional<String> name`? → `Optional`
  is not `Serializable`, doesn't play well with JPA/Jackson and most reflection-based frameworks,
  and adds an extra wrapper object allocation per field with no benefit over simply allowing the
  field itself to be `null` internally (only the *public accessor* returning it as `Optional<T>`
  is the intended pattern). → Follow-up: *So what's the recommended pattern instead?* Keep the
  internal field as a plain, possibly-null reference, and only wrap it in `Optional` at the
  boundary of a getter method that's meant to communicate "this might be absent" to its caller.
- [Advanced] Why is `Optional` documented as a "value-based class," and what does that forbid? →
  Value-based classes (also `LocalDate`, boxed primitives like `Integer` in certain ranges) are
  meant to be used purely by value — no identity-sensitive operations should be performed on them:
  don't synchronize on an `Optional` instance, don't compare with `==` expecting reference
  identity to matter, don't serialize its identity, and future JDK versions reserve the right to
  use more aggressive instance-sharing/value-type optimizations under the hood. → Follow-up: *Does
  `Optional` override `equals()`?* Yes — two present `Optional`s are equal if their wrapped values
  are `equals()`, and two empty `Optional`s are always equal to each other; this is exactly what
  makes value-based comparison (`.equals()`, not `==`) the correct way to compare them.

## 8. Exercises

There is no dedicated exercise stub for `Optional` in this module — it's exercised implicitly
throughout the banking-dataset exercises (topic 7), e.g. `orElseThrow()` after a `max`/`findFirst`
that must be non-empty by the exercise's stated constraints (`HighestAverageTransactionAccount`,
`MaxMinViaPrimitiveStream`). Study `OptionalDemo` directly for this topic; there is nothing to
implement here beyond reading and running it.

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| — | — | (no dedicated exercise — study via `OptionalDemo` and the `Optional` usage inside topic 7's exercises) | — | — |

## 9. Quick recap

- `Optional<T>` makes "might be absent" explicit in a return type — prefer it as a return type,
  avoid it as a field or parameter type.
- `Optional.of` fails fast on null; `Optional.ofNullable` is the null-safe constructor — use the
  latter whenever the source might legitimately be null.
- `map`/`filter`/`flatMap`/`orElse` chains replace nested null checks; use `flatMap` when the
  mapping function itself returns an `Optional`, to avoid `Optional<Optional<T>>`.
- `orElse(x)` always evaluates `x` eagerly; `orElseGet(supplier)` only evaluates it when actually
  needed — use `orElseGet` whenever the default is expensive to compute.
- Never call `get()` blind — use `orElse`, `orElseGet`, `orElseThrow(specificException)`, or
  `ifPresent`/`ifPresentOrElse` instead.
