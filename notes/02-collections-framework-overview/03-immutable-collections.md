# Immutable Collections

## 1. What it is

Java has two distinct flavors of "you can't mutate this": an **unmodifiable
view** (`Collections.unmodifiableList` and friends) that wraps a mutable
backing collection and rejects structural changes *through the wrapper*
while still reflecting changes made to the backing collection directly, and
a **truly immutable collection** (`List.of`, `Set.of`, `Map.of`, JDK 9+,
plus `List.copyOf`/`Set.copyOf`/`Map.copyOf`) whose contents are fixed for
its entire lifetime, with no backing collection anyone can mutate at all.
Confusing the two is a classic source of "immutable" bugs that aren't
actually immutable.

## 2. How it works internally

### 2.1 `Collections.unmodifiableList` — a view, not a copy

```java
List<String> backing = new ArrayList<>(List.of("a", "b"));
List<String> view = Collections.unmodifiableList(backing);
```

```
backing (ArrayList, mutable) ------ same underlying array ------> view (UnmodifiableList wrapper)
     ^                                                                    |
     |__________ backing.add("c") also becomes visible through view _____|

view.add("c")            -> throws UnsupportedOperationException (wrapper blocks writes)
backing.add("c")         -> succeeds; view.size() now reflects 3 elements too
```

Internally, `Collections.unmodifiableList(list)` returns an instance of a
private static nested class (`Collections.UnmodifiableList`) that holds a
reference to your original list and delegates all read methods
(`get`, `size`, `iterator`, ...) straight through, while every mutator
method (`add`, `remove`, `set`, `clear`, ...) is overridden to immediately
throw `UnsupportedOperationException`. It does **not** copy any data — it's
a thin read-only façade. `Collections.unmodifiableSet/Map/Collection/
SortedSet/SortedMap/NavigableMap` all follow the identical pattern.

### 2.2 `List.of` / `Set.of` / `Map.of` — genuinely immutable

```java
List<String> truly = List.of("a", "b");
```

```
truly (ImmutableCollections.ListN or List12, JDK-internal, package-private)
   - stores elements directly in its own final array/fields
   - there is NO separate "backing collection" anyone else has a handle to
   - every mutator method throws UnsupportedOperationException, always
   - constructed once; contents can never change for the life of the object
```

Internally these are backed by `java.util.ImmutableCollections` — a family
of small, package-private, final classes (`List12` for 0–2 elements,
optimized inline storage; `ListN` for larger lists, backed by an internal
`Object[]`). Because nothing external ever holds a mutable reference to that
backing array, there is no "spooky action at a distance" possible — the
collection is immutable by construction, not just by a wrapper's refusal to
expose mutators. `List.of()`/`Set.of()` additionally:

- Reject `null` elements — `NullPointerException` at creation time, not
  lazily on `add()`.
- For `Set.of`/`Map.of`, reject duplicate elements/keys — `IllegalArgumentException`
  at creation time.
- Have unspecified (effectively randomized per-JVM-run, via a salted hash)
  iteration order for `Set.of`/`Map.of`, intentionally — this stops code
  from accidentally depending on insertion order the way `LinkedHashSet`
  guarantees it.

### 2.3 `List.copyOf` / `Set.copyOf` / `Map.copyOf` — snapshot into true immutability

```java
List<String> mutable = new ArrayList<>(List.of("a", "b"));
List<String> snapshot = List.copyOf(mutable);
mutable.add("c");          // does NOT affect snapshot
System.out.println(snapshot); // [a, b]
```

`copyOf` takes a *defensive, one-time copy* of the source's elements into a
new `ImmutableCollections` instance — after that call, there is no
relationship between `mutable` and `snapshot` whatsoever. (As an
optimization, if the source is *already* one of the JDK's own immutable
collections, `copyOf` may return the same reference instead of copying
again — safe, since that source can never change anyway.)

### 2.4 `Collections.emptyList()` / `singletonList()`

`Collections.emptyList()` returns a shared, cached, truly immutable
singleton instance (no allocation per call) — equivalent in spirit to
`List.of()` with zero elements but predates it (Java 1.2) and permits (in
fact requires) no elements at all. `Collections.singletonList(x)` similarly
returns a compact immutable one-element list. Both throw
`UnsupportedOperationException` on any mutation attempt, same as `List.of`.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `Collections.unmodifiableList(list)` | O(1) | O(1) — just a wrapper object | no copy of elements |
| `List.of(e1, ..., en)` | O(n) to build | O(n) | compact internal storage, no wrapper indirection layer |
| `List.copyOf(collection)` | O(n) (O(1) if already an immutable JDK collection) | O(n) | defensive snapshot |
| read (`get`, `contains`, iteration) through either flavor | same as backing/own storage | O(1) extra | wrapper adds one indirection, negligible |
| mutation attempt (`add`/`remove`/`set`) on either flavor | O(1) — immediately throws | O(1) | fails fast, not silently ignored |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/collectionsoverview/examples/ImmutableCollectionsDemo.java`

```java
List<String> backing = new ArrayList<>(List.of("a", "b"));
List<String> view = Collections.unmodifiableList(backing);
List<String> truly = List.of("a", "b");

