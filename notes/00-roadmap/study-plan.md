# 8-Week Study Plan

Target: ~1.5-2 hrs/day, geared for a 3.5+ year Java backend developer
(banking domain) preparing for interviews. Assumes weekday study + heavier
weekend revision.

**Daily routine:** read notes for the day's topic → run the matching example
class → solve 2-4 exercises (attempt before peeking at solutions) → spend the
last 15 min re-reading yesterday's interview questions out loud (spoken
answers, not just silent recognition — interviews are verbal).

## Week 1 — Foundations + Collections overview
| Day | Focus |
|---|---|
| Mon | 01: Big-O, amortized analysis |
| Tue | 01: Generics (bounded types, wildcards, PECS) |
| Wed | 01: equals/hashCode contract, Comparable vs Comparator |
| Thu | 01: Iterable/Iterator, fail-fast vs fail-safe |
| Fri | 01: Arrays & Collections utility classes |
| Sat | 02: Collections hierarchy, choosing the right collection, immutable collections |
| Sun | **Revision** — redo Week 1 exercises from memory, re-read all [Advanced] Qs |

## Week 2 — List
| Day | Focus |
|---|---|
| Mon | List interface, ArrayList internals (array growth, arraycopy) |
| Tue | LinkedList internals (doubly linked nodes, Deque) |
| Wed | ArrayList vs LinkedList benchmark; Vector/Stack legacy; ArrayDeque |
| Thu | CopyOnWriteArrayList; ListIterator, subList gotchas |
| Fri | DSA patterns: two pointers, sliding window, prefix sum, in-place reversal |
| Sat | Build it yourself: MyArrayList, MySinglyLinkedList |
| Sun | **Revision** — mock Q&A on List module (10 questions, spoken) |

## Week 3 — Queue/Deque + Set
| Day | Focus |
|---|---|
| Mon | Queue interface, Deque/ArrayDeque internals (circular array) |
| Tue | PriorityQueue internals (binary heap, sift-up/down) |
| Wed | BlockingQueue family |
| Thu | Queue/Deque DSA patterns (monotonic deque, top-K, merge K sorted) |
| Fri | HashSet/LinkedHashSet/TreeSet (NavigableSet) |
| Sat | EnumSet, concurrent sets; Set DSA patterns |
| Sun | **Revision** — build-it-yourself: circular queue, min-heap, stack using two queues |

## Week 4 — Map (the interview favorite)
| Day | Focus |
|---|---|
| Mon | HashMap internals part 1: buckets, hash spreading, load factor |
| Tue | HashMap internals part 2: resize, treeification at 8 |
| Wed | LinkedHashMap access order → build an LRU cache |
| Thu | TreeMap (Red-Black tree), NavigableMap |
| Fri | ConcurrentHashMap Java 8+ internals, compute/merge |
| Sat | WeakHashMap/IdentityHashMap/EnumMap; Map DSA patterns |
| Sun | **Revision + Build it yourself:** MyHashMap, LRU cache from scratch |

## Week 5 — Mixed DSA + Streams
| Day | Focus |
|---|---|
| Mon-Tue | 07: DSA problem sets — 15 Easy/Medium problems |
| Wed | 07: remaining Hard problems, pattern cheat sheet review |
| Thu | 08: Stream pipeline, laziness, map/filter/flatMap |
| Fri | 08: reduce, collect, Collectors (groupingBy, partitioningBy, teeing) |
| Sat | 08: Optional, primitive streams, parallel streams |
| Sun | **Revision** — banking dataset exercises (group transactions, top-N customers) |

## Week 6 — Concurrency
| Day | Focus |
|---|---|
| Mon | Thread lifecycle, synchronized, wait/notify |
| Tue | volatile, Java Memory Model, happens-before |
| Wed | ReentrantLock, ReadWriteLock, StampedLock, Condition |
| Thu | Atomics/CAS, Executor framework, thread pool sizing |
| Fri | CompletableFuture, synchronizers (CountDownLatch, CyclicBarrier, Semaphore) |
| Sat | Classic problems: deadlock, race condition, producer-consumer, virtual threads |
| Sun | **Revision + Build it yourself:** simple thread pool, bounded blocking queue |

## Week 7 — Interview question banks
| Day | Focus |
|---|---|
| Mon | 10: core-java.md (OOP, String, immutability, exceptions, JVM memory, GC) |
| Tue | 10: collections.md, streams.md |
| Wed | 10: concurrency.md, java8-to-21-features.md |
| Thu | 11: core-spring.md, spring-boot.md |
| Fri | 11: rest-and-validation.md, exception-handling.md, data-jpa.md |
| Sat | 11: security.md, microservices.md, testing.md |
| Sun | **Revision** — scenario-based questions, speak every answer out loud |

## Week 8 — Final revision + mock interviews
| Day | Focus |
|---|---|
| Mon-Tue | 12: one-page cheat sheets — read every module's sheet twice |
| Wed | Mock interview round 1-2 (see `12-revision/mock-interviews.md`) |
| Thu | Mock interview round 3-4 |
| Fri | Mock interview round 5 + weak-area drilling |
| Sat | `last-week-checklist.md` — walk it top to bottom |
| Sun | Light review only. Sleep well. |

## Tips
- Say answers out loud, not just in your head — interview delivery is a
  different skill from recognition.
- For every internals topic, be able to draw the ASCII diagram from memory
  (array growth, node links, heap tree, hash buckets, thread states).
- Re-derive Big-O tables from the mechanism, don't memorize them blindly —
  interviewers probe the "why."
