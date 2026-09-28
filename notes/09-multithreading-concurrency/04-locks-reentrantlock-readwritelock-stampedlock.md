# ReentrantLock, ReadWriteLock, StampedLock, Condition

## 1. What it is

`java.util.concurrent.locks` gives you explicit, programmable locking as an alternative to the
implicit `synchronized` keyword. `ReentrantLock` is a drop-in replacement for `synchronized` with
extra capabilities (tryLock, timeouts, interruptibility, fairness, multiple wait-sets via
`Condition`). `ReadWriteLock` splits locking into a shared read lock and an exclusive write lock
for read-heavy data. `StampedLock` goes further with a lock-free **optimistic read** mode for
even lower overhead on read-mostly workloads.

## 2. How it works internally

### ReentrantLock vs synchronized

`synchronized` is a JVM-level intrinsic lock: the compiler emits `monitorenter`/`monitorexit`
bytecodes, acquisition/release is automatic (even on exception, via implicit unlock), but you get
no control over *how* you wait for it. `ReentrantLock` is a **library-level** lock built on
`AbstractQueuedSynchronizer` (AQS) — an internal `int state` field (0 = free, N = held with N
reentrant holds) updated via CAS, plus a FIFO wait queue of blocked threads (a doubly linked list
of `Node`s, each parking its thread with `LockSupport.park()`).

```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();   // MUST be in finally — no automatic unlock like synchronized
}
```

Extra capabilities over `synchronized`:
- `tryLock()` — returns immediately, `true`/`false`, never blocks.
- `tryLock(timeout, unit)` — blocks up to a bound, then gives up (returns `false`). This is the
  #1 reason to prefer `ReentrantLock` over `synchronized` for deadlock **avoidance** — a thread
  that can't get a lock in time can back off instead of blocking forever (see topic 10, deadlock).
- `lockInterruptibly()` — a thread blocked waiting can be interrupted and abandon the attempt
  (`synchronized` blocking is NOT interruptible).
- **Fairness**: `new ReentrantLock(true)` makes the lock FIFO — the longest-waiting thread gets it
  next. Default (`false`, "barging" / unfair) lets a thread that just arrives sometimes jump the
  queue ahead of threads already parked, which is faster in practice (avoiding the cost of
  waking a parked thread) but can starve a particular thread under sustained contention. Fair
  locks trade throughput for predictability — rarely worth it unless starvation is an observed
  problem.
- **Reentrancy**: same as `synchronized` — the owning thread can re-acquire without blocking;
  `getHoldCount()` tracks nesting depth; `isHeldByCurrentThread()` for assertions/debugging.

### Condition — multiple wait-sets per lock

`synchronized` gives every object exactly **one** implicit wait-set (`wait`/`notify`/`notifyAll`
all operate on it — see topic 2). `Lock.newCondition()` lets you create as many independent
wait-sets as you need on a *single* lock. Classic use case — a bounded buffer needs two distinct
conditions: producers wait on `notFull`, consumers wait on `notEmpty`. With a single monitor,
`notifyAll()` wakes *every* waiter (producers AND consumers), most of which immediately re-check
their condition and go back to sleep — wasted wakeups (a "thundering herd"). With two `Condition`s
on the same `ReentrantLock`, `notFull.signal()` wakes only a producer, `notEmpty.signal()` wakes
only a consumer — no wasted wakeups, no herd.

```java
private final Lock lock = new ReentrantLock();
private final Condition notFull  = lock.newCondition();
private final Condition notEmpty = lock.newCondition();

void put(T item) throws InterruptedException {
    lock.lock();
    try {
        while (isFull()) notFull.await();     // await() == wait(), condition-scoped
        enqueue(item);
        notEmpty.signal();                    // wake ONE consumer, not everyone
    } finally { lock.unlock(); }
}
```
Same spurious-wakeup rule as `wait()`: always re-check the condition in a `while` loop, never `if`.

### ReadWriteLock / ReentrantReadWriteLock

Splits a single logical lock into a **read lock** (shared — any number of readers can hold it
simultaneously) and a **write lock** (exclusive — one writer, and no readers, at a time).
Internally `ReentrantReadWriteLock` packs both counts into one AQS `state` int (upper 16 bits =
read-hold count, lower 16 bits = write-hold count), so acquiring/releasing either is still a
single CAS in the uncontended case.

- **Downgrading (allowed)**: acquire the write lock, do your update, acquire the read lock
  *before* releasing the write lock, then release the write lock — you now hold only the read
  lock, having never let another writer sneak in between your write and your read.
