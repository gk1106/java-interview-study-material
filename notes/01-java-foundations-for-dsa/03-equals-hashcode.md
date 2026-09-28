# equals() / hashCode() Contract

## 1. What it is

`equals()` defines logical equality between two objects (do they represent
the same value, not necessarily the same memory address); `hashCode()`
produces an int "bucket number" used by hash-based collections (`HashMap`,
`HashSet`, `HashTable`) to place and find objects quickly. The two must be
implemented *together* and consistently, or hash-based collections silently
break — objects "disappear" into the wrong bucket.

## 2. How it works internally

`Object`'s default `equals()` is reference equality (`this == other`) and
default `hashCode()` is derived from the object's identity (historically
related to its memory address, though the JVM is free to implement it any
way, e.g. a thread-local counter in some HotSpot configurations). When you
override `equals()` to mean "same field values", you MUST also override
`hashCode()` so that:

**The equals/hashCode contract (from `Object`'s Javadoc, paraphrased):**
1. **Consistency**: multiple invocations of `equals()`/`hashCode()` on the
   same unchanged object must keep returning the same result.
2. **If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` MUST be
   true.** (The reverse is not required — two unequal objects *can* share a
   hash code; that's called a collision, and it's allowed, just should be
   rare for a good hash function.)
3. **Reflexive**: `a.equals(a)` is always true.
4. **Symmetric**: `a.equals(b)` iff `b.equals(a)`.
5. **Transitive**: if `a.equals(b)` and `b.equals(c)`, then `a.equals(c)`.
6. **Non-null**: `a.equals(null)` must be false, never throw.

**Why breaking rule 2 corrupts a HashMap:**

```
HashMap bucket layout (simplified), table.length = 16:
bucket index = hashCode() spread-and-masked against (table.length - 1)

  put(key):  bucket = hash(key.hashCode()) & 15  -> store entry in that bucket's chain
  get(key):  bucket = hash(key.hashCode()) & 15  -> scan ONLY that chain, using equals()
                                                     to find the matching entry

If two equal keys (a.equals(b) == true) hash to DIFFERENT buckets because
hashCode() wasn't overridden consistently with equals():

  put(a, "value")   -> goes into bucket computed from a.hashCode()  (e.g. bucket 3)
  get(b)             -> looks in bucket computed from b.hashCode()  (e.g. bucket 9)
                        -> bucket 9 doesn't contain "value" -> get(b) returns null!
                        even though a.equals(b) is true.
```

Modern `HashMap` (Java 8+) additionally spreads the hash
(`h ^ (h >>> 16)`, paraphrased) to reduce collisions from poor low-bit
distribution, and treeifies a bucket's linked list into a red-black tree once
it has 8+ entries AND the table has capacity ≥ 64 (otherwise it just resizes
the table first) — turning worst-case bucket lookup from O(n) into O(log n).
That optimization only helps if `hashCode()` is implemented at all reasonably
— a `hashCode()` that always returns the same constant defeats it entirely
(every entry lands in one bucket, degrading to a single linked list/tree of
all N entries).

**Standard recipe** (what `Objects.hash(...)` and IDE-generated code do,
paraphrased, not verbatim JDK source): start with a non-zero seed (traditionally
17), and for each significant field: `result = 31 * result + fieldHash`. 31 is
chosen because it's an odd prime, and `31 * x` can be strength-reduced by the
JIT to `(x << 5) - x`, which is fast.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `equals()` (well-written) | O(k) where k = number of significant fields | O(1) | Short-circuit on first unequal field |
| `hashCode()` (well-written) | O(k) | O(1) | Must touch the SAME fields `equals()` uses |
| `HashMap.get`/`put` with correct contract | O(1) average | O(1) | Degrades to O(log n) per bucket (treeified) or O(n) (untreeified/pre-Java-8) under heavy collisions |
| `HashMap.get`/`put` with BROKEN contract | O(1) but WRONG RESULT | — | Silent correctness bug, not a performance bug |
| Array field `equals`/`hashCode` via `Arrays.equals`/`Arrays.hashCode` | O(array length) | O(1) | Plain `==`/default hashCode on an array field is a common bug (identity, not content) |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/foundations/examples/EqualsHashCodeDemo.java`

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Point p)) return false;
    return x == p.x && y == p.y;
}

