# synchronized, intrinsic locks, wait/notify

## 1. What it is

`synchronized` is Java's built-in mutual-exclusion mechanism: every object carries an implicit
**monitor** (intrinsic lock), and `synchronized` blocks/methods acquire that monitor for the
duration of the critical section, guaranteeing at most one thread runs inside at a time.
`Object.wait()`/`notify()`/`notifyAll()` are the companion coordination primitives that let a
thread holding the monitor voluntarily give it up while waiting for some condition, and let
another thread wake it back up once that condition becomes true.

## 2. How it works internally

### Monitors and `monitorenter`/`monitorexit`

Every Java object has an associated monitor (conceptually a mutex + a wait-set of parked
threads). `synchronized(obj) { ... }` compiles to `monitorenter obj` before the block and
`monitorexit obj` after it (the JVM also inserts a `monitorexit` on the exceptional exit path, so
release is automatic even if the block throws — unlike `Lock.unlock()`, see topic 4).
`synchronized` **instance methods** lock `this`; `synchronized` **static methods** lock the
`Class` object (`ClassName.class`) — a common gotcha: a synchronized instance method and a
synchronized static method on the same class do **not** exclude each other, because they lock two
different objects.

```
Thread A: synchronized(lockObj) { ... }     Thread B: synchronized(lockObj) { ... }

  A: monitorenter lockObj -> acquired (owner=A, count=1)
  B: monitorenter lockObj -> lockObj already owned by A -> B blocks (state: BLOCKED)
  A: monitorexit  lockObj -> owner=null, count=0
  B: (unblocked)  monitorenter lockObj -> acquired (owner=B, count=1)
```

### Reentrancy

Intrinsic locks are **reentrant**: if thread A already owns `lockObj`'s monitor and calls another
`synchronized(lockObj)` block (directly, or via a method call), the JVM just increments an
internal hold count instead of blocking A on its own lock — otherwise a method calling another
synchronized method on the same object would deadlock itself. Each `monitorexit` decrements the
count; the lock is only truly released when the count returns to zero.

### `wait()` / `notify()` / `notifyAll()` — the one wait-set per object

These three methods live on `Object` (not `Thread`) because they operate on the calling object's
monitor, and **must be called while holding that monitor** — calling `wait()`/`notify()` without
owning the lock throws `IllegalMonitorStateException`.

- **`wait()`** — atomically releases the monitor and parks the calling thread on the object's
  **wait-set**, moving it to `WAITING` (or `TIMED_WAITING` for `wait(ms)`). "Atomically" is the key
  word: there is no window where the thread has released the lock but not yet started waiting (or
  vice versa) — otherwise a `notify()` could slip through and be lost between release and park.
- **`notify()`** — wakes **one arbitrary** thread from the wait-set (the JVM does not guarantee
  FIFO order). The woken thread does not resume immediately — it must first **re-acquire the
  monitor**, so it moves from `WAITING` to `BLOCKED` until the notifying thread releases the lock
  (typically by exiting the `synchronized` block), at which point it competes for the lock like
  any other blocked thread.
- **`notifyAll()`** — wakes **every** thread in the wait-set; they all then compete to re-acquire
  the monitor one at a time. Necessary whenever the wait-set can contain threads waiting on
  logically *different* conditions on the same object (see the pitfall below) — `notify()` might
  wake the "wrong" kind of waiter, which then goes back to sleep, while the thread that actually
  needed waking never gets notified.

### Why `wait()` must always be called in a `while` loop, never `if`

Two independent reasons force re-checking the condition after `wait()` returns, in a loop:
1. **Spurious wakeups** — the JVM spec explicitly permits `wait()` to return without any
   `notify()`/`notifyAll()`/timeout ever happening (a rare artifact of how it's implemented on some
   platforms). Code that assumes "I was notified, so my condition must be true now" is technically
   incorrect even if it never fails in practice on a given JVM/OS.
