# Progress

Legend: `[ ]` not started · `[~]` in progress · `[x]` done

Mode: **GENERATE ALL** — running every module 00→12 back to back without
waiting for confirmation, in parallel batches of 3 via subagents.

## Module status

- [x] 00-roadmap — study-plan.md, how-to-use.md
- [x] 01-java-foundations-for-dsa — Big-O, Generics, equals/hashCode, Comparable/Comparator, Iterable/Iterator, Arrays/Collections utils
- [x] 02-collections-framework-overview — hierarchy, choosing a collection, immutable collections
- [x] 03-list — List, ArrayList, LinkedList, ArrayList vs LinkedList, Vector/Stack/ArrayDeque, CopyOnWriteArrayList, ListIterator/subList, DSA patterns, MyArrayList/MySinglyLinkedList
- [x] 04-queue-deque — Queue, Deque/ArrayDeque, PriorityQueue, BlockingQueue family, DSA patterns, build-it-yourself
- [x] 05-set — HashSet/LinkedHashSet/TreeSet, EnumSet/concurrent sets, DSA patterns
- [x] 06-map — HashMap, LinkedHashMap+LRU, TreeMap, ConcurrentHashMap, WeakHashMap/IdentityHashMap/EnumMap, DSA patterns, MyHashMap/LRU
- [ ] 07-dsa-problem-sets — 30 mixed problems (Easy/Medium/Hard) + pattern cheat sheet
- [ ] 08-streams — pipeline/laziness, map/filter/flatMap/etc, reduce/collect/Collectors, Optional, primitive streams, parallel streams, banking dataset exercises
- [ ] 09-multithreading-concurrency — lifecycle, synchronized/wait-notify, volatile/JMM, locks, atomics/CAS, executors, CompletableFuture, synchronizers, concurrent collections recap, classic problems, virtual threads, build-it-yourself
- [ ] 10-java-interview-questions — core-java, collections, streams, concurrency, java8-to-21-features (150+ Qs)
- [ ] 11-spring-boot-interview-questions — core-spring, spring-boot, rest-and-validation, exception-handling, data-jpa, security, microservices, testing, scenario-based (120+ Qs)
- [ ] 12-revision — one-page-cheatsheets, mock-interviews, last-week-checklist

## Build status

- [x] Project scaffold created (pom.xml, README.md, PROGRESS.md, mvnw/mvnw.cmd)
- [x] Batch 1 (01-03) compiled & tested — test-compile clean, 228/228 solution tests pass (47 solution test classes)
- [x] Batch 2 (04-06) compiled & tested — test-compile clean, 408/408 solution tests pass (77 solution test classes); fixed 2 wrong test expectations in module 06 (FirstNonRepeatingChar edge case, TwoSum largerInput assumed a specific pair that a correct single-pass algorithm doesn't return)
- [ ] Batch 3 (07-09) compiled & tested
- [ ] Batch 4 (10-12) compiled & tested
- [ ] Final full build green

## Notes

- Java 21, JUnit 5, AssertJ, single Maven module.
- Maven is not on global PATH on this machine; use `./mvnw` (bash) or
  `mvnw.cmd` (PowerShell), which point at the cached distribution under
  `~/.m2/wrapper/dists/apache-maven-3.9.11/...`.
- Repo was not a git repository at start; initialized locally. No git
  remote was configured, so commits are local-only unless a remote is
  added later.
