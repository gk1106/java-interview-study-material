# Comparable vs Comparator

## 1. What it is

`Comparable<T>` is implemented BY the class being sorted — it defines that
class's single "natural ordering" via `compareTo(T other)`. `Comparator<T>`
is a SEPARATE strategy object passed TO a sort — it defines an ordering
(possibly one of several) without touching the class being sorted. Use
`Comparable` when a type has one obvious natural order (numbers, dates);
use `Comparator` when you need flexible, situational, or multiple orderings
(sort by name vs by salary vs by department).

## 2. How it works internally

Both interfaces boil down to the same contract: a comparison method
returning a negative int (first < second), zero (equal), or positive int
(first > second). Every JDK sort (`Collections.sort`, `Arrays.sort` for
objects, `TreeMap`/`TreeSet` ordering, `PriorityQueue` ordering) calls this
method repeatedly to decide ordering — the algorithm itself (Timsort for
objects) doesn't care whether the comparison came from `compareTo` or a
`Comparator`.

```
Comparable<T>                          Comparator<T>
--------------                         -------------
int compareTo(T other)                 int compare(T a, T b)
implemented INSIDE the class           implemented OUTSIDE the class,
"this" is one side of comparison       as a separate strategy object
ONE natural ordering per class          MANY orderings, chosen at call site
e.g. Integer, String, LocalDate         e.g. Comparator.comparing(Employee::getSalary)
```

**Comparator factory methods (Java 8+)** let you build comparators
declaratively instead of hand-writing `compare()`:

- `Comparator.comparing(keyExtractor)` — build from a key extractor
  (`Function<T, U>` where U is Comparable).
- `.thenComparing(...)` — chain a tie-breaker; applied only when the
  previous comparator returned 0.
- `.reversed()` — flips a comparator's result (multiply-by-negative-one,
  conceptually).
- `Comparator.nullsFirst(cmp)` / `Comparator.nullsLast(cmp)` — wrap a
  comparator to push nulls to one end instead of throwing
  `NullPointerException` when a null slips into the comparison.
- `Comparator.naturalOrder()` / `Comparator.reverseOrder()` — comparator
  views of a type's own `Comparable` (or its inverse).

Internally, sorting objects in Java (`Collections.sort`, `Arrays.sort(Object[])`)
uses a variant of **Timsort** — a hybrid merge sort/insertion sort that
detects and exploits already-sorted "runs" in the input (common in real-world
data), giving O(n log n) worst case but close to O(n) on nearly-sorted input,
and is *stable* (equal elements keep their relative input order) — important
when you chain `thenComparing` or sort the same list by different keys in
sequence.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `compareTo`/`compare` single call | O(1) typically (O(k) if comparing k fields, O(s) if comparing strings of length s) | O(1) | Dominated by field access/string comparison cost |
| `Collections.sort` / `Arrays.sort(Object[])` | O(n log n) worst/average | O(n) auxiliary (Timsort merge buffer) | Stable sort |
| `Comparator.comparing(...).thenComparing(...)` chain construction | O(1) to build | O(1) | The chain itself is just composed function objects |
| `TreeMap`/`TreeSet` insert using compareTo/Comparator | O(log n) | O(log n) recursion/O(1) iterative | Red-black tree, ordering drives placement |
| `PriorityQueue` insert/poll using compareTo/Comparator | O(log n) | O(1) | Binary heap, ordering drives sift-up/down |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/foundations/examples/ComparableComparatorDemo.java`

```java
Comparator<Employee> byDeptThenName = Comparator
        .comparing(Employee::getDepartment)
        .thenComparing(Employee::getName);

Comparator<Employee> bySalaryNullsFirst = Comparator
        .comparing(Employee::getSalary, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Employee::getName, Comparator.reverseOrder());
