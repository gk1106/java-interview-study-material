# Generics (Bounded Types, Wildcards, PECS)

## 1. What it is

Generics let you parameterize classes, interfaces and methods over types
(`List<T>`, `Comparable<T>`), so the compiler enforces type safety at compile
time instead of relying on casts and `ClassCastException` at runtime. Bounded
types (`<T extends Number>`) restrict what a type parameter can be; wildcards
(`? extends T`, `? super T`) let API signatures accept a *range* of related
types when you don't need to name the exact type parameter.

## 2. How it works internally

Java generics are implemented via **type erasure**: generic type information
exists only in source code and is checked by the compiler; at bytecode level
it is mostly erased. `List<String>` and `List<Integer>` compile down to the
same `List` bytecode with casts inserted automatically at call sites. This is
why:

- You cannot do `new T[10]` (no runtime type to allocate) or `instanceof T`.
- A generic class has exactly one `.class` file, regardless of how many type
  parameters are used at various call sites (no template bloat like C++).
- Overloading two methods that differ only by erased generic type
  (`void m(List<String> l)` vs `void m(List<Integer> l)`) is a compile error
  — they have the same erasure.

```
Source:                       After erasure (bytecode-level, simplified):
List<String> list = ...;      List list = ...;
String s = list.get(0);       String s = (String) list.get(0);  // cast inserted by compiler
```

**PECS — "Producer Extends, Consumer Super"** (a mnemonic from Effective
Java) governs which wildcard to use in a method signature:

- If a parameterized type only *produces* values you read out of it (you
  never write into it), use `? extends T` — e.g. `List<? extends Number>` can
  be a `List<Integer>` or `List<Double>`, and you can safely read `Number`s
  out of it, but you can't add to it (the compiler doesn't know if it's
  really a `List<Integer>` or `List<Double>`, so adding *anything* except
  `null` is unsafe and rejected).
- If a parameterized type only *consumes* values you write into it (you don't
  read specific types back out, only as `Object`), use `? super T` — e.g.
  `List<? super Integer>` could be a `List<Integer>`, `List<Number>` or
  `List<Object>`; you can safely add `Integer`s to it because any of those
  backing lists can legally hold an `Integer`.
- If you both read and write a specific type, use an exact type parameter
  `List<T>`, not a wildcard.

```
        ? extends T                      ? super T
    (upper-bounded, "producer")      (lower-bounded, "consumer")
   ┌─────────────────────┐         ┌─────────────────────┐
   │  List<? extends Num> │         │  List<? super Int>   │
   │  could be:           │         │  could be:            │
   │   List<Integer>      │  READ   │   List<Integer>       │  WRITE
   │   List<Double>       │ ------> │   List<Number>        │ <------
   │   List<Number>       │  (safe) │   List<Object>        │  (safe)
   └─────────────────────┘         └─────────────────────┘
   you may READ as Number            you may WRITE an Integer
   you may NOT add (except null)     reading gives only Object
```

## 3. Complexity

Generics are a compile-time-only mechanism — there is no runtime time/space
cost from using them (erasure means the bytecode is the same as if you'd
written raw types with manual casts). The "complexity" that matters here is
correctness/expressiveness, not Big-O:

| Concept | What it buys you | Cost |
|---------|-------------------|------|
| Generic class/method | Compile-time type safety, no manual casts | None at runtime (erasure) |
| Bounded type `<T extends X>` | Restricts T to X or subtypes; lets you call X's methods on T | Compile-time only |
| `? extends T` wildcard | Read-only, covariant-safe access to a family of types | Cannot add (except null) |
| `? super T` wildcard | Write-only-ish, contravariant-safe access | Reads only give Object |
| Unbounded `<T>` / raw `List` | Raw types bypass checks entirely | Unchecked warnings, runtime `ClassCastException` risk |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/foundations/examples/GenericsDemo.java`

```java
static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

