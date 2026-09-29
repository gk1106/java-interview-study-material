# Mock Interviews — 5 Timed Rounds

This is a **timed drill script**, not a set of model answers — those live in the modules each
question links back to (`notes/03-list/` … `notes/11-spring-boot-interview-questions/`). Run each
round with a real timer. Speak every answer out loud (interview delivery is a different skill from
silent recognition — see `notes/00-roadmap/study-plan.md`). For coding problems, write real Java,
don't just describe the approach — then compare against the referenced solution file.

**How to run a round**: set a timer for the round's total budget, work through the questions in
order using the per-question budget as a soft checkpoint, then self-grade against that round's
rubric. Do at least 2 of the 5 rounds before a real interview (see `last-week-checklist.md`).

---

## Round 1 — DSA & Collections (45 min)

Coding problems reference exercise stubs under `src/main/java/com/gk/study/*/exercises/` — solve
on a blank file/whiteboard first, then diff against the stub's Javadoc and the matching
`solutions/` class.

| # | Budget | Prompt |
|---|---|---|
| 1 | 3 min | Conceptual: `ArrayList` vs `LinkedList` — state the Big-O for `get`, `add` at front, `add` at end, and say which you'd pick for a hot `get(i)` loop over 100k elements, and why. |
| 2 | 8 min | Code `TwoSumSorted` (module 07, E01) — two pointers, O(n) time, O(1) space. State the complexity before coding. |
| 3 | 6 min | Conceptual: walk through what happens internally on `HashMap.put()` — hashing, spreading, bucket index, treeification threshold, resize trigger. |
| 4 | 10 min | Code `KthLargestElement` (module 07, E09) using a size-k min-heap. Then explain why a **min**-heap is correct even though you want the k *largest* values. |
| 5 | 8 min | Code `MergeIntervals` (module 07, E08) — sort + linear scan. State why sorting first is required. |
| 6 | 5 min | Conceptual: design an LRU cache using `LinkedHashMap` (module 06, `02-linkedhashmap-lru.md`) — what constructor args and what override make it work, and what's the Big-O of get/put? |
| 7 | 5 min | Puzzle — predict the output: mutating a field an object's `hashCode()` depends on after it's inserted into a `HashSet`, then calling `set.contains(sameLogicalObject)`. What prints, and why? (See `10-java-questions-cheatsheet.md` puzzle #2.) |

### What a strong answer covers
- States the target time/space complexity **before** writing code, not after.
- Correctly distinguishes average-case vs worst-case (e.g. HashMap O(1) avg / O(log n) treeified worst).
- Names the actual JDK mechanism (spreading, treeify threshold 8, load factor 0.75, sift-up/down) rather than a vague "it hashes it."
- For the LRU/mutable-hash-key questions: explicitly connects the bug to *which bucket* the entry is computed into and why a later lookup misses it.
- Working code that compiles mentally — correct loop bounds, no off-by-one on sliding windows or interval merges.

---

## Round 2 — Streams & Java Language (30 min)

| # | Budget | Prompt |
|---|---|---|
| 1 | 4 min | Conceptual: what are the three parts of a stream pipeline, and which part actually executes anything? What happens if you build a pipeline and never call a terminal op? |
| 2 | 6 min | Code: given `List<Employee>`, produce `Map<String, List<String>>` of department → names, using `Collectors.groupingBy` + a downstream `mapping` collector. |
| 3 | 5 min | Conceptual: `map` vs `flatMap` — when does using `map` produce a compile error or an awkward nested type, and what's the fix? |
| 4 | 5 min | Conceptual: `orElse` vs `orElseGet` — which one always evaluates its argument, and why does that matter for an expensive fallback? |
| 5 | 4 min | Puzzle — predict the output: a `Stream.of(...).peek(...)` pipeline with **no terminal operation** at the end. What prints? (streams.md puzzle #1) |
| 6 | 3 min | Puzzle — predict the output: `numbers.stream().filter(...).findFirst()` on a stream with a `peek()` that prints each element visited — does it print all elements or stop early, and why? (streams.md puzzle #2) |
| 7 | 3 min | Rapid-fire: Integer caching — what does `Integer.valueOf(100) == Integer.valueOf(100)` print vs `Integer.valueOf(200) == Integer.valueOf(200)`, and why? (core-java.md puzzle #1) |

### What a strong answer covers
- Correctly identifies laziness — no element is touched until a terminal op runs.
- Names `IllegalStateException` as the failure mode for reusing a consumed stream, if asked.
- For groupingBy/mapping: gets the collector nesting right (`groupingBy(classifier, mapping(extractor, toList()))`) without needing to see it written down first.
- For the puzzles: explains the *mechanism* (autoboxing cache range -128..127; short-circuiting terminal ops), not just the printed value.
- Uses `orElseGet` correctly and can articulate the eager-vs-lazy distinction without hesitating.

---

## Round 3 — Concurrency (30 min)

| # | Budget | Prompt |
|---|---|---|
| 1 | 4 min | Conceptual: what is a race condition? Walk through why `count++` is not atomic, step by step (read, increment, write). |
| 2 | 6 min | Code: fix a racy counter incremented by multiple threads — show both an `AtomicInteger` fix and a `synchronized` fix, and say which you'd pick and why. |
| 3 | 6 min | Code: write a minimal two-thread **deadlock** (two accounts, opposite lock-acquisition order), then fix it with a consistent lock ordering. |
| 4 | 6 min | Code: producer-consumer using `wait()`/`notifyAll()` on a bounded buffer — including the `while` (not `if`) guard around the wait condition. Then explain how `BlockingQueue` would replace this in real code. |
| 5 | 4 min | Conceptual: `volatile` — what does it guarantee (visibility, ordering) and what does it explicitly NOT guarantee (atomicity of compound operations)? |
| 6 | 4 min | Puzzle — predict the output: a loop of 200,000 unsynchronized `count++` calls split across two threads, no `join()` issue — what does the final printed count look like across repeated runs? (concurrency.md puzzle #1) |

### What a strong answer covers
- Explicitly names the Coffman conditions or at least "circular wait" when discussing deadlock, and the fix is a **global lock-ordering rule**, not "just don't do that."
- Uses `while`, not `if`, around a `wait()` condition check, and can explain spurious wakeups / multiple waiters as the reason.
- Distinguishes `volatile` (visibility only) from atomics/locks (needed for compound operations) without conflating them.
- States that the race-condition puzzle's output is **nondeterministic and less than 200,000**, not a single fixed wrong number.
- Mentions `finally`-block release discipline for any lock/semaphore code written live.

---

## Round 4 — Spring Boot & System Design Lite (45 min)

| # | Budget | Prompt |
|---|---|---|
| 1 | 5 min | Conceptual: constructor injection vs field injection — give at least three concrete reasons constructor injection is preferred in production code. |
| 2 | 6 min | Conceptual: explain `@Transactional` propagation `REQUIRED` vs `REQUIRES_NEW` vs `NESTED`, each with a scenario where it's the right choice. |
| 3 | 5 min | Conceptual: why does calling a `@Transactional` method from another method in the *same class* silently not start a transaction? |
| 4 | 5 min | Conceptual: what is the N+1 select problem, and name two concrete fixes. |
| 5 | 4 min | Conceptual: what does `@ConditionalOnMissingBean` let you do, and why must auto-configuration classes be evaluated *after* your own `@Configuration` beans for that to work? |
| 6 | 10 min | Scenario: **"Your API's p99 latency has tripled over the last hour with no deployment. What do you check, in order?"** — answer as a structured triage (blast radius → what changed → resource saturation → thread dump → downstream/DB latency → slow leak), not a single guess. |
| 7 | 10 min | Scenario: **"A service throws intermittent 500s under load, works fine at low traffic, and passes all your tests. How do you debug it?"** — cover connection-pool exhaustion, shared mutable state in a singleton bean, and reproducing via logs/trace IDs before guessing. |

### What a strong answer covers
- For the scenario questions specifically: the **order and structure** of the investigation is graded, not just landing on the right root cause — dashboards/blast-radius first, then what changed, then resource saturation, then a thread dump, then DB/downstream latency.
- Correctly distinguishes propagation (transaction boundary/independence) from isolation (what one transaction can see of another) without conflating the two axes.
- Names the actual mechanism for the self-invocation bug: the bean you get from the container is an AOP **proxy**, and `this.method()` bypasses it.
- For N+1: names both a JPQL/`JOIN FETCH` or `@EntityGraph` fix, not just "turn on eager loading everywhere" (which trades one problem for a worse one).
- Ties DI questions back to testability (can you `new` the class with mocks without a Spring context?).

---

## Round 5 — Full Mixed Rapid-Fire (60 min)

Faster pace, broader coverage, deliberately touches every earlier module. Treat each item as a
60-90 second answer, not a monologue — this simulates a phone-screen pace.

| # | Budget | Prompt |
|---|---|---|
| 1 | 2 min | `ArrayList` growth factor, and why doubling/1.5x gives amortized O(1) append but a fixed increment doesn't. |
| 2 | 2 min | `PriorityQueue` — is its iterator sorted? How do you actually get sorted output? |
| 3 | 3 min | Code fragment: implement `Comparator<Employee>` sorting by department ascending, then salary descending, with nulls-first on salary. |
| 4 | 3 min | Code: `DailyTemperatures` (module 07, M07) — monotonic stack approach, state why it's amortized O(n). |
| 5 | 2 min | `ConcurrentHashMap` — why no null keys/values, when a plain `HashMap` allows one null key? |
| 6 | 3 min | Code: `Collectors.toMap` with a duplicate-key merge function — write the three-argument call from memory. |
| 7 | 2 min | `CompletableFuture.thenApply` vs `thenCompose` — when does using `thenApply` produce a nested future? |
| 8 | 3 min | Explain `ReadWriteLock`'s read→write upgrade hazard — why does it deadlock? |
| 9 | 2 min | Puzzle — predict the output: `"hel" + "lo" == "hello"` vs `new String("hello") == "hello"`. (core-java.md puzzle #2) |
| 10 | 2 min | Puzzle — predict the output: a `var list = new ArrayList<Integer>();` reassigned to a `LinkedList` two lines later — compiles or not? (java8-to-21-features.md puzzle #1) |
| 11 | 3 min | Spring: `@Primary` vs `@Qualifier` — when would you use each, and can they coexist? |
| 12 | 3 min | Spring: walk through the bean lifecycle order from constructor to `@PreDestroy`. |
| 13 | 5 min | Code: `NumberOfIslands` (module 07, M06) — BFS over a grid with a `HashSet` visited set. State the complexity in terms of grid size. |
| 14 | 5 min | Code: `MedianOfDataStream` (module 07, H01) — two-heap approach; explain the rebalancing invariant. |
| 15 | 3 min | Explain virtual threads' pinning hazard and the fix (`synchronized` → `ReentrantLock`). |
| 16 | 4 min | Scenario, condensed: "a `@Scheduled` batch job that used to take 5 minutes now takes 45 minutes" — give your top 3 things to check. |

### What a strong answer covers
- **Speed and precision** — this round grades whether facts are actually internalized (instant, confident, correctly-scoped answers) vs recalled slowly under pressure.
- No answer should need more than its budgeted time; if it does, that topic is flagged as a weak area for extra drilling (see `last-week-checklist.md`'s weak-area pass).
- Code answers are allowed to be terser here (helper-method signatures, core loop only) — the goal is confirming the pattern is automatic, not full production polish.
- Cross-module connections are a bonus signal of real understanding: e.g. noticing that `ConcurrentHashMap`'s null-prohibition and `Optional`'s whole design both trace back to "an ambiguous null return is unacceptable in concurrent/functional code."
