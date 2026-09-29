# Classic concurrency problems

## 1. What it is

This topic is a practical tour of the concurrency bugs and patterns that come up constantly in
interviews and in production: deadlock (how it happens, how to detect it, how to fix it), race
conditions (lost updates from unsynchronized compound operations), producer-consumer (the two
idiomatic ways to build it), alternating output between two threads, and the thread-safe singleton
pattern. Every technique used here is one already covered in topics 1-9 — this topic is where they
get applied to named, recognizable problems.

## 2. How it works internally

### Deadlock — the four necessary conditions, and how lock ordering breaks one of them

A deadlock requires **all four** of these conditions simultaneously (Coffman conditions):
1. **Mutual exclusion** — resources (locks) can't be shared.
2. **Hold and wait** — a thread holds one resource while waiting for another.
3. **No preemption** — a lock can't be forcibly taken away from the thread holding it.
4. **Circular wait** — a cycle of threads, each waiting for a resource held by the next.

The classic two-lock deadlock:
```
Thread A: synchronized(lockA) { ... synchronized(lockB) { ... } }   // acquires A, then wants B
Thread B: synchronized(lockB) { ... synchronized(lockA) { ... } }   // acquires B, then wants A

Timeline:
  A acquires lockA
  B acquires lockB
  A tries to acquire lockB -> blocks (B holds it)
  B tries to acquire lockA -> blocks (A holds it)
  -- both threads now wait forever: circular wait --
```
You can't easily remove mutual exclusion, hold-and-wait, or no-preemption from ordinary lock-based
code without much heavier machinery — the standard, practical fix targets **circular wait**
instead: **impose and enforce a consistent global lock-acquisition order** everywhere multiple
locks are taken together. If every thread always acquires `lockA` before `lockB` (never the
reverse), the cycle above becomes structurally impossible — a thread can never be waiting for a
lock that's held by another thread that is, in turn, waiting for a lock the first thread holds.

A concrete realistic version — a bank transfer between two accounts:
```java
// BUG: lock order depends on argument order, not a fixed rule
void transfer(Account from, Account to, int amount) {
    synchronized (from) {
        synchronized (to) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
// thread 1: transfer(accountA, accountB, 10)   -- locks A then B
// thread 2: transfer(accountB, accountA, 5)    -- locks B then A  -- CLASSIC DEADLOCK
```
```java
// FIX: always acquire the two locks in a fixed order, independent of from/to
void transferSafely(Account from, Account to, int amount) {
    Account first = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;
    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```
An alternative fix (when a stable ordering key isn't available, or you want a bounded-wait
alternative) is **`tryLock` with a timeout and backoff**: attempt both locks with a bound; if the
second `tryLock` fails, release everything already held and retry after a short random delay —
trading the certainty of lock ordering for the flexibility of not needing a total order, at the
cost of extra complexity and the (rare, bounded) possibility of livelock if backoff isn't randomized.

**Detecting** an actual deadlock at runtime: `ThreadMXBean.findDeadlockedThreads()`
(`java.lang.management`) inspects the JVM's thread states and lock-ownership graph and returns the
IDs of any threads currently deadlocked — this is exactly what triggers the "Found one Java-level
deadlock" section of a `jstack` thread dump. It's a genuine cycle-detection algorithm over the
lock-wait graph, not a timeout-based guess.

### Race conditions — non-atomic compound operations

A race condition happens when the correctness of a result depends on the (unsynchronized, thus
unpredictable) relative timing/interleaving of multiple threads. The most common form: a
**read-modify-write** on shared state where the three steps aren't atomic as a unit.
```java
private int count = 0;
void increment() { count++; }   // BUG: read, add 1, write -- three steps, not one
// Two threads interleaved: both read count=5, both compute 6, both write 6 -- one increment LOST
```
Fixes, in order of preference for a simple counter: an atomic class (`AtomicInteger`, topic 5 —
lock-free CAS retry loop), or `synchronized`/`ReentrantLock` around the whole read-modify-write if
more than one field must move together consistently.

### Producer-consumer — two idiomatic implementations