2. **Multiple waiters, one condition, delayed re-check** — with `notifyAll()`, several threads wake
   up but only one can re-acquire the lock and proceed first; by the time the *next* one gets the
   lock, the condition that was true when notified might no longer hold (another thread got there
   first and already consumed/changed the resource).

```java
synchronized (lock) {
    while (!conditionIsTrue()) {   // NEVER if (...) — always while (...)
        lock.wait();
    }
    // safe to proceed: condition is guaranteed true right here
}
```

### ASCII diagram — a bounded buffer's producer/consumer lifecycle on one monitor

```
                 synchronized(monitor)
   producer -----------------------------> [monitor owned by producer]
      buffer full? --yes--> monitor.wait()  -- releases lock, joins wait-set, state=WAITING
                                                        |
   consumer -----------------------------> [monitor owned by consumer]
      take an item, buffer now has room
      monitor.notifyAll()  -- wakes ALL waiters (producers AND consumers on ONE wait-set)
      exits synchronized block -- releases lock
                                                        |
      each woken thread: WAITING -> BLOCKED (competing to re-acquire) -> RUNNABLE (re-checks while)
```
This single shared wait-set is exactly the limitation `Lock`+`Condition` (topic 4) fixes: with
`synchronized`, every waiter — producer or consumer — sits in the *same* wait-set, so
`notifyAll()` is usually required (wasting wakeups on the wrong kind of waiter); a `Condition` per
role lets you `signal()` precisely.

## 3. Complexity

| Operation | Time (uncontended) | Notes |
|-----------|--------------------|-------|
| `synchronized` acquire/release | O(1) | JVM biased/thin-lock fast path when uncontended; inflates to a heavyweight OS mutex only under real contention |
| reentrant re-acquire (same thread) | O(1) | just increments the hold count, no blocking |
| `wait()` | blocks until notified/timeout | releases the monitor while parked |
| `notify()` | O(1) | wakes one arbitrary waiter; wake-up is not synchronous — woken thread still must re-acquire the lock |
| `notifyAll()` | O(n) wake-ups | n = wait-set size; all compete for the lock afterward |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/SynchronizedWaitNotifyDemo.java`
  — proves reentrancy with a hold-count assertion, shows a static-vs-instance-lock non-exclusion
  gotcha, and implements a wait/notifyAll bounded buffer with a deterministic producer/consumer
  handoff.

```java
private final Object lock = new Object();
private final Deque<Integer> buffer = new ArrayDeque<>();
private final int capacity = 2;

