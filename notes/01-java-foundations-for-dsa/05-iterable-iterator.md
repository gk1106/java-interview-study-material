# Iterable / Iterator, Fail-Fast vs Fail-Safe Iterators

## 1. What it is

`Iterable<T>` is the contract for "this can be looped over with a for-each
loop" — it has one method, `iterator()`, that returns an `Iterator<T>`.
`Iterator<T>` is the actual cursor: `hasNext()` and `next()` walk the
elements one at a time, and `remove()` optionally deletes the last-returned
element. "Fail-fast" iterators (most JDK collections) detect concurrent
structural modification and throw immediately; "fail-safe" iterators
(concurrent collections) iterate over a stable snapshot and never throw,
but may not reflect the very latest state.

## 2. How it works internally

The for-each loop `for (T t : iterable) { ... }` is pure syntactic sugar the
compiler rewrites to:

```java
Iterator<T> it = iterable.iterator();
while (it.hasNext()) {
    T t = it.next();
    ...
}
```

**Fail-fast mechanism** (how `ArrayList`, `HashMap`, `HashSet` iterators
work, paraphrased): the backing collection keeps an internal `modCount` int
field, incremented on every structural modification (add/remove — NOT a
`set()`/replace, which doesn't change structure). When `iterator()` is
called, the returned iterator snapshots the current `modCount` as
`expectedModCount`. Every `next()` (and often `remove()`) call first checks
`modCount == expectedModCount`; if they differ, it throws
`ConcurrentModificationException` immediately — "fail fast" — rather than
risk silently skipping elements or corrupting internal state.

```
ArrayList iterator fail-fast check:

  list.iterator() -> Itr { cursor=0, expectedModCount = list.modCount (say 5) }

  list.add(x)            -> list.modCount becomes 6
  it.next()               -> checks list.modCount(6) == expectedModCount(5)?  NO
                           -> throw ConcurrentModificationException

  it.remove()  (the ITERATOR's own remove, not the list's)
                           -> removes via the list, THEN re-syncs
                              expectedModCount = list.modCount  -> no exception on next call
```

Note: this detection is **best-effort**, not guaranteed — the Javadoc
explicitly says so. It's meant to catch bugs during development, not to be
relied on for correctness in concurrent code.

**Fail-safe / weakly-consistent iterators**: `CopyOnWriteArrayList`'s
iterator captures a reference to the backing array *at iterator-creation
time*; every mutation (`add`/`remove`) on the list replaces the ENTIRE
backing array with a new copy, so the iterator's captured array reference
never changes underneath it — concurrent modification is simply invisible to
an iterator already in flight, and no exception is ever thrown. This trades
"always see the latest data" for "never throws, always consistent snapshot."
`ConcurrentHashMap`'s iterators are similarly weakly consistent, but
implemented differently (segment/node traversal designed to tolerate
concurrent structural changes without a full-array copy).

```
Fail-fast (ArrayList):                Fail-safe (CopyOnWriteArrayList):
 iterator reads the SAME backing       iterator reads a captured reference
 array/state as active mutators   ->   to an array that mutators REPLACE
 detects the clash, throws             (never mutate in place) -> no clash
                                        possible, iterator sees the OLD array
```

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `iterator()` (fail-fast, e.g. ArrayList) | O(1) | O(1) | Just captures modCount + cursor=0 |
| `hasNext()` / `next()` (fail-fast) | O(1) amortized | O(1) | Full traversal is O(n) |
| `iterator()` (fail-safe, e.g. COWList) | O(1) | O(1) | Shares the snapshot array reference, no copy here |
| `add()`/`remove()` on `CopyOnWriteArrayList` | O(n) | O(n) | Copies the entire backing array every mutation |
| Full traversal, either kind | O(n) | O(1) extra (fail-fast) / O(n) held by snapshot (fail-safe, amortized across many iterators) | |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/foundations/examples/IteratorDemo.java`

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
for (Integer value : list) {
    if (value == 2) {
        list.remove(value); // structural modification DURING iteration
    }
}
// throws ConcurrentModificationException on the next it.next() call
```

Expected console output:
```
Fail-fast demo: caught java.util.ConcurrentModificationException as expected
Correct removal via Iterator.remove(): [1, 3]
Fail-safe demo (CopyOnWriteArrayList): iteration saw [1, 2, 3] while a concurrent add(4) happened; list is now [1, 2, 3, 4]
```

## 5. When to use / when NOT to use

- **Use** the default fail-fast collections (`ArrayList`, `HashMap`, ...) for
  single-threaded code — the fail-fast check is a *free* correctness safety
  net that catches "I forgot to use `Iterator.remove()`" bugs immediately
  instead of corrupting state silently.
- **Use** `Iterator.remove()` (not the collection's own `remove()`) whenever
  you need to delete elements while iterating a fail-fast collection — it's
  the one mutation method that's exempt from triggering the exception on the
  iterator that performed it.
- **Use** fail-safe/weakly-consistent collections (`CopyOnWriteArrayList`,
  `ConcurrentHashMap`) when many threads read far more often than they write
  and you need iteration to never throw — e.g. a rarely-changed list of
  registered listeners/handlers read by many threads.
- **Don't** use `CopyOnWriteArrayList` for write-heavy workloads — every
  mutation is O(n), copying the whole backing array; it's optimized
  specifically for the read-mostly case.
- **Don't** rely on fail-fast detection as a concurrency control mechanism —
  it's best-effort only and explicitly not guaranteed by the Javadoc.

## 6. Common pitfalls & gotchas

**Pitfall 1 — removing from a list with the collection's `remove()` inside a for-each loop.**

```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4));
for (Integer x : list) {
    if (x % 2 == 0) {
        list.remove(x); // BUG: throws ConcurrentModificationException
    }
}
```
```java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
    if (it.next() % 2 == 0) {
        it.remove(); // FIX: the iterator's own remove() re-syncs modCount, no exception
    }
}
```

**Pitfall 2 — "removing the second-to-last element avoids the exception, so
it must be safe" (a dangerously wrong mental model).** `ArrayList`'s
`next()` check happens on a specific internal condition (`cursor !=
size`), so removing the LAST element via `list.remove()` in some JDK
versions happens not to trigger the check on that particular loop's final
iteration (because `hasNext()` becomes false before `next()` is called
again) — but this is an implementation accident, not a guarantee, and
differs across collection types/JDK versions.

```java
// FRAGILE, do not rely on this "trick" in real code:
List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
for (Integer x : list) {
    if (x == 3) {
        list.remove(x); // "happens" not to throw here in some cases - don't depend on it
    }
}
```
```java
// FIX: always use Iterator.remove(), or collect indices/elements to remove
// in a separate pass and remove them after iteration completes.
list.removeIf(x -> x == 3); // idiomatic, safe, uses the iterator internally correctly
```

**Pitfall 3 — assuming a fail-safe iterator (e.g. `CopyOnWriteArrayList`)
reflects the newest data.**

```java
CopyOnWriteArrayList<String> handlers = new CopyOnWriteArrayList<>(List.of("A"));
Iterator<String> it = handlers.iterator();
handlers.add("B"); // does NOT throw, but "B" is invisible to `it`
while (it.hasNext()) {
    System.out.println(it.next()); // prints only "A" - "B" is silently missed by THIS iterator
}
```
```java
// Fix: if you need to see the latest additions, get a FRESH iterator after
// the mutation, or re-design so you don't need live-updating iteration
// (e.g. iterate handlers.toArray() taken right before use, understanding
// it's a snapshot).
```

## 7. Interview questions

- **[Basic]** What's the difference between `Iterable` and `Iterator`? →
  *`Iterable` is the "can be for-each looped" contract with a single
  `iterator()` factory method; `Iterator` is the actual stateful cursor with
  `hasNext()`/`next()`/optional `remove()` that walks the elements. A
  collection typically implements `Iterable` and returns a fresh `Iterator`
  instance (with its own cursor state) each time `iterator()` is called.* →
  Follow-up: *Can the same object implement both interfaces?* (Yes, though
  unusual — it's more common and safer for `iterator()` to return a new
  object each call so multiple concurrent for-each loops over the same
  collection don't share cursor state.)

- **[Basic]** What does "fail-fast" mean for an iterator, and which
  exception does it throw? → *A fail-fast iterator detects that the backing
  collection was structurally modified (elements added/removed) by something
  other than the iterator itself DURING iteration, and throws
  `ConcurrentModificationException` as soon as it notices, rather than
  continuing with corrupted/inconsistent state.* → Follow-up: *Is this
  detection guaranteed?* (No — the JDK Javadoc explicitly states it's
  best-effort, meant to catch bugs, not to be relied on as a correctness
  guarantee, especially not in genuinely concurrent/multi-threaded code.)

- **[Basic]** How do you safely remove elements while iterating an
  `ArrayList`? → *Use the `Iterator`'s own `remove()` method (call
  `it.next()` first, then `it.remove()`), or use `Collection.removeIf(...)`,
  or a `ListIterator` if you need to add/replace during iteration too. Never
  call the list's own `remove()` directly inside a for-each loop.* →
  Follow-up: *What's the difference between `Iterator.remove()` and
  `ListIterator.remove()`?* (Same idea, but `ListIterator` also supports
  `add()` and `set()` during traversal and can move backward with
  `hasPrevious()`/`previous()` — `Iterator` is forward-only with just
  `remove()`.)

- **[Intermediate]** How does `ArrayList`'s fail-fast check actually work
  internally? → *The list keeps a `modCount` field incremented on every
  structural change; when you call `iterator()`, the returned iterator
  captures that count as `expectedModCount`. Each `next()` call compares the
  list's current `modCount` to the captured value and throws
  `ConcurrentModificationException` if they differ. `Iterator.remove()` is
  special-cased to update `expectedModCount` to match after performing the
  removal, so it doesn't trip its own check.* → Follow-up: *Does modifying
  an element via `list.set(i, x)` trigger it?* (No — `set()` is not a
  structural modification (the list's size/shape doesn't change), so it
  doesn't increment `modCount` and won't trigger the fail-fast check.)

- **[Intermediate]** Why does `CopyOnWriteArrayList` never throw
  `ConcurrentModificationException`? → *Every mutating operation
  (add/remove/set) creates and swaps in an entirely new backing array rather
  than mutating the existing one in place. An iterator created before the
  mutation is holding a reference to the OLD array, which is never touched
  again — it's immutable from the iterator's point of view — so there's
  nothing for a fail-fast check to detect; the two are simply operating on
  different array objects.* → Follow-up: *What's the trade-off for this
  safety?* (Every write is O(n) (full array copy) and memory-heavy under
  high write volume; also, an iterator might show stale data, missing
  concurrent additions/removals that happened after it was created.)

- **[Intermediate]** What's the difference between `Iterator` and
  `ListIterator`? → *`ListIterator` extends `Iterator` and adds bidirectional
  traversal (`hasPrevious()`/`previous()`), access to the current index
  (`nextIndex()`/`previousIndex()`), and in-place mutation during iteration
  (`add(T)` to insert, `set(T)` to replace the last-returned element) — only
  available on `List` implementations, not general `Collection`s, since it
  relies on positional/index semantics.* → Follow-up: *Can you insert an
  element while iterating with a plain Iterator?* (No — `Iterator` only
  supports `remove()`; inserting requires a `ListIterator`.)

- **[Advanced]** Why is fail-fast detection described as "best effort" — can
  you construct a scenario where a genuine concurrent structural
  modification is NOT detected? → *`modCount` is a plain `int`, not
  `volatile` or otherwise synchronized, and the check is a simple equality
  comparison, not a lock — so under true multi-threaded races there's no
  happens-before guarantee the reading thread sees the writing thread's
  latest `modCount` value, meaning detection can be missed (or, rarely, a
  coincidental sequence of modifications could even leave `modCount` equal
  to `expectedModCount` again after multiple changes, silently passing the
  check). This is exactly why the Javadoc says "fail-fast behavior cannot be
  guaranteed" and to never write concurrent code that depends on catching
  this exception for correctness — use a proper concurrent collection or
  external synchronization instead.* → Follow-up: *What SHOULD you use
  instead for genuinely concurrent iteration + modification?*
  (`ConcurrentHashMap`, `CopyOnWriteArrayList`, or explicit locking
  (`synchronized` block/`ReadWriteLock`) around both the iteration and any
  concurrent mutation, chosen based on the read/write ratio.)

- **[Advanced]** Implementing a custom `Iterator`, what's the correct
  contract for `next()` when the sequence is exhausted, and why does it
  matter for for-each loop compatibility? → *`next()` must throw
  `NoSuchElementException` when there are no more elements — that's part of
  the `Iterator` interface contract (not just a convention), and
  well-behaved callers always guard with `hasNext()` first. The compiler's
  desugared for-each loop relies on exactly this pattern
  (`while (it.hasNext()) { T t = it.next(); ... }`), so an iterator that
  returns `null` or throws the wrong exception when exhausted will either
  loop forever, silently return garbage, or break callers who correctly rely
  on `NoSuchElementException` (e.g. catching it deliberately in some
  algorithms).* → Follow-up: *Should hasNext() ever have side effects?*
  (Generally no — it should be idempotent/safe to call repeatedly without
  consuming state; any "peeking" needed to determine availability should be
  cached internally rather than advancing the underlying source
  irreversibly.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Implement `Iterable<Integer>` over a half-open numeric range | custom Iterable/Iterator | `exercises/RangeIterable.java` |
| E02 | Medium | Implement a fail-fast `Iterator` for a custom array-backed list | modCount fail-fast pattern | `exercises/SimpleFailFastList.java` |
| E03 | Medium | Implement an Iterator decorator that yields every Nth element | iterator decorator | `exercises/SkippingIterator.java` |
| E04 | Hard | Implement a fail-safe (copy-on-write) Iterable | snapshot/copy-on-write iterator | `exercises/SnapshotIterable.java` |

- **E01 hint:**
  <details><summary>hint</summary>Return an anonymous/inner `Iterator<Integer>` whose `hasNext()` checks `current < end` and whose `next()` returns `current++` (throwing `NoSuchElementException` if exhausted).</details>
- **E02 hint:**
  <details><summary>hint</summary>Capture `modCount` at `iterator()` call time; every `next()` call must first compare it against the list's live `modCount` and throw `ConcurrentModificationException` on mismatch, exactly like `ArrayList`.</details>
- **E03 hint:**
  <details><summary>hint</summary>`hasNext()` simply delegates to the wrapped iterator's `hasNext()`. `next()` grabs one element to return, then calls the delegate's `next()` up to `n-1` more times (stopping early if exhausted) to skip ahead before returning.</details>
- **E04 hint:**
  <details><summary>hint</summary>`add()` should build a brand-new array one element longer and atomically replace the stored reference (copy-on-write); `iterator()` should capture that reference into a local variable and hand back an `Iterator` over exactly that fixed array — later `add()` calls replace the reference but never touch the array the iterator already captured.</details>

Target complexity — E01: O(1) per `next()`/`hasNext()`, O(n) full traversal.
E02: O(1) per operation, same as ArrayList's fail-fast iterator. E03: O(n)
per `next()` amortized to O(1) per original element read (each underlying
element is visited at most once across all calls). E04: `add()` is O(n)
(copy), `iterator()` is O(1) to create. Solutions are in
`src/main/java/com/gk/study/foundations/solutions/` — not shown here;
attempt the exercises first.

## 9. Quick recap

- The for-each loop is sugar for `iterator()` + `hasNext()`/`next()` calls — nothing magic happens at the language level.
- Fail-fast iterators (ArrayList, HashMap, HashSet) track a `modCount` and throw `ConcurrentModificationException` on any structural change they didn't make themselves — best-effort only, not a concurrency guarantee.
- Always use `Iterator.remove()` (or `Collection.removeIf`) to delete elements during iteration, never the collection's own `remove()`.
- Fail-safe/weakly-consistent iterators (CopyOnWriteArrayList, ConcurrentHashMap) never throw but may not reflect the very latest concurrent writes.
- A well-behaved custom `Iterator` must throw `NoSuchElementException` from `next()` when exhausted — that's part of the interface contract, not just convention.
