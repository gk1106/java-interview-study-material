# Folder structure and module order

Generate modules in THIS order. Each numbered folder under `notes/` = one module.

## 00-roadmap
- `study-plan.md` — 8-week plan (≈1.5–2 hrs/day): which modules per week,
  daily routine (read notes → run examples → solve exercises → revise interview Qs),
  weekend revision + mock-interview days.
- `how-to-use.md` — how to run examples, run exercise tests, check solutions.

## 01-java-foundations-for-dsa
1. Big-O: time & space complexity, amortized analysis
2. Generics (bounded types, wildcards, PECS)
3. `equals()` / `hashCode()` contract
4. `Comparable` vs `Comparator` (comparing, thenComparing, reversed, nullsFirst)
5. `Iterable` / `Iterator`, fail-fast vs fail-safe iterators
6. Arrays & `Arrays` / `Collections` utility classes

## 02-collections-framework-overview
1. Hierarchy diagram (Iterable → Collection → List/Set/Queue; Map separate)
2. Choosing the right collection (decision table)
3. Immutable collections (`List.of`, `Collections.unmodifiableList`, differences)

## 03-list
1. `List` interface & core operations
2. `ArrayList` internals (backing array, growth, `System.arraycopy`)
3. `LinkedList` internals (doubly linked nodes; also a Deque)
4. ArrayList vs LinkedList (benchmark example)
5. `Vector`, `Stack` (legacy) and why `ArrayDeque` is preferred
6. `CopyOnWriteArrayList`
7. `ListIterator`, `subList` gotchas, removing while iterating
8. DSA patterns on lists/arrays: two pointers, sliding window, prefix sum, in-place reversal
- Build it yourself: `MyArrayList<T>`, `MySinglyLinkedList<T>`

## 04-queue-deque
1. `Queue` interface (offer/poll/peek vs add/remove/element)
2. `Deque` & `ArrayDeque` internals (circular array)
3. `PriorityQueue` internals (binary heap, sift-up/down, custom comparator)
4. `BlockingQueue` family (ArrayBlockingQueue, LinkedBlockingQueue, PriorityBlockingQueue, DelayQueue, SynchronousQueue)
5. DSA patterns: stack using Deque (valid parentheses, next greater element),
   BFS with queue, monotonic deque (sliding window max), top-K with heap, merge K sorted lists
- Build it yourself: circular queue, min-heap, stack using two queues

## 05-set
1. `HashSet` (backed by HashMap), `LinkedHashSet`, `TreeSet` (NavigableSet: floor/ceiling/higher/lower)
2. `EnumSet`, concurrent sets
3. DSA patterns: duplicates, intersection/union, longest consecutive sequence

## 06-map
1. `HashMap` internals (buckets, hash spreading, load factor 0.75, resize, treeify at 8)
2. `LinkedHashMap` (access order) → LRU cache
3. `TreeMap` (Red-Black tree, NavigableMap)
4. `ConcurrentHashMap` (Java 8+ internals, compute/merge, why no null keys)
5. `WeakHashMap`, `IdentityHashMap`, `EnumMap` (brief)
6. DSA patterns: frequency counting, two-sum, group anagrams, subarray sum = K
- Build it yourself: `MyHashMap<K,V>`, LRU cache

## 07-dsa-problem-sets (mixed practice using collections)
- 30 problems graded Easy/Medium/Hard, each tagged with the pattern and the
  collection used. Include a pattern cheat sheet.

## 08-streams
1. Stream pipeline: source → intermediate → terminal, laziness
2. map / filter / flatMap / distinct / sorted / limit / skip / peek
3. reduce, collect, `Collectors` (toList, toMap with merge, groupingBy, partitioningBy, joining, counting, mapping, teeing)
4. Optional done right
5. Primitive streams (IntStream, boxed), `Stream.iterate/generate`
6. Parallel streams — when they help and when they hurt
7. Real-world exercises on an Employee/Order/Transaction dataset (banking-style: group transactions by account, top-N customers, etc.)

## 09-multithreading-concurrency
1. Thread lifecycle & states, `Thread` vs `Runnable` vs `Callable`
2. `synchronized`, intrinsic locks, `wait/notify`
3. `volatile`, Java Memory Model, happens-before
4. `ReentrantLock`, `ReadWriteLock`, `StampedLock`, `Condition`
5. Atomics & CAS (`AtomicInteger`, `LongAdder`)
6. Executor framework, thread pools (sizing, rejection policies), `Future`
7. `CompletableFuture` (thenApply/thenCompose/allOf/exceptionally)
8. Synchronizers: `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `Phaser`
9. Concurrent collections recap
10. Problems: deadlock (create + fix), race condition, producer–consumer (wait/notify AND BlockingQueue), print odd/even with two threads, thread-safe singleton
11. Virtual threads (Java 21) — overview + example
- Build it yourself: simple thread pool, bounded blocking queue

## 10-java-interview-questions
- `core-java.md` (OOP, String, immutability, exceptions, JVM memory, GC basics)
- `collections.md`, `streams.md`, `concurrency.md`, `java8-to-21-features.md`
- 150+ questions total, tagged [Basic]/[Intermediate]/[Advanced],
  each with model answer + follow-up. Include "output prediction" code puzzles.

## 11-spring-boot-interview-questions
- `core-spring.md` (IoC, DI, bean scopes, lifecycle, @Configuration vs @Component)
- `spring-boot.md` (auto-configuration, starters, profiles, properties, Actuator)
- `rest-and-validation.md`, `exception-handling.md`
- `data-jpa.md` (N+1, lazy/eager, transactions, @Transactional propagation/isolation pitfalls)
- `security.md` (filter chain, JWT, OAuth2 basics)
- `microservices.md` (service discovery, config, resilience, Kafka basics, API gateway)
- `testing.md` (@SpringBootTest, @WebMvcTest, @DataJpaTest, Mockito)
- Scenario-based questions ("your API is slow in prod, what do you check?")
- 120+ questions, same tagging and answer format

## 12-revision
- `one-page-cheatsheets/` — one page per module
- `mock-interviews.md` — 5 mock rounds (mixed questions, time limits)
- `last-week-checklist.md`
