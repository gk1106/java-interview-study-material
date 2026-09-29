# Cheat Sheet — 10: Java Interview Questions (150+ Q bank)

One-page pre-interview skim. Full notes: `notes/10-java-interview-questions/` (core-java.md,
collections.md, streams.md, concurrency.md, java8-to-21-features.md).

## Highest-value facts to have loaded

1. **Integer cache**: autoboxed `Integer` in `[-128, 127]` is cached/reused by `Integer.valueOf(int)` — `==` is `true` in that range, `false` outside it. Always use `.equals()` for boxed comparison.
2. **String pool**: literals are interned automatically; `"hel"+"lo"` (compile-time constant concat) resolves to the pooled `"hello"`; `new String("hello")` always creates a new heap object outside the pool.
3. `finally` with a `return` **overrides** a `return`/exception from the `try` block — control doesn't leave until `finally` finishes.
4. Static initializer blocks run **once per class**, at class initialization (first active use).
5. Overload resolution runs in strict phases (exact match → widening → autoboxing → varargs) and picks the **first phase** that finds a match — a common "predict the output" trap.
6. `var` infers the **static** (compile-time) type from the initializer, not a dynamic one — reassignment is still type-checked against that inferred type.
7. Records: `equals()`/`hashCode()`/`toString()` are component-based value equality, generated automatically; `==` is still identity.
8. JVM class loaders (parent-first delegation): Bootstrap → Platform/Extension → Application/System.
9. Object eligible for GC once unreachable from any GC root; minor GC = young gen, major/full GC = old gen (+ young).
10. Checked vs unchecked: checked exceptions must be declared/caught (compiler-enforced recoverable conditions); unchecked (`RuntimeException`) are programmer-error signals, not declared.
11. Pass-by-value always in Java — for object references, the *reference value* (the pointer) is copied, so mutating the referenced object is visible, but reassigning the parameter is not.
12. `HashMap` vs `Hashtable` vs `ConcurrentHashMap`: `Hashtable` = full-method sync (legacy); `HashMap` = no sync, allows one null key; `ConcurrentHashMap` = per-bin lock/CAS, no null keys/values.

## Question-bank coverage (topics per file)

