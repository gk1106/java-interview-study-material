# Concurrency interview questions

Cross-cutting drill on multithreading/concurrency — full mechanics live in
`notes/09-multithreading-concurrency/` (11 files: thread lifecycle, synchronized/wait-notify,
volatile/JMM, locks, atomics/CAS, executors, CompletableFuture, synchronizers, concurrent
collections, classic problems, virtual threads). This file is the rapid interview version, tags
`[Basic]` / `[Intermediate]` / `[Advanced]`.

Sections: [Thread safety basics](#1-thread-safety-basics) · [synchronized vs Lock](#2-synchronized-vs-lock)
· [volatile vs synchronized](#3-volatile-vs-synchronized) · [Deadlock](#4-deadlock)
· [ExecutorService & thread pool sizing](#5-executorservice--thread-pool-sizing)
· [CompletableFuture](#6-completablefuture) · [Synchronizers](#7-synchronizers)
· [Virtual threads](#8-virtual-threads-java-21) · [Predict-the-output puzzles](#9-predict-the-output-puzzles)

---

## 1. Thread safety basics

**[Basic] What does "thread-safe" actually mean, precisely?**
A class/method is thread-safe if it behaves correctly (per its specification) when accessed
concurrently by multiple threads, with no external synchronization needed by the caller — no data
races, no visibility problems, and any invariants it maintains hold under all possible interleavings
of the threads calling it. Thread safety is a property of **code**, not of data — the same mutable
object can be safe or unsafe depending entirely on how (and whether) access to it is synchronized.
*Follow-up: is an immutable object always automatically thread-safe?* Yes — an object with no
mutable state (all fields `final`, no setters, properly published so its `final` fields are visible)
can never have a data race, since there's nothing to race on; immutability is the simplest, most
robust thread-safety strategy available.

**[Basic] What is a race condition, concretely, using `i++` as the canonical example?**
`i++` is **not atomic** — it's actually three separate steps: read the current value of `i`, add 1,
write the new value back. If two threads both read the same old value before either writes back,
one thread's increment is silently **lost** — the final value is one less than the correct count.
```java
int counter = 0;
// two threads each run: for (int i = 0; i < 100_000; i++) counter++;
// expected final counter: 200_000
// actual: some number less than 200_000, and different on different runs -- classic lost update
```
*Follow-up: what are three different fixes for this specific counter example, in increasing order
of typical preference?* (1) `synchronized` block/method around the increment (correct but coarser,
more contended). (2) `AtomicInteger.incrementAndGet()` (lock-free CAS loop, usually faster under
moderate contention). (3) `LongAdder`/`DoubleAdder` (Java 8+, strips contention across internal
cells, then sums on read) — the best choice specifically for very high-contention counting where
you don't need to read the running value frequently mid-update.

**[Intermediate] What are the three properties the Java Memory Model is really about protecting
(beyond just "avoid races"), and give one concurrency primitive that addresses each?**
**Atomicity** — an operation (or sequence of operations) appears to happen as one indivisible unit,
no other thread can observe a half-done state (`synchronized`, `AtomicInteger`, locks).
**Visibility** — a change one thread makes is guaranteed to actually become observable to another
thread, not cached forever in a CPU register/core-local cache (`volatile`, `synchronized`'s
happens-before edges, `final` field safe-publication). **Ordering** — the compiler/JIT/CPU are
normally free to reorder independent instructions for performance; certain primitives establish a
**happens-before** relationship that constrains which reorderings are legal across threads
(`volatile` writes/reads, lock acquire/release, `Thread.start()`/`join()`). A correct concurrent
program needs all three where they matter — a common trap is fixing atomicity (e.g. with
`AtomicInteger`) while forgetting that a *different*, non-atomic field read elsewhere still has a
visibility problem.
*Follow-up: does making a field `volatile` give you atomicity for compound operations like
`count++`?* No — `volatile` only gives visibility and ordering guarantees for individual reads/
writes of that field; `count++` is still three separate operations even if `count` is `volatile`,
so it's still a race (this is one of the most common `volatile` misunderstandings).

**[Advanced] What's the difference between a "benign" data race and a genuinely harmful one — does
the JMM formally recognize such a distinction?**
No — the Java Memory Model makes **no** exception for races that "happen to usually work out" in
practice; formally, any data race (concurrent, unsynchronized access to the same mutable field where
at least one access is a write) leaves the behavior **undefined**, meaning the JIT is legally free
to apply optimizations (caching a value in a register, reordering, eliminating a "redundant" read)
that can make even an apparently-harmless race fail unpredictably, especially under different JIT
optimization levels or hardware. "It worked fine in my testing" is not evidence of correctness for
a data race — undefined behavior can manifest only under specific optimization/scheduling
conditions that differ between a developer's machine, CI, and production load. The correct stance in
interviews: there is no such thing as a genuinely safe data race in Java; always reach for a proper
synchronization mechanism instead of reasoning "well, at worst it reads a slightly stale value,
which is fine here" — that reasoning can be invalidated by JIT behavior you don't control.
*Follow-up: give a concrete example of a JIT optimization that breaks a supposedly-benign race.* A
non-`volatile` boolean flag used as a loop-termination signal set by another thread — the JIT can
legally hoist the field read outside the loop entirely (caching it once in a register, since nothing
*it can see* modifies it within the loop from that thread's perspective), turning what looks like a
polling loop into an infinite loop that never observes the other thread's write at all.

---

## 2. synchronized vs Lock

**[Basic] `synchronized` (intrinsic lock/monitor) vs `java.util.concurrent.locks.ReentrantLock` —
what capabilities does `ReentrantLock` add that `synchronized` doesn't have?**
`synchronized` is built into the language (a block or method modifier), automatically
released even if an exception is thrown (structured, can't be forgotten), but is otherwise
inflexible: no timeout on acquisition attempts, no way to interrupt a thread that's blocked waiting
for it, no way to check "is this lock currently held/try to acquire without blocking," and strictly
block-scoped (must acquire and release within the same lexical block). `ReentrantLock` (implementing
the `Lock` interface) is an explicit, API-based lock offering: `tryLock()` (non-blocking attempt,
optionally with a timeout), `lockInterruptibly()` (a blocked acquisition attempt can be cancelled via
`Thread.interrupt()`), the ability to acquire in one method and release in another (though this is
usually discouraged as risky), and **fairness** as an optional constructor policy (`new
ReentrantLock(true)` — FIFO ordering among waiting threads, at some throughput cost, versus the
default unfair/barging policy which is usually faster on average).
*Follow-up: why is `ReentrantLock` unfair by default, if fairness sounds obviously "more correct"?*
A fair lock forces strict FIFO handoff, which requires more coordination overhead and can leave a
lock idle briefly between releasing and the next queued thread waking up and acquiring; an unfair
lock lets a newly-arriving thread "barge" and grab the lock immediately if it happens to be free,
which usually gives significantly higher overall throughput at the cost of a small risk of thread
starvation under sustained heavy contention — the JDK's default reflects that this trade-off is
usually the right one for typical workloads.

**[Basic] Why must you always release a `ReentrantLock` in a `finally` block?**
Unlike `synchronized`, which the JVM automatically and unconditionally releases when a synchronized
block exits (normally or via exception), `ReentrantLock.unlock()` is a completely manual API call —
there's no language-level safety net. If an exception is thrown between `lock()` and `unlock()` and
you didn't wrap it in `try`/`finally`, the lock is **never released**, permanently blocking every
other thread waiting on it (or forever, until the process restarts) — a severe, easy-to-introduce
bug.
```java
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();   // guaranteed to run even if the critical section throws
}
```
*Follow-up: does `synchronized` have any downside `ReentrantLock`'s explicit try/finally pattern
avoids?* Not really a downside per se — `synchronized`'s automatic release is strictly safer by
default (impossible to forget); the trade-off is purely about the extra capabilities
(`tryLock`, interruptibility, fairness, multiple `Condition`s) `ReentrantLock` provides at the cost
of needing this manual discipline.

**[Intermediate] What is `ReadWriteLock` (`ReentrantReadWriteLock`), and when does it actually
outperform a plain `ReentrantLock`?**
It splits locking into two separate locks sharing the same underlying state: a **read lock**
(shared — any number of threads can hold it simultaneously, as long as no thread holds the write
lock) and a **write lock** (exclusive — only one thread, and blocks all readers while held). It
outperforms a single `ReentrantLock` specifically for **read-heavy, write-rare** workloads, since
concurrent readers no longer serialize against each other at all — only readers-vs-writer and
writer-vs-writer contend. For write-heavy or roughly-even read/write workloads, the extra
bookkeeping overhead of `ReadWriteLock` can make it **slower** than a plain lock, so it's not a
default upgrade — profile before reaching for it.
*Follow-up: can a thread holding the write lock also acquire the read lock (lock downgrading), and
is the reverse possible?* Yes — downgrading (hold write lock, acquire read lock, then release write
lock) is explicitly supported and a documented safe pattern. The reverse — **upgrading** a held read
lock directly to a write lock — is **not** supported and will deadlock (the thread would be waiting
for its own read lock to be released by "other" readers that will never release because it's
holding one itself); you must fully release the read lock first, then separately acquire the write
lock (accepting that another thread could act in between).

**[Advanced] What is `StampedLock`, and how does its "optimistic read" mode differ fundamentally
from `ReadWriteLock`'s read lock?**
`StampedLock` (Java 8+) adds a third mode beyond read/write: **optimistic reading**. A normal read
lock still blocks writers for its duration (mutual exclusion, just shared among readers).
Optimistic read (`tryOptimisticRead()`) takes **no lock at all** — it just returns a `stamp` (a
version marker) and lets the reader proceed reading fields without blocking anything, on the
optimistic assumption no writer will interfere; the reader then calls `validate(stamp)` afterward to
check whether a writer actually acquired the write lock during that window — if validation fails,
the reader must retry (typically falling back to a real read lock). This avoids blocking writers
entirely in the common (no-conflict) case, making it substantially faster than `ReadWriteLock` under
very read-heavy, low-write-contention workloads, at the cost of a noticeably more complex,
easy-to-misuse API (it is **not** reentrant, unlike `ReentrantLock`/`ReentrantReadWriteLock`, and
mixing it with normal `synchronized`-style reasoning is a common source of subtle bugs).
*Follow-up: why is `StampedLock` not a drop-in general-purpose replacement for `ReentrantLock`
despite its speed advantage?* It's not reentrant (a thread re-acquiring a lock it already holds
deadlocks itself, unlike `ReentrantLock`), and its `Condition`-equivalent support is missing —
it's a specialized tool for a specific read-heavy performance profile, not a general safe default.

---

## 3. volatile vs synchronized

**[Basic] What does `volatile` actually guarantee, and what does it explicitly NOT guarantee?**
Guarantees: (1) **visibility** — every read of a `volatile` field sees the most recent write to it
by any thread (no thread-local caching of a stale value), and (2) a **happens-before** ordering: a
write to a `volatile` field happens-before every subsequent read of that same field by another
thread, which also prevents certain compiler/CPU reorderings around it. Does **not** guarantee:
atomicity for compound operations (`count++`, `if (x == null) x = new X();` are still races even if
`x`/`count` are `volatile`), and does not provide mutual exclusion the way a lock does — two
threads can still "simultaneously" (from a coordination standpoint) attempt a check-then-act
sequence on a volatile field.
*Follow-up: give a correct, idiomatic use of a plain `volatile boolean` flag.* A single-writer,
multi-reader shutdown/cancellation flag: one thread sets `volatile boolean running = false;`, and
worker threads simply poll `while (running) { ... }` — a pure single-write, multi-read visibility
problem with no compound operation involved, exactly what `volatile` is designed for.

**[Intermediate] Why can't `volatile` alone safely implement something like a thread-safe counter or
a lazily-initialized singleton, and what's needed instead?**
Both involve a **check-then-act** or **read-modify-write** sequence spanning more than one memory
operation — `volatile` only makes each *individual* read or write visible/ordered correctly; it says
nothing about what happens between two related operations on that field. A counter's `count++` is
read+increment+write — three steps, a data race even with `volatile`. A naive lazy singleton
(`if (instance == null) instance = new Singleton();`) has the classic double-checked-locking hazard:
two threads can both see `null`, both construct, and (more subtly and dangerously) even a single
correct-looking check can observe a **partially constructed** object due to constructor/write
reordering unless the field is properly `volatile` *and* the check-then-act is also protected by
synchronization (the canonical fix: `synchronized` block around the *second* check in "double-checked
locking," combined with a `volatile` instance field to prevent the partial-construction reordering
hazard specifically).
*Follow-up: what's the simplest, safest alternative to double-checked-locking lazy init in modern
Java?* The "initialization-on-demand holder" idiom — a private static nested class holding the
singleton instance as a `static final` field, relying on the JLS class-initialization guarantee
(a class is initialized lazily, exactly once, and the JVM itself handles the necessary
synchronization) — no explicit locking code needed at all, and just as lazy.

**[Advanced] Why does the JMM require a `volatile` write to establish happens-before with respect to
ALL of a thread's prior writes, not just the volatile field itself — and what pattern does this
enable?**
The JMM's happens-before rule for `volatile` isn't limited to the volatile field in isolation: if
thread A writes to a `volatile` field V, and then thread B subsequently reads that same value of V,
then **every** write thread A performed *before* writing V (volatile or not) is guaranteed visible
to thread B *after* B's read of V — the volatile write/read pair acts as a full memory
barrier/synchronization point for everything that happened-before it on the writing thread. This is
exactly the mechanism that makes the "safe publication" pattern work: construct an object fully
(writing all its fields, non-volatile included), then publish the reference to it via a single
`volatile` field write; any thread that later reads that `volatile` reference is guaranteed to see
the object in its fully-constructed state, not a partially-initialized one — this is precisely why
`volatile` alone (not just `synchronized`) is a legitimate safe-publication mechanism, and it's the
theoretical basis for why the "initialization-on-demand holder" idiom and properly-guarded
double-checked locking both work correctly.
*Follow-up: does this happens-before transitivity chain across multiple threads (A writes, B reads
and then writes a different volatile, C reads that)?* Yes — happens-before is transitive; a chain of
correctly synchronized volatile writes/reads (or lock releases/acquires) propagates visibility
transitively across any number of hops, which is the formal foundation for reasoning about
correctness in larger concurrent systems, not just two-thread examples.

---

## 4. Deadlock

**[Basic] What four conditions must all simultaneously hold for a deadlock to occur (Coffman
conditions), and how does each concurrency tool typically break one of them?**
**Mutual exclusion** (a resource can only be held by one thread at a time — inherent to locks,
generally not something you'd remove). **Hold and wait** (a thread holds one lock while waiting for
another) — broken by acquiring all needed locks atomically/up front, or never blocking while holding
a lock. **No preemption** (a lock can't be forcibly taken from a thread holding it) — broken by using
`tryLock(timeout)` to voluntarily back off instead of blocking forever. **Circular wait** (a cycle
of threads each waiting on a resource the next one in the cycle holds) — broken by enforcing a
**global, consistent lock-ordering** across the whole codebase (always acquire locks in the same
defined order, e.g. by object hash code or an assigned id, regardless of call-site order).
*Follow-up: which single condition is the most practical one to target in typical application code,
and why?* Circular wait — consistent lock ordering is usually the most tractable, systemic fix
(a coding discipline/convention enforceable in code review or via static analysis), versus trying to
eliminate mutual exclusion (not realistic) or hold-and-wait (often requires invasive redesign).

**[Intermediate] Write a minimal two-thread deadlock (classic "dining philosophers" / lock-ordering
style), and then fix it.**
```java
// DEADLOCK: threads acquire locks in opposite order
Object lockA = new Object(), lockB = new Object();
// Thread 1:
synchronized (lockA) { Thread.sleep(50); synchronized (lockB) { /* ... */ } }
// Thread 2 (started concurrently):
synchronized (lockB) { Thread.sleep(50); synchronized (lockA) { /* ... */ } }
// Thread 1 holds lockA, waits for lockB; Thread 2 holds lockB, waits for lockA -- circular wait, deadlock
```
```java
// FIX: enforce a single, consistent global lock order (e.g. by System.identityHashCode, or an assigned id)
Object first = System.identityHashCode(lockA) < System.identityHashCode(lockB) ? lockA : lockB;
Object second = (first == lockA) ? lockB : lockA;
// BOTH threads now always acquire `first` then `second`, in that fixed order -- no cycle possible
synchronized (first) { synchronized (second) { /* ... */ } }
```
*Follow-up: how would `tryLock` with a timeout provide a different (non-ordering-based) fix for the
same scenario?* Instead of blocking indefinitely on `synchronized`, use
`lockA.tryLock(timeout)`/`lockB.tryLock(timeout)`; if the second lock can't be acquired within the
timeout, release the first lock already held and retry (with some backoff, ideally randomized to
avoid livelock) — this breaks the "no preemption" condition instead of "circular wait," trading a
lock-ordering discipline requirement for retry/backoff complexity.

**[Advanced] How would you actually detect and diagnose a deadlock in a running production JVM
without restarting it?**
Take a **thread dump** — `jstack <pid>` (or `kill -3 <pid>` on Unix, sending SIGQUIT which the JVM
handles by dumping to stdout/the log, or a JMX-based tool/`jcmd <pid> Thread.print`). The JVM's own
deadlock detector (built into thread dumps for monitor-based `synchronized` deadlocks, and since
Java 6+ extended to detect deadlocks involving `java.util.concurrent` `Lock`s too) explicitly
reports a **"Found one Java-level deadlock"** section, listing exactly which threads are waiting on
which locks held by which other threads — no manual reasoning about the stack traces required for
the detection itself, though understanding *why* it happened (the code path) still requires reading
the reported stack traces. In a Spring Boot app, Actuator's `/actuator/threaddump` endpoint exposes
the same information over HTTP without needing shell access to the host.
*Follow-up: what's a proactive way to guard against deadlocks reaching production rather than
diagnosing them after the fact?* Consistent lock ordering as a coding standard (enforceable via code
review, or static analysis tools that flag inconsistent lock-acquisition orders across the codebase),
preferring higher-level concurrency utilities (`java.util.concurrent` collections,
`ExecutorService`, `CompletableFuture`) that avoid manual multi-lock coordination entirely wherever
possible, and load/stress testing concurrent code paths specifically designed to surface timing-
dependent bugs before release.

---

## 5. ExecutorService & thread pool sizing

**[Basic] Name the common `Executors` factory methods and the specific danger of each unbounded
one.**
`newFixedThreadPool(n)` — fixed pool size, but backed by an **unbounded** `LinkedBlockingQueue` work
queue — under sustained overload, tasks queue up without limit instead of applying backpressure,
risking `OutOfMemoryError` from unbounded queue growth. `newCachedThreadPool()` — no fixed upper
bound on thread count at all (creates a new thread for every task if none are idle, up to
`Integer.MAX_VALUE`), reaping idle threads after 60s — under a burst of many tasks this can create
an unbounded number of threads, exhausting memory/OS thread limits. `newSingleThreadExecutor()` —
single worker thread, same unbounded queue issue as `newFixedThreadPool(1)`.
`newScheduledThreadPool(n)` — for delayed/periodic tasks. Because of these unbounded-resource
risks, most production guidance (including the JDK team's own more recent recommendation) is to
construct a `ThreadPoolExecutor` **directly** with an explicit **bounded** queue and a defined
rejection policy, rather than reaching for the `Executors` convenience factories.
*Follow-up: what specifically goes wrong under load with `newCachedThreadPool` in a real incident?*
A sudden burst of concurrent requests each submits a task; since no thread is idle, a new thread is
created for essentially every single task with no ceiling — thread count can spike into the
thousands, each consuming its OS stack allocation (hundreds of KB+), potentially exhausting memory or
hitting the OS's max-threads-per-process limit, causing the whole JVM to become unresponsive or
crash.

**[Intermediate] How do you reason about sizing a thread pool for CPU-bound work vs I/O-bound work?**
For **CPU-bound** work (heavy computation, little/no blocking), the optimal pool size is roughly
`number of CPU cores` (or `cores + 1`) — more threads than cores just adds context-switching
overhead without more actual parallel computation capacity, since every core is already saturated.
For **I/O-bound** work (blocking network/disk calls, threads spend most of their time waiting, not
computing), you want **more** threads than cores, because a blocked thread isn't consuming CPU — a
common estimation formula: `threads ≈ cores × (1 + waitTime / computeTime)`, i.e. if a task spends
90% of its time blocked waiting on I/O and only 10% actually computing, you can support roughly
10x more concurrent threads than cores before CPU becomes the bottleneck (assuming the downstream
resource, e.g. a database connection pool, can actually sustain that concurrency too — pool sizing
is never just about the calling side).
*Follow-up: is this formula a precise guarantee, or a starting point?* A starting point only — real
sizing should be validated with load testing and monitoring (queue depth, latency, CPU utilization
under realistic traffic), since actual wait/compute ratios are rarely known precisely in advance and
can shift under different load levels.

**[Intermediate] What are `ThreadPoolExecutor`'s four rejection policies, and when would you choose
each?**
`AbortPolicy` (default) — throws `RejectedExecutionException` immediately when the queue is full and
max pool size is reached; forces the caller to explicitly handle overload (fail fast, don't silently
drop or degrade). `CallerRunsPolicy` — runs the rejected task **on the calling thread itself**
instead of a pool thread; a simple, effective built-in backpressure mechanism (it slows down whoever
is submitting work, since they're now doing the work themselves, naturally throttling the submission
rate). `DiscardPolicy` — silently drops the rejected task with no exception at all (dangerous —
usually wrong unless task loss is genuinely acceptable and expected, e.g. best-effort metrics/logging
that's fine to lose under extreme load). `DiscardOldestPolicy` — drops the **oldest** queued task to
make room for the new one (a priority-inversion-prone policy — favors newer work over older,
appropriate mainly when only the most recent task actually matters, e.g. a "latest status" update
where stale queued updates are worthless anyway).
*Follow-up: which is generally the safest default for a typical backend service under load, and
why?* `CallerRunsPolicy` is often preferred over the default `AbortPolicy` in production services,
because it provides organic backpressure (slowing the producer) rather than either an abrupt failure
requiring explicit retry logic everywhere, or silent data/task loss — though the right choice always
depends on whether the calling thread can tolerate being blocked doing the rejected work itself
(e.g. it shouldn't be used if the caller is a latency-critical request thread that must stay
responsive).

**[Advanced] Why does `ExecutorService.submit(Callable)` silently swallow an exception thrown inside
the task unless you explicitly call `Future.get()`?**
A `Callable`/`Runnable` submitted via `submit()` runs on a pool worker thread; if it throws, that
exception is captured **inside** the returned `Future` object rather than propagating anywhere
visible (there's no "caller" thread still on the stack to catch it — the submitting thread has
already moved on). It only resurfaces when you call `future.get()`, which re-throws it wrapped in an
`ExecutionException` (with the original exception as the `cause`). If you never call `get()` (a
common pattern with "fire and forget" submissions), the exception is silently lost forever — no log,
no stack trace, nothing — a genuinely dangerous silent-failure trap in real systems.
*Follow-up: how would you catch such exceptions without relying on every caller remembering to call
`Future.get()`?* Wrap task logic in an internal try/catch that logs before rethrowing (or before
swallowing deliberately, if that's the intent), or set a custom
`Thread.UncaughtExceptionHandler`/`ThreadFactory` on the pool for `execute()`-submitted `Runnable`s
(note: `execute()`, unlike `submit()`, *does* propagate an uncaught exception to the thread's
uncaught exception handler, since there's no `Future` to swallow it into) — the asymmetry between
`execute()` and `submit()` here is itself a common interview follow-up.

---

## 6. CompletableFuture

**[Basic] `Future` vs `CompletableFuture` — what capability gap does `CompletableFuture` close?**
A plain `Future<T>` (from `ExecutorService.submit`) only supports **blocking** `get()` to retrieve
the result (or a timeout-bounded blocking `get(timeout, unit)`) — there's no way to attach a
callback, chain a follow-up computation, or combine multiple futures without manually blocking a
thread to wait. `CompletableFuture<T>` (Java 8+) supports genuinely **asynchronous, non-blocking
composition**: chaining transformations (`thenApply`), chaining further async work
(`thenCompose`), running side effects (`thenAccept`), combining independent futures
(`thenCombine`, `allOf`, `anyOf`), and exception handling (`exceptionally`, `handle`) — all without
any thread ever needing to block and wait just to set up the next step.
*Follow-up: does calling `.get()` on a `CompletableFuture` still block, same as a plain `Future`?*
Yes — `CompletableFuture` still implements `Future`, so `.get()` is still a blocking call if you
choose to use it; the *point* of `CompletableFuture` is that you're no longer forced to, since the
chaining methods let you stay fully async end to end.

**[Basic] `thenApply` vs `thenCompose` — why can't you just always use `thenApply`?**
`thenApply(Function<T,R>)` is for a **synchronous, non-future-returning** transformation — it takes
the completed value and maps it to a plain `R`. `thenCompose(Function<T, CompletableFuture<R>>)` is
for chaining another **asynchronous** operation that itself returns a `CompletableFuture<R>` —
using `thenApply` here would produce a `CompletableFuture<CompletableFuture<R>>` (a nested,
"un-flattened" future), which is almost never what you want and requires manually unwrapping;
`thenCompose` **flattens** it automatically, analogous to `flatMap` on `Optional`/`Stream` vs `map`.
```java
CompletableFuture<User> userFuture = fetchUserAsync(id);
// WRONG shape: CompletableFuture<CompletableFuture<Order>>
userFuture.thenApply(user -> fetchOrdersAsync(user));
// RIGHT: flattened to CompletableFuture<Order>
userFuture.thenCompose(user -> fetchOrdersAsync(user));
```
*Follow-up: is this the same `map` vs `flatMap` distinction as in Streams/Optional?* Exactly the same
conceptual shape — `thenApply` = `map` (one value in, one value out, no nested wrapper), `thenCompose`
= `flatMap` (one value in, a wrapped/async value out, automatically flattened).

**[Intermediate] `thenApply` vs `thenApplyAsync` — what does the `Async` suffix actually change?**
The non-`Async` variant (`thenApply`, `thenAccept`, `thenCompose`, etc.) runs its callback on
**whichever thread completes the preceding stage** — which could be the thread that called
`complete()`, or a pool thread from wherever the prior async stage ran, meaning the exact executing
thread is somewhat unpredictable and callback logic effectively "borrows" whatever thread happens to
finish the previous step. The `Async` variant explicitly submits the callback to an
**`Executor`** — either the common `ForkJoinPool` by default (if no executor is passed), or an
explicit one you supply as an extra argument (`thenApplyAsync(fn, myExecutor)`) — guaranteeing the
callback runs on a pool thread, never inline on the completing thread. This matters when the
completing thread might be something you don't want doing extra work (e.g. an I/O event-loop
thread in a reactive framework, or the JVM's own common pool being used for unrelated work).
*Follow-up: why would you always pass an explicit `Executor` to the `Async` variants in production
code rather than relying on the default?* Relying on the default common `ForkJoinPool` risks the
exact cross-cutting starvation problem described for parallel streams — unrelated code elsewhere in
the same JVM sharing that same pool for blocking/slow work can starve your callback's execution (or
vice versa); passing a dedicated `Executor` (sized appropriately for the workload) isolates your
async pipeline's resource usage from everyone else's.

**[Intermediate] How do you wait for several independent `CompletableFuture`s to all complete, and
how do you get the first one to complete?**
`CompletableFuture.allOf(cf1, cf2, cf3)` returns a `CompletableFuture<Void>` that completes once
**all** given futures complete (it doesn't itself carry any of their results — you must call
`.get()`/`.join()` on the originals afterward to retrieve individual values, a commonly-cited API
awkwardness). `CompletableFuture.anyOf(cf1, cf2, cf3)` completes as soon as **any one** of them
completes, with that one's result (typed as `CompletableFuture<Object>`, requiring a cast — another
API rough edge).
```java
CompletableFuture<Void> all = CompletableFuture.allOf(cf1, cf2, cf3);
all.join();  // blocks until all three are done
List<String> results = Stream.of(cf1, cf2, cf3).map(CompletableFuture::join).toList(); // now safe, all done
```
*Follow-up: if one of the futures passed to `allOf` completes exceptionally, what happens to the
combined future?* The combined future also completes exceptionally (with that same exception) —
`allOf` propagates the first exceptional completion it observes among its inputs, even if the other
inputs are still pending or succeed later.

**[Advanced] `exceptionally` vs `handle` vs `whenComplete` — what's the precise difference in when
each runs and what it can do?**
`exceptionally(Function<Throwable,T>)` runs **only** if the stage completed exceptionally, producing
a recovery value of the same type `T` — a normal completion skips it entirely (pass-through).
`handle(BiFunction<T, Throwable, R>)` runs **unconditionally** (whether the stage succeeded or
failed), receiving both the result (`null` if it failed) and the exception (`null` if it succeeded)
— and, importantly, **can transform the type/return a new value regardless of outcome**, effectively
"resetting" the pipeline to a successful state either way. `whenComplete(BiConsumer<T, Throwable>)`
also runs unconditionally with both result and exception, but is purely a **side-effecting observer**
— it cannot alter the outcome; whatever exception or value was already present continues propagating
downstream unchanged after `whenComplete` runs (useful for logging/metrics without altering the
pipeline's outcome).
*Follow-up: which one would you use to log an error but still let the exception propagate to the
next stage?* `whenComplete` — it observes without altering the outcome, so the exception (if any)
continues flowing to subsequent stages exactly as before; `handle` would be the wrong choice here
since returning any value from it converts the pipeline to a successful completion, swallowing the
exception from downstream stages' point of view.

---

## 7. Synchronizers

**[Basic] `CountDownLatch` vs `CyclicBarrier` — what's the structural difference, and can either be
reused?**
`CountDownLatch` — initialized with a count; any thread can call `countDown()` to decrement it, and
any thread can call `await()` to block until the count reaches zero. It is **one-shot** — once the
count hits zero, it stays at zero forever; there is no way to reset it (a new latch must be created
for a repeated scenario). `CyclicBarrier` — initialized with a party count; each participating
thread calls `await()`, and **all** threads block until exactly that many threads have called
`await()`, at which point they're all released simultaneously and the barrier **automatically
resets** for reuse in a next round — designed specifically for repeated "wait for everyone to reach
this point" synchronization, optionally running a supplied `Runnable` once per round when the
barrier trips.
*Follow-up: give a realistic use case for each.* `CountDownLatch` — a main thread waiting for N
worker threads to finish an initialization/warm-up phase before proceeding (one-time gate).
`CyclicBarrier` — a multi-phase parallel computation where all worker threads must finish phase 1
before any of them can start phase 2 (repeated rendezvous point across many rounds).

**[Basic] What is `Semaphore`, and how is it different from a lock?**
A `Semaphore` maintains a count of available **permits**; `acquire()` blocks until a permit is
available then decrements the count, `release()` increments it back. Unlike a lock (which is
inherently a binary, 1-permit, typically-owner-restricted concept — the thread that locked it is
usually the one expected to unlock it), a `Semaphore` can be initialized with **N** permits (bounding
concurrent access to N simultaneous holders, not just 1), and critically, **any** thread can call
`release()` — it doesn't have to be the same thread that called `acquire()`, which enables patterns
locks don't (e.g. one thread produces/releases a permit that a different thread consumes/acquires).
*Follow-up: give a realistic use case.* Bounding concurrent access to a limited external resource —
e.g. capping the number of simultaneous outbound connections to a rate-limited third-party API to,
say, 10 at a time, regardless of how many application threads want to call it concurrently.

**[Intermediate] What does `Phaser` add over `CyclicBarrier`, and why is it rarely used in typical
application code despite being more powerful?**
`Phaser` (Java 7+) generalizes `CyclicBarrier`'s fixed-party-count, single-rendezvous-style
synchronization to support a **dynamically changing number of registered parties** (threads can
register and deregister between phases — useful when the set of participants isn't known/fixed up
front, e.g. a pipeline where worker threads come and go), and supports building **hierarchical**
(tree-structured) phasers to reduce contention among very large numbers of parties. It's used
rarely in typical business/backend code because most real synchronization needs fit
`CountDownLatch`/`CyclicBarrier`'s simpler, fixed-party model just fine, and `Phaser`'s more general
API is correspondingly more complex to reason about correctly (more edge cases around registration/
termination) for a benefit most codebases never actually need.
*Follow-up: does `Phaser` support the one-time "gate" pattern `CountDownLatch` provides too?* Yes —
a `Phaser` can be used in a one-shot mode by simply never advancing past the first phase, but that's
strictly more machinery than a `CountDownLatch` needs for that simpler use case — pick the simplest
tool that fits the actual requirement.

---

## 8. Virtual threads (Java 21)

**[Basic] What is a virtual thread, and how is it fundamentally different from a platform
(traditional) thread?**
A platform `Thread` maps **1:1** to a dedicated OS thread, with its own OS-allocated stack (often
several hundred KB+) — creating and context-switching between them is relatively expensive, which is
exactly why thread pools exist (reuse a bounded number of expensive platform threads across many
tasks). A **virtual thread** (`Thread.ofVirtual()`, Java 21, JEP 444) is a lightweight thread managed
entirely by the **JVM**, not the OS — many thousands (even millions) of virtual threads can be
"mounted" onto a much smaller number of underlying platform ("carrier") threads, which the JVM
schedules cooperatively: when a virtual thread performs a **blocking** operation (I/O, `sleep`,
`Lock.lock()`, most JDK blocking calls updated to be virtual-thread-aware), the JVM automatically
**unmounts** it from its carrier thread (parking it cheaply, off the OS thread entirely) and frees
that carrier to run a different virtual thread, then remounts the original once it's unblocked.
*Follow-up: does this mean virtual threads make CPU-bound code faster?* No — virtual threads don't
add parallelism for CPU-bound work (still bounded by actual CPU core count); they specifically target
the "many concurrent blocked-on-I/O threads" scenario, letting you write simple **one-thread-per-task,
blocking-style code** that scales to huge concurrency numbers without the cost (memory, context-switch
overhead) traditional platform threads would impose at that same scale.

**[Intermediate] Why does the standard advice become "never pool virtual threads, just create a new
one per task," reversing the traditional platform-thread-pool wisdom?**
Platform thread pools exist specifically because platform thread **creation** is expensive (OS
stack allocation, kernel scheduling registration) — pooling amortizes that fixed cost across many
tasks. Virtual threads are deliberately designed to be **cheap to create and cheap to discard** (no
dedicated OS thread or large stack allocated up front; the JVM manages their state far more
efficiently) — so the entire justification for pooling (avoid repeated expensive creation) simply
doesn't apply. Pooling virtual threads instead reintroduces the downsides pools have without their
one benefit: a fixed-size pool artificially **caps concurrency** at the pool size (exactly the
scaling limitation virtual threads exist to remove) and adds unnecessary bookkeeping overhead for
no gain.
*Follow-up: is `ExecutorService` still the right API to submit virtual-thread tasks to?* Yes —
`Executors.newVirtualThreadPerTaskExecutor()` gives you the familiar `ExecutorService` submission
API, but it creates a brand-new virtual thread for every submitted task rather than reusing a fixed
pool of worker threads, combining API familiarity with the "don't pool virtual threads" guidance.

**[Advanced] What is "thread pinning" with virtual threads, and what's the classic cause?**
Normally, when a virtual thread blocks, the JVM unmounts it from its carrier platform thread,
freeing the carrier to run other virtual threads. **Pinning** happens when a virtual thread blocks
while it **cannot** be safely unmounted — the historically most common case (as of Java 21) is
executing inside a `synchronized` block/method: the JVM's `synchronized` monitor implementation is
tied to the OS-level carrier thread, so a virtual thread blocked (or performing a blocking call)
*while holding* a `synchronized` lock keeps its carrier thread occupied the whole time, defeating
the scalability benefit for that code path — potentially even causing carrier-thread starvation if
enough virtual threads pin simultaneously (a fixed, small carrier pool with many pinned virtual
threads can deadlock/starve the whole system). The practical mitigation: replace `synchronized`
blocks that wrap blocking operations with `java.util.concurrent.locks.ReentrantLock` instead (which
virtual threads unmount around correctly), specifically in code paths that mix locking with
blocking I/O; `synchronized` blocks with no blocking calls inside them are unaffected and fine.
*Follow-up: how would you detect pinning in a running application?* Enable the JDK Flight Recorder
`jdk.VirtualThreadPinned` event (or run with `-Djdk.tracePinnedThreads=full/short`, which logs a
stack trace whenever pinning occurs), then look specifically for `synchronized` blocks in the
reported stack traces surrounding blocking calls.

---

## 9. Predict-the-output puzzles

**Puzzle 1 — race condition on a shared non-atomic counter**
```java
class Counter { int count = 0; void increment() { count++; } }
Counter counter = new Counter();
Runnable task = () -> { for (int i = 0; i < 100_000; i++) counter.increment(); };
Thread t1 = new Thread(task), t2 = new Thread(task);
t1.start(); t2.start();
t1.join(); t2.join();
System.out.println(counter.count);
```
**Output:** some number **less than 200000**, and it varies from run to run (not deterministic; a
specific number cannot be predicted).
**Why:** `count++` is not atomic — it's a read, increment, write sequence. Without `synchronized`
(or an `AtomicInteger`/`LongAdder`), both threads can read the same value of `count` before either
writes back its incremented result, causing one thread's increment to be silently lost (a "lost
update"). The exact final value depends on the unpredictable interleaving of the two threads' 200,000
combined operations on this run of the JVM/OS scheduler — it's genuinely non-deterministic, which is
itself the point being tested: recognizing *that* it's wrong (and why), not computing a specific
wrong number.

**Puzzle 2 — deadlock never printing anything (program hangs)**
```java
static final Object lockA = new Object();
static final Object lockB = new Object();
public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
        synchronized (lockA) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            synchronized (lockB) { System.out.println("t1 done"); }
        }
    });
    Thread t2 = new Thread(() -> {
        synchronized (lockB) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            synchronized (lockA) { System.out.println("t2 done"); }
        }
    });
    t1.start(); t2.start();
    t1.join(); t2.join();
    System.out.println("main done");
}
```
**Output:** the program **hangs forever** — nothing is ever printed (not even "main done"), and the
process never exits normally (would need to be killed).
**Why:** `t1` acquires `lockA` then, after a 100ms sleep (deliberately giving `t2` time to also
grab its first lock), tries to acquire `lockB`. `t2` acquires `lockB` then tries to acquire `lockA`.
After both sleeps elapse, `t1` holds `lockA` and waits for `lockB` (held by `t2`); `t2` holds `lockB`
and waits for `lockA` (held by `t1`) — a circular wait, satisfying all four Coffman conditions
simultaneously. Neither thread ever releases its lock (both are permanently blocked inside a nested
`synchronized`), so `t1.join()`/`t2.join()` in `main` also block forever, and `"main done"` is never
reached. A thread dump (`jstack`) taken on this hung process would show `"Found one Java-level
deadlock"` naming exactly these two threads and locks.