- **Upgrading (NOT allowed)**: holding the read lock and then calling `writeLock().lock()` will
  **deadlock** — the write lock cannot be granted while any read lock (including your own) is
  held, and you can't release your own read lock and grab the write lock atomically without a
  race, so the API doesn't support it. You must fully release the read lock first, then acquire
  the write lock (accepting that another thread may run in between).
- **When it helps**: read-heavy, write-rare data (a cache of reference data, config, FX rate
  tables refreshed once a minute) — many threads read concurrently with zero blocking between
  readers.
- **When it doesn't**: write-heavy or roughly balanced workloads — the extra bookkeeping
  (tracking read-hold counts, fairness ordering between readers/writers) adds overhead over a
  plain `ReentrantLock`/`synchronized`, and readers still fully block during any write, so you
  don't gain much if writes are frequent. `ConcurrentHashMap` is usually a better fit than
  hand-rolling `ReadWriteLock` around a plain `HashMap` for a shared map.

### StampedLock — optimistic reads

`StampedLock` (Java 8+) has three modes and does **not** implement `Lock`/`ReadWriteLock` (its
own API, returns a `long` **stamp** from every acquire that you must present to unlock/validate):
1. **Write lock** — exclusive, like a normal lock (`writeLock()` / `unlockWrite(stamp)`).
2. **Pessimistic read lock** — shared, like `ReadWriteLock`'s read lock (`readLock()`).
3. **Optimistic read** — `tryOptimisticRead()` returns a stamp **without blocking anything and
   without acquiring any real lock at all**. You read the fields you need, then call
   `validate(stamp)`: if no writer acquired the write lock since your optimistic stamp was
   issued, validation succeeds and your reads were safe; if a writer intervened, validation fails
   and you must retry — typically by falling back to a real `readLock()`.

```java
long stamp = lock.tryOptimisticRead();
double x = this.x, y = this.y;             // read fields without locking
if (!lock.validate(stamp)) {               // did a writer sneak in?
    stamp = lock.readLock();               // fall back to a real (blocking) read lock
    try { x = this.x; y = this.y; }
    finally { lock.unlockRead(stamp); }
}
```
This makes the *uncontended* read path essentially free (no CAS, no memory fence for the lock
itself — just a stamp comparison), which beats even `ReentrantReadWriteLock` for read-dominated,
low-write-contention workloads (e.g., a mutable point/coordinate updated occasionally, read
constantly).

**Critical gotchas**: `StampedLock` is **NOT reentrant** — calling `writeLock()` again from a
thread that already holds it deadlocks (unlike `synchronized`/`ReentrantLock`). It has **no
`Condition` support**. Losing the stamp (e.g., not saving it) means you can never unlock —
there's no "current owner" concept to recover from, unlike intrinsic locks.

### ASCII diagram — two Conditions on one Lock avoiding a thundering herd

```
ReentrantLock lock
 +-- Condition notFull   -- wait-set: [producerA, producerB]
 +-- Condition notEmpty  -- wait-set: [consumerX]

consumer calls take(), buffer becomes non-full ->
  notFull.signal()  -- wakes ONLY one thread from notFull's wait-set (producerA)
                     -- consumerX is untouched, never woken for a "not full" event
```

## 3. Complexity