```

Expected console output:
```
Natural order (by id): [Employee{id=1}, Employee{id=2}, Employee{id=3}]
By department then name: [Eng/Amy, Eng/Bob, Ops/Zoe]
By salary nulls-first then name desc: [null/Zoe, 50000.0/Bob, 50000.0/Amy]
Reversed natural order: [Employee{id=3}, Employee{id=2}, Employee{id=1}]
```

## 5. When to use / when NOT to use

- **Use `Comparable`** when the class has one obvious, universally-agreed
  natural order (numeric types, `String` lexicographic, `LocalDate`
  chronological) — and make sure it's consistent with `equals()` (see
  pitfall below) so it behaves predictably in sorted collections.
- **Use `Comparator`** whenever you need to sort the same type multiple
  different ways at different call sites, when you can't modify the class
  (e.g. it's from a third-party library), or when the "natural" order isn't
  obvious/agreed upon (e.g. should Employees sort by id, name, or salary? —
  that's a `Comparator`, not a `Comparable`, decision).
- **Don't** implement `Comparable` "for convenience" on a class with no true
  natural order — it misleads callers into using `Collections.sort(list)`
  expecting a sensible default when there isn't one.

## 6. Common pitfalls & gotchas

**Pitfall 1 — `compareTo` inconsistent with `equals`.**

```java
class Money implements Comparable<Money> {
    BigDecimal amount; String currency;
    @Override public int compareTo(Money o) { return amount.compareTo(o.amount); } // ignores currency!
    @Override public boolean equals(Object o) { /* compares amount AND currency */ }
}
// BUG: a TreeSet<Money> will treat 10 USD and 10 EUR as "the same" (compareTo == 0),
// silently dropping one, even though equals() says they're different.
```
```java
// Fix: either make compareTo consider every field equals() considers (with a
// documented tie-break order), or clearly document/accept the inconsistency
// ("Note: this class has a natural ordering inconsistent with equals").
class Money implements Comparable<Money> {
    BigDecimal amount; String currency;
    @Override public int compareTo(Money o) {
        int byAmount = amount.compareTo(o.amount);
        return byAmount != 0 ? byAmount : currency.compareTo(o.currency);
    }
}
```

**Pitfall 2 — integer subtraction overflow in a hand-written `compare`.**

```java
Comparator<Integer> cmp = (a, b) -> a - b; // BUG: overflows for large magnitude a, b
// e.g. a = Integer.MIN_VALUE, b = 1  =>  a - b overflows and wraps to a positive number
```
```java
Comparator<Integer> cmp = Integer::compare; // FIX: no overflow, use the built-in
// or: (a, b) -> Integer.compare(a, b)
```

**Pitfall 3 — `NullPointerException` from a naive comparator when a field
can be null.**

```java
Comparator<Employee> bySalary = Comparator.comparing(Employee::getSalary);
list.sort(bySalary); // throws NPE the moment it compares against a null salary
```
```java
Comparator<Employee> bySalary =
        Comparator.comparing(Employee::getSalary, Comparator.nullsFirst(Comparator.naturalOrder()));
