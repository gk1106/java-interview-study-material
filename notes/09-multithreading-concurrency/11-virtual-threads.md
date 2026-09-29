# Virtual threads (Java 21)

## 1. What it is

Virtual threads (JEP 444, finalized in Java 21) are lightweight threads managed by the JVM rather
than mapped 1:1 to OS threads. They implement the exact same `Thread` API you already know
(`Thread.start()`, `Thread.currentThread()`, thread-locals, interruption) but can be created in
their **millions** without exhausting memory or OS resources, because blocking a virtual thread
doesn't block an underlying OS (**carrier**) thread — it unmounts, freeing the carrier to run other
virtual threads.

## 2. How it works internally

### Platform threads vs virtual threads

Every `Thread` you've used so far (topics 1, 6) is a **platform thread**: a thin Java wrapper
around one real OS thread, with its own OS-allocated stack (often ~512KB-1MB), scheduled by the OS
kernel. Creating one is relatively expensive (syscall, stack allocation); the OS can typically only
support a few thousand to tens of thousands of them per machine before memory/scheduler overhead
becomes prohibitive — which is exactly why thread pools (topic 6) exist: reuse a small, bounded
number of expensive platform threads across many tasks.

A **virtual thread** is a `Thread` object whose execution is scheduled by the **JVM**, not the OS,
onto a small pool of platform threads called **carrier threads** (by default, sized to the number
of CPU cores, backed by a `ForkJoinPool` in FIFO mode internally). A virtual thread's stack lives
on the *heap* (as a resizable, initially very small — hundreds of bytes to a few KB — structure),
not as a fixed-size OS-allocated stack, which is exactly why you can have millions of them without
exhausting memory.

### Mounting, unmounting, and why blocking is nearly free

The key mechanism: when a virtual thread's code calls a **blocking** operation the JVM knows about
(I/O, `Thread.sleep`, `java.util.concurrent` locks, blocking socket/file operations), the JVM
**unmounts** it from its carrier thread — the virtual thread's state is saved (its stack frames,
heap-allocated), and the carrier thread is freed to go mount and run a *different* virtual thread.
When the blocking operation completes, the virtual thread is **remounted** onto some available
carrier thread (not necessarily the same one) and resumes exactly where it left off.

```
Carrier thread pool (size ~= CPU cores)
 carrier-1: running VT-42 --- VT-42 calls blocking I/O --- VT-42 unmounts --- carrier-1 picks up VT-107
 carrier-2: running VT-58 --- ...
 carrier-3: idle --- picks up VT-91 --- ...

Meanwhile VT-42 (unmounted, stack saved on heap) waits for I/O completion in the background;
once ready, it's rescheduled onto WHICHEVER carrier thread is free next -- could be carrier-3.
```
This is the entire point: thousands of virtual threads can be **blocked at once** (waiting on
different downstream calls, database queries, etc.) while only a handful of carrier (OS) threads
actually exist, because a blocked virtual thread costs essentially nothing — no OS thread is tied
up sitting idle waiting.

### Structured, simple code instead of reactive/async complexity

The practical payoff: you can write plain, sequential, blocking-style code —
```java
Thread.ofVirtual().start(() -> {
    String data = blockingHttpCall();     // "blocks" -- but only unmounts the virtual thread
    String parsed = parse(data);
    saveToDb(parsed);                     // "blocks" again -- unmounts again
});
```
— and get the throughput characteristics that used to require reactive/async frameworks
(`CompletableFuture` chains, callback-based I/O) specifically to avoid tying up expensive platform
threads on blocking calls. Virtual threads let ordinary, easy-to-read, easy-to-debug (real stack
traces! step-through debuggable!) blocking code scale to huge levels of concurrency instead.

### Creating virtual threads