@Override
public int hashCode() {
    return Objects.hash(x, y);
}
```

Expected console output:
```
p1.equals(p2) = true, p1.hashCode()==p2.hashCode() = true
HashSet dedup size (expect 2 unique points among 3, one duplicate) = 2
Broken point (no hashCode override) lost in HashMap: lookup after re-creating equal key = null
Fixed point found in HashMap: lookup after re-creating equal key = "origin"
```

## 5. When to use / when NOT to use

- **Always** override both together when a class represents a *value*
  (an amount of money, a coordinate, an account number, an immutable DTO)
  that you intend to compare by content or store in a `HashSet`/use as a
  `HashMap` key.
- **Don't** override `equals()`/`hashCode()` for mutable entity classes you
  plan to use as hash-map/hash-set keys where the significant fields can
  change after insertion — mutating a key's hashCode after it's already
  placed in a bucket makes it unfindable (it's still in its *old* bucket, but
  a fresh lookup computes its *new* bucket). If you must use a mutable object
  as a key, base equals/hashCode ONLY on an immutable identifier (e.g. a
  database primary key that never changes) — or better, don't put mutable
  keys in hash structures at all.
- **Don't** bother for classes compared purely by reference identity (most
  Spring `@Service`/`@Component` singletons, JPA entities managed purely by
  reference-in-memory during a request) — the default `Object` behavior is
  correct there and adding a bespoke `equals`/`hashCode` (e.g. based only on
  a possibly-null generated ID) is a well-known source of bugs with JPA
  entities in Sets before they're persisted.

## 6. Common pitfalls & gotchas

**Pitfall 1 — overriding `equals()` but forgetting `hashCode()`.**

```java
public class Point {
    int x, y;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }
    // no hashCode() override -> inherits Object's identity hashCode
}
// BUG: set.add(new Point(1,1)); set.contains(new Point(1,1)) -> false!
```
```java
public class Point {
    int x, y;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y); // FIX: consistent with equals' fields
    }
}
```

**Pitfall 2 — using an array field directly in `equals`/`hashCode` without
`Arrays.equals`/`Arrays.hashCode`.**

```java
class Row {
    int[] cells;
    @Override
    public boolean equals(Object o) {
        Row r = (Row) o;
        return cells == r.cells; // BUG: reference equality on the array, not content
    }
    @Override
    public int hashCode() {
        return cells.hashCode(); // BUG: Object.hashCode() on the array = identity, not content
    }
}
```
```java
class Row {
    int[] cells;
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Row r)) return false;
        return Arrays.equals(cells, r.cells); // FIX: content equality
    }
    @Override
    public int hashCode() {
        return Arrays.hashCode(cells); // FIX: content-based hash
    }
}
```

**Pitfall 3 — mutating a field used in `hashCode()` after inserting the
object into a `HashSet`/`HashMap` key position.**

```java
Set<Point> set = new HashSet<>();
Point p = new Point(1, 1);
set.add(p);
p.x = 99;                 // mutate a field that hashCode() depends on
set.contains(p);          // BUG: often false! p is now "lost" in its old bucket.
```
```java
// Fix: make value classes used as hash keys IMMUTABLE (final fields, no setters),
// or never mutate an object once it's a key in a hash-based collection.
final class Point {
    private final int x, y; // immutable -> hashCode never changes after construction
    // ... constructor, equals, hashCode, no setters
}
```

## 7. Interview questions

- **[Basic]** Why must you override `hashCode()` whenever you override
  `equals()`? → *Because hash-based collections use `hashCode()` to pick a
  bucket and `equals()` to confirm a match within that bucket. If two
  objects are equal by `equals()` but have different hash codes, a
  `HashMap`/`HashSet` will look for one in the wrong bucket and silently
  fail to find it — `get`/`contains` return the wrong answer even though the
  objects are logically equal.* → Follow-up: *Is the reverse also required —
  must equal hash codes imply equals() is true?* (No — hash collisions are
  allowed and expected occasionally; two different objects can share a hash
  code, `equals()` is what disambiguates within a bucket.)

- **[Basic]** What are the five properties of the `equals()` contract? →
  *Reflexive (`a.equals(a)`), symmetric (`a.equals(b) == b.equals(a)`),
  transitive (`a.equals(b) && b.equals(c) => a.equals(c)`), consistent
  (repeated calls return the same result if nothing changed), and
  `a.equals(null)` must return false (never throw).* → Follow-up: *Give an
  example of breaking symmetry.* (A subclass that adds `instanceof
  Subclass` check and compares extra fields against a superclass instance —
  `subclass.equals(superclassInstance)` may be false while
  `superclassInstance.equals(subclass)` uses only superclass fields and
  returns true, or vice versa, depending on which side is which type.)

- **[Basic]** What does `Objects.equals(a, b)` do differently from
  `a.equals(b)`? → *`Objects.equals` is null-safe: it returns true if both
  are null, false if exactly one is null, and otherwise delegates to
  `a.equals(b)` — avoiding a `NullPointerException` you'd get calling
  `a.equals(b)` directly if `a` were null.* → Follow-up: *Where is this
  commonly used?* (Inside generated/hand-written `equals()` implementations
  to compare nullable fields, e.g. `Objects.equals(this.name, other.name)`.)

- **[Intermediate]** Can you use `instanceof` vs `getClass() ==
  o.getClass()` in `equals()` — what's the trade-off? → *`instanceof`
  (or a pattern-matched `instanceof Point p`) allows a subclass instance to
  be equal to a superclass instance, which can violate the *transitive* or
  *symmetric* property when the subclass adds significant fields.
  `getClass() == o.getClass()` is stricter — only exact same runtime class
  can be equal — which is safer for value classes designed with inheritance
  in mind, but forbids two "equivalent" instances of different subclasses
  from being equal even if that would be intuitive.* → Follow-up: *What does
  Effective Java recommend?* (Prefer composition over inheritance for value
  classes; if you must subclass a class with `equals()`, favor `getClass()`
  comparison, or better, make the value class `final` so there are no
  subclasses to worry about.)

- **[Intermediate]** How does `HashMap` use `hashCode()` internally to place
  entries — is the raw `hashCode()` used as the bucket index directly? →
  *No — `HashMap` spreads/mixes the hash (XOR-ing the hash with its own
  unsigned right shift by 16 bits, paraphrased) before masking it with
  `(table.length - 1)` to compute the bucket index. This spreading step
  reduces collisions caused by hash codes that differ only in their high
  bits, since the mask only keeps the low bits when table.length is a small
  power of two.* → Follow-up: *Why must table.length be a power of two?*
  (So `hash & (length - 1)` is equivalent to `hash % length` but much
  faster — bitmasking instead of a division/modulo instruction — and gives a
  clean bit-mask range.)

- **[Intermediate]** Why is it dangerous to use a mutable field in
  `hashCode()` for an object stored as a `HashMap` key? → *Once inserted,
  the entry lives in the bucket computed from the key's hash code AT
  INSERTION TIME. If you later mutate a field that `hashCode()` depends on,
  a future lookup recomputes a DIFFERENT bucket for that (now-mutated) key,
  so the entry becomes practically unreachable — it's still in the map (memory
  leak) but `get`/`remove`/`containsKey` with an equal-looking key won't find
  it.* → Follow-up: *How do you safely use identifiers that might change,
  like a JPA entity, as a Set element?* (Base equals/hashCode on an immutable
  business key (or a constant, e.g. always `super.hashCode()` until an ID is
  assigned), or avoid using JPA entities in Sets before they have a stable
  persisted ID.)

- **[Advanced]** Explain what "hash flooding" / adversarial hash collisions
  are and how Java's `HashMap` mitigates them. → *An attacker who can
  control keys inserted into a public-facing `HashMap` (e.g. HTTP form field
  names, JSON keys) could craft many keys that collide into the same bucket,
  degrading `get`/`put` from O(1) to O(n) per operation — a denial-of-service
  vector. Java 8+ mitigates this by treeifying a bucket into a red-black
  tree once it accumulates 8+ entries (and the table is large enough),
  bounding worst-case per-bucket lookup at O(log n) instead of O(n), using
  the keys' natural ordering (if `Comparable`) or a tie-break via identity
  hash / class name comparison as a secondary order.* → Follow-up: *Does
  treeification help if the colliding keys aren't Comparable?* (Yes, still
  bounds it via a deterministic tie-breaking comparison, though it's less
  efficient than a natural ordering; the key point is it avoids true O(n)
  linked-list degradation.)

- **[Advanced]** If `hashCode()` always returns a constant (e.g. `return
  1;`), is the `equals()`/`hashCode()` contract technically satisfied? What's
  the practical consequence? → *Yes, technically legal — equal objects still
  have equal (identical, even) hash codes, satisfying the contract. The
  practical consequence is catastrophic performance: every single entry
  lands in the exact same bucket, so `HashMap`/`HashSet` operations degrade
  from O(1) average to O(n) (or O(log n) once treeified, still far worse than
  intended) — it's a legality-vs-quality distinction: a "legal but terrible"
  hash function.* → Follow-up: *What makes a hash function "good" beyond
  legality?* (Uniform distribution across the output range so that
  unrelated objects rarely collide, using all the significant fields, and
  being fast to compute — this is why `Objects.hash(...)`'s `31*result+field`
  recipe is the standard default rather than something simpler.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Contract-correct equals/hashCode for an immutable `Money` value class | value class + BigDecimal | `exercises/Money.java` |
| E02 | Easy | Contract-correct equals/hashCode for an immutable `PhoneNumber` value class | value class | `exercises/PhoneNumber.java` |
| E03 | Medium | Case-insensitive `equals`/`hashCode` wrapper usable correctly in a `HashSet` | normalized hashing | `exercises/CaseInsensitiveString.java` |
| E04 | Hard | Contract-correct equals/hashCode for a value class with an array field | `Arrays.equals`/`Arrays.hashCode` | `exercises/VectorValue.java` |

- **E01 hint:**
  <details><summary>hint</summary>Compare `amount` with `BigDecimal.compareTo(...) == 0` (NOT `.equals()`, since `BigDecimal.equals` also compares scale — `10.0` and `10.00` would be "unequal" otherwise), and hash using `amount.stripTrailingZeros()` or round to a canonical scale before hashing so equal-by-compareTo amounts also hash equally.</details>
- **E02 hint:**
  <details><summary>hint</summary>Use `Objects.equals` for each nullable field and `Objects.hash(field1, field2, ...)` for the hash — the textbook pattern.</details>
- **E03 hint:**
  <details><summary>hint</summary>Normalize with `value.toLowerCase(Locale.ROOT)` consistently in BOTH equals and hashCode (same normalization in both, or the contract breaks).</details>
- **E04 hint:**
  <details><summary>hint</summary>Never use `==` or the array's own default `hashCode()`/`equals()` on the array field — always go through `java.util.Arrays.equals(int[], int[])` and `java.util.Arrays.hashCode(int[])`.</details>

Target complexity — all four: O(k) time / O(1) space per `equals`/`hashCode`
call, where k = number of significant fields (or array length for E04).
Solutions are in `src/main/java/com/gk/study/foundations/solutions/` — not
shown here; attempt the exercises first.

## 9. Quick recap

- If `a.equals(b)` is true, `a.hashCode()` MUST equal `b.hashCode()` — the reverse is not required (collisions are fine).
- Forgetting `hashCode()` after overriding `equals()` is the single most common Java collections bug — objects silently "disappear" from `HashSet`/`HashMap`.
- Never use a mutable field (that can change after insertion) as part of a hash-based collection key's `hashCode()`.
- Array fields need `Arrays.equals`/`Arrays.hashCode` — plain `==` or the array's default `hashCode()` compares identity, not content.
- `HashMap` spreads/mixes hash bits and treeifies buckets with 8+ colliding entries (table ≥ 64) to bound worst-case lookup at O(log n) instead of O(n).