void put(int item) throws InterruptedException {
    synchronized (lock) {
        while (buffer.size() == capacity) {
            lock.wait();
        }
        buffer.addLast(item);
        lock.notifyAll();
    }
}
```
Expected console output (abbreviated):
```
reentrant synchronized: nested call succeeded without self-deadlock, hold count observed = 2
static lock vs instance lock: both entered concurrently = true (different monitors, no exclusion)
wait/notifyAll bounded buffer: producer put 5 items, consumer took 5 items, buffer empty at end = true
```

## 5. When to use / when NOT to use

- Use `synchronized` for straightforward mutual exclusion where you don't need `tryLock`,
  timeouts, interruptibility, fairness, or multiple wait-sets — it's simpler and release is
  automatic (see topic 4 for when to reach for `ReentrantLock`/`Condition` instead).
- Use `wait()`/`notify()`/`notifyAll()` only when you're implementing your own low-level
  coordination primitive; for almost all application code, prefer higher-level constructs —
  `BlockingQueue` (topic 9), `CountDownLatch`/`Semaphore`/`CyclicBarrier` (topic 8), or
  `Condition` (topic 4) — which get the subtle wait-set semantics right for you.
- Prefer `notifyAll()` over `notify()` unless you can prove every waiter on that monitor is
  waiting on the exact same condition and waking any one of them is always correct — `notify()`'s
  "pick an arbitrary waiter" behavior is a common source of lost-wakeup bugs when the wait-set is
  mixed.
- Never use `synchronized` for coordination across process boundaries or for long-held locks that
  span I/O — a thread blocked on I/O while holding a monitor stalls every other thread waiting on
  it.

## 6. Common pitfalls & gotchas

**Calling `wait()`/`notify()` without holding the lock**:
```java
Object lock = new Object();
lock.wait();   // BUG: throws IllegalMonitorStateException -- must be inside synchronized(lock)
// fix:
synchronized (lock) {
    lock.wait();
}
```

**Using `if` instead of `while` around `wait()`**:
```java
synchronized (lock) {
    if (buffer.isEmpty()) {     // BUG: spurious wakeup or a second waiter beating you to the item
        lock.wait();            // can leave `buffer.isEmpty()` true right after wait() returns
    }
    return buffer.removeFirst();   // may throw NoSuchElementException
}
// fix: while (buffer.isEmpty()) { lock.wait(); }
```

**Using `notify()` when the wait-set holds threads waiting on different conditions**:
```java
// producers AND consumers both wait() on the SAME lock/condition
synchronized (lock) {
    // ... consumer just freed a slot ...
    lock.notify();   // BUG: might wake ANOTHER waiting consumer instead of a waiting producer,
}                     // which re-checks, finds nothing to do, and goes right back to wait()
// fix: lock.notifyAll();  (or use two separate Conditions -- topic 4)
```

**Locking on a mutable or interned/shared object accidentally**:
```java
synchronized ("shared-key") { ... }   // BUG: String literals are interned -- every place in the
                                       // JVM using the same literal shares this exact monitor,
                                       // causing unrelated code to contend with each other
