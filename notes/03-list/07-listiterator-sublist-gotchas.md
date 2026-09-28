# ListIterator, subList gotchas, removing while iterating

## 1. What it is

`ListIterator<E>` is `List`'s richer iterator: bidirectional traversal (`hasNext`/`hasPrevious`,
`next`/`previous`) plus **mutation during traversal** (`set`, `add`, `remove`) without corrupting
iteration state. `subList(from, to)` returns a **live view** onto a range of the parent list.
Both are frequent sources of `ConcurrentModificationException` (CME) and subtle bugs when misused.

## 2. How it works internally

**Fail-fast mechanism (recap from `Iterable`/`Iterator` in module 01, applied to `List`):**
every structural change to an `ArrayList`/`LinkedList` increments `modCount`. An `Iterator`/
`ListIterator` snapshots `expectedModCount = modCount` at creation; every `next()`/`previous()`
call checks `modCount == expectedModCount` and throws `ConcurrentModificationException` if they
differ. `Iterator.remove()`/`ListIterator.remove()`/`set()`/`add()` are the **only** sanctioned
ways to mutate during traversal, because they update `expectedModCount` to match the new
`modCount` after performing the change — keeping the iterator in sync with itself.

**`ListIterator` cursor model:**
```
list:      [ A | B | C | D ]
cursor position (between elements), e.g. after next()=B:
            A   B | C   D
                ^-- cursor here; next()=C, previous()=B, nextIndex()=2, previousIndex()=1
```
`set(e)` replaces the **last element returned** by `next()`/`previous()`; `add(e)` inserts
**before** the element that would be returned by a subsequent `next()`. Calling `set`/`remove`
without a preceding `next`/`previous` (or twice in a row) throws `IllegalStateException`.

**`subList` is a view, not a copy:**
```
parent:  [A, B, C, D, E]
          idx: 0  1  2  3  4
sub = parent.subList(1, 4);   // view over indices [1,4) -> [B, C, D]
sub.set(0, "X");              // parent becomes [A, X, C, D, E]
sub.add("Y");                 // parent becomes [A, X, C, D, Y, E]  (inserted at sublist's end -> parent idx 4)
parent.remove(0);             // structurally changes the PARENT outside the sublist's own modCount tracking
sub.get(0);                   // throws ConcurrentModificationException — sublist detected parent's structural change
```
The sublist implementation stores an offset into the parent and delegates most operations,
checking the **parent's** `modCount` on every access — any structural change to the parent made
through any reference other than the sublist itself invalidates all outstanding sublists.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `ListIterator.next/previous` | O(1) amortized (ArrayList), O(1) (LinkedList) | O(1) | per-step cost |
| `ListIterator.set/add/remove` | O(1) (LinkedList splice) / O(n) (ArrayList shift) | O(1) | same underlying cost as the list's own op |
| `subList(from, to)` creation | O(1) | O(1) | it's a view, no copying |
| operations on the sublist | same as parent's per-op cost | O(1) | delegates with an index offset |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/ListIteratorGotchasDemo.java`

```java
List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
// WRONG: throws ConcurrentModificationException
try {
    for (Integer n : nums) {
        if (n % 2 == 0) nums.remove(n);
    }
} catch (ConcurrentModificationException e) {
    System.out.println("CME as expected: " + e.getClass().getSimpleName());
}
// RIGHT #1: Iterator.remove()
Iterator<Integer> it = nums.iterator();
while (it.hasNext()) if (it.next() % 2 == 0) it.remove();
System.out.println(nums); // [1, 3, 5]
```
Expected console output:
```
CME as expected: ConcurrentModificationException
[1, 3, 5]
```

## 5. When to use / when NOT to use

- Use `ListIterator` when you need to mutate a list **while** scanning it in a single pass (e.g.
  normalizing/removing entries based on a computed condition) — it's the only iterator that lets
  you `set`/`add`/`remove` safely mid-traversal on a plain `List`.
- Use `subList` for read-only windowed views or for a scoped bulk operation
  (`list.subList(from, to).clear()` is the idiomatic way to bulk-delete a range in one O(n) shift
  instead of N separate O(n) removes) — but don't hold onto a sublist reference across unrelated
  mutations of the parent.