| Operation | Time (uncontended) | Notes |
|-----------|--------------------|-------|
| `ReentrantLock.lock()`/`unlock()` | O(1), one CAS | blocks (parks) under contention |
| `tryLock()` | O(1), non-blocking | fails fast instead of waiting |
| `ReadWriteLock` read acquire (no writer) | O(1) CAS | concurrent readers don't block each other |
| `ReadWriteLock` write acquire | O(1) CAS, but waits for all readers/writers to drain | exclusive |
| `StampedLock` optimistic read | O(1), no CAS at all | just a stamp read + later comparison |
| `StampedLock.validate()` | O(1) | compares stamp to current lock state |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/LocksDemo.java` — demonstrates
  `tryLock(timeout)`, a `Condition`-based bounded hand-off, concurrent `ReadWriteLock` readers
  proven via a live-reader counter, and a `StampedLock` optimistic-read invalidated by a
  concurrent write.

```java
StampedLock sl = new StampedLock();
long stamp = sl.tryOptimisticRead();
int snapshot = sharedValue;              // read without locking
boolean writerIntervened = !sl.validate(stamp);
```
Expected console output (abbreviated):
```
tryLock(timeout) while held by another thread -> acquired=false (timed out as expected)
tryLock(timeout) once released -> acquired=true
bounded hand-off via Condition: producer put 5 items, consumer took 5 items
read-write lock: peak concurrent readers observed = 3 (> 1, proving readers overlap)
read-write lock: writer excluded all readers during its write
stamped lock optimistic read: valid=false after concurrent write (as expected) -> fell back to readLock()
```

## 5. When to use / when NOT to use

- Use `ReentrantLock` over `synchronized` when you need `tryLock`/timeouts, interruptible
  acquisition, fairness, or multiple `Condition` wait-sets — otherwise prefer `synchronized` for
  its simplicity and automatic release.
- Use `ReadWriteLock` for read-heavy, write-rare shared state where readers genuinely benefit from
  running concurrently.
- Use `StampedLock` for read-dominated hot paths where you can tolerate a fallback path and don't
  need reentrancy or `Condition`s — it's an optimization, reach for it only after profiling shows
  `ReadWriteLock` overhead matters.
- Avoid `StampedLock` if any code path might reacquire it on the same thread (reentrant calls) or
  needs to wait on a condition.

## 6. Common pitfalls & gotchas

**Forgetting `unlock()` in `finally`**:
```java
lock.lock();
doWork();          // BUG: if this throws, lock is never released -> permanent deadlock for others
lock.unlock();
// fix: lock.lock(); try { doWork(); } finally { lock.unlock(); }
```

**`await()`/`wait()`-style spurious wakeup with `if` instead of `while`** — same rule as topic 2;
always re-check the condition in a loop after `await()` returns.

**Trying to upgrade a `ReadWriteLock` read lock to a write lock while holding the read lock**:
```java
rwLock.readLock().lock();
rwLock.writeLock().lock();   // BUG: deadlocks — write lock can't be granted while a read lock
                              // (even your own) is held
