# Thread lifecycle & states; Thread vs Runnable vs Callable

## 1. What it is

A Java `Thread` is the JVM's unit of concurrent execution, mapped (on the platform-thread path) to
an OS thread. Every thread moves through a well-defined set of states over its life —
`Thread.State` — and the JVM exposes exactly which state a thread is in via `getState()`. `Thread`,
`Runnable`, and `Callable` are the three ways to describe "the work a thread should run," each with
different capabilities around return values and checked exceptions.

## 2. How it works internally

### The six `Thread.State` values and what drives each transition

```
                         start()
   NEW  ------------------------------------------->  RUNNABLE
                                                       (ready or
                                                        actually running --
                                                        the JVM does not
                                                        distinguish these
                                                        two as separate
                                                        states)
                                                          |   ^
                          synchronized block/method       |   |
                          contended (waiting to enter) ---+   |  lock acquired
                                                          |   |  after
                                                          v   |  notify/notifyAll
                                                       BLOCKED
                                                          ^
                                                          | (Object.wait() releases
                                                          |  the lock and parks here;
                                                          |  notify/notifyAll moves the
                                                          |  thread OUT of WAITING and
                                                          |  into BLOCKED until it
                                                          |  re-acquires the lock)
                                                          |
   RUNNABLE  --- Object.wait() -------------------->  WAITING
             --- Thread.join()  (no timeout) ------->  WAITING
             --- LockSupport.park() ---------------->  WAITING
             --- Object.wait(ms) ------------------->  TIMED_WAITING
             --- Thread.join(ms) ------------------->  TIMED_WAITING
             --- Thread.sleep(ms) ------------------>  TIMED_WAITING
             --- LockSupport.parkNanos(ms) --------->  TIMED_WAITING

   RUNNABLE  --- run() method returns / exception escapes run() ----> TERMINATED
```

- **NEW** — a `Thread` object has been constructed but `start()` has not been called. No OS thread
  exists yet.
- **RUNNABLE** — `start()` has been called. The JVM makes no distinction between "eligible to run,
  waiting for CPU time from the OS scheduler" and "actually executing on a core right now" — both
  are reported as `RUNNABLE`. This is a common interview trap: `RUNNABLE` does **not** mean
  "currently running."
- **BLOCKED** — the thread is trying to enter a `synchronized` block/method and another thread
  currently holds that monitor. Also the state a `WAITING`/`TIMED_WAITING` thread passes through
  after being woken by `notify()`/`notifyAll()` but before it has re-acquired the lock (see topic
  2) — waking up from `wait()` does not mean "running again," it means "eligible to compete for the
  lock again."
- **WAITING** — the thread is waiting indefinitely for another thread to do something: it called
  `Object.wait()` (no timeout), `Thread.join()` (no timeout), or `LockSupport.park()`. It will not
  become `RUNNABLE` again until explicitly `notify()`/`notifyAll()`'d, the joined thread
  terminates, or `LockSupport.unpark()` is called.
- **TIMED_WAITING** — same as `WAITING` but with a bound: `Thread.sleep(ms)`,
  `Object.wait(ms)`, `Thread.join(ms)`, `LockSupport.parkNanos/parkUntil`. Returns to `RUNNABLE`
  either when the timeout elapses or (for `wait`/`join`) when signaled early.
- **TERMINATED** — `run()` has returned (normally or via an uncaught exception). A terminated
  thread cannot be restarted — calling `start()` again throws `IllegalThreadStateException`.

### `interrupt()` — a cooperative signal, not a forceful stop

`Thread.interrupt()` sets an internal boolean "interrupt status" flag on the target thread. It does
**not** forcibly stop anything. Two different things can happen:
- If the target thread is currently blocked in an interruptible wait (`Object.wait`, `Thread.sleep`,
  `Thread.join`, `Lock.lockInterruptibly()`, many blocking I/O/NIO calls), that call **throws
  `InterruptedException` immediately** and **clears** the interrupt status flag back to `false` as
  part of throwing.
- If the target thread is running normal code (not blocked in one of those calls), interrupting it
  only sets the flag; the running code must **poll** `Thread.currentThread().isInterrupted()` (or
  the static `Thread.interrupted()`, which also clears the flag) and decide to stop itself.
  Interrupting a thread blocked on a `synchronized` monitor entry (i.e. `BLOCKED`) has **no
  effect** — `synchronized` acquisition is not interruptible; use `Lock.lockInterruptibly()`
  instead if you need that.