- Prefer `removeIf(predicate)` (Java 8+) over manual iterator loops whenever the removal condition
  doesn't need the iteration index/position — it's clearer and internally handles the fail-fast
  bookkeeping correctly for you.

## 6. Common pitfalls & gotchas

**Removing from a list with a for-each loop or the list's own `remove` during iteration →
CME:**
```java
for (Integer n : nums) {
    if (n == 3) nums.remove(n);   // ConcurrentModificationException on next hasNext()/next()
}
```

**The "skip an element" bug** — removing via `list.remove(index)` inside a plain indexed loop
doesn't throw, but silently skips the element that shifted into the just-vacated index:
```java
for (int i = 0; i < list.size(); i++) {
    if (shouldRemove(list.get(i))) list.remove(i); // next element shifts to i, but i++ skips it
}
// fix: iterate backwards, or use Iterator.remove()/removeIf()
```

**Three correct alternatives, in order of preference:**
```java
list.removeIf(n -> n % 2 == 0);                 // #1 cleanest, Java 8+

Iterator<Integer> it = list.iterator();          // #2 classic, needed when logic is complex
while (it.hasNext()) if (it.next() % 2 == 0) it.remove();

for (int i = list.size() - 1; i >= 0; i--) {     // #3 backward index loop, no CME risk
    if (list.get(i) % 2 == 0) list.remove(i);
}
```

**Holding a `subList` across an unrelated parent mutation:**
```java
List<String> parent = new ArrayList<>(List.of("A","B","C","D"));
List<String> sub = parent.subList(1, 3);   // [B, C]
parent.add("E");                            // structural change via the PARENT reference
sub.get(0);                                 // ConcurrentModificationException
// fix: finish all work through the sublist first, or take an independent copy:
List<String> snapshot = new ArrayList<>(parent.subList(1, 3));
```

## 7. Interview questions

- [Basic] What's the difference between `Iterator` and `ListIterator`? → `ListIterator` extends
  `Iterator`, adding backward traversal (`hasPrevious`/`previous`), position queries
  (`nextIndex`/`previousIndex`), and in-place mutation (`set`, `add`) — plain `Iterator` only
  supports forward traversal and `remove()`. → Follow-up: *Can you get a ListIterator from a
  Set?* No — `ListIterator` is only available from `List` (`list.listIterator()`), since it
  relies on positional/index semantics that `Set` doesn't have.
- [Basic] Why does removing an element with the list's own `remove()` method inside a for-each
  loop throw `ConcurrentModificationException`? → The for-each loop uses a plain `Iterator`
  internally; the list's `remove()` increments `modCount` directly without informing that
  iterator, so the iterator's next `next()`/`hasNext()` call detects the mismatch and fails fast.
  → Follow-up: *Does this happen on the very last element too?* Often it does NOT throw if you
  remove the second-to-last element (a well-known quirk: `hasNext()` can return false before the
  mismatch is checked) — which makes this bug worse, since it "works" in small tests and fails
  intermittently based on size/position.
- [Basic] How do you safely remove elements while iterating a `List`? → `Iterator.remove()`
  inside a `while(it.hasNext())` loop, or `list.removeIf(predicate)`, or a backward indexed loop
  — never the list's own `add`/`remove` methods from inside a for-each. → Follow-up: *Which is
  idiomatically preferred today?* `removeIf` for simple predicate-based removal; explicit
  `Iterator` when you need more control (e.g. stop early, track extra state).
- [Intermediate] Is `subList` a copy or a view, and what's a concrete consequence? → A live view;
  mutating it mutates the parent (and vice versa), and any structural change to the parent through
  a different reference invalidates the sublist (throws CME on next access). → Follow-up: *What's
  a useful, idiomatic thing you can do with subList that you couldn't do as easily otherwise?*
  `list.subList(from, to).clear()` — bulk-removes a whole range in a single O(n) shift, versus
  calling `remove(index)` `(to - from)` times, each an O(n) shift (O(n²) total).