static void copy(List<? extends Number> src, List<? super Number> dest) {
    for (Number n : src) {
        dest.add(n); // PECS: src produces (extends), dest consumes (super)
    }
}
```

Expected console output:
```
max(3, 7) = 7
max("apple", "banana") = banana
PECS copy: source=[1, 2, 3] -> destination=[1, 2, 3]
Raw type warning demo: ClassCastException would occur here (caught) - message: class java.lang.Integer cannot be cast to class java.lang.String
```

## 5. When to use / when NOT to use

- **Use** generics whenever an API operates on a container or algorithm that
  should work across types safely (any collection, any comparator, any
  cache) — this is the default and correct choice in almost all modern Java.
- **Use** wildcards in *method parameter* positions to maximize API
  flexibility (accept the widest safe range of caller types) — this is the
  PECS guidance. Don't use wildcards on *return types*; callers gain nothing
  from `? extends T` as a return type and lose the ability to call methods
  that need the exact `T`.
- **Don't** use raw types (`List` instead of `List<String>`) in new code —
  they exist only for backward compatibility with pre-Java-5 code and
  disable compile-time checking, deferring bugs to runtime
  `ClassCastException`s.
- **Don't** over-engineer with deeply nested bounded wildcards
  (`Map<? extends K, ? extends List<? super V>>`) when a simpler exact-type
  signature reads just as well and is used by only one call site.

## 6. Common pitfalls & gotchas

**Pitfall 1 — trying to add to a `? extends T` list.**

```java
List<? extends Number> nums = new ArrayList<Integer>();
nums.add(5); // COMPILE ERROR: cannot add to a "produces-only" wildcard list
```
```java
// Fix: either use the exact type, or only read from an "extends" list.
List<Integer> nums = new ArrayList<>();
nums.add(5); // fine, exact type known
```

**Pitfall 2 — generic array creation.**

```java
// COMPILE ERROR: generic array creation
T[] array = new T[10];
```
```java
// Fix: create an Object[] and cast (with @SuppressWarnings("unchecked")),
// exactly how java.util.ArrayList itself works internally.
@SuppressWarnings("unchecked")
T[] array = (T[]) new Object[10];
```

**Pitfall 3 — assuming generics give runtime type safety across a raw-type
boundary.**

```java
List<String> strings = new ArrayList<>();
List raw = strings;         // raw type reference to the same list
raw.add(42);                 // compiles with an unchecked warning, no error!
String s = strings.get(0);  // throws ClassCastException at runtime here,
                              // NOT at the raw.add(42) line that caused it
