# Core Java interview questions

How to use this file: each question is tagged `[Basic]` / `[Intermediate]` / `[Advanced]`, has a
short **spoken-interview-style** model answer (say this out loud, don't read it verbatim — it's a
script, not an essay), and a likely **follow-up** the interviewer will chase if your answer lands
well. Deeper internals live in the linked topic notes (`notes/01-...` through `notes/06-...`) —
this file is the rapid-fire drill, not the textbook.

Sections: [OOP pillars](#1-oop-pillars) · [Java language semantics](#2-java-language-semantics-pass-by-value-static-final-etc)
· [Strings](#3-strings-pool-immutability-stringbuilder) · [Exceptions](#4-exceptions)
· [JVM memory](#5-jvm-memory-areas) · [Class loading](#6-class-loading) · [Garbage collection](#7-garbage-collection-basics)
· [Predict-the-output puzzles](#8-predict-the-output-puzzles)

---

## 1. OOP pillars

**[Basic] What are the four pillars of OOP, and give a one-line Java example of each?**
Encapsulation — hiding internal state behind `private` fields and exposing behavior through
methods (a class validating a value in its setter). Abstraction — exposing *what* something does
without *how* (an interface like `List`, or an abstract class). Inheritance — a class reusing and
extending another's state/behavior via `extends`. Polymorphism — the same method call behaving
differently depending on the runtime type (`Shape.area()` overridden differently by `Circle` and
`Square`), resolved via dynamic dispatch.
*Follow-up: which of these does Java sacrifice by not supporting multiple inheritance of classes?*
Pure inheritance-based code reuse across two unrelated hierarchies — Java works around it with
interfaces (which since Java 8 can carry default method bodies) and composition.

**[Basic] Why does Java not support multiple inheritance of classes?**
To avoid the "diamond problem" — if class `B` and `C` both extend `A` and override a method
differently, and `D extends B, C`, the compiler can't unambiguously decide which version `D`
inherits. Java sidesteps this by allowing a class to extend only one superclass but implement
multiple interfaces; since Java 8, interfaces can have `default` methods, and Java resolves
default-method diamonds with an explicit rule: the most specific interface wins, and if it's
genuinely ambiguous, the implementing class **must** override the method itself — the compiler
refuses to guess.
*Follow-up: how would you resolve a diamond conflict between two default methods with the same
signature from two interfaces?* Override the method in the implementing class and, if needed, call
a specific interface's version explicitly with `InterfaceName.super.methodName()`.

**[Intermediate] Compile-time (overloading) vs runtime (overriding) polymorphism — how does the
JVM actually pick which method runs in each case?**
Overloading is resolved by the **compiler** at compile time, purely from the *static/declared*
types of the arguments and the method signature (a `javac`-time decision baked into the bytecode
as a specific method reference). Overriding is resolved by the **JVM at runtime** via **dynamic
dispatch**: instance method calls compile to the `invokevirtual` bytecode instruction, which looks
up the method in the actual runtime class's vtable (method table), not the reference's declared
type — that's what makes `Animal a = new Dog(); a.speak();` call `Dog.speak()`.
*Follow-up: does this dynamic dispatch happen for `private`, `static`, or `final` methods too?* No
— those (plus `invokespecial` for constructors/super calls) are resolved statically at compile
time because they can't be overridden (static methods are hidden, not overridden; `invokestatic`
and `invokespecial` don't consult the vtable).

**[Intermediate] What is the difference between method overloading and method hiding (static
methods)?**
Overriding applies to instance methods and is resolved dynamically by runtime type. If a subclass
declares a `static` method with the same signature as a superclass's `static` method, that's
**method hiding**, not overriding — which version runs is decided by the **reference's declared
(compile-time) type**, not the object's actual runtime type, because static methods belong to the
class, not an instance, and calls compile to `invokestatic`.
```java
class Animal { static String kind() { return "Animal"; } }
class Dog extends Animal { static String kind() { return "Dog"; } }
Animal a = new Dog();
System.out.println(a.kind()); // prints "Animal" -- resolved by declared type, not object type
```
*Follow-up: would `a.speak()` behave the same way if `speak()` were a non-static instance method
overridden in `Dog`?* No — it would print `Dog`'s version, because instance methods use dynamic
dispatch based on the actual object.

**[Intermediate] What's the difference between an abstract class and an interface, and when do you
pick one over the other (post-Java 8, when interfaces can have default methods)?**
An abstract class can hold instance state (non-static, non-final fields), constructors, and a mix
of abstract + concrete methods, but a class can extend only one. An interface (even with
`default`/`static` methods since Java 8, and `private` interface methods since Java 9) still can't
hold instance state — only `public static final` constants — but a class can implement many.
Practically: use an abstract class when subclasses share actual state and a common implementation
("is-a" with shared internals, e.g. a `Shape` base holding `color`); use an interface to describe a
capability/contract that unrelated classes can all opt into ("can-do", e.g. `Comparable`,
`Serializable`), especially across independent hierarchies.
*Follow-up: can an interface have a constructor?* No — interfaces are never instantiated directly,
so there's nothing for a constructor to initialize.

**[Advanced] What is the difference between `instanceof` pattern matching (Java 16+) and a
classic `instanceof` + cast, purely from a bytecode/behavior standpoint — and why did the JDK add
it?**
Classic `instanceof` only answers a boolean question; you then need a separate, redundant explicit
cast (`if (obj instanceof String) { String s = (String) obj; ... }`) that duplicates the type
check the JVM just performed. Pattern matching (`if (obj instanceof String s)`) folds the check and
the cast into one expression — the compiler introduces the binding `s`, scoped to where the type
check is known true (including via `&&` short-circuiting or a negated check with an early
return/`continue`), eliminating a redundant `checkcast` bytecode path in source and removing a
common source of `ClassCastException` typos. See `java8-to-21-features.md` for the full pattern
matching writeup (switch patterns, records, guards).
*Follow-up: does the pattern variable's scope extend past the `if` block?* Yes, via flow-sensitive
scoping — if the `if` has no `else` and the `then` branch always exits (`return`/`throw`/`continue`),
the compiler knows `s` is definitely assigned and in scope for code *after* the `if` too.

---

## 2. Java language semantics: pass-by-value, static, final, etc.

**[Basic] Is Java pass-by-value or pass-by-reference?**
Java is **always pass-by-value** — there is no pass-by-reference in the language, full stop. What
confuses people: for object types, the *value being copied* is the reference (the memory address /
handle) itself, not the object. So a method can mutate the object the reference points to (both
caller and callee's copies point at the same heap object), but it can never make the caller's
variable point at a *different* object — reassigning the parameter inside the method only rebinds
the local copy of the reference, invisible to the caller.
```java
void reassign(StringBuilder sb) { sb = new StringBuilder("new"); }  // caller's reference unaffected
void mutate(StringBuilder sb)   { sb.append("!"); }                  // caller sees the mutation
```
*Follow-up: so why does `swap(a, b)` never work in Java the way it does in C++ with pointers?*
Because `a` and `b` (whether primitives or object references) are always copied by value into the
method's parameters; reassigning the local copies inside `swap` never touches the caller's original
variables — there's no mechanism in the language to hand a method the caller's actual variable
slot.

**[Basic] What's the difference between `==` and `.equals()`?**
`==` compares primitives by value, and object references by **identity** — do both references
point at the exact same object in memory. `.equals()` is a method (from `Object`, overridable) that
by default also does identity comparison, but classes like `String`, wrapper types, and any class
that overrides it (per the `equals`/`hashCode` contract) redefine it to mean **logical/value
equality** instead. Always use `.equals()` (or `Objects.equals()` for null-safety) to compare
object *content*.
*Follow-up: what breaks if you override `equals()` but not `hashCode()`?* You violate the contract
that equal objects must have equal hash codes — the object becomes unreliable as a `HashMap`
key/`HashSet` element (two "equal" objects can land in different buckets and both be "found"
missing via `get`/`contains`). See `notes/01-java-foundations-for-dsa/03-equals-hashcode.md`.

**[Basic] What does the `final` keyword mean for a variable, a method, and a class?**
On a variable: it can be assigned exactly once (a "blank final" can be assigned later, once, e.g.
in every constructor path) — for a reference type this makes the *reference* unreassignable, not
the referenced object immutable (a `final List<String> list` can still have elements added). On a
method: it cannot be overridden by a subclass. On a class: it cannot be subclassed at all (e.g.
`String`, `Integer`).
*Follow-up: is a `final` field on an object thread-safe to read without synchronization once the
constructor finishes?* Yes, with a caveat — the JMM guarantees that once a constructor finishes
without leaking `this` during construction, other threads that later obtain a reference to the
object will see the correctly initialized value of its `final` fields, without extra
synchronization (this is the "safe publication via final fields" guarantee).

**[Intermediate] What's the difference between a `static` and an instance (non-static) member?**
A `static` member belongs to the **class** — one shared copy exists regardless of how many
instances (or zero instances) exist, allocated when the class is loaded/initialized. An instance
member belongs to each **object** — a separate copy per instance, allocated when that object is
constructed. `static` methods can't access instance members directly (no implicit `this`) and can't
be overridden (only hidden — see above).
*Follow-up: where do static fields live in memory (conceptually, post-Java 8)?* Since Java 8
removed PermGen, static fields (as part of a class's metadata) live in **Metaspace** (native
memory), while the actual referenced *objects* they point to live on the heap like any other
object.

**[Intermediate] Why is composition generally preferred over inheritance ("favor composition over
inheritance")?**
Inheritance couples a subclass tightly to its superclass's *implementation*, not just its contract
— a seemingly unrelated internal change in the superclass (even one that doesn't touch its public
API) can silently break subclasses that depended on old behavior (the "fragile base class"
problem), and a class can only extend one superclass, burning that slot. Composition (holding a
reference to another object and delegating to it) keeps the relationship to a stable, explicit
interface, lets you swap implementations at runtime, and doesn't consume your one inheritance slot.
Effective Java's guidance: use inheritance only for genuine "is-a" relationships designed and
documented for extension; default to composition otherwise.
*Follow-up: give a concrete real bug this causes.* Overriding a method that the superclass's own
constructor or other methods call internally — the subclass's override runs before the subclass's
own fields are initialized (constructors run superclass-first), or runs in a partially-initialized
state, producing subtle bugs (calling an overridable method from a constructor is itself a known
anti-pattern for exactly this reason).

**[Advanced] What is the difference between deep copy and shallow copy, and what does
`Object.clone()` actually give you by default?**
A shallow copy duplicates an object's own fields, but for reference-type fields, copies the
*reference* — both the original and copy end up pointing at the same nested mutable objects (so
mutating a nested object through the copy is visible through the original too). A deep copy
recursively duplicates the nested objects as well. `Object.clone()` (protected, requires
implementing the `Cloneable` marker interface or it throws `CloneNotSupportedException`) performs a
**shallow** field-by-field copy by default — you must override `clone()` yourself and manually
deep-copy any mutable reference fields if you need deep-copy semantics.
*Follow-up: why do many style guides (Effective Java included) recommend avoiding `Cloneable`/
`clone()` altogether?* It's a famously broken design — `Cloneable` is a marker interface with no
methods, the contract is enforced only by convention, checked exceptions leak through an API that
shouldn't need them, and final fields interact badly with the required no-arg-constructor-bypassing
mechanics. A copy constructor or a static factory (`copyOf`) is the modern recommended replacement.

---

## 3. Strings: pool, immutability, StringBuilder

**[Basic] Why is `String` immutable in Java, and what does "immutable" actually guarantee?**
Once constructed, a `String`'s internal character data can never change — every apparent
"mutation" (`concat`, `substring`, `replace`, `+`) returns a **new** `String` object, leaving the
original untouched. Reasons the JDK designers chose this: (1) **String pool caching/interning**
only works safely if pooled strings can never change under a caller that didn't request the
change; (2) **thread safety** — an immutable object can be freely shared across threads with zero
synchronization, since there's no mutation to race on; (3) **security** — strings are used
pervasively for things like class names, file paths, network hosts, DB connection info; if
`String` were mutable, code could pass a string to be validated/checked and then mutate it
afterward, bypassing the check (a classic TOCTOU-style exploit) — immutability makes the value a
trustworthy, unforgeable snapshot; (4) **safe `hashCode()` caching** — `String` caches its computed
hash code the first time it's needed (a `private int hash` field), which is only correct if the
underlying characters can never change afterward, making `String` an excellent, fast `HashMap` key.
*Follow-up: how is a String's immutability actually enforced internally (Java 9+)?* The backing
`byte[] value` field (a `char[]` before Java 9's compact strings) is `private final`, and no public
API exposes a way to mutate it in place — every string-transforming method allocates and returns a
new array/object.

**[Basic] What is the String pool (String intern pool), and how does `new String("x")` interact
with it?**
The string pool is a special region (part of the heap since Java 7, was PermGen before) holding one
canonical instance per distinct string literal value. String **literals** are automatically
interned by the compiler/JVM — two occurrences of `"hello"` anywhere in compiled code refer to the
exact same pooled object. `new String("hello")` explicitly forces allocation of a **new**, separate
`String` object on the heap (with its own identity) that happens to hold the same character data as
the pooled `"hello"` — it deliberately opts out of pool sharing.
```java
String a = "hello";
String b = "hello";
String c = new String("hello");
System.out.println(a == b);          // true  -- same pooled literal object
System.out.println(a == c);          // false -- c is a distinct heap object
System.out.println(a.equals(c));     // true  -- same character content
System.out.println(a == c.intern()); // true  -- intern() returns the pooled instance
```
*Follow-up: is it a good idea to call `.intern()` on every string you build to save memory?* Not
generally — interning has its own CPU/lookup cost and, for high-cardinality dynamic strings (e.g.
random IDs), pool growth can itself become a memory/GC concern; it's a targeted optimization for a
known, bounded set of frequently-repeated dynamic strings, not a default habit.

**[Intermediate] `StringBuilder` vs `StringBuffer` vs plain `String` concatenation in a loop —
when does each matter?**
`String` is immutable, so `s = s + x` in a loop allocates a brand-new `String` (and typically a new
`StringBuilder` under the hood, per `+` operation before Java 9's `invokedynamic`-based string
concatenation) on every iteration — O(n) allocations for n iterations, O(n²) total copying work in
the worst case. `StringBuilder` is a mutable, resizable character buffer (`char[]`/`byte[]` array
that grows like `ArrayList`, roughly doubling) — appends are amortized O(1), and it's **not
thread-safe**, which is fine and preferred for single-threaded/local use (the vast majority of
cases: building output, loop concatenation, etc.). `StringBuffer` is the same API but with every
method `synchronized`, making it thread-safe but slower under uncontended (i.e. almost all) use —
it predates `StringBuilder` (Java 1.0 vs Java 1.5) and is essentially legacy now; there is no
realistic reason to reach for it in new code over a properly externally-synchronized `StringBuilder`
or a different concurrency design entirely.
*Follow-up: does the compiler automatically use StringBuilder for you anywhere?* Yes — a chain of
`+` concatenations in a single expression (e.g. `"a" + b + "c"`) is compiled (Java 9+, via
`invokedynamic` and `StringConcatFactory`; pre-9, directly to `StringBuilder.append` calls) into
efficient buffer-building code automatically. The loop case above is different because each
iteration is a *separate* statement/expression, so naively the compiler builds a fresh builder each
time unless you hoist one yourself.

**[Intermediate] What is Java 9's "compact strings" optimization?**
Before Java 9, `String` stored characters in a `char[]` — 2 bytes per character unconditionally,
even for strings that are pure Latin-1 (ASCII-range) content, wasting half the memory for the
common case. Java 9 changed the backing field to a `byte[]` plus a `coder` flag: if every character
fits in Latin-1 (one byte), it's stored as 1 byte/char (`LATIN1` coder); if any character needs
UTF-16 (e.g. non-Latin scripts, emoji), it falls back to 2 bytes/char (`UTF16` coder) for the whole
string. This is transparent to all `String` APIs — pure internal memory optimization, roughly
halving heap usage for the (very common) case of ASCII-heavy strings.
*Follow-up: does this change String's public immutability or equality semantics?* No — entirely an
internal storage optimization; `.equals()`, `.hashCode()`, `.charAt()` etc. all behave identically
from the caller's point of view.

**[Advanced] Explain exactly what `String.intern()` does and where interned strings live, and how
that changed across JVM versions.**
`intern()` looks up the calling string's content in the JVM's string pool; if an equal entry
already exists, it returns that canonical pooled reference; otherwise, it adds this string (or an
equivalent copy) to the pool and returns it. Location has changed: pre-Java 7, the pool lived in
**PermGen** (a fixed, often-tight memory region — this was a classic `OutOfMemoryError: PermGen
space` source if code interned huge numbers of dynamic strings); Java 7+ moved the string pool
**into the main heap**, making it subject to normal (much larger, GC'able) heap sizing and garbage
collection like any other object — interned strings that are no longer referenced elsewhere can now
actually be collected, which wasn't reliably true under the old PermGen-based pool.
*Follow-up: can you size the string pool independently?* Yes — `-XX:StringTableSize=N` controls the
hash bucket count of the intern table (a tuning knob, rarely touched in practice).

---

## 4. Exceptions

**[Basic] Checked vs unchecked exceptions — what's the actual language-level difference, and why
does the distinction exist?**
Checked exceptions (`Exception` and its subclasses, excluding `RuntimeException` and its
subclasses) must be either caught or declared in a method's `throws` clause — the **compiler**
enforces this. Unchecked exceptions (`RuntimeException` and `Error` subclasses) require no such
declaration. The intent: checked exceptions model **recoverable, expected** failure conditions a
caller should be forced to consciously handle (e.g. `IOException` — a file might genuinely not
exist, that's a normal possibility); unchecked exceptions model **programming errors** or
unrecoverable conditions (`NullPointerException`, `IllegalArgumentException`,
`ArrayIndexOutOfBoundsException`) that shouldn't clutter every method signature up the call stack,
because in principle *any* method call could throw one.
*Follow-up: is this a hard rule or a design choice you'd criticize?* It's widely criticized in
practice — checked exceptions don't compose well with lambdas/functional interfaces (a
`Function<T,R>` can't declare a checked throw), encourage catch-and-swallow anti-patterns to satisfy
the compiler, and most modern JVM languages (Kotlin, Scala) dropped the distinction entirely;
`Optional`/`Either`-style return values or unchecked exceptions with clear documentation are common
alternatives in modern Java code.

**[Basic] What is `Error` vs `Exception`, and should you ever catch an `Error`?**
Both extend `Throwable`. `Exception` represents conditions an application might reasonably want to
catch and recover from. `Error` represents serious problems typically outside the application's
control — `OutOfMemoryError`, `StackOverflowError`, `NoClassDefFoundError` — conditions from which
recovery is usually not meaningful or safe (the JVM itself may be in a corrupted state). You
generally should **not** catch `Error` broadly (never `catch (Throwable)` in normal business logic)
— let it propagate and crash/restart the process; catching it and continuing as if nothing happened
is far more dangerous than letting it fail fast.
*Follow-up: is `Error` checked or unchecked?* Unchecked — `Error` does not extend `Exception`, but
like `RuntimeException` it's exempt from the compiler's mandatory catch-or-declare rule.

**[Basic] Explain `try`-`catch`-`finally` execution order, and what `try`-with-resources adds.**
`finally` always runs — whether the `try` completes normally, throws (caught or not), or even
returns/breaks/continues out of the block — with the sole exception of `System.exit()` being
called, or the JVM process dying (crash, `kill -9`). `try`-with-resources (Java 7+) automatically
calls `.close()` on any resource implementing `AutoCloseable`, declared in the `try(...)` parens, in
**reverse declaration order**, after the try block (and any catch) completes, replacing manual
`finally { resource.close(); }` boilerplate and correctly suppressing (not losing) an exception from
`close()` if the try block itself already threw.
```java
try (var a = new ResourceA(); var b = new ResourceB()) {
    // use a, b
} // b.close() runs first, then a.close() -- reverse of declaration order
```
*Follow-up: what happens if both the try block AND close() throw?* The try block's exception is the
one propagated to the caller; `close()`'s exception is attached to it as a **suppressed exception**
(retrievable via `getSuppressed()`), not silently lost and not replacing the original — this is a
deliberate improvement over manual `finally`-based cleanup, where a `close()` exception in `finally`
used to silently *replace* the original exception (see the puzzle below).

**[Intermediate] What happens if a `finally` block itself contains a `return` statement?**
It **swallows** any exception or return value from the `try`/`catch` block — the `finally`
block's `return` (or `throw`) always wins, unconditionally overriding whatever the try/catch was
about to do. This is almost always a bug when it happens accidentally (see the puzzle below) — it's
one of the most cited reasons to avoid putting control-flow statements (`return`, `break`,
`continue`, `throw`) inside `finally` at all.
*Follow-up: does the same swallowing happen for an exception thrown in `finally` (not just
`return`)?* Yes — a new exception thrown in `finally` similarly replaces/discards any exception that
was propagating from the try/catch block, unless you're using `addSuppressed()` yourself or relying
on try-with-resources, which handles this correctly automatically.

**[Intermediate] How do you design a good custom (checked) exception hierarchy for, say, a payment
processing module?**
Extend `Exception` (checked) if callers genuinely need to be forced to handle it (e.g.
`InsufficientFundsException`, `PaymentDeclinedException` — real, expected business outcomes), or
`RuntimeException` if it represents a programming/contract violation the caller shouldn't need to
declare everywhere (`InvalidAccountStateException` from a bug). Provide constructors that accept
and forward a `cause` (`super(message, cause)`) so the original stack trace/root exception is never
lost when wrapping a lower-level exception (e.g. wrapping a `SQLException` in a domain-specific
`PaymentException`) — swallowing the cause is a common, debugging-crippling mistake. Keep the
hierarchy shallow and meaningful (a small set of specific types callers can `catch` individually,
rather than one giant generic exception with an error-code field).
*Follow-up: why is preserving the original cause important beyond just debugging?* Exception
chaining preserves the full causal stack trace across layers (service → repository → JDBC driver),
which is often the only way to diagnose a production incident after the fact — losing it means
losing the "why," not just the "what."

**[Advanced] What's the performance cost of exceptions in Java, and why are they expensive to
*throw* but cheap to declare/catch when nothing is thrown?**
The cost is dominated by **stack trace capture** — when a `Throwable` is constructed, the JVM walks
and records the entire current call stack (`fillInStackTrace()`), which is genuinely expensive
relative to normal method calls, especially in deep call chains or hot loops. This is why using
exceptions for **ordinary control flow** (e.g. throwing to break out of a loop instead of using
`break`, or using exceptions to signal "not found" in a hot path instead of returning `null`/
`Optional.empty()`) is a well-known anti-pattern and performance trap. A `try` block with no
exception actually thrown has near-zero overhead in modern JVMs (no cost to "entering" a try block
in the common case) — the cost is specifically in construction/throwing, not in the surrounding
try/catch machinery itself.
*Follow-up: is there a way to create an exception without the stack trace cost?* Yes — override
`fillInStackTrace()` to a no-op in a custom exception subclass (or use the protected 4-arg
`Throwable` constructor with `writableStackTrace=false`, Java 7+) when you're using exceptions for
frequent, expected, non-debugging signaling and don't need a trace — a known technique for very
hot-path "exceptional but frequent" cases, though most style guides recommend just not using
exceptions for control flow in the first place.

---

## 5. JVM memory areas

**[Basic] What are the main runtime memory areas of the JVM?**
**Heap** — shared across all threads, holds all objects and arrays (further divided into Young
Generation — Eden + two Survivor spaces — and Old/Tenured Generation; see GC section). **Stack** —
one per thread, holds stack frames (local variables, method parameters, partial results, the
operand stack, return addresses) per method call; a primitive local variable and object
*references* live here, the actual objects they point to live on the heap. **Metaspace** (replaced
PermGen in Java 8) — native (off-heap) memory holding class metadata: class structure, method
bytecode, runtime constant pool, static fields. **PC (Program Counter) Register** — per-thread,
tracks the currently executing bytecode instruction. **Native method stacks** — support for
native (JNI) code.
*Follow-up: which of these can throw `OutOfMemoryError`, and with what distinguishing message?*
Heap exhaustion → `OutOfMemoryError: Java heap space`; Metaspace exhaustion → `OutOfMemoryError:
Metaspace`; a thread stack growing too deep (e.g. infinite/too-deep recursion) →
`StackOverflowError` (not `OutOfMemoryError`, and note it's an `Error` not caused by heap
exhaustion at all); too many native threads created → `OutOfMemoryError: unable to create new
native thread`.

**[Basic] Where do local variables live, and where do objects live?**
Local variables (primitives, and object *references*) live on the **stack**, in the current
method's stack frame — they're automatically reclaimed the instant the method returns, no GC
involved. The actual object *instances* those references point to (via `new`) live on the **heap**
and are only reclaimed by the garbage collector once nothing reachable still references them.
*Follow-up: does that mean a primitive local variable is never garbage collected?* Correct — a
`int x = 5;` local variable is popped off the stack frame when the method returns; there's nothing
for the GC to do with it, it was never a heap object.

**[Intermediate] Why did Java 8 remove PermGen and replace it with Metaspace, and what's the
practical difference?**
PermGen had a **fixed maximum size** set at JVM startup (`-XX:MaxPermSize`, defaulting to a
relatively small value), shared as part of the managed heap's GC scope; applications with many
dynamically generated/loaded classes (common with app servers doing hot-redeploys, heavy reflection/
proxy generation frameworks, OSGi) would routinely hit `OutOfMemoryError: PermGen space` even when
plenty of heap was free, because class metadata had nowhere else to grow. Metaspace moved this
metadata to **native (off-heap) memory**, which by default grows dynamically (limited only by
available system memory, though it can still be capped via `-XX:MaxMetaspaceSize`), largely
eliminating that specific class-of-failure and decoupling class metadata's memory management from
the object heap's GC cycles.
*Follow-up: are interned strings and static variables also in Metaspace now?* Static fields (as
part of class metadata) are represented in Metaspace, but the actual *objects* they reference
(including the string pool, moved to the heap back in Java 7) live on the heap, not in Metaspace —
Metaspace holds class structure/metadata, not object instances.

**[Intermediate] What's the difference between the heap and the stack in terms of thread-safety and
sizing?**
The heap is **shared** by all threads in the JVM — any thread can reference any heap object, which
is exactly why heap-shared mutable state needs explicit synchronization for thread safety. Each
thread gets its **own** private stack, so a thread's local variables/parameters are inherently
thread-safe (never visible to or shared with another thread), which is a large part of why "keep
state as local variables where possible" is good concurrent-code hygiene. Sizing: heap size is
controlled by `-Xms` (initial)/`-Xmx` (max) JVM flags and is comparatively large (hundreds of MB to
many GB); each thread's stack size is controlled by `-Xss` and is comparatively small (default often
512KB-1MB per thread on most platforms) — this is exactly why creating too many platform threads is
expensive (topic in `09-multithreading-concurrency`) and why deep/infinite recursion throws
`StackOverflowError` well before it would ever threaten heap space.
*Follow-up: can two threads share the same stack frame?* No, never — stack frames are strictly
per-thread and per-call; there is no mechanism to share one.

**[Advanced] Walk through the JVM memory layout for this snippet, conceptually — what lives where?**
```java
class Point { int x, y; }
void method() {
    int a = 10;                 // primitive local -> on the current thread's STACK frame
    Point p = new Point();      // 'p' (the reference) -> STACK; the Point OBJECT itself -> HEAP
    p.x = 5;                    // mutates a field on the HEAP object p points to
}
```
`a` and the reference variable `p` both live in `method()`'s stack frame, popped automatically when
`method()` returns. The `Point` object created by `new Point()` is allocated on the heap (in Eden,
initially — see GC section) and stays there until nothing reachable references it, at which point
it becomes eligible for garbage collection — entirely independent of when `method()` returns; if
`p` had been stored somewhere reachable beyond the method (a field, a returned value, a collection),
the object would outlive the stack frame that created it.
*Follow-up: what determines whether this particular Point object survives Young GC and gets
promoted to Old Gen?* Whether it's still reachable by the time a Young GC runs, and how many prior
Young GC cycles it has survived (tracked via an object age counter) relative to the JVM's tenuring
threshold — see the generational GC question below.

---

## 6. Class loading

**[Basic] What are the three built-in class loaders and their (parent-first) delegation order?**
**Bootstrap** class loader (native, loads core JDK classes like `java.lang.*` from the JDK's own
modules — no Java-level `ClassLoader` object represents it, it appears as `null` from Java code) →
**Platform/Extension** class loader (loads JDK platform modules) → **Application/System** class
loader (loads your application's classpath — your own compiled classes and third-party JARs). The
default delegation model is **parent-first**: before a class loader tries to load a class itself, it
asks its parent to try first, only falling back to load it itself if every ancestor failed —
guaranteeing, among other things, that application code can never shadow/replace a core class like
`java.lang.String`.
*Follow-up: why is parent-first delegation a security feature?* It prevents a malicious or
accidental application-level class named e.g. `java.lang.String` from ever being loaded in place of
the real JDK class — the bootstrap loader always gets first refusal for `java.lang.*`, and the
delegation chain never lets a "closer" loader override a class an ancestor already successfully
loaded.

**[Intermediate] What are the three phases of class loading?**
**Loading** — find the class's bytecode (from the classpath, a JAR, network, etc.) and construct its
in-memory `Class` object / metadata (in Metaspace). **Linking**, itself three sub-steps: *verification*
(bytecode is checked for structural/type-safety correctness — this is what stops hand-crafted or
corrupted `.class` files from crashing/exploiting the JVM), *preparation* (static fields are
allocated and set to their default zero-like values — `0`, `null`, `false` — not yet their real
initializers), *resolution* (symbolic references to other classes/methods/fields are resolved to
direct references — can happen lazily). **Initialization** — static initializers and static field
initializers actually run, top to bottom in source order, exactly once, triggered by the class's
**first active use** (first instantiation, first static method/field access, or a subclass being
initialized — not simply by being referenced in a `import` or a variable *declaration* of that
type).
*Follow-up: what's a case where a class is loaded but deliberately NOT yet initialized?* Referencing
a `static final` compile-time constant (e.g. `int x = SomeClass.CONSTANT;` where `CONSTANT` is a
`static final int` with a literal value) is inlined by the compiler at the call site and does not
force `SomeClass` to initialize at all — the constant's value is baked directly into the calling
class's bytecode.

**[Advanced] What is a classloader-related memory leak, and why do they classically happen in app
servers running many redeploys?**
If a class loaded by a *custom* class loader (e.g. a web app's own loader in a servlet container)
is still reachable — even indirectly, e.g. through a thread that's still running, a
`ThreadLocal` that was never cleaned up, a static reference held by a JDK class, or a JDBC driver
registered in `DriverManager` — then that entire class loader (and every class *it* loaded, and
every instance of those classes) cannot be garbage collected, even after the application has been
"undeployed" and redeployed. Repeated redeploys under this condition leak an entire generation of
classes/class loaders each time, exhausting Metaspace over time (`OutOfMemoryError: Metaspace`)
even though the "old" application is supposedly gone.
*Follow-up: name one concrete, commonly-cited cause.* A background thread started by the old
application (e.g. a connection pool's cleanup thread) that was never explicitly stopped on
undeploy — the thread object's call stack keeps the old classloader (and everything it loaded)
reachable indefinitely.

---

## 7. Garbage collection basics

**[Basic] When does an object become eligible for garbage collection?**
The instant it becomes **unreachable** — no live thread can reach it anymore by following any chain
of references starting from a **GC root** (local variables on any thread's stack, active static
fields, JNI references, etc.). Note this is about reachability, not about reference count reaching
zero, and not about scope exiting per se — an object can go out of a method's local scope yet still
be reachable (and thus NOT eligible) if it was stored into a field, a collection, or returned and
held elsewhere; conversely, two objects can reference *each other* (a cycle) yet both be eligible if
nothing outside the cycle reaches either of them — Java's tracing GC handles cycles correctly
(unlike naive reference counting, which would leak them).
*Follow-up: does setting a local variable to `null` force immediate collection?* No — it can make
the object eligible *sooner* (removes one path of reachability, which can matter for a long-lived
method holding a large object it's done with), but actual collection still happens whenever the GC
next decides to run a cycle that reclaims it; there's no way to force immediate collection of a
specific object (`System.gc()` is only a *hint*, not a guarantee).

**[Basic] What's the difference between a minor GC and a major/full GC?**
A **minor (Young Gen) GC** collects only the Young Generation (Eden + Survivor spaces) — fast and
frequent, because most objects die young ("weak generational hypothesis": the vast majority of
allocated objects become garbage almost immediately, e.g. loop-local temporaries, so collecting
just this small, fast-changing region is cheap and pays off often). A **major/full GC** collects the
Old Generation (and, depending on the collector, usually the Young Generation and Metaspace too) —
much more expensive because the Old Gen is larger and holds longer-lived objects, and (for
stop-the-world collectors) causes a noticeably longer application pause. A well-tuned application
should see frequent, cheap minor GCs and rare major GCs; frequent major GCs (or one right after
another) usually signal a memory pressure/leak problem worth investigating.
*Follow-up: what's a "stop-the-world" pause?* A GC phase during which **all** application threads
are suspended so the collector can safely trace/move objects without the heap changing underneath
it — minimizing stop-the-world duration (or eliminating it for most phases, as modern collectors
like G1/ZGC/Shenandoah do) is the central design goal of modern GC algorithms.

**[Intermediate] Explain generational garbage collection and object promotion, end to end.**
New objects are allocated in **Eden** (part of the Young Generation). When Eden fills, a minor GC
runs: live objects are copied out of Eden into one of two **Survivor spaces** (S0/S1 — only one is
"active"/empty at a time, the copying collector alternates between them each cycle), and dead
objects in Eden are simply not copied (reclaimed implicitly — this is why minor GC is cheap: it's
proportional to *live* objects, not total heap size). Each surviving object's **age** counter
increments on every minor GC it survives; once an object's age crosses the JVM's **tenuring
threshold** (`-XX:MaxTenuringThreshold`, adaptively tuned by default), it's **promoted** to the
**Old Generation** instead of another survivor space, on the theory that an object that's survived
several youth-generation cycles is likely to be long-lived, so it's not worth the copying cost of
keeping it in the fast-churning young generation any longer.
*Follow-up: what happens if an object is too large to fit in Eden at all?* Very large objects can
be allocated directly into the Old Generation ("humongous allocation" in G1's terminology),
bypassing the young generation entirely — this avoids repeatedly copying a huge object through
survivor spaces, but too many such large allocations can pressure the Old Gen and trigger more
frequent major GCs.

**[Advanced] Name a few modern JVM garbage collectors and the trade-off each makes.**
**Serial GC** — single-threaded, stop-the-world for both young and old; simplest, lowest overhead
for small heaps/single-core environments, but pauses scale with heap size — fine for small
scripts/tools, wrong for a server. **Parallel GC** (historically the Java 8 default) —
multi-threaded collection, still stop-the-world, optimized for maximum **throughput** (total
application work done per unit time), accepting longer individual pauses in exchange. **G1
(Garbage-First)** — the default since Java 9 — divides the heap into many fixed-size regions and
prioritizes collecting the regions with the most garbage first, targeting a **configurable maximum
pause time goal** (`-XX:MaxGCPauseMillis`) rather than maximum throughput; a good general-purpose
default balancing latency and throughput for mid-to-large heaps. **ZGC** and **Shenandoah** —
low-latency, mostly-concurrent collectors designed for very large heaps with **sub-millisecond
pause targets**, doing almost all tracing/compaction work concurrently with running application
threads (using techniques like colored pointers/load barriers), at the cost of somewhat higher CPU
overhead — the right choice when tail-latency (p99 pause time) matters more than raw throughput,
e.g. large in-memory caches or latency-sensitive trading/serving systems.
*Follow-up: how would you pick between G1 and ZGC for a typical Spring Boot microservice?* G1 is
usually the right default — good balance, well understood, minimal tuning; reach for ZGC/Shenandoah
specifically when you have a large heap (many GB+) and have measured G1 pause times violating a
strict latency SLA, not preemptively.

---

## 8. Predict-the-output puzzles

**Puzzle 1 — Integer caching (`-128..127`)**
```java
Integer a = 100;
Integer b = 100;
Integer c = 200;
Integer d = 200;
System.out.println(a == b);
System.out.println(c == d);
```
**Output:** `true` then `false`.
**Why:** Autoboxing `int` → `Integer` for literals goes through `Integer.valueOf(int)`, which
**caches** and reuses `Integer` instances for values in the range **-128 to 127** (`IntegerCache`,
a JLS-mandated minimum range the JDK implements exactly as -128..127 by default, tunable up via
`-XX:AutoBoxCacheMax`). `100` falls in the cached range, so `a` and `b` are literally the same
pooled object → `==` is `true`. `200` is outside the cached range, so each autoboxing allocates a
**new** `Integer` object → `==` is `false` even though the values are equal. This is exactly why
you must use `.equals()` (or unbox to `int` for comparison) with wrapper types, never `==`.

**Puzzle 2 — String pool identity vs `new String(...)`**
```java
String a = "hello";
String b = "hel" + "lo";              // compile-time constant expression
String c = new String("hello");
String suffix = "lo";
String d = "hel" + suffix;            // NOT a compile-time constant (variable involved)
System.out.println(a == b);
System.out.println(a == c);
System.out.println(a == d);
```
**Output:** `true`, `false`, `false`.
**Why:** `"hel" + "lo"` is concatenation of two **compile-time constant literals** — the compiler
folds it into a single constant `"hello"` at compile time and interns it, so `b` refers to the
same pooled object as `a`. `new String("hello")` explicitly creates a distinct heap object outside
the pool. `"hel" + suffix` involves a variable, so it **cannot** be folded at compile time — it's
evaluated at runtime via `StringBuilder`, producing a new, non-interned `String` object even though
its content equals `"hello"`.

**Puzzle 3 — `finally` swallowing a return value / exception**
```java
static int test() {
    try {
        return 1;
    } finally {
        return 2;
    }
}
System.out.println(test());
```
**Output:** `2`.
**Why:** The `try` block's `return 1` is *about* to return, but before control actually leaves the
method, `finally` runs — and since `finally` itself contains a `return`, it unconditionally
overrides the pending return value (and would equally override a pending *exception*). This is a
correctness trap real code has hit: never put `return`/`break`/`continue`/`throw` inside a
`finally` block.

**Puzzle 4 — static initialization order**
```java
class Parent {
    static { System.out.println("Parent static block"); }
    Parent() { System.out.println("Parent constructor"); }
}
class Child extends Parent {
    static { System.out.println("Child static block"); }
    Child() { System.out.println("Child constructor"); }
}
new Child();
new Child();
```
**Output:**
```
Parent static block
Child static block
Parent constructor
Child constructor
Parent constructor
Child constructor
```
**Why:** Static initializer blocks run **once per class**, at class initialization (triggered here
by the first active use — the first `new Child()`), in superclass-before-subclass order, regardless
of how many instances are later created. Instance constructors run **every time**, also always
superclass-constructor-first (an implicit `super()` call is the first statement of any constructor
that doesn't explicitly call another constructor). The second `new Child()` triggers neither static
block again — only the two constructors.

**Puzzle 5 — overload resolution with autoboxing and varargs**
```java
static void call(int x)         { System.out.println("int"); }
static void call(long x)        { System.out.println("long"); }
static void call(Integer x)     { System.out.println("Integer"); }
static void call(Object... x)   { System.out.println("varargs"); }
call(5);
```
**Output:** `int`.
**Why:** Java's overload resolution runs in strict phases, and picks the **first** phase that finds
a match: (1) exact match / widening **primitive** conversion only (no boxing, no varargs) — `int x
= 5` matches `call(int)` directly via no conversion at all, so this phase wins immediately and
resolution stops. `call(long)` would only be chosen if `call(int)` didn't exist (widening
`int`→`long`). `call(Integer)` (requires autoboxing) and `call(Object...)` (requires varargs
packing) are strictly lower-priority phases, only tried if phase 1 finds nothing — so they're never
even considered here. This ordering (exact/widening > boxing > varargs) is a frequently-asked
"gotcha" because many developers assume the *most specific* overload always wins regardless of
category.