- [Intermediate] What does `ListIterator.set(e)` actually replace? → The element most recently
  returned by `next()` or `previous()` on that same iterator — calling `set` without first calling
  `next`/`previous` (or calling it twice without an intervening `next`/`previous`) throws
  `IllegalStateException`. → Follow-up: *Does set() change modCount?* No — like `List.set`, it's
  not a structural change (size unchanged), so it doesn't trip fail-fast iteration for *other*
  iterators watching the same list... but note it still can affect correctness if another thread
  is reading concurrently without synchronization (visibility, not CME).
- [Intermediate] Why can indexed removal in a forward loop silently skip elements instead of
  throwing? → `list.remove(i)` doesn't go through an `Iterator` at all — there's no `modCount`
  check involved for a raw indexed loop — so nothing detects anything is "wrong"; the bug is
  purely logical: the element after the removed one shifts down into index `i`, but the loop's
  `i++` then skips over it. → Follow-up: *How would you catch this in code review?* Look for any
  loop that both indexes with `i++` and calls `list.remove(index)` inside the loop body — that
  combination is a near-automatic bug unless the loop explicitly compensates (`i--` after removal,
  or iterates backward).
- [Advanced] Why is `ConcurrentModificationException` described as "best-effort," and what does
  that imply for production code? → The `modCount` check is not a synchronization mechanism and
  gives no hard guarantee — under race conditions (multi-threaded mutation without
  synchronization) it might not throw at all and instead corrupt internal state or produce wrong
  results; it exists purely to catch **bugs** (single-threaded logic errors) early, not to provide
  thread safety. → Follow-up: *So is CME ever expected in correctly-synchronized multi-threaded
  code?* No — correctly synchronized code should never see it; if you need actual concurrent
  mutation-during-iteration, use a concurrent collection (`CopyOnWriteArrayList`,
  `ConcurrentHashMap`) designed for that, not fail-fast collections with manual locking sprinkled
  around.
- [Advanced] How does `ListIterator.add(e)` interact with subsequent `next()`/`previous()` calls?
  → The added element is inserted immediately before the implicit cursor position (before the
  element `next()` would return); a following call to `next()` returns the element that was
  originally there (skipping over what you just added, since the cursor moved past the new
  element), while `previous()` immediately after `add` returns the newly added element. This is a
  common source of off-by-one confusion when building a list transformation in a single pass. →
  Follow-up: *How do you insert several elements and continue iterating the original sequence
  correctly?* Call `add()` for each new element, and be deliberate about whether you want the
  cursor to "consume" the elements you just added (call `next()` to skip them) or revisit them.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E1 | Easy | Check whether a list of characters/integers reads the same forwards and backwards | Two pointers | `exercises/IsPalindromeList.java` |

**E1 — Is Palindrome (List)**
- Input: `[1, 2, 3, 2, 1]` → Output: `true`; Input: `[1, 2, 3]` → Output: `false`
- Constraint: O(n) time, O(1) extra space — compare from both ends inward using index access
  (`get(left)`/`get(right)`), not a `ListIterator`, but this is the natural place to practice
  "walk from both ends" thinking that also underlies `ListIterator`'s forward/backward symmetry.
- <details><summary>Hint</summary>Two pointers, `left = 0`, `right = size - 1`; compare and move
  inward; stop early on mismatch.</details>

Solution is in the `solutions` package (`IsPalindromeListSolution`) — not shown here.

## 9. Quick recap

- `ListIterator` adds backward traversal + safe in-place `set`/`add`/`remove` during a single
  pass — the only sanctioned way to mutate a `List` while iterating it (besides `removeIf`).
- CME is driven by comparing `modCount` (list) vs `expectedModCount` (iterator) — best-effort,
  not a concurrency guarantee.
- `subList` is a live view sharing the parent's `modCount` tracking — structural changes to the
  parent outside the sublist invalidate it.
- Indexed removal in a forward loop doesn't throw — it silently skips elements; iterate backward
  or use `Iterator.remove()`/`removeIf()` instead.
- `list.subList(a, b).clear()` is the idiomatic O(n) bulk-range-delete.
