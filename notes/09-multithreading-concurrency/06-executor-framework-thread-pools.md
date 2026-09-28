# Executor Framework, Thread Pools & Future

## 1. What it is

The `java.util.concurrent` executor framework decouples **task submission** from **thread
management**. Instead of manually creating/starting/joining `Thread`s, you submit `Runnable`/
`Callable` tasks to an `ExecutorService`, which owns a managed pool of worker threads, a task
queue, and a policy for what happens when the pool is saturated. `Future<V>` represents the
eventual result of an asynchronously submitted task.

## 2. How it works internally

### Hierarchy

`Executor` (one method: `execute(Runnable)`) → `ExecutorService` (adds `submit`, `shutdown`,
`awaitTermination`, batch methods `invokeAll`/`invokeAny`) → `ScheduledExecutorService` (adds
delayed/periodic scheduling: `schedule`, `scheduleAtFixedRate`, `scheduleWithFixedDelay`). The
concrete workhorse behind almost all of them is `ThreadPoolExecutor`.

### `Executors` factory methods — what they hide, and why that's a problem

| Factory | Under the hood | Hidden risk |
|---------|-----------------|-------------|
| `newFixedThreadPool(n)` | `core=max=n`, unbounded `LinkedBlockingQueue` | queue grows without limit under sustained overload → `OutOfMemoryError` instead of backpressure |
| `newSingleThreadExecutor()` | `core=max=1`, unbounded `LinkedBlockingQueue` | same unbounded-queue risk |
| `newCachedThreadPool()` | `core=0, max=Integer.MAX_VALUE`, `SynchronousQueue`, 60s keep-alive | effectively unbounded thread creation under a burst → thread/memory exhaustion, OS thread-count limits |
| `newScheduledThreadPool(n)` | `ScheduledThreadPoolExecutor`, unbounded `DelayedWorkQueue` | same unbounded-queue category of risk |

This is exactly why `Executors.*` factory methods are **discouraged for production code**: each
one makes an invisible, hard-coded decision about the queue (bounded vs unbounded) or the max pool
size, and that decision is precisely the one that determines whether your service degrades
gracefully (rejects/backpressures) or fails catastrophically (OOM, thread exhaustion) under load.
The recommended practice is to construct `new ThreadPoolExecutor(...)` **directly**, so every
limit is an explicit, reviewed decision:

```java
ExecutorService pool = new ThreadPoolExecutor(
    4,                                  // corePoolSize
    8,                                  // maximumPoolSize
    60L, TimeUnit.SECONDS,              // keepAliveTime (for threads above core)
    new ArrayBlockingQueue<>(100),      // bounded work queue — a conscious capacity limit
    new ThreadPoolExecutor.CallerRunsPolicy()   // explicit, deliberate rejection policy
);
```

### `ThreadPoolExecutor` constructor parameters

- **corePoolSize** — threads kept alive even when idle (unless `allowCoreThreadTimeOut(true)`).
- **maximumPoolSize** — the hard ceiling on total worker threads.
- **keepAliveTime/unit** — how long a thread *above* core size sits idle before being reclaimed.
- **workQueue** — where tasks wait when all core threads are busy. Choice matters:
  `ArrayBlockingQueue` (bounded, FIFO — forces a real capacity decision), `LinkedBlockingQueue`
  (optionally bounded — unbounded by default, the trap above), `SynchronousQueue` (zero capacity —
  a `put` only succeeds if a thread is immediately ready to take it, forcing the pool toward
  `maximumPoolSize` quickly), `PriorityBlockingQueue` (unbounded, priority-ordered — for
  priority-based scheduling, not FIFO fairness).
- **threadFactory** — customize thread naming (essential for debugging thread dumps),
  daemon status, uncaught exception handlers.
- **handler** (`RejectedExecutionHandler`) — invoked when the pool cannot accept a task (see
  below).

### The actual task-submission decision order (commonly misunderstood)

`execute(task)` does **not** simply "add to queue, and grow the pool if the queue is full." The
real order is:

```
1. if runningThreads < corePoolSize:
       start a new CORE thread to run this task directly    (even if the queue is empty)
2. else if workQueue.offer(task) succeeds:
       task is queued; an existing thread will pick it up eventually
3. else if runningThreads < maximumPoolSize:
       start a new NON-CORE thread to run this task directly (queue was full)
4. else:
       reject the task -> RejectedExecutionHandler.rejectedExecution(task, executor)
```
A common misconception: people expect the pool to grow toward `maximumPoolSize` *before* the
queue fills up. It doesn't — with an unbounded queue (`newFixedThreadPool`), step 2 always
succeeds, so the pool **never** grows past `corePoolSize`, no matter how large `maximumPoolSize`
is configured, because the queue never reports "full."

### Rejection policies (`RejectedExecutionHandler`)

- **`AbortPolicy`** (default) — throws `RejectedExecutionException` immediately; caller must
  handle it.
- **`CallerRunsPolicy`** — the thread that called `submit`/`execute` runs the task itself,
  synchronously, on its own stack. This is a natural, elegant backpressure mechanism: the
  producer is slowed down (blocked doing the work itself) exactly when the pool is saturated,
  which throttles the rate of new submissions without dropping any work.
- **`DiscardPolicy`** — silently drops the task (no exception, no execution) — dangerous unless
  task loss is genuinely acceptable (e.g., best-effort metrics).
- **`DiscardOldestPolicy`** — drops the *oldest* queued task, then retries submitting the new one
  — favors newer work over older, useful when only the freshest tasks matter (e.g., latest price
  ticks).

### Shutdown: `shutdown()` vs `shutdownNow()` vs `awaitTermination()`

- **`shutdown()`** — graceful: stop accepting new tasks (`execute` after this throws
  `RejectedExecutionException`), but let already-submitted (queued + running) tasks finish.
- **`shutdownNow()`** — aggressive: stops accepting new tasks, attempts to **interrupt** all
  actively running tasks, and returns the `List<Runnable>` of tasks that were queued but never
  started. It does **not guarantee** running tasks actually stop — that depends on those tasks
  respecting `InterruptedException`/`Thread.interrupted()`; a task with a tight non-blocking loop
  that never checks interruption will keep running.
- **`awaitTermination(timeout, unit)`** — blocks the calling thread until either the pool has
  fully terminated or the timeout elapses; returns `false` on timeout.

Idiomatic full shutdown sequence:
```java
executor.shutdown();
try {
    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();                                  // escalate
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.err.println("pool did not terminate");
        }
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();                          // restore interrupt status
}
```

### `Future<V>` and `submit()` vs `execute()`

`execute(Runnable)` (from `Executor`) fires-and-forgets — no handle to the result or any exception
thrown (an uncaught exception in a `Runnable` submitted via `execute` goes to the thread's
uncaught-exception handler and the thread dies/is replaced). `submit(Callable<V>)` /
`submit(Runnable, V result)` (from `ExecutorService`) return a `Future<V>` — a handle that lets
you: `get()` (block until done, rethrowing the task's exception wrapped in
`ExecutionException`), `get(timeout, unit)` (bounded wait, throws `TimeoutException` if not done),
`cancel(mayInterruptIfRunning)` (attempts to cancel; if `true` and already running, interrupts
it), `isDone()`, `isCancelled()`.

### Thread pool sizing formulas

**CPU-bound tasks** (pure computation, no blocking): `N_threads = N_cpu + 1`. The `+1` keeps one
extra thread ready so that if a thread briefly stalls (page fault, GC pause, OS scheduling
hiccup), a core doesn't sit idle — but going much beyond `N_cpu` just adds context-switch
overhead for no benefit, since every core is already saturated with useful work.

**I/O-bound / blocking tasks**: `N_threads = N_cpu * (1 + waitTime/computeTime)`. Reasoning: if a
task spends most of its time *blocked* (waiting on a DB call, a downstream REST call, disk I/O),
that thread isn't using the CPU at all during the wait — you need enough extra threads so that
while some are blocked waiting, others are actively using the CPU cores, keeping them saturated
(this is the same intuition as Little's Law: throughput = concurrency / latency, so to sustain a
given throughput with high per-task latency, you need proportionally more concurrent workers).