// fix: use a private, dedicated final Object (or `this`) as the lock
private final Object lock = new Object();
```

**Deadlocking two synchronized methods that lock `this` and are called in opposite object order
from two threads** — see topic 10 for the full deadlock scenario, detection, and the lock-ordering
fix.

## 7. Interview questions

- [Basic] What does `synchronized` actually lock — the method, or something else? → It locks the
  monitor of an *object*: for an instance method, that's `this`; for a static method, it's the
  `Class` object; for `synchronized(obj) { }`, it's whatever `obj` refers to. → Follow-up: *Do a
  synchronized instance method and a synchronized static method on the same class exclude each
  other?* No — they lock two different objects (`this` vs the `Class` object), so they can run
  concurrently.
- [Basic] Why must `wait()`/`notify()`/`notifyAll()` be called while holding the object's monitor?
  → Because the JVM must atomically release the lock and register the thread on the wait-set (for
  `wait()`), or atomically inspect the wait-set while no one else can concurrently modify it (for
  `notify`) — doing this without holding the lock would race; calling them without the lock throws
  `IllegalMonitorStateException`. → Follow-up: *What does wait() do to the lock while the thread is
  parked?* It fully releases it (including any reentrant hold count), so other threads can acquire
  it — this is different from `Thread.sleep()`, which holds any locks it has the whole time.
- [Basic] What's the difference between `notify()` and `notifyAll()`? → `notify()` wakes one
  arbitrary thread from the wait-set; `notifyAll()` wakes every thread in it, and they all then
  compete to re-acquire the lock one at a time. → Follow-up: *When is notify() safe to use?* Only
  when every thread in the wait-set is waiting on the exact same condition, so waking any one of
  them is always correct.
- [Intermediate] Why must the condition around `wait()` be re-checked in a `while` loop instead of
  an `if`? → Two reasons: the JVM permits spurious wakeups (returning from `wait()` with no
  notify/timeout at all), and with multiple waiters woken by `notifyAll()`, the condition that was
  true when notified may already be consumed by the time a particular thread actually re-acquires
  the lock. → Follow-up: *Give a concrete failure with `if`.* Two consumers both wait() on an empty
  buffer; a producer adds one item and calls notifyAll(); both consumers wake, but only one
  re-acquires the lock first and removes the item; the second, using `if`, proceeds to
  `removeFirst()` on the now-empty buffer and throws.
- [Intermediate] Explain reentrancy for intrinsic locks with a concrete example. → If thread A
  holds `lockObj`'s monitor and, while still inside that `synchronized` block, calls another method
  that also does `synchronized(lockObj)`, the JVM recognizes A already owns it and just increments
  an internal hold count rather than blocking A on its own lock; each exit decrements the count,
  and the lock is released to other threads only once the count returns to zero. → Follow-up: *What
  would happen without reentrancy?* A synchronized method calling another synchronized method on
  the same object (a very common pattern) would self-deadlock every time.
- [Intermediate] Why is locking on a `String` literal or a boxed `Integer` dangerous? → String
  literals are interned by the JVM, and small boxed `Integer`s are cached (`Integer.valueOf`
  cache, -128..127) — so two completely unrelated pieces of code that happen to synchronize on
  `"same-literal"` or `Integer.valueOf(1)` are unknowingly sharing the exact same monitor, causing
  spurious contention or, worse, subtle correctness bugs if one side assumes exclusive ownership. →
  Follow-up: *What should you lock on instead?* A `private final Object lock = new Object();`
  dedicated purely to that critical section, guaranteed not to be shared with unrelated code.
- [Advanced] Walk through exactly what happens, state by state, when thread A calls `notifyAll()`
  and there are three threads waiting. → All three move out of `WAITING` on the object's wait-set;
  each then attempts to re-acquire the monitor, but only one can hold it at a time, so the other
  two transition to `BLOCKED` until it's their turn; as each thread eventually acquires the lock,
  it resumes right after its `wait()` call, re-checks its `while` condition, and either proceeds or
  calls `wait()` again if the condition no longer holds for it. → Follow-up: *Does notifyAll()
  guarantee all three get a turn immediately, one after another, before any new external
  contender can acquire the lock?* No — once the monitor is free, any thread (including a fresh one
  not in the original wait-set) can win the race to acquire it; there's no guaranteed priority for
  previously-waiting threads.
- [Advanced] Compare the "thundering herd" cost of `notifyAll()` on a single shared monitor versus
  using two `Condition`s (topic 4) for producers and consumers separately. → With one monitor and
  `notifyAll()`, every waiter (regardless of role) wakes up, and most immediately find their own
  condition still false and go back to `wait()` — CPU and scheduling overhead wasted on doomed
  wakeups, scaling with total waiter count. Two `Condition`s let you `signal()` (not `signalAll()`)
  exactly the role that should proceed, so only the relevant single waiter wakes. → Follow-up: *So
  why not always use Condition instead of synchronized/wait/notify?* `Condition` requires
  `ReentrantLock` (manual lock/unlock, more verbose, no automatic release on exception) — for
  simple, single-condition coordination, plain `synchronized`/`wait`/`notifyAll` is simpler and
  sufficient; reach for `Condition` specifically when you have genuinely distinct wait conditions.

## 8. Exercises

No dedicated exercises for this topic — see `notes/09-multithreading-concurrency/10-classic-concurrency-problems.md`
for the wait/notify-based producer-consumer exercise built directly on these primitives.

## 9. Quick recap

- `synchronized` locks an object's monitor: instance methods lock `this`, static methods lock the
  `Class` object, `synchronized(obj)` locks whatever `obj` is — automatic release on any exit path,
  including exceptions.
- Intrinsic locks are reentrant: the same thread re-entering a `synchronized` block it already
  owns just increments a hold count instead of blocking itself.
- `wait()`/`notify()`/`notifyAll()` require holding the lock, operate on one wait-set per object,
  and `wait()` atomically releases the lock while parking.
- Always re-check the condition in a `while` loop after `wait()` returns — spurious wakeups and
  multiple competing waiters both make `if` unsafe.
- Prefer `notifyAll()` unless every waiter shares the exact same condition; a mixed wait-set with
  `notify()` risks waking the wrong kind of thread and losing a wakeup.
