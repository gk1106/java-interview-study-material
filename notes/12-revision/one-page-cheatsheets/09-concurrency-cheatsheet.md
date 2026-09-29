# Cheat Sheet — 09: Multithreading & Concurrency

One-page pre-interview skim. Full notes: `notes/09-multithreading-concurrency/`.

## Thread states

`NEW → RUNNABLE ⇄ BLOCKED/WAITING/TIMED_WAITING → RUNNABLE → TERMINATED`. `RUNNABLE` means
*eligible* to run, not "currently executing on a core."

## Lock / synchronizer comparison

| Tool | Key property | Reach for it when |
|---|---|---|
| `synchronized` | reentrant intrinsic monitor, auto-release on any exit incl. exceptions | default, simplest case |
| `ReentrantLock` | manual `unlock()` in `finally`, adds `tryLock`/timeouts/interruptibility/fairness/multiple `Condition`s | need `synchronized`'s limits lifted |
| `ReadWriteLock` | many concurrent readers OR one exclusive writer; downgrade write→read is safe, **upgrade read→write deadlocks** | read-heavy shared state |
| `StampedLock` | optimistic read acquires **nothing**, just a stamp to `validate()` later — near-free reads; not reentrant, no `Condition` | read-mostly hot paths |
| `CountDownLatch` | one-shot, cannot reset | "wait for N things" / simultaneous start gate |
| `CyclicBarrier` | waits for N *threads*, runs a barrier action, auto-resets | repeated phased computation |
| `Semaphore` | bounds concurrency via permit count, no ownership tracking | limiting concurrent access to a resource |
| `Phaser` | dynamic party count | rare — participant count changes at runtime |

All four synchronizers are AQS-based, same family as `ReentrantLock`.

## Executor / thread-pool sizing

- CPU-bound pools: size near `N_cpu + 1`.
- I/O-bound pools: size much larger, `N_cpu * (1 + wait/compute)`.
- `Executors.*` factories hide either an unbounded queue (`newFixedThreadPool`, `newSingleThreadExecutor`) or unbounded max size (`newCachedThreadPool`) — prefer `new ThreadPoolExecutor(...)` directly with an explicit bounded queue + rejection policy.
- Rejection policies: `AbortPolicy` (throw), `CallerRunsPolicy` (natural backpressure), `Discard`/`DiscardOldestPolicy` (drop — only if loss is acceptable).
- `shutdown()` = graceful; `shutdownNow()` interrupts running tasks but doesn't guarantee they stop.

## Most-likely-asked facts

1. `wait()`/`notify()` require holding the monitor; `wait()` atomically releases the lock while parking. Always re-check the condition in a **`while`** loop (spurious wakeups, multiple waiters) — never `if`.
2. `notifyAll()` is the safe default unless every waiter shares the exact same condition — `notify()` risks waking the wrong kind of thread and losing a wakeup.
3. `volatile` guarantees **visibility** and forbids certain reorderings — it does **not** make compound read-modify-write (`x++`) atomic.
4. happens-before chains transitively; `synchronized` (unlock-before-lock) and `volatile` writes/reads both establish it.
5. CAS = single atomic hardware instruction, lock-free; failed CAS means retry, never block. ABA problem: `AtomicStampedReference`/`AtomicMarkableReference` fix it.
6. `LongAdder` stripes updates across cells to avoid hot-counter contention — cheap `add()`, expensive `sum()` (walks all cells) — same idea `ConcurrentHashMap.size()` uses.
7. `Future.get()` wraps a task's thrown exception in `ExecutionException` — unwrap with `.getCause()`.
8. `thenApply` = map-like (plain value); `thenCompose` = flatMap-like (function returns another `CompletableFuture`, avoids nested futures). `allOf(...)` returns `Void`, not results.
9. Plain chaining methods run on whichever thread completed the prior stage; `*Async` variants submit to `ForkJoinPool.commonPool()` (or your executor) — pass an explicit executor for real work.
10. Deadlock needs all four Coffman conditions; the practical fix is a **consistent global lock-acquisition order**.
11. Double-checked-locking singleton **requires `volatile`** on the instance field, or a thread can observe a non-null but partially-constructed object.
12. Virtual threads (Java 21): unmount from carrier thread on blocking I/O, remount after — millions can exist. **Never pool them** (`newVirtualThreadPerTaskExecutor()`, unbounded by design). Pinning happens inside `synchronized` blocks that block, or native/JNI frames — replace with `ReentrantLock` on hot paths. They don't speed up CPU-bound work.
13. `ConcurrentHashMap` (Java 8+): CAS on empty bin, `synchronized` per-bin-head-node on collision, no null keys/values, weakly-consistent iterators (never throw CME).

## Top pitfalls

- **Swallowing `InterruptedException`** — restore the flag (`Thread.currentThread().interrupt()`) or propagate; never silently ignore.
- **Locking in inconsistent order based on caller-supplied argument order** (classic transfer-between-accounts deadlock) — fix: resolve to a stable id-based order before locking.
- **Forgetting `unlock()`/`release()` in `finally`** for `ReentrantLock`/`Semaphore` — permanent deadlock or permit leak on the throw path.
- **Racy check-then-act even on a thread-safe structure** — `if (!map.containsKey(k)) map.put(...)` isn't atomic; use `computeIfAbsent`.
- **Relying on the shared `ForkJoinPool.commonPool()`** for blocking I/O — starves unrelated `CompletableFuture`/parallel-stream work JVM-wide.
- **Assuming a race condition "will surface in testing"** — timing-dependent bugs can pass every CI run and still lose updates constantly in production.
- **Pooling virtual threads like platform threads** — defeats their entire point (they're meant to be cheap and disposable).

## Classic problems / build-it-yourself to rehearse

Deadlock (create + fix via lock ordering) · race condition fix (AtomicInteger) · producer-consumer
(wait/notify version AND BlockingQueue version) · print odd/even alternation with two threads ·
thread-safe double-checked-locking singleton · simple thread pool · bounded blocking queue.