**The cardinal sin**: catching `InterruptedException` and doing nothing (`catch (InterruptedException e) {}`).
This silently swallows the cancellation signal — the thread just keeps going as if nothing happened,
and any outer code polling the interrupt flag will never see it (the flag was already cleared by the
exception). Correct handling is either to let the exception propagate (declare `throws
InterruptedException`), or if it must be caught, **restore the flag** before continuing:
`Thread.currentThread().interrupt();` so callers further up can still observe that a cancellation
was requested.

### `Thread` vs `Runnable` vs `Callable<V>`

- **`Runnable`** — a functional interface with `void run()`. No return value, and `run()` cannot
  declare or throw any checked exception (only unchecked). It represents "fire-and-forget work."
- **`Callable<V>`** — a functional interface with `V call() throws Exception`. It **can** return a
  result and **can** throw a checked exception. It exists specifically because `Runnable` can't do
  either, and was introduced alongside the Executor framework (`ExecutorService.submit(Callable<V>)`
  returns a `Future<V>` you can `get()` the result/exception from — see topic 6).
- **`Thread`** itself **implements `Runnable`** (`public class Thread implements Runnable`). You
  can either subclass `Thread` and override `run()`, or (the recommended approach) construct a
  plain `Thread` and pass it a `Runnable`:
  ```java
  Thread t1 = new Thread(() -> System.out.println("via Runnable"));
  ```
  **Prefer composition (pass a `Runnable`) over inheritance (extend `Thread`)**: Java has single
  inheritance, so extending `Thread` burns your one superclass slot and couples "what work to do"
  to "how it's executed" (a `Thread` subclass can't also extend some other base class, and can't be
  handed to an `ExecutorService` as reusable *work* — an `ExecutorService` manages `Thread`s
  itself). Passing a `Runnable`/`Callable` keeps the task description independent of the execution
  mechanism, so the exact same task object can run on a raw `Thread`, inside an `ExecutorService`,
  or scheduled — this separation of concerns is the standard justification interviewers look for.

### Daemon vs non-daemon threads

Every thread is either a **user thread** (non-daemon, default) or a **daemon thread**
(`thread.setDaemon(true)`, must be called **before** `start()`). The JVM keeps running as long as
at least one non-daemon thread is alive; it exits (killing all remaining daemon threads
immediately, mid-execution, with no cleanup) the moment the *last* non-daemon thread finishes.
Daemon threads are for background/support work that should never keep the JVM alive by itself
(e.g., a periodic cache-refresher) — never put anything that must complete cleanly (like flushing
a file) in a daemon thread's shutdown path, because it can be killed at an arbitrary instruction.

### ASCII diagram — the six states end to end

See the transition diagram above; the key exam-relevant facts to remember: `RUNNABLE` conflates
"ready" and "running," `BLOCKED` is specifically about monitor entry contention (not all waiting),
and a thread woken from `WAITING`/`TIMED_WAITING` by `notify()` still has to pass through
`BLOCKED` to re-acquire the lock before it's truly `RUNNABLE` again.

## 3. Complexity