**Hand-rolled with `wait`/`notify`** (topic 2) — a bounded buffer where `put()` waits while full,
`take()` waits while empty, each signaling the other via the shared monitor's wait-set. Educational
because it makes the low-level mechanics explicit (see topic 2's full walkthrough), but easy to get
subtly wrong (using `if` instead of `while`, `notify()` instead of `notifyAll()` with mixed
waiters, forgetting the lock around the whole check-then-act).

**Via `BlockingQueue`** (module 04, revisited in topic 9) — `ArrayBlockingQueue`/
`LinkedBlockingQueue`'s `put()`/`take()` already implement exactly this bounded-wait behavior
internally, correctly, with none of the wait/notify pitfalls exposed to your code. This is the
idiomatic, production-recommended approach — the hand-rolled version is worth building once (for
understanding), then set aside in favor of the standard library's tested implementation.

### Print odd/even alternately with two threads

Two threads must strictly alternate: thread O prints 1, 3, 5...; thread E prints 2, 4, 6...; the
combined output must be exactly 1, 2, 3, 4, 5, ... in order. This needs a **shared turn indicator**
and a wait/notify (or `Lock`/`Condition`) handshake — each thread waits until it's its turn, does
its print, flips the turn, and signals the other:
```java
private final Object lock = new Object();
private volatile boolean oddsTurn = true;   // shared state, guarded by `lock`
void printOdd(int n) {
    synchronized (lock) {
        for (int i = 1; i <= n; i += 2) {
            while (!oddsTurn) { lock.wait(); }
            System.out.println(i);
            oddsTurn = false;
            lock.notifyAll();
        }
    }
}
// printEven is the mirror image, waiting for oddsTurn == true -> false transition reversed
```
This is a direct, minimal application of the wait/notify pattern from topic 2 — the "condition" is
simply "is it my turn."

### Thread-safe singleton — double-checked locking

The classic **eager initialization** (`private static final Instance INSTANCE = new Instance();`)
is already thread-safe (the JVM's class-loading mechanism guarantees this runs exactly once,
before any thread can observe the field) but always constructs the instance at class-load time,
even if never used. **Double-checked locking** defers construction until first use while staying
thread-safe:
```java
public final class Singleton {
    private static volatile Singleton instance;   // volatile is NOT optional -- see topic 3
    private Singleton() { }
    public static Singleton getInstance() {
        if (instance == null) {                     // first check, unsynchronized (fast path)
            synchronized (Singleton.class) {
                if (instance == null) {              // second check, inside the lock
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```
The `volatile` on `instance` is essential, not decorative — see topic 3 for exactly why: without
it, the JIT/CPU could reorder the constructor's field writes to occur *after* the reference is
published, letting a second thread's unsynchronized first check see a non-null but
partially-constructed object. The simpler, JVM-guaranteed-safe alternative for most cases is the
**initialization-on-demand holder** idiom, which gets the same lazy-and-thread-safe result without
any explicit locking at all, by relying on class-loading semantics:
```java
public final class Singleton {
    private Singleton() { }
    private static final class Holder {
        static final Singleton INSTANCE = new Singleton();   // loaded lazily, only on first access
    }
    public static Singleton getInstance() {
        return Holder.INSTANCE;   // JVM guarantees thread-safe, one-time class initialization
    }
}
```

### ASCII diagram — deadlock's circular wait vs the lock-ordering fix

```
DEADLOCK (circular wait):              FIXED (consistent global order, e.g. by id):

Thread A: holds lockA, wants lockB     Thread A: transfer(acc1, acc2) -> locks acc1 then acc2
Thread B: holds lockB, wants lockA     Thread B: transfer(acc2, acc1) -> STILL locks acc1 then acc2
    A ---wants---> lockB                        (because the fix reorders by id, not by from/to)
    ^                  |
    |                  v                 No cycle possible: every thread that wants both locks
   lockA <---wants--- B                  always asks for acc1 first -- the second lock is never
   (cycle -> deadlock forever)           held by someone also waiting for the first.
```

## 3. Complexity

| Problem | Technique | Notes |
|---------|-----------|-------|
| Deadlock avoidance (lock ordering) | O(1) extra comparison per transfer | the fix is a comparison + branch, not an algorithmic cost |
| Deadlock detection (`ThreadMXBean`) | O(threads + locks) graph traversal | run periodically/on-demand, not on every lock acquisition |
| Race condition fix (`AtomicInteger`) | O(1) amortized (CAS retry loop) | see topic 5 |
| Producer-consumer (either style) | O(1) per `put`/`take` | blocking is O(1) bookkeeping; wall-clock wait depends on the other side |
| Odd/even alternation | O(1) per handshake | one wait/notify round-trip per printed number |
| Double-checked locking singleton | O(1) after first call (volatile read only, no lock) | first call pays one synchronized block |

## 4. Example code
- `src/main/java/com/gk/study/concurrency/examples/DeadlockDemo.java` — triggers a genuine
  deadlock on two daemon threads, detects it deterministically via
  `ThreadMXBean.findDeadlockedThreads()` (no hang — the JVM exits normally since the stuck threads
  are daemons), then runs the lock-ordering-fixed version under real concurrent load and proves
  completion within a bounded timeout.
- `src/main/java/com/gk/study/concurrency/examples/RaceConditionDemo.java` — reproduces lost
  updates on a plain `int` counter under contention, then fixes it with `AtomicInteger`.
- `src/main/java/com/gk/study/concurrency/examples/ProducerConsumerWaitNotifyDemo.java` — hand-rolled
  bounded buffer.
- `src/main/java/com/gk/study/concurrency/examples/ProducerConsumerBlockingQueueDemo.java` —
  `ArrayBlockingQueue`-based equivalent.
- `src/main/java/com/gk/study/concurrency/examples/OddEvenAlternatingDemo.java` — two threads
  alternating output, captured into an ordered list and verified.
- `src/main/java/com/gk/study/concurrency/examples/ThreadSafeSingletonDemo.java` — double-checked
  locking and the holder idiom, each proven to construct exactly one instance under concurrent
  access.

Expected console output (abbreviated, across the demos):
```
DeadlockDemo: genuine deadlock detected via ThreadMXBean -> 2 threads involved (as expected)
DeadlockDemo: lock-ordering-fixed transfer completed 10000 opposite-order transfers within 5s, balances conserved
RaceConditionDemo: unsynchronized counter final=through some run < expected 20000 (lost updates reproduced)
RaceConditionDemo: AtomicInteger counter final=20000 (matches expected, no lost updates)
ProducerConsumerWaitNotifyDemo: produced=500, consumed=500, all items accounted for
ProducerConsumerBlockingQueueDemo: produced=1000, consumed=1000, no duplicates, no drops
OddEvenAlternatingDemo: combined output = [1, 2, 3, 4, ..., 20] (strictly ascending)
ThreadSafeSingletonDemo: double-checked locking -> 1 unique instance across 50 threads
ThreadSafeSingletonDemo: holder idiom -> 1 unique instance across 50 threads
```

## 5. When to use / when NOT to use

- Use consistent lock ordering as the default deadlock-avoidance strategy whenever code must hold
  more than one lock at a time — it's simple, has zero runtime overhead beyond a comparison, and is
  provably correct.
- Use `tryLock` + timeout + backoff only when a stable total ordering genuinely isn't available
  (e.g., locks identified by something without a natural comparable key) — it's more complex and
  needs careful backoff to avoid livelock.
- Use `AtomicInteger`/`LongAdder` for simple counters; use `synchronized`/`Lock` when more than one
  related field must be updated together atomically.
- Prefer `BlockingQueue` over hand-rolled `wait`/`notify` producer-consumer code in real
  applications — build the hand-rolled version once to understand the mechanics, then use the
  standard library.
- Prefer the initialization-on-demand holder idiom over double-checked locking for a lazy
  singleton when the class-loading-based approach fits — it needs no explicit `volatile`/lock
  reasoning at the call site at all; reach for double-checked locking specifically when you need
  parameterized/conditional re-initialization logic the holder idiom can't express.

## 6. Common pitfalls & gotchas

**Locking in inconsistent order based on caller-supplied argument order**:
```java
void transfer(Account from, Account to, int amt) {
    synchronized (from) { synchronized (to) { ... } }   // BUG: order depends on caller's args
}
// fix: always resolve to a stable, id-based order before locking (see section 2 above)
```

**Forgetting `volatile` on a double-checked-locking singleton's instance field**:
```java
private static Singleton instance;   // BUG: missing volatile -- a thread can observe a non-null
                                      // but partially-constructed instance due to reordering
// fix: private static volatile Singleton instance;
```

**Using `if` instead of `while` in a hand-rolled producer-consumer** — see topic 2's full
treatment; the same spurious-wakeup / multiple-waiter risk applies directly here.

**Assuming a race condition will "usually" surface in testing** — race conditions are timing
dependent; a buggy unsynchronized counter can pass every test run on a lightly loaded CI machine
and still lose updates constantly in production under real concurrent load. Never rely on "it
passed the tests" as proof of thread safety for code with shared mutable state — reason about it
explicitly instead.

## 7. Interview questions

- [Basic] What are the four necessary conditions for deadlock? → Mutual exclusion, hold-and-wait,
  no preemption, and circular wait — all four must hold simultaneously for a deadlock to occur. →
  Follow-up: *Which one does lock ordering target?* Circular wait — a fixed global acquisition
  order makes a cycle of waiting threads structurally impossible.
- [Basic] Why does `count++` need synchronization even though it looks like one operation? →
  It's actually three separate steps at the bytecode/hardware level — read the current value,
  compute the incremented value, write it back — and without synchronization two threads can
  interleave those steps such that one thread's increment is silently overwritten by another's,
  losing an update. → Follow-up: *What's the simplest fix for a plain counter?* Replace the `int`
  field with an `AtomicInteger` and call `incrementAndGet()`.
- [Basic] What's the difference between the hand-rolled wait/notify producer-consumer and the
  `BlockingQueue`-based one? → Both achieve the same bounded-buffer hand-off semantics; the
  hand-rolled version explicitly manages a `synchronized` block, a `while` condition check, and
  `wait()`/`notifyAll()` calls, while `BlockingQueue.put()`/`take()` already implement exactly that
  internally, tested and correct, with none of that mechanics exposed to your code. → Follow-up:
  *Which would you actually use in production code?* `BlockingQueue` — the hand-rolled version is
  primarily for understanding the underlying mechanics.
- [Intermediate] Walk through why `synchronized(from) { synchronized(to) { ... } }` can deadlock
  even though each individual `transfer` call looks correct in isolation. → The deadlock only
  emerges from *two concurrent calls with opposite argument order* — thread 1 calling
  `transfer(A, B, ...)` acquires A then wants B, while thread 2 calling `transfer(B, A, ...)`
  concurrently acquires B then wants A; each call individually is fine, but together they form a
  circular wait, since each thread holds what the other needs. → Follow-up: *How does resolving
  lock order by account ID fix this without changing the transfer's observable behavior?* Both
  calls, regardless of which account is `from` and which is `to`, end up acquiring the
  lower-ID account's lock first — so there's never a scenario where one thread holds the
  higher-ID lock while waiting for the lower-ID one that another thread already holds; the actual
  debit/credit logic (using the original `from`/`to` references) is unaffected by which lock was
  acquired first.
- [Intermediate] How would you actually detect a deadlock in a running JVM, in production, without
  guessing from symptoms like "the service seems stuck"? → Trigger a thread dump (e.g., `jstack
  <pid>`, or `kill -3` on the process, or programmatically via
  `ThreadMXBean.findDeadlockedThreads()`), which performs genuine cycle detection over the JVM's
  current lock-ownership graph and explicitly reports "Found one Java-level deadlock" with the
  exact threads and locks involved, rather than requiring you to infer it from timeouts or stalled
  requests. → Follow-up: *Does a thread dump also show threads that are merely slow, not
  deadlocked?* Yes — it shows every thread's current state and stack trace; distinguishing "blocked
  waiting on a lock forever" (deadlock) from "just slow" requires looking at whether the
  lock-ownership graph actually contains a cycle, which is exactly what
  `findDeadlockedThreads()`/the dump's deadlock section does for you.
- [Intermediate] Why is `volatile` required (not just good practice) on the instance field in
  double-checked locking? → Without it, there's no happens-before edge preventing the JIT/CPU from
  reordering the singleton's constructor field writes to logically occur *after* the reference
  assignment is published; a second thread's unsynchronized first `if (instance == null)` check
  could then observe a non-null reference pointing to an object whose fields aren't fully written
  yet, using a broken half-constructed singleton. → Follow-up: *Does the initialization-on-demand
  holder idiom have the same requirement?* No — it relies entirely on the JVM's class-initialization
  guarantees (a class is initialized exactly once, safely published to all threads, before first
  active use), so it needs no explicit `volatile` or locking at the call site at all.
- [Advanced] Design a fix for a deadlock scenario where the two resources don't have a natural,
  stable ordering key (e.g., they're identified by object identity only, or the set of locks needed
  isn't known until runtime). → Use `tryLock(timeout, unit)` (topic 4) to acquire each required
  lock with a bound; if any acquisition in the sequence fails, release every lock already acquired
  for this attempt and retry after a randomized backoff delay (randomization matters — without it,
  two threads that both back off and retry in lockstep can livelock, repeatedly colliding forever
  even though neither is technically deadlocked). This trades the zero-overhead certainty of a
  fixed lock order for the flexibility of not needing one, at the cost of retry complexity and a
  (bounded, low-probability with randomized backoff) livelock risk. → Follow-up: *Why is
  randomized backoff specifically important here, versus a fixed backoff delay?* A fixed delay
  means both competing threads back off for exactly the same duration and retry at exactly the
  same instant, potentially colliding again on every single retry — deterministically, not just
  occasionally; randomizing the delay makes repeated collision increasingly improbable with each
  retry.
- [Advanced] Compare the memory and initialization-timing trade-offs of eager initialization,
  double-checked locking, and the initialization-on-demand holder idiom for a singleton. → Eager
  initialization (`static final` field assigned directly) is simplest and fully thread-safe via
  class-loading guarantees, but always pays construction cost at class-load time even if the
  singleton is never used. Double-checked locking defers construction to first actual use and only
  pays a `synchronized` block's cost on that first call (subsequent calls are a single `volatile`
  read on the fast path), at the cost of more subtle code (the `volatile` requirement, the double
  check) that's easy to get wrong. The holder idiom gets the same lazy-construction benefit as
  double-checked locking, with none of the subtlety (no explicit `volatile`/lock reasoning needed)
  by offloading the "safe one-time initialization" guarantee entirely to the JVM's class-loading
  semantics — its only real limitation is that it can't easily support parameterized or
  conditionally-re-triggerable initialization logic, since a class's static initializer runs
  exactly once, unconditionally, on first active use. → Follow-up: *When would double-checked
  locking still be preferred over the holder idiom despite being more error-prone?* When the
  singleton's construction genuinely needs runtime parameters or conditional logic decided outside
  simple static initialization (e.g., choosing between implementations based on a config value
  read at startup) that doesn't map cleanly onto a static nested class's own initializer.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Fix a race condition on a shared counter under real concurrent load | Atomic CAS / synchronized | `exercises/ThreadSafeCounter.java` |
| E02 | Easy | Implement a lazily-initialized, thread-safe singleton via double-checked locking | volatile + double-checked locking | `exercises/ThreadSafeSingleton.java` |
| E03 | Easy | Alternate printing odd/even numbers 1..N in order using two threads | wait/notify handshake | `exercises/PrintOddEven.java` |
| E04 | Medium | Implement a bounded producer-consumer buffer from scratch with `wait`/`notify` | monitor + wait/notify | `exercises/ProducerConsumerWaitNotify.java` |
| E05 | Medium | Implement a producer-consumer pipeline using `ArrayBlockingQueue` with multiple producers/consumers | BlockingQueue + poison pill | `exercises/ProducerConsumerBlockingQueue.java` |
| E06 | Medium | Fix a two-account bank-transfer deadlock via consistent lock ordering, proven under concurrent opposite-direction transfers | lock ordering | `exercises/BankTransferDeadlockFix.java` |
| B01 | Build it yourself | Build a fixed-size thread pool from scratch (own worker threads + task queue, `submit`/`shutdown`/`awaitTermination`) | worker threads + internal queue | `exercises/SimpleThreadPoolExercise.java` |
| B02 | Build it yourself | Build a generic bounded blocking queue from scratch (`put`/`take` block on full/empty) | wait/notify-based ring buffer | `exercises/BoundedBlockingQueueExercise.java` |

- Each exercise: problem statement, input/output examples, constraints, hint (collapsed with
  `<details>`), target complexity — see the exercise file's Javadoc for the full statement.
- Solutions are in the `com.gk.study.concurrency.solutions` package — do not look until you've
  attempted the exercise; every solution has its own passing test suite.

## 9. Quick recap

- Deadlock needs all four Coffman conditions; the practical fix targets circular wait via a
  consistent global lock-acquisition order (or bounded `tryLock` + randomized backoff when no
  stable order exists).
- Race conditions come from non-atomic compound operations (`count++` is three steps, not one) —
  fix with atomics for single fields, `synchronized`/`Lock` for multi-field invariants.
- Producer-consumer: build the hand-rolled `wait`/`notify` version once to learn the mechanics,
  then use `BlockingQueue` in real code.
- Odd/even alternation and thread-safe singletons are both direct, minimal applications of the
  wait/notify (topic 2) and volatile/happens-before (topic 3) fundamentals.
- `ThreadMXBean.findDeadlockedThreads()` (and `jstack`) perform genuine lock-graph cycle detection
  — deadlock diagnosis doesn't require guessing from symptoms.