```
```java
// Fix: never mix raw types with parameterized types; enable
// -Xlint:unchecked and treat every "unchecked" warning as a real bug to fix.
List<String> strings = new ArrayList<>();
strings.add("safe"); // compiler now rejects strings.add(42) directly
```

## 7. Interview questions

- **[Basic]** What is type erasure and why does Java use it? → *Generic type
  parameters exist only at compile time; the compiler checks them and then
  erases them, replacing type parameters with their bound (`Object` if
  unbounded) and inserting casts where needed. Java chose erasure for
  backward compatibility with pre-generics (Java 1.4) bytecode and
  libraries, so old and new code can interoperate on the same collection
  classes.* → Follow-up: *What can't you do because of erasure?* (`new T[]`,
  `instanceof T`, `T.class`, overloading on erased-equal signatures, and
  creating exceptions parameterized by a generic type.)

- **[Basic]** What's the difference between `List<Object>` and a raw `List`?
  → *`List<Object>` is still fully type-checked — you can add any object but
  the compiler knows it's a `List<Object>`. A raw `List` disables generics
  checking entirely, producing unchecked warnings and deferring type errors
  to runtime `ClassCastException`s. Also, `List<String>` is NOT a subtype of
  `List<Object>` (generics are invariant), but `List<String>` IS assignable
  to a raw `List`.* → Follow-up: *Why is `List<String>` not a `List<Object>`?*
  (If it were, you could `listOfObject.add(42)` through a `List<String>`
  reference and violate type safety — that's exactly what wildcards +
  `? extends`/`? super` are designed to solve safely.)

- **[Basic]** What does `<T extends Comparable<T>>` mean? → *It's a bounded
  type parameter: T must implement `Comparable<T>`, so within the generic
  method/class you can call `t1.compareTo(t2)` on values of type T. Without
  the bound, T is only known to be `Object`, and you couldn't call
  `compareTo`.* → Follow-up: *Can a type parameter have multiple bounds?*
  (Yes: `<T extends Number & Comparable<T>>` — at most one class bound
  (must be listed first) plus any number of interface bounds.)

- **[Intermediate]** Explain PECS with a concrete `Collections.copy`-like
  example. → *"Producer Extends, Consumer Super": if you're reading values
  out of a generic structure, bound it with `extends` (it produces values for
  you); if you're writing values into it, bound it with `super` (it consumes
  values from you). `Collections.copy(List<? super T> dest, List<? extends T> src)`
  is the canonical example — src only ever gives you T's out (producer), dest
  only ever receives T's in (consumer).* → Follow-up: *What if a parameter
  both produces and consumes T?* (Then wildcards don't help — use the exact
  type parameter `List<T>` for that parameter.)

- **[Intermediate]** Why can't you overload `void process(List<String> l)`
  and `void process(List<Integer> l)` in the same class? → *After type
  erasure both methods have the identical signature `void process(List l)`
  — it's a compile-time "erasure clash" error, not something that can be
  resolved at runtime since generic type info isn't retained on the
  parameter.* → Follow-up: *How would you achieve similar behavior?* (Give
  the methods different names, or take a `Class<T>` token parameter to
  discriminate at runtime, or wrap each in a distinguishable type.)

- **[Intermediate]** What is a bridge method and why does the compiler
  generate them? → *When a generic class is subclassed/implemented with a
  concrete type argument and a method is overridden, the compiler generates
  a synthetic "bridge" method with the erased (Object-based) signature that
  delegates to your typed override, so that erasure-based polymorphism and
  reflection/casts still work correctly at the bytecode level (e.g.
  overriding `compareTo(T)` from `Comparable<T>`).* → Follow-up: *Can you see
  bridge methods via reflection?* (Yes — `Method.isBridge()`; they show up as
  extra synthetic methods when you reflect over a generic class hierarchy.)

- **[Advanced]** Why is it legal to call `list.add(null)` on a
  `List<? extends Number>` but nothing else? → *`null` is a valid value of
  every reference type, so it's the one value the compiler can prove is safe
  to insert regardless of which concrete type the wildcard actually
  represents at runtime. Any non-null value could violate type safety since
  the compiler doesn't know if the underlying list is `List<Integer>`,
  `List<Double>`, etc.* → Follow-up: *Is there a way to add non-null values
  to an unknown wildcard-typed list safely?* (Only by capturing the wildcard
  into a type parameter via a private helper method — a technique called
  "wildcard capture" — which lets the compiler treat the unknown `?` as a
  concrete-but-unnamed type `T` for the duration of that helper call.)

- **[Advanced]** How do generics interact with arrays regarding covariance,
  and why does that matter for `ArrayStoreException`? → *Arrays are
  covariant at runtime (`Object[] arr = new String[3]` compiles, and the JVM
  checks each store, throwing `ArrayStoreException` if you store the wrong
  type) — a runtime check. Generics are erased and invariant, checked only
  at compile time with no runtime check at all. This mismatch is exactly why
  you can't create `new T[]` directly: there's no way to enforce the runtime
  array-store check for an erased type, so the JDK collection classes
  internally use `Object[]` and cast on read instead of using true generic
  arrays.* → Follow-up: *How does `ArrayList.toArray(T[] a)` handle this
  safely?* (It uses `java.lang.reflect.Array.newInstance(componentType, size)`
  to create a correctly-typed array at runtime, using the caller-supplied
  array's actual runtime component type as the source of truth, since T
  itself is erased.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Generic `max` of three Comparable values | bounded type parameter | `exercises/MaxOfThree.java` |
| E02 | Easy | Sum a `List<? extends Number>` as a double | bounded wildcard, producer | `exercises/BoundedNumberSum.java` |
| E03 | Medium | PECS-correct generic copy method | `? extends T` / `? super T` | `exercises/PecsCopy.java` |
| E04 | Hard | Generic resizable stack backed by `Object[]` | generic array workaround | `exercises/GenericStack.java` |

- **E01 hint:**
  <details><summary>hint</summary>Bound T with `<T extends Comparable<T>>`, then chain two `compareTo` calls (or use `Collections.max` conceptually) to find the largest of three.</details>
- **E02 hint:**
  <details><summary>hint</summary>`<T extends Number>` on the method, iterate calling `.doubleValue()` on each element and accumulate.</details>
- **E03 hint:**
  <details><summary>hint</summary>Source parameter type is `List<? extends T>` (producer, you read from it); destination parameter type is `List<? super T>` (consumer, you write to it).</details>
- **E04 hint:**
  <details><summary>hint</summary>Back the stack with `Object[]`, cast to `T` on `pop()`/`peek()` with `@SuppressWarnings("unchecked")` — this mirrors how the JDK's own generic collections are implemented internally.</details>

Target complexity — E01: O(1). E02: O(n) time, O(1) space. E03: O(n) time,
O(1) extra space (writes into an existing dest list). E04: push/pop/peek
amortized O(1). Solutions are in
`src/main/java/com/gk/study/foundations/solutions/` — not shown here;
attempt the exercises first.

## 9. Quick recap

- Generics are erased at compile time — no runtime cost, but also no runtime type info (`instanceof T`, `new T[]` are illegal).
- Bounded type parameters (`<T extends X>`) let you call X's methods on T inside the generic code.
- PECS: `? extends T` for producers (you read T out), `? super T` for consumers (you write T in), exact `T` when you do both.
- Generics are invariant (`List<String>` is not a `List<Object>`); arrays are covariant with a runtime `ArrayStoreException` check — this mismatch is why generic array creation is disallowed.
- Never mix raw types with generic types — it silently disables compile-time checking and defers errors to a confusing runtime `ClassCastException`.