backing.add("c");
System.out.println(view);   // [a, b, c]  <- view reflects the backing mutation
System.out.println(truly);  // [a, b]     <- unaffected, there's no backing to mutate

try {
    view.add("d");
} catch (UnsupportedOperationException e) {
    System.out.println("view.add rejected: " + e.getClass().getSimpleName());
}
```

Expected console output (abridged, full output documented in the class):
```
view after backing.add("c") : [a, b, c]
truly (List.of) unaffected  : [a, b]
view.add rejected: UnsupportedOperationException
truly.add rejected: UnsupportedOperationException
List.of(1, null) rejected at creation: NullPointerException
```

## 5. When to use / when NOT to use

- Use `List.of`/`Set.of`/`Map.of` for **compile-time-known constant data**
  (config lookup tables, enum-like fixed sets, default values) — they're
  compact, genuinely immutable, and their null-hostility catches bugs early.
- Use `List.copyOf`/`Set.copyOf`/`Map.copyOf` when you receive a
  caller-supplied mutable collection and want to **defensively snapshot**
  it before storing it as a field — this is the standard fix for the
  "leaked mutable reference" bug (see Pitfall 1 below).
- Use `Collections.unmodifiableX` when you want to **expose a read-only
  view of a collection you still need to mutate internally** — e.g. a
  class that maintains a private mutable `List` and exposes a
  `getItems()` accessor that returns an unmodifiable view over the same
  live data (so callers see live updates but can't corrupt your internal
  state).
- Do NOT use `Collections.unmodifiableX` when you actually want immutability
  guarantees for thread-safety or defensive copying — it does not protect
  against the backing collection being mutated by anyone else who holds a
  reference to it (including your own code, by accident).
- Do NOT assume `List.of()`/`Map.of()` accept `null` — they don't, by
  design; use `Optional`/sentinel values, or fall back to
  `Collections.unmodifiableList(Arrays.asList(...))` if you truly need
  null-tolerant "read-only" semantics (rare, and usually a design smell).

## 6. Common pitfalls & gotchas

**Pitfall 1 — "leaked mutable reference" through a constructor.**

```java
public final class Portfolio {
    private final List<String> holdings;
    public Portfolio(List<String> holdings) {
        this.holdings = Collections.unmodifiableList(holdings); // BUG: still a view!
    }
}
List<String> callerList = new ArrayList<>(List.of("AAPL"));
Portfolio p = new Portfolio(callerList);
callerList.add("GOOG");           // Portfolio's "immutable" holdings just changed underneath it!
```

Fix: take a defensive copy, not just an unmodifiable view of the caller's list.

```java
this.holdings = List.copyOf(holdings); // snapshot; caller's later mutations have no effect
```

**Pitfall 2 — assuming `Arrays.asList` behaves like `List.of`.**

```java
List<Integer> a = Arrays.asList(1, 2, 3);
a.set(0, 99);   // OK! Arrays.asList allows set() — it's a fixed-SIZE view, not fixed-CONTENT
a.add(4);       // UnsupportedOperationException — but set() worked a line earlier, confusingly
```

Fix: know exactly which guarantee you need. `Arrays.asList` = fixed size,
mutable elements, backed directly by the array you passed in (even writes
through the array itself are visible!). `List.of` = fixed size AND fixed
content, nothing mutable about it.

**Pitfall 3 — `UnsupportedOperationException` thrown lazily, not caught at
compile time.**

```java
List<String> config = List.of("dev", "staging", "prod");
// ... 200 lines later, in a totally different method ...
config.add("canary"); // compiles fine, blows up at RUNTIME only when this line executes
```

Fix: there's no compiler-level fix (Java has no `const`-correctness for
collections) — mitigate with clear naming/docs, defensive copying at
boundaries, and unit tests that exercise the mutation paths you expect to
be rejected, exactly like this module's exercises do.

**Pitfall 4 — `Set.of`/`Map.of` silently reject duplicates, unlike `HashSet`.**

```java
Set<String> s = Set.of("a", "a"); // IllegalArgumentException at creation, NOT silent dedup
```

Fix: if you actually want deduplication, build a mutable `Set` first and
optionally snapshot it: `Set.copyOf(new HashSet<>(possiblyDuplicateList))`.

## 7. Interview questions

- [Basic] Q: What's the difference between `Collections.unmodifiableList`
  and `List.of`?
  A: `unmodifiableList` wraps an existing mutable list and blocks mutation
  *through the wrapper*, but the wrapper still reflects changes made
  directly to the backing list — it's a view, not a copy. `List.of` builds
  its own internal, self-contained storage with no backing collection at
  all, so there's nothing else that could ever mutate it — it's truly
  immutable.
  Follow-up: If you only have a reference to the `unmodifiableList` view
  (not the backing list), can the contents still change? (Yes, if some
  other code elsewhere holds a reference to the original backing list and
  mutates it — that's exactly the "leaked reference" pitfall.)

- [Basic] Q: What exception do you get when you call `.add()` on an
  immutable list, and when is it thrown — compile time or runtime?
  A: `UnsupportedOperationException`, thrown at runtime, the moment the
  mutator method executes. There's no compile-time protection — the method
  signature looks identical to a mutable list's `add()`.
  Follow-up: Why didn't Java give immutable and mutable lists different
  static types (like Kotlin's `List` vs `MutableList`)? (Backward
  compatibility — the single `List` interface predates immutable factories
  by 15+ years; splitting it would have been a massive breaking change.)

- [Basic] Q: Does `List.of(1, 2, null)` compile and run?
  A: It compiles, but throws `NullPointerException` at the moment `of(...)`
  executes — `List.of`/`Set.of`/`Map.of` are explicitly null-hostile by
  design, unlike `ArrayList` or `Arrays.asList`, which tolerate nulls.
  Follow-up: Why did the JDK designers choose null-hostility here? (To catch
  accidental nulls early and to allow more compact internal representations
  that don't need null-checks scattered through every read path.)

- [Intermediate] Q: You need to store a caller-supplied `List` as a field
  and guarantee it can never change after construction, even if the caller
  keeps a reference to what they passed in. What do you do?
  A: `this.field = List.copyOf(callerList);` — this takes a one-time
  defensive snapshot into a genuinely immutable collection; nothing the
  caller does afterward (including mutating their original list) can affect
  your field.
  Follow-up: What if `callerList` might contain `null` elements and you
  need to tolerate that? (`List.copyOf` will throw NPE on nulls just like
  `List.of` — you'd need a different strategy, e.g.
  `Collections.unmodifiableList(new ArrayList<>(callerList))`, accepting
  that it's a defensive-copy-into-a-view, which does tolerate nulls, at the
  cost of not being a JDK "true immutable" type.)

- [Intermediate] Q: What happens when you call `Set.of("a", "a")` or
  `Map.of("k", 1, "k", 2)`?
  A: Both throw `IllegalArgumentException` immediately, at construction —
  duplicate elements/keys are treated as a programmer error, not silently
  deduplicated the way building up a `HashSet` via repeated `add()` calls
  would be.
  Follow-up: How is this different from `new HashSet<>(List.of("a", "a"))`?
  (That succeeds and silently produces a 1-element set — `HashSet`'s
  constructor doesn't error on duplicates, it just dedupes, matching
  `Set`'s general contract; only the `Set.of` *factory* is stricter.)

- [Intermediate] Q: Is the iteration order of `Set.of("a", "b", "c")`
  guaranteed?
  A: No — it's intentionally unspecified and can even vary between JVM runs
  of the same program (implementations may salt the hash to discourage
  reliance on iteration order). Contrast with `LinkedHashSet`, which
  explicitly guarantees insertion order.
  Follow-up: Why would the JDK deliberately randomize something that could
  be made deterministic? (To prevent applications from accidentally
  depending on undefined behavior — if it were merely "unspecified but
  happens to be stable," people would silently rely on it and break when
  the JDK's internal implementation changed.)

- [Intermediate] Q: `Arrays.asList(1, 2, 3).add(4)` throws, but
  `Arrays.asList(1, 2, 3).set(0, 99)` succeeds. Why the difference?
  A: `Arrays.asList` returns a fixed-*size* list backed directly by the
  array — `set()` only replaces an element in place (array stays the same
  size, so it's supported), while `add()`/`remove()` would need to resize
  the backing array, which the view can't do, so those throw
  `UnsupportedOperationException`.
  Follow-up: What happens if you modify the original array after calling
  `Arrays.asList` on it? (The list reflects the change immediately — it's
  a live view over the same array, not a copy.)

- [Advanced] Q: Both `Collections.unmodifiableList` views and `List.of`
  immutables throw the same `UnsupportedOperationException` on mutation.
  From a caller's perspective using only the `List` interface, can you tell
  them apart, and does it matter?
  A: Not reliably through the public `List` API alone — both just throw on
  mutators. It matters because their *guarantees* differ: an unmodifiable
  view can still change from under you if something else mutates the
  backing collection, while `List.of` genuinely cannot change, ever. If
  your code depends on true immutability (e.g., safely sharing a collection
  across threads without synchronization, or using it as a cache key),
  you must know which one you actually have — checking
  `list.getClass()` is a code smell but sometimes the only way to verify in
  a debugger; the better fix is to control construction so you *know* which
  one you're holding.
  Follow-up: Is an unmodifiable view thread-safe to read concurrently while
  the backing list is being mutated by another thread? (No — that's a data
  race on the backing collection itself, e.g. `ArrayList` is not
  thread-safe for concurrent read+write; the "view" only protects against
  mutation *through the wrapper*, it adds zero concurrency guarantees.)

- [Advanced] Q: Why does `List.of()` (zero-arg) and `List.of(e1)` use a
  different internal class than `List.of(e1, e2, ..., e10)`?
  A: The JDK's `ImmutableCollections` provides specialized classes for 0–2
  elements (`List12`, storing elements directly in fields, no array
  allocation at all for the common tiny-list case) versus `ListN` for
  larger lists (backed by an `Object[]`). This is a memory/performance
  optimization: most real immutable lists are small (constants, singleton
  results), so avoiding an array allocation for those cases meaningfully
  reduces overhead at scale.
  Follow-up: Does this mean `List.of(1)` and `List.of(1, 2)` are different
  runtime classes? (Yes — `List12` handles both 1 and 2 element cases via
  a nullable second field, while 3+ elements switch to `ListN`; this is an
  internal implementation detail you should never rely on via
  `getClass()`, only observable behavior via the `List` contract matters.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Detect whether a given `List` rejects mutation, and classify the exception it throws | mutation probing | `MutationRejectionProbe.java` |
| E02 | Easy | Demonstrate/prove the view-vs-copy difference: after mutating the backing list, show which output changed and which didn't | view vs true immutability | `ViewVsTrueImmutability.java` |
| E03 | Medium | Write a defensive-copy utility that snapshots a caller-supplied list so future mutations to the source never affect the stored copy | defensive copying | `DefensiveSnapshot.java` |
| E04 | Medium | Classify a batch of insertion attempts (including nulls and duplicates) against `List.of`, `Set.of`, and `Collections.unmodifiableList(new ArrayList<>(...))`, reporting which exception (if any) each produces | null-hostility / exception classification | `ImmutabilityExceptionClassifier.java` |

Solutions live only in `src/main/java/com/gk/study/collectionsoverview/solutions/`.

<details>
<summary>E01 hint</summary>
Attempt a no-op mutation like <code>list.add(list.isEmpty() ? someElement : list.get(0))</code>
wrapped in try/catch, and return the caught exception's simple class name,
or "mutable" if nothing was thrown. Remember to actually undo any mutation
you perform if the list turns out to be mutable, so the probe has no side
effects.
</details>

<details>
<summary>E02 hint</summary>
Build a mutable backing <code>ArrayList</code>, wrap it with
<code>Collections.unmodifiableList</code>, and separately build a
<code>List.of(...)</code> with the same initial contents. Mutate the
backing list, then return both list's current contents so the test can
assert one changed and the other didn't.
</details>

<details>
<summary>E03 hint</summary>
<code>List.copyOf(source)</code> is the one-line answer — but make sure
your method signature accepts any <code>List&lt;T&gt;</code>, not just
<code>ArrayList</code>, and think about why <code>copyOf</code> is safe to
call even if <code>source</code> is itself already immutable.
</details>

<details>
<summary>E04 hint</summary>
For each collection type and each candidate value/operation, wrap the
attempt in try/catch(RuntimeException) and record the exception's simple
class name (or "OK" if it succeeded/was accepted at construction time).
Remember <code>Set.of</code>/<code>Map.of</code> validate at construction,
not later — you may need to attempt construction itself inside the
try/catch, not just a later mutation call.
</details>

## 9. Quick recap

- `Collections.unmodifiableX` = a thin read-only *view* over a mutable
  backing collection; mutating the backing collection is still visible
  through the view.
- `List.of`/`Set.of`/`Map.of` (and `copyOf`) = genuinely immutable, no
  backing collection exists that anyone could mutate.
- Both flavors throw `UnsupportedOperationException` at *runtime* on
  mutation attempts — there's no compile-time immutability in Java.
- `List.of`/`Set.of`/`Map.of` are null-hostile (NPE at creation) and
  `Set.of`/`Map.of` reject duplicates (`IllegalArgumentException` at
  creation) — `HashSet`/`ArrayList` tolerate both.
- To defend a field against a caller mutating what they passed in, use
  `List.copyOf(...)` (a true snapshot), not
  `Collections.unmodifiableList(callerList)` (still a live view over their list).