| Operation | Cost | Notes |
|-----------|------|-------|
| `new Thread(...)` | O(1), but allocates a real OS thread stack (~512KB-1MB default) on `start()` | expensive relative to a method call — this is why thread pools exist (topic 6) |
| `Thread.getState()` | O(1) | a live snapshot; can be stale the instant after it's read |
| `interrupt()` | O(1) | just sets a flag (or triggers an immediate throw if blocked in an interruptible call) |
| `join()` / `join(ms)` | blocks the caller | O(1) bookkeeping; wall-clock cost is "however long the target thread takes" |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/ThreadLifecycleDemo.java` —
  creates threads via `Runnable` and via `Callable` (through `ExecutorService`), observes
  `getState()` at a few points, demonstrates a bounded `join(timeout)`, and demonstrates a sleeping
  thread correctly handling `interrupt()`.

```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();  // restore the flag, don't swallow it
        System.out.println("worker: interrupted while sleeping, exiting");
    }
});
System.out.println("state before start(): " + t.getState());   // NEW
t.start();
Thread.sleep(50);
System.out.println("state shortly after start(): " + t.getState()); // TIMED_WAITING (sleeping)
t.interrupt();
t.join(2000);
System.out.println("state after join: " + t.getState());        // TERMINATED
```
Expected console output (abbreviated, first two lines are always exactly this; later lines may
vary slightly in wording but the state values are deterministic):
```
state before start(): NEW
state shortly after start(): TIMED_WAITING
worker: interrupted while sleeping, exiting
state after join: TERMINATED
```

## 5. When to use / when NOT to use

- Use `Runnable` for fire-and-forget work with no result and no checked exceptions to propagate —
  the vast majority of `ExecutorService.execute(...)` submissions.
- Use `Callable<V>` when the task produces a result you need back, or needs to throw a checked
  exception that the caller should observe via `Future.get()`'s `ExecutionException`.
- Prefer constructing a plain `Thread`/submitting to an `ExecutorService` with a `Runnable`/`Callable`
  over subclassing `Thread` — reserve subclassing `Thread` for the rare case where you genuinely
  need to override other `Thread` behavior itself (almost never in application code).
- Never rely on thread **priority** (`setPriority`) for correctness — it's only a scheduling hint,
  and its effect is platform/OS-dependent; use proper synchronization instead of trying to bias
  scheduling order.

## 6. Common pitfalls & gotchas

**Calling `run()` instead of `start()`**:
```java
Thread t = new Thread(() -> System.out.println(Thread.currentThread().getName()));
t.run();     // BUG: runs synchronously on the CALLING thread, no new thread is ever created
// fix:
t.start();   // starts a genuinely new thread which then invokes run()
```

**Swallowing `InterruptedException`**:
```java
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // BUG: silently ignored — cancellation signal is lost, flag already cleared by the throw
}
// fix:
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // restore the flag for callers to observe
    return;                               // and actually stop what you were doing
}
```

**Assuming `RUNNABLE` means "currently executing on a CPU core"** — it only means "eligible to
run," which the OS scheduler may or may not have actually granted a core to at this instant.

**Restarting a terminated thread**:
```java
Thread t = new Thread(() -> {});
t.start();
t.join();
t.start();   // BUG: throws IllegalThreadStateException — a Thread object is single-use
```

## 7. Interview questions

- [Basic] What are the six `Thread.State` values? → `NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`,
  `TIMED_WAITING`, `TERMINATED`. → Follow-up: *Does `RUNNABLE` mean the thread is currently
  executing?* Not necessarily — it means it's eligible to run; the JVM doesn't expose a separate
  "actually on a CPU right now" state.
- [Basic] What's the difference between `Runnable` and `Callable`? → `Runnable.run()` returns
  nothing and can't throw checked exceptions; `Callable<V>.call()` returns a `V` and can throw a
  checked `Exception`, which is why `ExecutorService.submit(Callable)` returns a `Future<V>` you can
  `get()` a result or exception from. → Follow-up: *Can you submit a Runnable to
  ExecutorService.submit() too?* Yes — there's an overload that wraps it and returns a
  `Future<?>` whose `get()` just returns `null` on success.
- [Basic] Why do we prefer implementing `Runnable` over extending `Thread`? → Java has single
  inheritance, so extending `Thread` uses up your one superclass slot and tightly couples "what to
  run" with "how it runs"; a plain `Runnable`/`Callable` can be handed to a raw `Thread`, an
  `ExecutorService`, or a scheduler interchangeably. → Follow-up: *Is there ever a good reason to
  extend Thread?* Rarely — only if you genuinely need to override `Thread`'s own behavior itself,
  not just supply work for it to run.
- [Basic] What does calling `.run()` directly instead of `.start()` do? → It executes the code
  synchronously on the calling thread — no new thread is created at all, defeating the entire
  purpose. → Follow-up: *Would getState() still report NEW after calling run() directly?* Yes,
  since `start()` was never called, the Thread object's state machine never advances past `NEW`.
- [Basic] What happens if you call `start()` twice on the same `Thread` object? → Throws
  `IllegalThreadStateException` — a `Thread` instance can only be started once; once it's
  `TERMINATED` (or even mid-run) it cannot be restarted. → Follow-up: *How would you run the same
  logic again?* Construct a new `Thread` instance with the same `Runnable`/`Callable`.
- [Intermediate] What exactly does `interrupt()` do, and what does it NOT do? → It sets a boolean
  interrupt-status flag on the target thread; if the thread is currently blocked in an
  interruptible call (`sleep`, `wait`, `join`, `lockInterruptibly`, some blocking I/O), that call
  throws `InterruptedException` immediately and clears the flag. It does **not** forcibly stop or
  kill the thread — code running normal (non-blocking) logic must proactively check
  `isInterrupted()` and choose to stop. → Follow-up: *Does interrupt() do anything to a thread
  BLOCKED trying to enter a synchronized block?* No — monitor entry via `synchronized` is not
  interruptible; use `Lock.lockInterruptibly()` if you need that capability.
- [Intermediate] Why is swallowing `InterruptedException` (catching it and doing nothing)
  considered a serious bug? → Because the act of throwing `InterruptedException` already cleared
  the interrupt flag, so if you also do nothing in the catch block, the entire cancellation signal
  is lost — no code anywhere can now tell the thread was asked to stop, and the thread just
  continues as if uninterrupted. → Follow-up: *What are the two correct ways to handle it?* Either
  let it propagate (`throws InterruptedException` on your method) so the caller decides, or if you
  must catch it locally, call `Thread.currentThread().interrupt()` to restore the flag before
  continuing/returning.
- [Intermediate] What is the difference between `BLOCKED` and `WAITING`? → `BLOCKED` specifically
  means the thread is trying to enter a `synchronized` block/method and another thread currently
  holds that monitor; `WAITING` means the thread itself voluntarily gave up running (via
  `wait()`/`join()`/`park()` with no timeout) until something else wakes it. → Follow-up: *Can a
  thread pass through BLOCKED on its way out of WAITING?* Yes — after `notify()`/`notifyAll()`
  wakes a waiting thread, it must re-acquire the monitor's lock before `wait()` can return, so it
  transitions to `BLOCKED` first if the lock isn't immediately available.
- [Intermediate] Why does the JVM keep running while daemon threads are alive but exit when only
  daemon threads remain? → The JVM's shutdown policy is defined in terms of non-daemon ("user")
  threads: as long as at least one is alive, the process stays up; daemon threads are meant purely
  for background support work, so the JVM exits (abruptly killing any remaining daemon threads mid
  execution, with no guaranteed cleanup) once the last non-daemon thread finishes. → Follow-up:
  *What's a concrete bug this causes?* Putting essential cleanup (like flushing a file or
  completing a database write) inside a daemon thread — it can be killed at an arbitrary point
  when the JVM exits, silently losing that work.
- [Advanced] Why is thread creation considered expensive, and how does that motivate the Executor
  framework (topic 6)? → Each platform `Thread` maps 1:1 to a real OS thread with its own
  kernel-scheduled stack (often ~512KB-1MB by default), so creating thousands of short-lived
  threads wastes memory and burns time on OS-level thread creation/teardown and context-switch
  overhead; reusing a bounded pool of long-lived worker threads (topic 6) amortizes that cost across
  many tasks instead of paying it per task. → Follow-up: *Does this same cost apply to virtual
  threads (topic 11)?* No — virtual threads are deliberately cheap to create (JVM-managed, no
  dedicated OS thread per virtual thread), which is exactly why "one virtual thread per task,
  never pool them" becomes a reasonable default in that model.

## 8. Exercises

No dedicated exercises for this topic — see `notes/09-multithreading-concurrency/10-classic-problems.md`
and `notes/09-multithreading-concurrency/12-build-it-yourself.md` for hands-on practice.

## 9. Quick recap

- Six states: `NEW → RUNNABLE ⇄ BLOCKED`, `RUNNABLE → WAITING/TIMED_WAITING → BLOCKED → RUNNABLE`,
  `RUNNABLE → TERMINATED`. `RUNNABLE` means "eligible to run," not "currently executing."
- `Runnable.run()`: no return value, no checked exceptions. `Callable<V>.call()`: returns `V`, can
  throw checked `Exception` — pairs with `ExecutorService.submit()` and `Future<V>`.
- Prefer passing a `Runnable`/`Callable` to a `Thread`/`ExecutorService` over subclassing `Thread`
  — keeps "what to run" decoupled from "how it runs."
- `interrupt()` is cooperative, not forceful: it either throws `InterruptedException` from an
  interruptible blocking call (clearing the flag) or just sets a flag that running code must poll.
  Never swallow `InterruptedException` silently — restore the flag or propagate it.
- Daemon threads never keep the JVM alive and can be killed mid-instruction on JVM exit — never
  put essential cleanup there.