Worked example: 8 cores, a task that spends 80% of its wall-clock time blocked on a downstream
call and 20% on CPU work (a wait/compute ratio of 4): `N = 8 * (1 + 4) = 40` threads. A banking
example: a service validating a transaction in-memory (CPU-bound, `N=cpu+1`) needs a small pool;
the same service then calling a core-banking downstream API to post the transaction (I/O-bound,
dominated by network wait) needs a much larger pool sized by the wait/compute ratio — conflating
the two and using one pool sized for CPU work would starve the I/O-heavy calls, while sizing the
CPU-bound pool as if it were I/O-bound would waste threads and context-switching overhead for no
throughput gain.

### ASCII diagram — thread pool architecture

```
                     submit(task)
                          |
                          v
      +-------------------------------------------+
      |  ThreadPoolExecutor                        |
      |                                             |
      |  running < core? --yes--> start CORE thread |
      |       | no                                  |
      |       v                                     |
      |  workQueue.offer(task) --success--> queued -----> [core thread 1] --\
      |       | full                                                        |--> pick up & run
      |       v                                                             |
      |  running < max? --yes--> start NON-CORE thread ---> [extra thread]--/
      |       | no
      |       v
      |  RejectedExecutionHandler
      |     AbortPolicy / CallerRunsPolicy / DiscardPolicy / DiscardOldestPolicy
      +-------------------------------------------+
             workQueue: [task][task][task]...   (bounded ArrayBlockingQueue recommended)
```

## 3. Complexity