```java
// One-off virtual thread
Thread vt = Thread.ofVirtual().name("worker-1").start(() -> doWork());

// Executor-style: one new virtual thread PER SUBMITTED TASK, never reused, never pooled
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> doWork());
}   // try-with-resources: close() waits for submitted tasks (AutoCloseable since Java 19+)

// Thread.Builder gives both platform and virtual variants with a shared fluent API
Thread.Builder builder = Thread.ofVirtual().name("req-", 0);   // "req-0", "req-1", ...
```
`Executors.newVirtualThreadPerTaskExecutor()` deliberately has **no pooling and no bounded queue
concept at all** — every `submit()` gets a brand-new virtual thread, run to completion, then
discarded. This is a deliberate philosophy shift from topic 6's thread pool sizing formulas: with
platform threads, you carefully size a *bounded* pool because threads are expensive; with virtual
threads, creation is cheap enough that "one per task, unbounded" is the recommended default — the
**carrier thread pool** (not your code) is what actually stays bounded and reused.

### Pinning — the critical gotcha

A virtual thread can **fail to unmount** when it would otherwise block, "pinning" it to its
carrier thread for the duration of the blocking call — during a pin, the carrier thread is stuck
(can't run any other virtual thread), which defeats the entire scalability benefit for that call.
Two well-known pinning causes:
1. **A `synchronized` block/method that blocks inside it.** As of Java 21, blocking (I/O,
   `Object.wait()`, etc.) *inside* a `synchronized` block pins the carrier thread for that
   duration, because the JVM's monitor implementation is tied to the OS thread, not the virtual
   thread. (This is a known, actively-being-relaxed JDK limitation across releases — check the
   running JDK version's release notes for current status.)
2. **Native code / JNI frames on the call stack** — a virtual thread cannot unmount while native
   code is on its stack, since the JVM can't relocate that portion of execution.

```java
private final Object lock = new Object();
void handleRequest() {
    synchronized (lock) {
        String result = blockingDownstreamCall();   // BUG on a virtual thread: pins the carrier
    }                                                 // for the ENTIRE duration of the call
}
// fix: swap synchronized for java.util.concurrent.locks.ReentrantLock (topic 4) --
// Lock.lock()/unlock() do NOT pin; the virtual thread unmounts normally while blocked
private final ReentrantLock lock = new ReentrantLock();
void handleRequest() {
    lock.lock();
    try {
        String result = blockingDownstreamCall();   // unmounts normally, no pinning
    } finally {
        lock.unlock();
    }
}
```
Run with `-Djdk.tracePinnedThreads=full` (or `short`) to have the JVM print a stack trace every
time a virtual thread is pinned — the standard way to find pinning hotspots in an existing
codebase before migrating hot paths to virtual threads.

### What virtual threads are NOT for

Virtual threads solve **thread-per-blocking-task scalability**. They do **not** speed up CPU-bound
work — a CPU-bound virtual thread still needs a carrier thread mounted the entire time it's
running (there's nothing to unmount from, since it's never blocked), so running thousands of
CPU-bound virtual threads competes for the same small number of CPU cores exactly like thousands of
CPU-bound platform threads would; you gain nothing (and add minor scheduling overhead) by using
virtual threads for a CPU-bound thread pool. They also should **never be pooled** — pooling defeats
their purpose (they're meant to be cheap and disposable; a thread pool exists specifically to
*reuse* expensive threads, which virtual threads are not) and can starve the small number of
carrier threads if a bounded "virtual thread pool" holds onto virtual threads across tasks.

### ASCII diagram — platform threads vs virtual threads under heavy blocking I/O load

```
PLATFORM THREADS (thread pool of ~40, sized for I/O wait ratio, topic 6):
  40 OS threads, each 1MB+ stack -- 10,000 concurrent requests need queueing,
  most requests wait in the pool's queue for a free thread.

VIRTUAL THREADS (Executors.newVirtualThreadPerTaskExecutor()):
  10,000 virtual threads created, one per request -- each "blocks" on I/O by unmounting;
  only ~8 (CPU core count) carrier threads exist and are shared as virtual threads
  unmount/remount around their blocking calls -- no queueing needed, no pool sizing formula needed.
```

## 3. Complexity

| Operation | Time / cost | Notes |
|-----------|-------------|-------|
| `Thread.ofVirtual().start(...)` | ~microseconds, heap allocation only | orders of magnitude cheaper than a platform `Thread` (no OS thread, no fixed-size stack) |
| Unmount on blocking call | O(1), JVM-managed | frees the carrier thread immediately |
| Remount after blocking completes | O(1) + scheduling | may land on a different carrier thread than before |
| CPU-bound work on a virtual thread | same as platform thread | no benefit — still needs a mounted carrier the whole time |
| Pinned blocking call (`synchronized`, native frames) | ties up a whole carrier thread | exactly like a platform thread blocking — the scalability benefit is lost for that call |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/VirtualThreadsDemo.java` —
  creates and runs thousands of virtual threads that each "block" briefly (proving the JVM doesn't
  need thousands of OS threads to do this, via a bounded, fast completion time), demonstrates
  `Executors.newVirtualThreadPerTaskExecutor()`, and reproduces the `synchronized`-pinning gotcha
  versus the `ReentrantLock` fix by tracing carrier-thread reuse.

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Integer>> futures = IntStream.range(0, 10_000)
            .mapToObj(i -> executor.submit(() -> {
                Thread.sleep(10);         // "blocks" -- unmounts the virtual thread, doesn't tie up a carrier
                return i;
            }))
            .toList();
    // all 10,000 tasks complete quickly despite each "blocking" -- proof carrier threads are shared, not per-task
}
```
Expected console output (abbreviated):
```
started 10000 virtual threads, each sleeping 10ms -> all completed in ~<1s wall-clock (not 10000*10ms)
Thread.ofVirtual() thread name/isVirtual() = true
synchronized-based pinning: carrier thread blocked for the whole downstream call (pinning observed)
ReentrantLock-based fix: carrier thread freed during the downstream call (no pinning observed)
```

## 5. When to use / when NOT to use

- Use virtual threads for I/O-bound, blocking-style workloads with high concurrency — request
  handlers making downstream HTTP/DB calls, one virtual thread per incoming request, written as
  plain sequential blocking code.
- Use `Executors.newVirtualThreadPerTaskExecutor()` and let it create one virtual thread per task
  — never wrap virtual threads in a bounded pool; that defeats their purpose.
- Do NOT use virtual threads for CPU-bound work expecting a speedup — they don't parallelize CPU
  work beyond what the underlying core count already allows; a correctly-sized platform thread
  pool (topic 6, `N_cpu+1`) is exactly as fast for pure computation.
- Audit any code migrating to virtual threads for `synchronized` blocks that perform blocking
  calls inside them (the pinning gotcha) — replace with `ReentrantLock` where the blocking call
  can't be avoided; use `-Djdk.tracePinnedThreads=full` to find pinning hotspots.
- Thread-locals still work on virtual threads but are discouraged at large scale (millions of
  virtual threads each holding their own thread-local state can add up in memory) — `ScopedValue`
  (a newer, more virtual-thread-friendly alternative) is worth knowing exists, though a deep dive
  is out of scope here.

## 6. Common pitfalls & gotchas

**Pooling virtual threads like platform threads**:
```java
ExecutorService pool = Executors.newFixedThreadPool(200, Thread.ofVirtual().factory());
// BUG: bounds virtual thread concurrency to 200 "slots" -- defeats the entire point;
// virtual threads are meant to be cheap and disposable, created per task, not reused/pooled
// fix:
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

**Blocking inside `synchronized` on a hot path, expecting virtual-thread scalability**:
```java
synchronized (lock) {
    callSlowDownstreamService();   // BUG: pins the carrier thread for the whole call duration
}
// fix: use java.util.concurrent.locks.ReentrantLock instead -- doesn't pin
```

**Assuming virtual threads make CPU-bound code faster**:
```java
// BUG assumption: "switching this CPU-bound matrix multiplication to virtual threads will speed it up"
// reality: CPU-bound work never unmounts (nothing to block on), so it just competes for the same
// small number of CPU cores as before -- virtual threads add nothing here (and a small scheduling
// overhead), only reworking the algorithm or adding more cores actually helps
```

**Treating `Thread.currentThread().isVirtual()` checks as a substitute for actually testing under
load** — pinning and carrier-thread starvation issues typically only show up under real concurrent
load; low-concurrency manual testing can look fine even with a pinning bug present.

## 7. Interview questions

- [Basic] What is the fundamental difference between a platform thread and a virtual thread? → A
  platform thread maps 1:1 to a real OS thread with its own OS-allocated stack, scheduled by the
  OS kernel; a virtual thread is a JVM-managed unit of execution whose stack lives on the heap and
  is scheduled by the JVM onto a small, reused pool of "carrier" platform threads — you can create
  millions of virtual threads without the memory/OS overhead a million platform threads would
  require. → Follow-up: *Does a virtual thread run without any OS thread at all?* No — it still
  needs to be "mounted" onto a carrier (platform) thread to actually execute; the key difference is
  that it *unmounts* during blocking operations instead of tying up that carrier thread the whole
  time.
- [Basic] Why can you create far more virtual threads than platform threads on the same machine? →
  Platform threads each reserve a real OS thread and a fixed-size OS-allocated stack (often
  512KB-1MB), which the OS and JVM can only support in the thousands before running out of
  memory/scheduler capacity; virtual threads have small, resizable, heap-allocated stacks and don't
  consume a dedicated OS thread at all while blocked, so millions can exist simultaneously, with
  only a small pool of carrier threads actually needed to run the ones that are currently doing
  active (non-blocked) work. → Follow-up: *What determines the default size of the carrier thread
  pool?* By default it's sized to the number of available CPU cores, since carrier threads are only
  needed for actual CPU-bound execution — unmounted (blocked) virtual threads need no carrier at
  all.
- [Basic] How do you create a virtual thread per submitted task using the executor framework? →
  `Executors.newVirtualThreadPerTaskExecutor()` returns an `ExecutorService` that starts a brand
  new virtual thread for every task submitted, runs it to completion, and discards it — no pooling,
  no bounded queue. → Follow-up: *Why does this executor have no configurable pool size, unlike
  ThreadPoolExecutor?* Because virtual threads are meant to be cheap and disposable; pooling/reusing
  them the way you'd pool expensive platform threads defeats their purpose and can artificially cap
  concurrency for no benefit.
- [Intermediate] Explain what happens, step by step, when code running on a virtual thread calls a
  blocking I/O operation. → The JVM recognizes the blocking call as one it knows how to unmount for
  (most blocking I/O and `java.util.concurrent` APIs are already virtual-thread-aware); it saves
  the virtual thread's execution state (its heap-allocated stack) and detaches it from its current
  carrier platform thread, freeing that carrier to mount and run a different, ready virtual thread;
  once the I/O completes, the originally-blocked virtual thread is rescheduled onto *some* available
  carrier thread (not necessarily the same one it started on) and resumes exactly where it left off.
  → Follow-up: *Does the virtual thread's identity (its Thread object, thread-locals) survive being
  moved to a different carrier thread?* Yes — the virtual thread's own identity and state are
  preserved across the unmount/remount; only the underlying carrier (OS) thread executing it can
  change.
- [Intermediate] What is carrier-thread pinning, and name its two classic causes. → Pinning is when
  a virtual thread is unable to unmount during a blocking operation, tying up its carrier thread
  for the entire duration instead — defeating the scalability benefit for that call. The two
  classic causes: (1) blocking inside a `synchronized` block/method (as of Java 21, this pins the
  carrier due to how monitors are implemented), and (2) native code / JNI frames on the call stack,
  which the JVM cannot relocate off the carrier thread. → Follow-up: *How would you find pinning in
  an existing codebase before migrating it to virtual threads?* Run with the JVM flag
  `-Djdk.tracePinnedThreads=full` (or `short`), which prints a stack trace every time a pinning
  event occurs, pinpointing the exact blocking call and enclosing `synchronized` block.
- [Intermediate] Why does replacing `synchronized` with `ReentrantLock` fix the pinning problem? →
  `ReentrantLock.lock()`/`unlock()` are ordinary Java library code built on `AbstractQueuedSynchronizer`,
  fully aware of and compatible with virtual thread unmounting — a virtual thread blocked waiting
  for a `ReentrantLock`, or blocked on I/O while holding one, can unmount normally, freeing its
  carrier; `synchronized`'s monitor mechanism, by contrast, is implemented at a lower level tied to
  the OS thread, so as of Java 21 it cannot release the carrier during a blocking operation
  performed while holding it. → Follow-up: *Does this mean synchronized should never be used with
  virtual threads at all?* Not necessarily — brief, non-blocking critical sections inside
  `synchronized` are fine even on virtual threads (the pin only lasts as long as the block itself,
  which is short); the problem specifically arises when a *blocking* call happens *inside* a
  `synchronized` block on a hot, high-concurrency path.
- [Advanced] Why do virtual threads not improve throughput for CPU-bound workloads, and what
  would actually help there? → A CPU-bound task never calls a blocking operation, so it never
  unmounts — it holds its carrier thread mounted for its entire execution, identical to how a
  platform thread would occupy a CPU core; running thousands of CPU-bound virtual threads just
  means thousands of tasks competing for the same small number of carrier threads (bounded by CPU
  core count), with the same throughput ceiling as an equivalent number of platform threads, plus a
  small amount of extra scheduling overhead from the JVM-level scheduler. What actually helps a
  CPU-bound workload is more CPU cores, better algorithms, or parallelizing the computation itself
  (e.g., via `ForkJoinPool`/parallel streams) — not switching the thread implementation. → Follow-up:
  *Is there ever a legitimate reason to run CPU-bound work on a virtual thread?* Mixed workloads —
  a task that's mostly I/O-bound but has a brief CPU-bound segment doesn't need to be split across
  different thread types; the brief CPU segment just mounts its carrier normally like any other
  code, with no special handling needed, so there's no harm in it, just no special *benefit* either.
- [Advanced] A team migrates a Spring MVC-style, one-thread-per-request platform-thread server to
  virtual threads and doesn't see the expected concurrency improvement under load. What would you
  investigate? → First, check for pinning: enable `-Djdk.tracePinnedThreads=full` and look for
  `synchronized` blocks around blocking calls (a very common source in older code using
  `synchronized` for simple thread-safety around a resource that also happens to do I/O, like a
  legacy JDBC connection wrapper) — every pinned call silently degrades back to platform-thread-like
  carrier occupation. Second, check whether the workload is actually I/O-bound at all — if it's
  secretly CPU-bound (e.g., heavy JSON serialization, in-memory computation dominating request
  time), virtual threads provide no benefit by design, and the bottleneck is core count, not thread
  model. Third, check for an accidentally-reintroduced pool/bound somewhere in the stack (e.g., a
  downstream HTTP client's own internal connection pool or thread pool capping real concurrency
  regardless of how many virtual threads are willing to make requests). → Follow-up: *Why might a
  downstream connection pool cap the benefit even with virtual threads used correctly on the
  server's own request-handling threads?* Because virtual threads solve *thread* scalability, not
  *any other resource's* scalability — if requests ultimately funnel through a fixed-size database
  connection pool or a fixed-size downstream HTTP client pool, that pool's size becomes the real
  concurrency ceiling regardless of how many (cheap) virtual threads are waiting to use it.

## 8. Exercises

No dedicated exercises for this topic — virtual threads are a scheduling/runtime concern layered on
top of the coordination primitives already exercised in
`notes/09-multithreading-concurrency/10-classic-concurrency-problems.md`; the pinning gotcha (avoid
`synchronized` around blocking calls) directly reinforces the lock-choice discussion from topic 4.

## 9. Quick recap

- Virtual threads are JVM-scheduled, heap-stack threads that unmount from their carrier (OS)
  thread during blocking operations instead of tying it up — millions can exist where only
  thousands of platform threads would fit.
- `Executors.newVirtualThreadPerTaskExecutor()` creates one virtual thread per task, unbounded, by
  design — never pool virtual threads.
- They let plain, sequential, blocking-style code scale like reactive/async code used to require
  specifically to avoid — with real, debuggable stack traces.
- Pinning (carrier thread stuck despite a "blocking" call) happens inside `synchronized` blocks
  that block, and with native/JNI frames on the stack — replace `synchronized` with `ReentrantLock`
  on hot paths that must call blocking code while holding a lock.
- Virtual threads do not speed up CPU-bound work — they solve thread-per-blocking-task scalability,
  not computation throughput.