```

**Losing a `StampedLock` stamp** — always keep the stamp returned by `writeLock()`/`readLock()`
in a local variable and pass the exact same value to the matching `unlock*` call; there is no
"current owner" bookkeeping to fall back on like `ReentrantLock`.

## 7. Interview questions

- [Basic] What can `ReentrantLock` do that `synchronized` can't? → Non-blocking `tryLock()`,
  bounded `tryLock(timeout)`, interruptible acquisition via `lockInterruptibly()`, configurable
  fairness, and multiple `Condition` wait-sets per lock. → Follow-up: *What does `synchronized`
  do better?* Automatic release on exception/return (no `finally` to forget), and simpler syntax
  with zero risk of a mismatched `lock()`/`unlock()`.
- [Basic] Why must `unlock()` always be called in a `finally` block? → Because unlike
  `synchronized`, `ReentrantLock` release is not automatic — if the critical section throws and
  `unlock()` is only on the "happy path," the lock is held forever, permanently blocking every
  other thread that needs it. → Follow-up: *Does the same risk exist for `synchronized`?* No —
  the JVM emits the monitor exit on any exit path, including exceptions, as part of the bytecode
  contract.
- [Basic] What is lock fairness, and what's the trade-off? → A fair lock (`new
  ReentrantLock(true)`) grants the lock to the longest-waiting thread (FIFO), preventing
  starvation; the default unfair lock allows "barging" (a newly arriving thread can jump ahead of
  parked threads), which is faster on average because it avoids the cost of waking a parked
  thread just to hand it the lock. → Follow-up: *When would you actually reach for a fair lock?*
  Only when you've observed real starvation of a specific thread under sustained contention —
  fairness has a real throughput cost, so it's not a default choice.
- [Basic] What does `Condition.await()` require, just like `Object.wait()`? → The calling thread
  must hold the associated lock, and the condition must be re-checked in a `while` loop after
  `await()` returns, because of spurious wakeups. → Follow-up: *What happens if you call
  `condition.await()` without holding the lock?* Throws `IllegalMonitorStateException`, same as
  `wait()` without holding the monitor.
- [Intermediate] Why would a bounded buffer use two separate `Condition`s instead of one? → With
  one wait-set (as in plain `synchronized`), `notifyAll()` wakes both producers and consumers
  indiscriminately, most of which just re-check and go back to sleep — wasted wakeups. Two
  `Condition`s (`notFull`, `notEmpty`) on the same lock let you `signal()` only the relevant group,
  avoiding that thundering herd. → Follow-up: *Could you use `signal()` instead of `signalAll()`
  safely with a single Condition shared by both producers and consumers?* No — `signal()` picks
  an arbitrary waiter from that Condition's wait-set, so mixing producers and consumers on one
  Condition risks waking the wrong kind of thread; you need separate Conditions to target
  correctly.
- [Intermediate] Explain lock downgrading in `ReentrantReadWriteLock` and why upgrading isn't
  supported. → Downgrading (write lock -> acquire read lock -> release write lock) is safe and
  supported: you never let another writer in between your write and your subsequent read, because
  you never fully released exclusive access before gaining the read lock. Upgrading (read lock ->
  try to acquire write lock) is not supported because the write lock cannot be granted while any
  read lock is held, including your own — releasing your read lock first to attempt upgrade opens
  a race window where another writer could intervene, so the API forbids it outright rather than
  offering a footgun. → Follow-up: *What would you do if you needed upgrade-like behavior?*
  Release the read lock, acquire the write lock, and re-validate your assumptions (data may have
  changed while you held neither lock).
- [Intermediate] When would `ReadWriteLock` actually hurt performance compared to a plain lock? →
  Under write-heavy or balanced read/write workloads, the extra bookkeeping (tracking read-hold
  counts, coordinating fairness between waiting readers and writers) adds overhead that a simple
  `ReentrantLock`/`synchronized` doesn't have, while you gain nothing from reader concurrency
  since writers dominate anyway. → Follow-up: *What's a red flag in production metrics that
  ReadWriteLock was the wrong choice?* Write lock acquisition rate close to or exceeding read
  lock acquisition rate.
- [Intermediate] What does `StampedLock.tryOptimisticRead()` actually acquire? → Nothing — no
  lock state is set, no thread blocks; it just returns a stamp representing the lock's current
  "version." The caller reads shared fields unprotected, then calls `validate(stamp)` to check
  whether a writer's exclusive lock was acquired and released since the stamp was issued. →
  Follow-up: *What happens if you read fields during a window where a writer is actively
  mutating them (not yet committed)?* You might read a torn/inconsistent combination of fields —
  which is exactly why `validate()` must be checked before trusting or acting on those reads.
- [Advanced] Why is `StampedLock` not reentrant, and what breaks if you call `writeLock()`
  recursively on the same thread? → `StampedLock` tracks lock state purely via the stamp/version
  counter, with no notion of "owning thread" or hold-count the way `ReentrantLock`/`synchronized`
  do — so a second `writeLock()` call from the same thread just blocks waiting for the write lock
  to be released, which will never happen because the same thread is the one holding it: a
  self-deadlock. → Follow-up: *How would you refactor code that must call a `StampedLock`-guarded
  method recursively?* Separate the public locking entry point from a private, unlocked internal
  method that assumes the lock is already held, and only ever call the internal method
  recursively.
- [Advanced] Compare the "fast path" cost of a `StampedLock` optimistic read against a
  `ReentrantReadWriteLock` read lock acquisition. → The `ReadWriteLock` read lock still performs a
  real CAS on shared state (incrementing a read-hold counter) even when uncontended, and must be
  explicitly released. The `StampedLock` optimistic read performs **no CAS and no lock-state
  mutation at all** — it just reads a volatile stamp value and compares it after the fact, making
  it essentially free on the fast path, at the cost of needing a fallback code path for when
  validation fails. → Follow-up: *Does that make StampedLock strictly better?* No — it trades
  simplicity and safety (no reentrancy, no Condition, easy-to-misuse stamp handling, and every
  optimistic-read call site needs a fallback branch) for that speed; it's a targeted optimization,
  not a general-purpose replacement.

## 8. Exercises

No dedicated exercises for this topic — see `notes/09-multithreading-concurrency/10-classic-problems.md`
and `notes/09-multithreading-concurrency/12-build-it-yourself.md` for hands-on practice using
these building blocks.

## 9. Quick recap

- `ReentrantLock` adds `tryLock`/timeouts/interruptibility/fairness/multiple `Condition`s over
  `synchronized`, but requires manual `unlock()` in `finally` — nothing is automatic.
- `Condition` gives one lock multiple independent wait-sets (e.g., `notFull`/`notEmpty`),
  avoiding the thundering-herd cost of a single monitor's `notifyAll()`.
- `ReadWriteLock`: many concurrent readers OR one exclusive writer; downgrading (write->read) is
  safe, upgrading (read->write) is not supported and would deadlock.
- `StampedLock`'s optimistic read acquires nothing at all — just a stamp to `validate()` later —
  making read-mostly hot paths nearly free, but it's not reentrant and has no `Condition` support.
- Reach for `ReentrantLock`/`ReadWriteLock`/`StampedLock` only when `synchronized`'s
  limitations actually bite; they add power at the cost of manual discipline.