list.sort(bySalary); // nulls sorted to the front instead of throwing
```

## 7. Interview questions

- **[Basic]** What's the core difference between `Comparable` and
  `Comparator`? → *`Comparable` is implemented by the class itself to define
  one natural ordering (`compareTo`); `Comparator` is a separate strategy
  object passed to a sort method, letting you define any number of
  orderings without modifying the class.* → Follow-up: *Which one does
  `Collections.sort(list)` with no second argument use?* (The elements'
  natural ordering via `Comparable` — it throws `ClassCastException` at
  runtime if the elements don't implement `Comparable`.)

- **[Basic]** What must `compareTo`/`compare` return, and what do the sign
  conventions mean? → *A negative int if the first argument is "less than"
  the second, zero if equal, positive if "greater than" — the exact
  magnitude doesn't matter, only the sign. Sorting uses this to order
  elements ascending by default.* → Follow-up: *How would you sort
  descending using the same comparator?* (Wrap it with `.reversed()`, or
  swap the arguments in a lambda, or use `Comparator.reverseOrder()` for
  natural ordering.)

- **[Basic]** How do you sort a `List<Employee>` by salary descending using
  `Comparator.comparing`? → *`list.sort(Comparator.comparing(Employee::getSalary).reversed())`
  — build the ascending comparator by key extractor first, then reverse it.*
  → Follow-up: *What if two employees have the same salary — how do you add
  a tie-break by name?* (Chain `.thenComparing(Employee::getName)` after the
  `.reversed()` call.)

- **[Intermediate]** Why does `(a, b) -> a - b` make a buggy `Comparator<Integer>`?
  → *Subtracting two `int`s can overflow for large-magnitude values with
  opposite signs (e.g. `Integer.MIN_VALUE - 1` wraps around), producing an
  incorrect sign and therefore an incorrect ordering. Always use
  `Integer.compare(a, b)` (or the boxed `Integer::compareTo`) instead, which
  handles the full range correctly without overflow.* → Follow-up: *Does
  this overflow risk apply to `Comparator.comparingInt`?* (No —
  `Comparator.comparingInt` internally uses `Integer.compare` on the
  extracted int, avoiding the subtraction trap.)

- **[Intermediate]** What does `thenComparing` actually do, mechanically? →
  *It returns a new `Comparator` that first applies the original comparator;
  if and only if that returns 0 (a tie), it applies the next comparator in
  the chain to break the tie. You can chain as many `thenComparing` calls as
  you have tie-break levels.* → Follow-up: *Is the final ordering
  guaranteed stable if none of the comparators produce a total order (some
  ties remain unresolved)?* (Yes for elements that compare fully equal
  through the whole chain — Java's object sort (Timsort) is stable, so
  those elements retain their original relative input order.)

- **[Intermediate]** How do `Comparator.nullsFirst` and `Comparator.nullsLast`
  work? → *They're comparator decorators: given an underlying comparator for
  non-null values, they wrap it so that a null argument is always treated as
  smaller (`nullsFirst`) or larger (`nullsLast`) than any non-null value,
  without invoking the underlying comparator (and thus without risking an
  NPE) when either side is null.* → Follow-up: *Can you combine nullsFirst
  with reversed() — does order of wrapping matter?* (Yes it matters:
  `Comparator.nullsFirst(cmp.reversed())` reverses only the non-null
  ordering while nulls stay first; `Comparator.nullsFirst(cmp).reversed()`
  reverses EVERYTHING including null placement, pushing nulls to the end.)

- **[Advanced]** Why must a well-formed `Comparator`/`Comparable` be
  *transitive and consistent* (`compare(a,b) > 0 && compare(b,c) > 0` implies
  `compare(a,c) > 0`), and what breaks if it isn't? → *Sorting algorithms and
  ordered collections (`TreeMap`, `TreeSet`, `PriorityQueue`) assume a total
  order to make correctness/performance guarantees; a non-transitive
  comparator can cause `Collections.sort`/`Arrays.sort` to throw
  `IllegalArgumentException: Comparison method violates its general
  contract!` (Java 7+'s Timsort actively detects certain contract
  violations), or silently corrupt a `TreeMap`'s red-black tree balance,
  causing incorrect `get`/`containsKey` results for keys that are actually
  present.* → Follow-up: *Give a realistic example of an accidentally
  non-transitive comparator.* (Comparing floating-point values with a
  tolerance/epsilon check, e.g. treating values within 0.001 of each other
  as "equal" — a can be "equal" to b, b "equal" to c, but a and c differ by
  more than the epsilon and compare as unequal, breaking transitivity.)

- **[Advanced]** How does `TreeMap`/`TreeSet` decide which ordering to use —
  `Comparable` or a supplied `Comparator` — and what happens if you insert an
  element that doesn't implement `Comparable` into a no-arg `TreeSet`? →
  *`TreeMap`/`TreeSet` have constructors that accept an explicit
  `Comparator`; if none is supplied, they fall back to the elements' natural
  ordering via `Comparable`, and use it EXCLUSIVELY for ordering AND
  uniqueness — `equals()` is never called by the sorted-set/map's internal
  logic, so `compareTo() == 0` (not `.equals()`) is what determines "same
  key" for insertion purposes ("consistent with equals" is only a
  recommendation, not enforced). Adding a non-Comparable element to a
  no-comparator `TreeSet` throws `ClassCastException` at insertion time
  (lazily, not at construction time).* → Follow-up: *What's a concrete bug
  this causes if compareTo and equals disagree?* (`TreeSet.add` can silently
  refuse to add an element that `equals()` says is different but
  `compareTo()` says is "equal" (0), because the tree only ever checks
  `compareTo() == 0` to detect duplicates.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Implement `Comparable<Employee>` natural order by id | Comparable | `exercises/Employee.java` |
| E02 | Easy | `Comparator<Employee>` by department then name | `comparing`/`thenComparing` | `exercises/EmployeeComparators.java` |
| E03 | Medium | `Comparator<Employee>` with nulls-first salary, then name descending | `nullsFirst` + `reversed` | `exercises/EmployeeComparators.java` |
| E04 | Hard | Comparator for dotted version strings, numeric not lexicographic | custom tokenizing comparator | `exercises/VersionComparator.java` |

- **E01 hint:**
  <details><summary>hint</summary>Use `Integer.compare(this.id, other.id)` — avoid raw subtraction to sidestep overflow.</details>
- **E02 hint:**
  <details><summary>hint</summary>`Comparator.comparing(Employee::getDepartment).thenComparing(Employee::getName)`.</details>
- **E03 hint:**
  <details><summary>hint</summary>`Comparator.comparing(Employee::getSalary, Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(Employee::getName, Comparator.reverseOrder())`.</details>
- **E04 hint:**
  <details><summary>hint</summary>Split both strings on `"\\."`, pad the shorter one conceptually with zeros (treat a missing segment as 0), parse each segment as `int`, and compare segment by segment, returning on the first non-zero comparison.</details>

Target complexity — E01: O(1). E02/E03: O(1) to build the comparator (sort
itself is O(n log n) when applied). E04: O(k) where k = number of dotted
segments. Solutions are in `src/main/java/com/gk/study/foundations/solutions/`
— not shown here; attempt the exercises first.

## 9. Quick recap

- `Comparable` = one natural order, defined inside the class; `Comparator` = any number of orderings, defined outside the class.
- Always use `Integer.compare`/`Comparator.comparingInt` instead of subtraction — subtraction overflows for extreme values.
- `Comparator.nullsFirst`/`nullsLast` prevent NPEs when a sort key can be null — wrap the underlying comparator, don't hand-roll null checks.
- Chain tie-breakers with `.thenComparing(...)`; it only activates when the previous comparator returned 0.
- `TreeMap`/`TreeSet` use `compareTo`/`Comparator` exclusively for both ordering AND uniqueness — `equals()` is not consulted, so an inconsistent compareTo can silently drop "different" elements.
