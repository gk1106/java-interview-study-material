# Java 8 → 21 language features — interview questions

Covers the language-level evolution from Java 8 through 21 (LTS-to-LTS): lambdas/method references,
interface default/static methods, `var`, switch expressions, text blocks, records, sealed classes,
pattern matching (`instanceof` and `switch`), virtual threads (language/API surface — see
`notes/09-multithreading-concurrency/11-virtual-threads.md` and
`notes/10-java-interview-questions/concurrency.md` for the concurrency-mechanics deep dive), and
sequenced collections. Streams/Collectors internals are covered separately in `streams.md`. Tags:
`[Basic]` / `[Intermediate]` / `[Advanced]`.

Sections: [Lambdas & method references](#1-lambdas--method-references) · [Default & static interface methods](#2-default--static-interface-methods)
· [var (Java 10)](#3-var-java-10) · [Switch expressions (Java 14)](#4-switch-expressions-java-14)
· [Text blocks (Java 15)](#5-text-blocks-java-15) · [Records (Java 16)](#6-records-java-16)
· [Sealed classes (Java 17)](#7-sealed-classes-java-17) · [Pattern matching (17/21)](#8-pattern-matching-for-instanceof-and-switch-java-1721)
· [Sequenced collections (Java 21)](#9-sequenced-collections-java-21) · [Virtual threads recap](#10-virtual-threads-java-21-recap)
· [Predict-the-output puzzles](#11-predict-the-output-puzzles)

---

## 1. Lambdas & method references

**[Basic] What is a functional interface, and why does a lambda expression need one?**
A functional interface is any interface with exactly **one abstract method** (it may have any
number of `default`/`static`/`private` methods, and inherited `Object` methods like `equals` don't
count toward the "one abstract method" total) — `@FunctionalInterface` is an optional but
recommended compiler-enforced annotation documenting this intent. A lambda expression is purely
syntactic sugar for an instance of a functional interface: the lambda's parameter list and body
supply the implementation of that single abstract method, and the compiler infers which functional
interface is being implemented entirely from the **target type context** (a variable's declared
type, a method parameter type, a return type) — a lambda has no meaning or type in isolation.
*Follow-up: can you write a lambda for an interface with two abstract methods?* No — the compiler
rejects it; a lambda can only ever implement exactly one abstract method, since its body IS that one
method's implementation with no way to express which of two methods it's for.

**[Basic] What are the four method reference forms, with an example of each?**
`ClassName::staticMethod` (reference to a static method — `Integer::parseInt`).
`instance::instanceMethod` (reference to an instance method of a **particular, already-existing**
object — `myList::add`). `ClassName::instanceMethod` (reference to an instance method, called on
**whichever object is supplied as the first lambda parameter** at call time —
`String::toUpperCase`, equivalent to `s -> s.toUpperCase()`). `ClassName::new` (constructor
reference — `ArrayList::new`, equivalent to `() -> new ArrayList<>()`).
*Follow-up: what's the practical difference between `instance::instanceMethod` and
`ClassName::instanceMethod` when both compile?* The former always operates on the **same, fixed**
captured object regardless of what arguments the functional interface passes in; the latter treats
the functional interface's first parameter as the receiver object itself, so it works generically
across whatever object is supplied at each call, not one fixed instance.

**[Intermediate] What exactly does a lambda "capture" from its enclosing scope, and why must
captured local variables be effectively final?**
A lambda can reference local variables, method parameters, and `this` from its enclosing scope —
this is "capturing." Captured **local variables/parameters** must be **effectively final** (never
reassigned after initialization, even if not explicitly marked `final`) because the lambda body may
outlive the stack frame it was created in (e.g. stored and invoked later, or run on another thread) —
the JVM implements capture by copying the *value* of the local variable into the lambda instance at
creation time, not by referencing the original stack slot (which may no longer exist by the time the
lambda runs); if reassignment were allowed, the lambda's captured copy and the "live" variable would
silently diverge, an inherently confusing and bug-prone semantic the language simply disallows at
compile time instead.
*Follow-up: can a lambda capture and mutate an instance field of its enclosing class?* Yes —
instance (and static) fields aren't subject to the effectively-final restriction at all, since they
live on the heap (or in the class's static storage) independently of any particular stack frame, so
capturing `this` (implicitly, to reach an instance field) and mutating that field through it is
perfectly legal and a common source of shared-mutable-state bugs in concurrent lambda usage.

**[Advanced] How are lambdas actually implemented at the bytecode level — are they compiled to
anonymous inner classes?**
No — despite behaving similarly at the source level, lambdas are **not** compiled to anonymous inner
classes. The lambda body is compiled into a private synthetic method on the enclosing class, and the
actual functional interface instance is constructed at runtime via the `invokedynamic` bytecode
instruction, using a bootstrap method (`LambdaMetafactory`) that generates the implementing class
**dynamically at first invocation** (and caches it) rather than generating a separate `.class` file
for every lambda at compile time. This avoids anonymous-inner-class overhead (a distinct `.class`
file, and a `new` object allocation with a captured-variable-holding constructor, for every lambda
occurrence in source) and defers class generation until actually needed, which can also reduce
startup class-loading cost for lambdas that are never actually exercised on a given run.
*Follow-up: does this mean two calls to the same lambda expression share the exact same generated
class?* Yes, typically — the `invokedynamic` call site is linked once (lazily, on first execution)
and the generated implementation class is reused for all subsequent invocations from that same
lambda expression's call site; a *different* lambda expression elsewhere in the code gets its own
independently generated implementation.

---

## 2. Default & static interface methods

**[Basic] Why were default methods added to interfaces in Java 8, and what problem do they solve?**
Before Java 8, adding a new method to a widely-implemented interface would break **every** existing
implementation (a compile error for every class not providing that new method) — this made it
practically impossible to evolve core JDK interfaces like `Collection`/`List` (e.g. adding
`stream()`, `forEach()`, `removeIf()`) without breaking the entire ecosystem of third-party
implementations. **Default methods** let an interface provide a **default implementation body** for
a method, so existing implementors that don't override it simply inherit the default behavior
automatically, with no compile break — this is precisely how the JDK retrofitted Streams-era methods
onto pre-existing collection interfaces without breaking backward compatibility.
*Follow-up: can a default method be overridden?* Yes — implementing classes can override it just
like any other inherited method; the default only applies when the implementor doesn't provide its
own.

**[Intermediate] What happens when a class implements two interfaces that both declare a default
method with the identical signature (the "diamond problem" for default methods)?**
The Java compiler forces the implementing class to **explicitly override** that method — it will not
silently pick one interface's default over the other, because that would be an arbitrary, ambiguous
choice; leaving it unresolved is a **compile-time error** (unless one interface's default method is
declared to override the other's, e.g. via sub-interfacing, which the compiler can then use to
resolve unambiguously). Inside the override, you can still explicitly delegate to a specific
interface's version using `InterfaceName.super.methodName()`.
```java
interface A { default String greet() { return "A"; } }
interface B { default String greet() { return "B"; } }
class C implements A, B {
    public String greet() { return A.super.greet() + B.super.greet(); }  // must override; can delegate explicitly
}
```
*Follow-up: does a class's own superclass method win automatically over an interface default method
with the same signature, without needing an explicit override?* Yes — "class wins" is an unambiguous
JLS rule: a concrete method inherited from a superclass always takes priority over any interface
default method with the same signature, no diamond ambiguity or explicit override needed in that
specific case.

**[Intermediate] What are `static` interface methods for, and why not just put them in a separate
utility class instead?**
A `static` method on an interface belongs to the interface itself (not to any implementing instance,
and not inherited by implementing classes — must be called as `InterfaceName.method()`, never via an
instance reference) — used for interface-related factory/helper methods that logically belong with
the interface's contract (e.g. `Comparator.naturalOrder()`, `Comparator.comparing(...)`,
`List.of(...)`). Compared to a separate utility class (the pre-Java-8 pattern, e.g.
`Collections`/`Comparators`-style helper classes): keeping the factory/helper methods **on the
interface itself** groups clearly related API surface together (discoverable via the interface's own
Javadoc/autocomplete) instead of splitting a type's public contract across two separately-named
classes.
*Follow-up: can an interface's static method be overridden by an implementing class?* No — static
methods aren't inherited/polymorphic at all in this sense; an implementing class can declare its own
unrelated static method with the same name, but it neither overrides nor hides the interface's
static method in any meaningful sense (they're simply two separate static methods reached via
different qualifying names).

---

## 3. `var` (Java 10)

**[Basic] What does `var` actually do, and what is it explicitly NOT?**
`var` (Java 10+) enables **local variable type inference** — the compiler infers the variable's
static type from the initializer expression at the declaration site, and bakes that inferred type
into the compiled bytecode exactly as if you'd written it explicitly. It is emphatically **not**
dynamic typing — `var x = 5;` gives `x` the genuine, fixed compile-time type `int`, just like
`int x = 5;` would; you cannot later assign a `String` to it, and there is zero runtime type-checking
overhead added or removed. It's purely a source-level convenience restricted to **local variables**
with an initializer (also for-loop and try-with-resources variables) — it cannot be used for fields,
method parameters, or return types.
*Follow-up: why can't `var` be used for fields or method parameters?* Both are part of a class/method's
*public API contract* readable from outside the method body — inferring their types from usage would
undermine clear, stable API declarations and, for fields specifically, there's often no single
initializer expression to infer from at the declaration site at all (fields can be assigned in
constructors).

**[Intermediate] Give two concrete cases where `var` cannot be used at all, and explain the
underlying reason for each.**
`var x;` (no initializer) — illegal, because there's nothing for the compiler to infer the type
*from*; type inference requires an initializer expression present on the same statement.
`var x = null;` — illegal, because `null` carries no type information the compiler could infer a
concrete static type from (it's compatible with *any* reference type, so there's no single correct
inference). Also illegal: `var` for an array initializer with no explicit type, e.g.
`var arr = {1, 2, 3};` (this shorthand array syntax specifically requires an explicit target array
type to make sense of; `var arr = new int[]{1, 2, 3};` works fine, since that expression itself has
an unambiguous inferable type).
*Follow-up: can `var` be used for a lambda expression's variable, e.g. `var f = () -> "x";`?* No —
a lambda expression (or an unqualified method reference) has no type of its own in isolation; its
type is only known from the target functional-interface context, which `var` doesn't supply — the
compiler has nothing concrete to infer, so this is a compile error.

**[Advanced] Does using `var` change the compiled bytecode, runtime performance, or reflection
behavior compared to writing the explicit type?**
No, in essentially every respect that matters — `var` is a purely **compile-time, source-level**
feature; the compiler resolves the inferred type once during compilation and emits **exactly** the
same bytecode as if the explicit type had been written by hand (same local variable slot type,
same method calls, no boxing/unboxing differences introduced). Reflection over the compiled class
(inspecting local variable table debug info, if compiled with `-g`) would show the same inferred
concrete type, not some special "var" marker — there is no runtime artifact of `var` having been
used at all; it exists solely to reduce source-code verbosity for the human reader/writer.
*Follow-up: is there a legitimate criticism of overusing `var` despite this zero runtime cost?* Yes
— readability: `var result = process(data);` hides the type from a reader skimming the code (no IDE
open), unlike `ProcessResult result = process(data);`; most style guides recommend `var` only when
the type is already obvious from the right-hand side (e.g. `var list = new ArrayList<String>();`)
and avoiding it when the inferred type would otherwise be non-obvious from context.

---

## 4. Switch expressions (Java 14)

**[Basic] What's the difference between a classic `switch` statement and a Java 14 switch
**expression**, both in syntax and in the fall-through bug class it eliminates?**
A classic `switch` **statement** executes a matched case's code and, without an explicit `break`,
**falls through** into the next case's code — a long-standing, frequently-cited source of bugs from
a forgotten `break`. A switch **expression** (using the arrow `->` syntax) **produces a value**
directly (assignable to a variable, or returned), each arrow-case is scoped independently with no
fall-through risk at all, and the compiler performs **exhaustiveness checking** — for an `enum`
switch expression, every constant must be covered (or a `default` provided), or it's a compile error,
catching missed cases at compile time rather than as a silent runtime gap.
```java
// classic statement: fall-through risk if a break is forgotten
int day = 3; String name;
switch (day) {
    case 1: name = "Mon"; break;
    case 2: name = "Tue"; break;
    default: name = "?";
}
// switch expression: no fall-through possible, each arm independently scoped
String name2 = switch (day) {
    case 1 -> "Mon";
    case 2 -> "Tue";
    default -> "?";
};
```
*Follow-up: can a switch expression's arm execute multiple statements, not just a single expression?*
Yes — using a block body `case 1 -> { ...; yield "Mon"; }`, where `yield` (the switch-expression
analogue of `return`) supplies the produced value from within the block.

**[Intermediate] What is `yield` for, and how does it differ from `return` inside a switch
expression used inside a method?**
`yield` produces the value of a switch **expression**'s current case block, without exiting the
enclosing method — it's specific to switch expressions (and, since Java 21, guarded pattern-matching
switch case blocks). `return`, if used accidentally inside a switch expression's block arm, would
exit the **entire enclosing method** immediately, not just supply a value for that switch case — a
meaningful semantic difference the compiler enforces: a single-expression arrow arm (`case 1 -> "Mon"`)
implicitly yields that expression's value with no `yield` keyword needed at all, but a **block** arm
(`case 1 -> { ... }`) *must* use `yield` explicitly to produce the switch expression's value.
*Follow-up: is `yield` a reserved keyword everywhere in Java now?* No — it's a **contextual**
keyword, meaning it only has special meaning inside a switch expression/statement block; code
elsewhere using `yield` as an identifier (a variable or method name) continues to compile fine,
preserving backward source compatibility.

**[Advanced] Does an old-style colon-case (`case X:`) switch **expression** (not statement) still
fall through between cases?**
Yes — Java 14 kept the classic colon-case syntax usable in a switch *expression* too (not just the
new arrow syntax), and when you do, the **traditional fall-through semantics apply unchanged**
(you must still use `yield` on each reachable branch, and still need explicit `break`/fall-through
discipline) — mixing colon-case and arrow-case within the *same* switch block is not allowed at all
(a compile error), specifically to avoid a syntax where fall-through behavior would be ambiguous or
inconsistent within one switch. Exhaustiveness checking still applies to expression form regardless
of which case-syntax is chosen.
*Follow-up: so does the arrow syntax's "no fall-through" guarantee only actually apply when you use
arrow-case, not just because it's a switch expression?* Correct — "no fall-through" is a property of
the **arrow (`->`) case syntax specifically**, not of switch expressions in general; a colon-case
switch expression is still fall-through-prone exactly like a colon-case switch statement, it's simply
also required to produce a value via `yield`.

---

## 5. Text blocks (Java 15)

**[Basic] What problem do text blocks (`"""`) solve, and what's the core indentation rule?**
Text blocks eliminate the need for manual `\n` escapes and string-concatenation gymnastics for
multi-line string literals (JSON payloads, SQL queries, HTML fragments embedded in Java source) —
you write the content between triple-quote delimiters, largely as-is, across multiple lines. The
compiler determines and **strips a common minimum leading whitespace (indentation)** across all
non-blank lines (including the closing `"""` delimiter's own line, which is why the closing
delimiter's column position controls how much indentation survives) — this lets the text block's
source-code indentation match the surrounding code's nesting without polluting the actual string
content with unwanted leading whitespace.
```java
String json = """
    {
      "name": "Alice",
      "age": 30
    }
    """;
```
*Follow-up: does a text block automatically add a trailing newline?* Yes, if the closing `"""` is on
its own line following the content — the content ends with a newline before it, matching what you'd
visually expect; placing the closing delimiter immediately after the last character of content (same
line) omits that trailing newline.

**[Intermediate] How do you suppress an unwanted trailing newline, or force a specific trailing
space, in a text block?**
A backslash at the end of a line (`\` immediately before the line break) is a **line continuation**
— it suppresses the newline character that would otherwise be inserted at that point, letting a long
logical line be wrapped across multiple source lines without embedding a literal newline in the
resulting string. `\s` is an explicit **single-space** escape — useful specifically because trailing
whitespace at the end of a source line inside a text block is otherwise **automatically stripped** by
the compiler (to avoid invisible, hard-to-spot trailing whitespace differences), so `\s` is how you
deliberately preserve a trailing space that would otherwise be silently removed.
```java
String noBreak = """
    line one \
    still line one""";     // backslash suppresses the newline -> one continuous line
String trailingSpace = """
    padded  \s
    """;                   // \s preserves an intentional trailing space that plain whitespace would lose
```
*Follow-up: are the normal string escapes (`\n`, `\t`, `\"`, etc.) still usable inside a text
block?* Yes — all standard escape sequences remain valid inside a text block; `\"""` is specifically
how you'd embed a literal triple-quote sequence without it being misread as the closing delimiter
(though a lone `"` or even `""` needs no escaping at all, only three-in-a-row does).

---

## 6. Records (Java 16)

**[Basic] What does declaring a `record` automatically generate, and what is a record fundamentally
for?**
`record Point(int x, int y) {}` automatically generates: a canonical constructor accepting all
components in declaration order, `private final` fields for each component, public accessor methods
matching the component names exactly (`x()`, `y()` — deliberately **not** JavaBean-style
`getX()`/`getY()`), and correctly implemented `equals()`, `hashCode()` (based on **all** components),
and `toString()` (a readable `Point[x=1, y=2]`-style format) — all without writing any of that
boilerplate by hand. Records are specifically for modeling **immutable data carriers** ("plain data,
nothing more") — a `record`'s fields are implicitly `final`, and the class itself is implicitly
`final` (cannot be extended), because a record's entire contract is "I am exactly these values, and
nothing more."
*Follow-up: can a record have additional fields beyond its declared components?* Only **static**
fields — instance fields beyond the declared record components are explicitly disallowed, precisely
because a record's identity/equality/hashCode contract is defined to be exactly its component list;
allowing hidden extra instance state would break that guarantee silently.

**[Intermediate] What is a "compact constructor," and why would you write one instead of the default
canonical constructor?**
A compact constructor lets you add **validation or normalization logic** to a record's canonical
constructor without having to re-declare the full parameter list or manually assign each field — you
omit the parameter list and the explicit `this.field = field` assignments; the compiler still
performs the assignments automatically **after** your compact constructor body runs.
```java
record Range(int min, int max) {
    Range {                                    // compact constructor -- no parameter list repeated
        if (min > max) throw new IllegalArgumentException("min > max");
        // no explicit "this.min = min;" needed -- compiler still does it after this body
    }
}
```
*Follow-up: can a compact constructor reassign a component's value (e.g. to normalize it), not just
validate?* Yes — reassigning the parameter variable itself inside the compact constructor body (e.g.
`min = Math.min(min, max);`) changes what value the compiler's implicit field assignment afterward
actually stores — a common pattern for normalization (trimming strings, clamping ranges) alongside
validation.

**[Advanced] Can a record implement an interface, and can it override a generated accessor or
`equals`/`hashCode`/`toString`? What are the constraints?**
Yes — a record can implement any number of interfaces (just not extend another class, since it
implicitly extends the special `Record` class already, and Java has single class inheritance). You
**can** override any of the generated methods (`x()`, `equals()`, `hashCode()`, `toString()`)
explicitly, in which case your version replaces the compiler-generated one entirely — useful, for
instance, to add validation-free custom formatting to `toString()`, or (rarely, and usually a design
smell) to redefine equality on a subset of components. What you **cannot** do: add extra instance
fields (covered above), or make a record `abstract` or extend another class.
*Follow-up: if you override `equals()` on a record, are you still bound by the general
equals/hashCode contract (must also override `hashCode()` consistently)?* Yes — overriding one
without the other on a record breaks the same universal `equals`/`hashCode` contract it would for any
class; the compiler doesn't force you to keep them consistent if you override, so this is a real
footgun to watch for.

---

## 7. Sealed classes (Java 17)

**[Basic] What does `sealed` restrict, and what must every `permits`-listed subclass do?**
A `sealed` class or interface explicitly declares (via a `permits` clause, or implicitly if all
permitted subtypes are in the same source file) the **complete, closed set** of classes/interfaces
allowed to directly extend/implement it — no other class, anywhere, in any other file or module, can
extend it. Every class named in the `permits` list must itself declare exactly one of three
modifiers: `final` (no further subclassing allowed at all), `sealed` (further restricts its own
subtypes via its own `permits` list), or `non-sealed` (explicitly reopens unrestricted, ordinary
extensibility from that point in the hierarchy onward) — this exhaustive three-way choice is
mandatory, the compiler rejects a permitted subclass that doesn't pick one.
```java
sealed interface Shape permits Circle, Square, Triangle {}
final class Circle implements Shape { double radius; }
final class Square implements Shape { double side; }
non-sealed class Triangle implements Shape { /* can be further, freely subclassed */ }
```
*Follow-up: why would you choose `non-sealed` for one branch of an otherwise sealed hierarchy?* To
deliberately allow open extensibility for one specific subtype (e.g. a plugin/extension point) while
keeping the rest of the hierarchy closed and exhaustively known — a controlled escape hatch rather
than an all-or-nothing choice.

**[Intermediate] What's the actual interview-relevant benefit of sealed classes when combined with
switch pattern matching (Java 21)?**
Because the compiler knows the **complete, closed** set of possible subtypes at compile time, a
`switch` over a sealed type's subtypes can be verified **exhaustive** without needing a `default`
branch at all — if you later add a new permitted subtype to the sealed hierarchy, every switch
statement/expression matching over it elsewhere in the codebase that lacks a case for the new type
becomes a **compile error**, not a silent runtime gap (unlike an ordinary open class hierarchy, where
the compiler has no way to know or verify you've covered every possible subtype, so a `default` is
always required and a forgotten case is only ever caught at runtime, if at all).
```java
sealed interface Shape permits Circle, Square {}
double area(Shape s) {
    return switch (s) {
        case Circle c -> Math.PI * c.radius() * c.radius();
        case Square sq -> sq.side() * sq.side();
        // no default needed -- compiler proves these two cases are exhaustive for a sealed Shape
    };
}
```
*Follow-up: what happens to this switch's exhaustiveness if a new `Triangle` permitted subtype is
added to `Shape` later, but this switch isn't updated?* It becomes a **compile error** immediately
— "the switch expression does not cover all possible input values" — forcing the developer to
consciously handle the new case at every affected call site, rather than the code silently
compiling and failing (or worse, silently doing the wrong thing) at runtime.

**[Advanced] How do sealed classes and records compose to model an algebraic data type
(sum type) idiomatically in modern Java, and why is this a meaningfully new capability?**
A `sealed interface` defines the closed set of "variant" cases, and each permitted subtype is
typically a `record` (a pure, immutable data carrier for that variant's specific fields) — together
this reproduces the "sum type" / "tagged union" pattern common in functional languages (Kotlin's
`sealed class`, Scala's `sealed trait` + case classes, Rust's `enum`), which Java previously had no
clean, exhaustiveness-checked way to express (an ordinary open class hierarchy plus `instanceof`
chains gave neither closure nor compiler-verified exhaustiveness).
```java
sealed interface Result<T> permits Success, Failure {}
record Success<T>(T value) implements Result<T> {}
record Failure<T>(String error) implements Result<T> {}

String describe(Result<Integer> r) {
    return switch (r) {
        case Success<Integer> s -> "value: " + s.value();
        case Failure<Integer> f -> "error: " + f.error();
    };
}
```
*Follow-up: is this pattern a genuine, idiomatic replacement for checked exceptions in some designs?*
It's a legitimate, increasingly common alternative — a `Result<T>`-style sealed sum type makes
failure an explicit, exhaustively-checked part of a method's **return type** rather than a checked
exception that (as covered in `core-java.md`) composes poorly with lambdas/streams — trading Java's
traditional exception-based error signaling for a more functional-style, compiler-enforced
"handle every case" discipline, at the cost of it being a less conventional/familiar pattern to
teams used to exceptions.

---

## 8. Pattern matching for `instanceof` and `switch` (Java 17/21)

**[Basic] What does pattern matching for `instanceof` (Java 16, finalized) eliminate, syntactically?**
The redundant, error-prone two-step "check the type, then separately cast to it" pattern.
```java
// before:
if (obj instanceof String) {
    String s = (String) obj;   // redundant re-statement of the same type check
    System.out.println(s.length());
}
// pattern matching:
if (obj instanceof String s) {
    System.out.println(s.length());   // 's' is already bound and correctly typed, no cast needed
}
```
The compiler introduces the binding variable `s`, scoped (via flow analysis) to wherever the type
check is known to hold true — including, notably, **after** an `if` block when the `then` branch
always exits (`return`/`throw`/`continue`/`break`), and inside the right-hand side of `&&` where the
left side already established the type.
*Follow-up: does the pattern variable's scope extend into the `else` branch of a **negated** check?*
Yes — `if (!(obj instanceof String s)) { return; } System.out.println(s.length());` — since the `if`
body always returns when the pattern *doesn't* match, the compiler proves `s` must be bound (and the
right type) for all code reachable after the `if`, so it's in scope there too.

**[Intermediate] What are "record patterns" (Java 21), and how do they enable **deconstruction**
inside `instanceof`/`switch`?**
A record pattern matches an object's type **and simultaneously destructures its record
components** into individually-bound variables in one step, including **nested** record patterns for
records containing other records.
```java
record Point(int x, int y) {}
record Line(Point start, Point end) {}

if (obj instanceof Line(Point(int x1, int y1), Point(int x2, int y2))) {
    System.out.println("from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");
}
```
This single pattern both confirms `obj` is a `Line` *and* recursively confirms/destructures its two
`Point` components down to their individual `int` fields, all in one expression — replacing what
would otherwise require several separate accessor calls (`line.start().x()`, etc.) after a manual
type check.
*Follow-up: can you mix explicit types and `var` within a record pattern's nested bindings?* Yes —
`Line(Point(var x1, var y1), Point(var x2, var y2))` is equally valid, inferring each component's
type from the record's declared component type.

**[Intermediate] What are "guarded patterns" (`when` clause) in a switch, and why are they needed
in addition to plain type patterns?**
A `case Type binding when booleanCondition ->` lets a switch case match based on **both** a type
pattern *and* an additional runtime boolean condition on the bound variable — necessary because plain
type patterns alone can only discriminate by type/shape, not by a value predicate.
```java
static String classify(Object obj) {
    return switch (obj) {
        case Integer i when i < 0  -> "negative int";
        case Integer i when i >= 0 -> "non-negative int";
        case String s               -> "string: " + s;
        default                     -> "other";
    };
}
```
Note the compiler cannot statically prove the two guarded `Integer` cases are jointly exhaustive over
all `Integer` values (a `when` clause is an arbitrary boolean expression, not something the compiler
reasons about exhaustiveness-wise) — a switch containing any guarded pattern still generally requires
a `default` (or otherwise-provably-exhaustive unguarded case) to compile, unlike a purely
type-pattern, sealed-hierarchy switch.
*Follow-up: does case ordering matter for guarded patterns the way it does for a chain of
`if`/`else if`?* Yes — cases are still evaluated top-to-bottom, first match wins, exactly like a
sequential `if`/`else if` chain; an earlier, broader case can "shadow" a later, more specific one if
ordered incorrectly (the compiler does flag some provably-unreachable later cases as an error, but
not all such cases, depending on how expressible the unreachability proof is).

**[Advanced] What is `case null` in a switch (Java 21), and what changed about `NullPointerException`
behavior in a traditional switch on a boxed/reference type?**
Traditionally, a `switch` on a reference type (e.g. `Integer`, `String`) throws an immediate
`NullPointerException` if the switch's selector expression evaluates to `null` — before even
checking any case — a frequent, easy-to-forget crash source. Java 21's pattern-matching switch lets
you explicitly write `case null ->` to handle the null case **within** the switch itself, alongside
other cases, instead of it being an implicit crash; if a switch has type patterns but no explicit
`case null`, the old implicit-NPE-on-null behavior is preserved (for backward compatibility) —
`case null` must be written explicitly to opt into handling it. A common combined idiom: `case null,
default ->` groups the null case with the default case in one arm when they should be handled
identically.
```java
static String describe(Object obj) {
    return switch (obj) {
        case null      -> "it's null";
        case Integer i -> "int: " + i;
        default        -> "something else";
    };
}
```
*Follow-up: is `case null, default` different from writing two separate case labels, `case null ->`
and `default ->`, with identical bodies?* Functionally equivalent in outcome, but `case null,
default` is the idiomatic single-arm shorthand specifically provided for the very common "treat null
the same as everything else not otherwise matched" case, avoiding body duplication.

**[Basic] What common runtime exception did type patterns in `instanceof`/`switch` help reduce, and
how?**
Casting mistakes — historically, a chain of `if (obj instanceof Foo) { Foo f = (Foo) obj; ... }`
style checks had a real, if simple, failure mode: a copy-paste error casting to the **wrong** type
after checking a *different* type (`if (obj instanceof Foo) { Bar b = (Bar) obj; ... }`), which the
compiler cannot catch (the cast is a separate statement with no link back to the `instanceof`
check) and which throws `ClassCastException` at runtime. Pattern matching (`if (obj instanceof Foo
f)`) makes the checked type and the bound variable's type the **same expression**, structurally
eliminating the possibility of that specific copy-paste mismatch, since there's no separate cast
statement left to get wrong.
*Follow-up: does pattern matching eliminate `ClassCastException` from Java entirely?* No — explicit
casts elsewhere in code (not tied to a preceding `instanceof` check) can still throw it; pattern
matching only removes the *redundant, error-prone* re-statement of a type already just verified.

---

## 9. Sequenced collections (Java 21)

**[Basic] What problem does the `SequencedCollection`/`SequencedMap`/`SequencedSet` interface family
solve?**
Before Java 21, there was no **common interface** unifying "collections with a well-defined
encounter order and both ends" — `List` had `get(0)`/`get(size()-1)` for its ends, `LinkedHashSet`
and `TreeSet` had their own separate ways to reach first/last, and `Deque` had `getFirst`/`getLast`,
but none of these shared a common supertype expressing "this collection has a first and a last
element and can be reversed" uniformly. `SequencedCollection` (extended by `List`, `Deque`, and
retrofitted onto `LinkedHashSet`/`TreeSet` via `SequencedSet`, and `LinkedHashMap`/`TreeMap` via
`SequencedMap`) adds a uniform API: `getFirst()`, `getLast()`, `addFirst(e)`, `addLast(e)`,
`removeFirst()`, `removeLast()`, and `reversed()` (returning a **reversed view**, not a copy) —
usable polymorphically across any type implementing the interface, without needing to know the
concrete collection type's own specific first/last API.
*Follow-up: does calling `reversed()` copy the collection?* No — it returns a live, reversed
**view** backed by the original collection; mutations through either the original or the reversed
view are reflected in both, exactly like `List.subList`'s view semantics.

**[Intermediate] Why did retrofitting `SequencedMap` onto `LinkedHashMap` matter specifically for
the classic first-in-first-out / most-recently-used access patterns?**
Before Java 21, getting the first or last entry of a `LinkedHashMap` (e.g. for an LRU cache's
"peek at the least-recently-used entry" without removing it) required somewhat awkward workarounds
(iterating to the first `entrySet()` element, or maintaining separate bookkeeping) since `Map` itself
has no inherent concept of "first"/"last" at all (a plain `HashMap`'s entries have no defined order to
even ask that question of). `SequencedMap` gives `LinkedHashMap` (which already has a well-defined
insertion/access order internally) a direct, efficient, standard API — `firstEntry()`, `lastEntry()`,
`pollFirstEntry()`, `pollLastEntry()`, `putFirst(k,v)`, `putLast(k,v)` — for exactly these
order-dependent access patterns, without hand-rolled iterator gymnastics.
*Follow-up: does `TreeMap` (already `NavigableMap`) gain anything new from also implementing
`SequencedMap`?* Mostly interface **uniformity** rather than new capability — `NavigableMap` already
exposed `firstEntry()`/`lastEntry()`/etc. conceptually; `SequencedMap` lets code written generically
against the sequenced-collection abstraction work polymorphically across `TreeMap`, `LinkedHashMap`,
and others without depending on each one's own bespoke API surface.

---

## 10. Virtual threads (Java 21) recap

**[Basic] In one sentence, what problem do virtual threads solve, and where's the deep dive?**
They let you write simple, blocking, one-thread-per-task style code that scales to massive
concurrent-request counts (thousands to millions) without the memory/OS-thread cost traditional
platform threads impose at that scale, by having the JVM itself multiplex many lightweight virtual
threads onto a much smaller pool of OS carrier threads. Full mechanics — mounting/unmounting,
carrier threads, the "don't pool virtual threads" guidance, and thread pinning — are covered in
`notes/10-java-interview-questions/concurrency.md` (`## 8. Virtual threads`) and
`notes/09-multithreading-concurrency/11-virtual-threads.md`; this entry exists purely so this file's
Java-8-to-21 feature list is complete without duplicating that content.

---

## 11. Predict-the-output puzzles

**Puzzle 1 — `var` infers the STATIC (compile-time) type, not a dynamic one**
```java
var list = new ArrayList<Integer>();
list.add(1);
list.add(2);
// list = new LinkedList<Integer>();   // <-- would this line compile if uncommented?
System.out.println(list.getClass().getSimpleName());
```
**Output:** `ArrayList` — and the commented-out reassignment line, if uncommented, **would NOT
compile**.
**Why:** `var` infers `list`'s type as `ArrayList<Integer>` (the initializer's exact static type),
not the more general `List<Integer>` you might expect from habit — this is a real, commonly-cited
`var` gotcha: `var` infers from the **right-hand side's actual type**, not from any target/declared
interface type, so `list` here is statically typed as the concrete class `ArrayList<Integer>`,
and later trying to assign a `LinkedList<Integer>` to it is a compile error (`incompatible types`) —
whereas `List<Integer> list = new ArrayList<>();` would have allowed that reassignment, since `list`
would be statically typed as the more general `List` interface.

**Puzzle 2 — record `equals()` is component-based value equality, not identity**
```java
record Point(int x, int y) {}
Point p1 = new Point(1, 2);
Point p2 = new Point(1, 2);
System.out.println(p1 == p2);
System.out.println(p1.equals(p2));
System.out.println(p1.hashCode() == p2.hashCode());
```
**Output:** `false`, `true`, `true`.
**Why:** `p1` and `p2` are two distinct heap objects, so `==` (identity comparison) is `false` —
records don't get any special identity-sharing/pooling treatment. But the compiler-generated
`equals()` compares **all record components** for value equality (`x == x && y == y` in effect),
returning `true` since both have `x=1, y=2`; the generated `hashCode()` is likewise derived
deterministically from the same components, so two "equal" records always produce equal hash codes
too — correctly satisfying the `equals`/`hashCode` contract automatically, exactly the boilerplate a
hand-written class would need to get right manually.