| Operation | Time | Notes |
|-----------|------|-------|
| `execute`/`submit` (queue has room) | O(1) | enqueue only |
| `get()` (task already done) | O(1) | returns immediately |
| `get()` (task not done) | blocks until completion (or timeout with `get(timeout,unit)`) | |
| `shutdown()`/`shutdownNow()` | O(1) to initiate | actual drain time depends on task count/duration |
| pool growth (core->max) | O(1) per new thread | bounded by `maximumPoolSize` |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/ExecutorThreadPoolDemo.java` —
  builds a `ThreadPoolExecutor` directly with a small bounded queue, demonstrates
  `CallerRunsPolicy` and `AbortPolicy` rejection, `Future.get(timeout)` and `cancel()`, and a full
  shutdown -> awaitTermination -> shutdownNow fallback sequence.

```java
ExecutorService pool = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(2), new ThreadPoolExecutor.CallerRunsPolicy());
Future<Integer> future = pool.submit(() -> 21 * 2);
System.out.println(future.get(2, TimeUnit.SECONDS));   // 42
```
Expected console output (abbreviated):
```
CallerRunsPolicy: submitting thread ran the overflow task itself -> submittingThreadRanTask=true
AbortPolicy: overflow submission threw RejectedExecutionException as expected
Future.get(timeout) result = 42
cancel() on a still-running task -> cancelled=true, isDone=true
shutdown sequence completed cleanly within timeout
```

## 5. When to use / when NOT to use

- Use `ThreadPoolExecutor` directly (not `Executors.*`) in production code so queue capacity and
  rejection behavior are explicit design decisions.
- Use `CallerRunsPolicy` when you want natural backpressure without dropping work; use
  `AbortPolicy` when the caller must know immediately and decide what to do; avoid
  `DiscardPolicy`/`DiscardOldestPolicy` unless task loss is genuinely acceptable.
- Use `submit()`+`Future` when you need the result, error, cancellation or completion status;
  use `execute()` only for true fire-and-forget where an uncaught exception killing/replacing the
  thread is acceptable.
- Avoid sizing every pool the same way — size CPU-bound pools near `N_cpu+1`, I/O-bound pools much
  larger using the wait/compute ratio formula; never share one pool between very different task
  profiles without understanding which profile dominates.

## 6. Common pitfalls & gotchas

**Assuming `newFixedThreadPool` bounds memory usage** — it bounds *thread* count, not the queue;
under sustained overload the unbounded `LinkedBlockingQueue` behind it can grow until
`OutOfMemoryError`.

**Forgetting `Future.get()` rethrows task exceptions wrapped** — a `RuntimeException` thrown
inside the `Callable` surfaces at `get()` as `ExecutionException` with the original as its cause,
not as the raw exception:
```java
try {
    future.get();
} catch (ExecutionException e) {
    Throwable cause = e.getCause();   // the ACTUAL exception the task threw
}
```

**Calling `shutdownNow()` and assuming tasks stop immediately** — it only *requests* interruption;
a task that ignores `InterruptedException`/never checks `Thread.currentThread().isInterrupted()`
keeps running to completion regardless.

**Never calling `awaitTermination`** — calling `shutdown()` and moving on immediately means your
program (or test) can exit/proceed while pool threads are still mid-task, silently losing work or
racing with cleanup that assumed the pool was fully drained.

## 7. Interview questions

- [Basic] What's the difference between `execute()` and `submit()`? → `execute(Runnable)` is
  fire-and-forget with no return handle — an uncaught exception kills/replaces the worker thread
  silently; `submit()` returns a `Future` that lets you retrieve the result, catch the task's
  exception via `get()`, or cancel it. → Follow-up: *If a Runnable submitted via execute() throws,
  where does the exception go?* To the thread's `UncaughtExceptionHandler` — by default it's
  printed to stderr and the worker thread terminates (the pool replaces it with a fresh one).
- [Basic] What does `shutdown()` do versus `shutdownNow()`? → `shutdown()` stops accepting new
  tasks but lets already-queued and already-running tasks finish; `shutdownNow()` additionally
  tries to interrupt running tasks and returns the tasks that were still queued and never started.
  → Follow-up: *Does shutdownNow() guarantee everything stops immediately?* No — it only requests
  interruption; a task ignoring interruption keeps running.
- [Basic] Why is `Executors.newFixedThreadPool()` often discouraged in production? → It's backed
  by an unbounded `LinkedBlockingQueue`, so under sustained overload the queue (not the thread
  count) grows without limit, risking `OutOfMemoryError` instead of applying backpressure or
  rejecting excess work — a capacity limit you never explicitly chose. → Follow-up: *What's the
  recommended alternative?* Construct `new ThreadPoolExecutor(...)` directly with an explicit
  bounded queue and a deliberate `RejectedExecutionHandler`.
- [Basic] What's the risk with `Executors.newCachedThreadPool()`? → Its `maximumPoolSize` is
  effectively `Integer.MAX_VALUE` with a `SynchronousQueue`, so a burst of submissions can spawn
  an unbounded number of threads, exhausting memory or OS thread limits. → Follow-up: *When is it
  actually a reasonable choice?* Short-lived, bursty, naturally self-limiting workloads where you
  fully trust the submission rate won't spike unboundedly — rare in production services with
  untrusted or highly variable load.
- [Intermediate] Walk through `ThreadPoolExecutor`'s actual decision order when a task is
  submitted. → If running threads are below `corePoolSize`, start a new core thread to run the
  task immediately, even if the queue is empty; otherwise try to enqueue the task; if the queue is
  full, start a new thread up to `maximumPoolSize`; if that's also maxed out, hand the task to the
  `RejectedExecutionHandler`. → Follow-up: *With an unbounded queue, will the pool ever grow past
  corePoolSize?* No — the queue will always accept the task (step 2 always succeeds), so the pool
  never reaches step 3 and never grows toward `maximumPoolSize`, regardless of how high it's set.
- [Intermediate] Explain `CallerRunsPolicy` and why it's often a good default. → When the pool and
  queue are both saturated, the thread that called `submit`/`execute` runs the task itself,
  synchronously, instead of the task being queued or dropped — this naturally throttles the
  producer (it's now busy doing the work itself instead of generating more load) without losing
  any task, acting as organic backpressure. → Follow-up: *What's a downside of CallerRunsPolicy?*
  It can block whatever thread is submitting tasks (e.g., a request-handling thread), which may
  itself have latency requirements — it trades "no dropped work" for "the submitter absorbs the
  slowdown."
- [Intermediate] How would you size a thread pool for a service that mostly waits on a downstream
  database call? → It's I/O-bound, so use `N_threads = N_cpu * (1 + waitTime/computeTime)`, not
  `N_cpu + 1` — with e.g. 8 cores and a wait/compute ratio of 4 (task spends 80% of its time
  blocked), that's `8 * 5 = 40` threads, because each blocked thread isn't using a core, so you
  need more concurrent threads than cores to keep the CPUs actually busy while others wait. →
  Follow-up: *What would happen if you used N_cpu+1 for this workload instead?* Severe
  under-utilization — most threads spend most of their time blocked, cores sit idle, and
  throughput is far below what the hardware could sustain.
- [Intermediate] Why must `unlock`-style cleanup and `Future.get()` error handling both go through
  `finally`/`try-catch` carefully? → `Future.get()` wraps any exception the task threw in an
  `ExecutionException` — catching plain `RuntimeException` around `get()` won't catch the task's
  original exception; you must catch `ExecutionException` and inspect `getCause()`. → Follow-up:
  *What does get() throw if the task was cancelled?* `CancellationException`.
- [Advanced] Why does `awaitTermination` need a bounded timeout even in a graceful shutdown
  sequence, and what's the idiomatic escalation pattern? → A task might hang indefinitely (bug,
  stuck downstream call with no timeout of its own), so blocking on `awaitTermination` with no
  bound risks the shutdown sequence itself hanging forever; the idiomatic pattern is `shutdown()`
  -> `awaitTermination(timeout)` -> if it returns false, `shutdownNow()` (escalate to interruption)
  -> `awaitTermination(timeout)` again -> log/give up if still not terminated, while also handling
  `InterruptedException` on the awaiting thread itself by calling `shutdownNow()` and restoring
  the interrupt flag. → Follow-up: *Why restore the interrupt flag with `Thread.currentThread().interrupt()`
  in the catch block?* Catching `InterruptedException` clears the thread's interrupted status;
  restoring it lets calling code further up the stack still observe that an interruption occurred,
  rather than silently swallowing it.
- [Advanced] Two services both do "mostly CPU work with an occasional blocking call." How would
  you decide whether to size the pool as CPU-bound or I/O-bound? → Measure (or estimate) the
  actual wait/compute ratio in the hot path rather than guessing from the task's general
  description — profile or instrument average time spent blocked vs actively computing per task;
  if the wait fraction is small (say under ~10-20%), `N_cpu+1`-style sizing is close enough and the
  formula's `(1+ratio)` term barely inflates the thread count; if blocking dominates, the I/O
  formula matters a lot. In practice, many production services also separate the two concerns into
  different pools (a small CPU-bound pool for computation, a larger I/O-bound pool for downstream
  calls) rather than trying to size one pool for a mixed workload. → Follow-up: *What's a risk of
  using one shared pool for both profiles?* A burst of slow I/O-bound tasks can starve/delay fast
  CPU-bound tasks queued behind them, since they compete for the same limited worker threads.

## 8. Exercises

No dedicated exercises for this topic — see `notes/09-multithreading-concurrency/10-classic-problems.md`
and `notes/09-multithreading-concurrency/12-build-it-yourself.md` (which builds a minimal thread
pool from scratch, directly applying these internals) for hands-on practice.

## 9. Quick recap

- `Executors.*` factories hide either an unbounded queue (`newFixedThreadPool`,
  `newSingleThreadExecutor`) or an unbounded max pool size (`newCachedThreadPool`) — prefer
  `new ThreadPoolExecutor(...)` directly with an explicit bounded queue and rejection policy.
- Submission order is core threads first, then queue, then extra threads up to max, then
  rejection — an unbounded queue means the pool never grows past `corePoolSize`.
- Rejection policies: `AbortPolicy` (throw), `CallerRunsPolicy` (natural backpressure, caller does
  the work), `DiscardPolicy`/`DiscardOldestPolicy` (drop work — use only if loss is acceptable).
- `shutdown()` is graceful (finish what's queued/running); `shutdownNow()` interrupts running
  tasks and returns never-started ones, with no guarantee running tasks actually stop.
- Size CPU-bound pools near `N_cpu+1`; size I/O-bound pools much larger via
  `N_cpu*(1+wait/compute)` — conflating the two profiles in one pool under- or over-provisions it.