- **core-java.md**: OOP pillars, why no multiple class inheritance, overloading vs overriding, abstract class vs interface, pass-by-value, `==` vs `.equals()`, `final`, static vs instance, composition over inheritance, deep vs shallow copy, String immutability/pool, StringBuilder vs StringBuffer, compact strings (Java 9), checked/unchecked exceptions, `Error` vs `Exception`, try-finally order, custom exception hierarchies, exception cost, JVM memory areas, PermGen→Metaspace, class loading phases/leaks, GC eligibility, minor/major GC, generational GC, modern collectors (Serial/Parallel/G1/ZGC trade-offs).
- **collections.md**: ArrayList vs LinkedList, `remove(int)` vs `remove(Object)`, `Arrays.asList` gotcha, ArrayList growth cost, `List.of` vs `Arrays.asList` vs `new ArrayList<>`, subList gotcha, HashSet/LinkedHashSet/TreeSet, mutable objects in a HashSet, set algebra via streams, EnumSet performance, HashMap.put() walkthrough, HashMap null-key rule, HashMap vs Hashtable vs ConcurrentHashMap, HashMap vs TreeMap vs LinkedHashMap, ConcurrentHashMap null prohibition, Queue method families, ArrayDeque vs Stack/LinkedList, PriorityQueue O(log n) guarantee, LinkedBlockingQueue vs ArrayBlockingQueue, WeakHashMap, CME + fail-fast/fail-safe, BlockingQueue vs Queue, Comparable vs Comparator, compareTo contract, collection-choice decision framework, when CopyOnWriteArrayList is a bad choice.
- **streams.md**: pipeline 3 parts, stream reuse, laziness with interleaving example, `peek()` misuse, map vs flatMap, `distinct()` basis, filter/sorted ordering effects, stateless vs stateful ops, `toList()` vs `toUnmodifiableList()`, groupingBy vs partitioningBy, toMap merge function, `Collectors.mapping`, `Collectors.teeing`, `Collectors.joining`, reduce's 3 overloads, 3-arg reduce combiner, reduce vs collect, primitive streams rationale, `iterate` vs `generate`, `.parallel()` on `Stream.iterate`, range vs rangeClosed, Optional purpose/misuse, `isPresent()`+`get()` anti-pattern, orElse vs orElseGet, `of` vs `ofNullable`, parallel stream mechanics/when it helps or hurts, forEach thread-safety.
- **concurrency.md**: thread-safety definition, race condition (`i++`), JMM's 3 properties (atomicity/visibility/ordering), benign vs harmful data races, synchronized vs ReentrantLock, finally-unlock discipline, ReadWriteLock, StampedLock optimistic reads, volatile guarantees, volatile-alone-insufficient-for-counters, JMM happens-before requirement, Coffman conditions, minimal 2-thread deadlock, production deadlock diagnosis, Executors factories + danger, CPU vs I/O pool sizing, 4 rejection policies, submit(Callable) exception swallowing, Future vs CompletableFuture, thenApply vs thenCompose, Async suffix meaning, waiting on several futures, exceptionally/handle/whenComplete differences, CountDownLatch vs CyclicBarrier, Semaphore vs lock, Phaser vs CyclicBarrier, virtual threads fundamentals, why never pool virtual threads, thread pinning.
- **java8-to-21-features.md**: functional interfaces, 4 method-reference forms, lambda capture rules, lambda bytecode (`invokedynamic`), default methods rationale, diamond-default-conflict resolution, static interface methods, `var` semantics + restrictions + bytecode impact, switch expressions vs statements (fall-through elimination), `yield`, text blocks + indentation rule, records (auto-generated members, compact constructors, interface implementation), sealed classes + `permits`, sealed+records = algebraic data types, pattern matching for `instanceof`/`switch`, record patterns (deconstruction), guarded patterns (`when`), `case null`, `SequencedCollection`/`SequencedMap` family, virtual threads recap.

## Output-prediction puzzles to drill (13 total — practice explaining the "why" out loud)

| # | File | Puzzle |
|---|---|---|
| 1 | collections.md | `ConcurrentModificationException` from direct `list.remove()` inside a for-each loop |
| 2 | collections.md | Mutable key silently "disappearing" from a `HashSet` after a field it hashes on changes |
| 3 | concurrency.md | Race condition on a shared non-atomic counter — output is nondeterministic, always < expected |
| 4 | concurrency.md | Deadlock that hangs forever, prints nothing |
| 5 | core-java.md | Integer caching (`-128..127`) — `==` true then false |
| 6 | core-java.md | String pool identity vs `new String(...)` — `true`, `false`, `false` |
| 7 | core-java.md | `finally` swallowing a `return`/exception from `try` |
| 8 | core-java.md | Static initialization order across classes |
| 9 | core-java.md | Overload resolution with autoboxing and varargs — picks `int` overload first |
| 10 | java8-to-21-features.md | `var` infers the static type, not dynamic — reassignment still type-checked |
| 11 | java8-to-21-features.md | Record `equals()` is component-based value equality, not identity |
| 12 | streams.md | Laziness — `peek()` on a stream with no terminal op never runs |
| 13 | streams.md | Short-circuiting — `findFirst()` doesn't process every element |

## Top pitfalls to name-drop

- Comparing boxed `Integer`s with `==` outside the cache range.
- `finally` overriding a `try`'s return value — a genuine footgun, not just trivia.
- Confusing fail-fast (`ArrayList`/`HashMap`, throws CME) with fail-safe/weakly-consistent (`CopyOnWriteArrayList`/`ConcurrentHashMap`, never throws, may miss recent writes).
- Assuming `ConcurrentModificationException` detection is reliable enough to depend on for correctness — it's best-effort only.
