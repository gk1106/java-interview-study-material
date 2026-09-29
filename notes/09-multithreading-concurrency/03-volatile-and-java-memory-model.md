# volatile and the Java Memory Model

## 1. What it is

The Java Memory Model (JMM, defined in JLS Chapter 17) specifies what values a read of a shared
variable is **allowed** to see when other threads are concurrently writing it — without it, a
"correct-looking" program could still behave unpredictably because compilers, CPUs, and caches are
all free to reorder and cache memory operations for performance. `volatile` is the simplest tool
the JMM gives you: it guarantees **visibility** (every read sees the latest write) and forbids
certain **reorderings**, without providing mutual exclusion.

## 2. How it works internally

### Why plain shared fields can "never see" another thread's write

Without any synchronization, the JIT compiler and CPU are both allowed to:
- **Cache a field in a register** instead of re-reading main memory on every access (a loop reading
  a plain `boolean running` field may hoist the read entirely out of the loop, since nothing in the
  loop body tells the compiler the field can change from another thread).
- **Reorder independent instructions** for pipelining/optimization, as long as *single-threaded*
  observable behavior is unchanged — but another thread observing both fields can see the reorder.

```java
// classic broken "stop flag" -- NO volatile
class Worker extends Thread {
    private boolean running = true;
    public void run() {
        while (running) { /* work */ }   // JIT may hoist this read out of the loop entirely
    }
    void stopMe() { running = false; }   // may never become visible to the worker thread
}
```
This is not a timing bug that "usually works" — it is legally allowed to loop forever under the
JMM, and does in practice once the JIT compiler optimizes the loop after warm-up.

### What `volatile` guarantees

1. **Visibility**: every write to a `volatile` field is immediately visible to every subsequent
   read of that field by any thread — the compiler cannot cache it in a register/CPU cache line
   invisibly; each read goes to (the equivalent of) main memory, each write flushes to it.
2. **Ordering (happens-before)**: a write to a `volatile` field **happens-before** every subsequent
   read of that same field (JLS 17.4.5). This also prevents the compiler/CPU from reordering
   *other* reads/writes around the volatile access — the JIT inserts memory fences so that
   everything written *before* the volatile write is visible to any thread that reads the volatile
   field *after* that write and sees the new value. This makes `volatile` useful as a **publication
   point**: it doesn't just protect itself, it protects everything written before it.

```java
class Config {
    private int timeout;          // plain field
    private volatile boolean ready;

    void init() {
        timeout = 30;              // (1) plain write
        ready = true;              // (2) volatile write -- happens-after (1) in program order
    }
    void use() {
        if (ready) {                // (3) volatile read
            System.out.println(timeout);  // (4) guaranteed to see 30, NOT 0 -- because (1) happens-before (2)
        }                                //     happens-before (3) happens-before (4), by transitivity
    }
}
```

### What `volatile` does NOT guarantee: atomicity of compound operations

`volatile` makes a single read or a single write atomic and visible — it does **not** make a
read-modify-write sequence atomic:
```java
private volatile int counter = 0;
void increment() { counter++; }   // BUG: counter++ is read, add 1, write -- THREE separate steps.
                                   // two threads can both read the same old value before either writes back,
                                   // losing an update, exactly like a non-volatile int would.
```
`volatile` solves the "stale cached value" visibility problem; it does nothing for check-then-act
or read-modify-write races — that needs `synchronized`, a `Lock`, or an atomic class (`AtomicInteger`,
topic 5) whose CAS-based methods really are a single atomic operation.

### `happens-before` — the general framework

`volatile` reads/writes are just one source of `happens-before` edges. The full set that matters
day-to-day:
- **Program order** within a single thread (each action happens-before the next action of the same
  thread, in source order — modulo reorderings invisible to that thread itself).
- **Monitor lock**: a `synchronized` block's `unlock` happens-before every subsequent `lock` of the
  *same* monitor by any thread (this is why `synchronized` gives visibility too, not just mutual
  exclusion).
- **Volatile**: a write to a volatile field happens-before every subsequent read of that field.
- **Thread start/join**: `Thread.start()` happens-before anything the started thread does;
  everything a thread does happens-before another thread successfully returns from `join()` on it.
- **Transitivity**: if A happens-before B, and B happens-before C, then A happens-before C — this
  is exactly what makes the `Config` example above work: the plain `timeout` write is transitively
  visible through the volatile `ready` flag, without `timeout` itself needing to be volatile.

Without an established happens-before edge between a write in one thread and a read in another,
the JMM makes **no guarantee at all** about what the reader sees — not "probably stale," but
formally undefined, including seeing a value that was never validly written (for non-atomic 64-bit
types like `long`/`double` without `volatile` — "word tearing," see pitfalls).

### `volatile` vs `synchronized` vs atomics — what each one buys you

| | Visibility | Atomicity of compound ops | Mutual exclusion (blocking) |
|---|---|---|---|
| `volatile` | Yes | No | No |
| `synchronized` | Yes | Yes (within the block) | Yes |
| `AtomicInteger`/CAS | Yes | Yes (single field only) | No (lock-free) |

### ASCII diagram — visibility without volatile vs with volatile

```
NO volatile:                              WITH volatile:

Thread A          Thread B                Thread A          Thread B
write flag=true   read flag                write flag=true   read flag
   |  (cached in    (may read stale           |  (flushed,       (always re-read,
   |   register/     cached "false"            |   fenced)        sees latest)
   v   core-local     forever, in                v                   v
 [maybe never       theory)                [main memory]  <----  sees true
  flushed]
```

## 3. Complexity

| Operation | Time | Notes |
|-----------|------|-------|
| `volatile` read | O(1) | slightly more expensive than a plain read — cannot be cached across a memory fence |
| `volatile` write | O(1) | inserts a memory fence (store-barrier); more expensive than a plain write, cheaper than a lock acquire |
| establishing happens-before via `synchronized` | O(1) | same lock cost as topic 2 |
| establishing happens-before via `Thread.join()` | blocks until target thread terminates | O(1) bookkeeping |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/VolatileMemoryModelDemo.java`
  — runs a bounded (never-hangs) side-by-side comparison of a non-volatile stop flag that a worker
  thread may never observe versus a volatile one that reliably stops the worker, and demonstrates
  the safe-publication pattern (plain field visible transitively through a volatile flag).

```java
private volatile boolean ready = false;
private int payload;   // plain field, made visible via the volatile write below

void publish() {
    payload = 42;     // (1)
    ready = true;      // (2) volatile write -- happens-after (1)
}
void consume() {
    if (ready) {        // (3) volatile read
        System.out.println(payload);   // guaranteed to print 42, never 0, once ready observed true
    }
}
```
Expected console output (abbreviated):
```
non-volatile stop flag: worker did not observe the stop within the bound (demonstrates the risk; loop force-stopped via interrupt)
volatile stop flag: worker observed the stop flag and exited cleanly = true
safe publication via volatile: payload seen after ready==true = 42 (never 0)
```

## 5. When to use / when NOT to use

- Use `volatile` for a simple status/flag field written by one thread and read by others (a "stop
  requested" flag, a "config reloaded" flag, a double-checked-locking singleton's instance
  reference — topic 10) where you need visibility but not compound atomicity.
- Use `volatile` as a lightweight publication mechanism: writing all the "real" fields first, then
  a single `volatile` flag last, safely publishes everything written before it to any thread that
  observes the flag as `true`.
- Do NOT use `volatile` for counters or any read-modify-write logic (`count++`,
  `if (x < limit) x++`) — reach for `AtomicInteger`/`LongAdder` (topic 5) or `synchronized` instead.
- Do NOT use `volatile` as a substitute for a lock when multiple related fields must be updated
  together consistently — volatile only protects the single field it's declared on, not a
  multi-field invariant.

## 6. Common pitfalls & gotchas

**Treating `volatile` as if it makes `x++` atomic**:
```java
private volatile int counter = 0;
void increment() { counter++; }   // BUG: read-modify-write, still racy under concurrency
// fix: use AtomicInteger, or synchronized
private final AtomicInteger counter = new AtomicInteger(0);
void increment() { counter.incrementAndGet(); }
```

**Word tearing on non-volatile 64-bit fields**: the JMM does not guarantee atomic reads/writes of
plain (non-volatile) `long`/`double` on all platforms — a reader could in principle observe a value
made of half the old write and half the new write.
```java
private long timestamp;   // BUG (in theory): non-volatile 64-bit field, torn read is legally possible
// fix:
private volatile long timestamp;   // volatile 64-bit reads/writes are always atomic by JMM guarantee
```

**Forgetting that a busy-wait loop without any memory barrier can spin forever**:
```java
boolean done = false;             // plain field
while (!done) { }                 // BUG: JIT may hoist the read; this can loop forever after warm-up
// fix: private volatile boolean done = false;
```

**Assuming `volatile` on a reference makes the referenced object's fields thread-safe**:
```java
private volatile List<String> items = new ArrayList<>();
items.add("x");   // BUG: the reference read/write is visible, but ArrayList.add() itself is NOT
                   // thread-safe -- volatile says nothing about the mutability of what it points to
// fix: use a genuinely thread-safe collection (CopyOnWriteArrayList, Collections.synchronizedList,
// or swap the whole reference atomically with an immutable list each time)
```

## 7. Interview questions

- [Basic] What does `volatile` guarantee, in one sentence? → That every read of the field sees the
  most recently completed write to it from any thread (visibility), and that the write
  happens-before that subsequent read, preventing certain compiler/CPU reorderings around it. →
  Follow-up: *Does volatile make an operation atomic?* Only a single read or a single write is
  atomic; a compound read-modify-write like `x++` is not.
- [Basic] Why can a loop like `while (!stopFlag) { }` never terminate if `stopFlag` is a plain
  (non-volatile) field? → Without `volatile`, the JIT compiler is legally allowed to cache the
  field's value (e.g., in a register) across loop iterations instead of re-reading it from memory
  each time, since nothing tells it the field can change from another thread — in practice, after
  JIT warm-up, this can hoist the read entirely out of the loop. → Follow-up: *What are two fixes
  besides volatile?* Mark the field `volatile`, or use `AtomicBoolean`, or coordinate via a lock/
  `synchronized` block around the check.
- [Basic] Is a `volatile long` field's read/write atomic? → Yes — the JMM specifically guarantees
  that reads and writes of `volatile` 64-bit fields (`long`/`double`) are atomic, unlike plain
  (non-volatile) ones, which are permitted (though rare in practice on modern 64-bit JVMs) to
  suffer "word tearing." → Follow-up: *Is a plain (non-volatile) long field's read/write
  guaranteed atomic?* No — the JMM explicitly does not guarantee it, even though most real 64-bit
  JVMs happen to make it atomic in practice; don't rely on that.
- [Intermediate] Explain happens-before and why it matters more than "visibility" alone. →
  Happens-before is a partial ordering the JMM defines between actions (program order within a
  thread, monitor unlock-before-lock, volatile write-before-read, thread start/join) such that if A
  happens-before B, A's effects are guaranteed visible to B, and by transitivity, chains of these
  edges let a plain field's write become visible through an unrelated volatile write/read pair,
  without the plain field itself needing any synchronization. Without an established
  happens-before edge, the JMM makes literally no guarantee about what a reader sees. → Follow-up:
  *Give an example of transitivity in action.* Writing a plain field then a volatile flag in thread
  A, and reading the volatile flag then the plain field in thread B: the plain field's value is
  guaranteed visible to B once it observes the flag as set, because A's plain-write happens-before
  A's volatile-write (program order), which happens-before B's volatile-read (volatile rule), which
  happens-before B's plain-read (program order) — chained by transitivity.
- [Intermediate] Why is `volatile` the mechanism behind the double-checked-locking singleton
  pattern, and what breaks without it? → The singleton's instance field must be `volatile` so that
  once a thread publishes the fully-constructed instance, the volatile write's happens-before edge
  guarantees other threads reading it (in the first, unsynchronized check) see either `null` or a
  *fully initialized* object — never a partially-constructed one; without `volatile`, the JIT/CPU
  can reorder the object's field writes to happen *after* the reference is published, letting
  another thread observe a non-null-but-incompletely-constructed instance. → Follow-up: *Why
  doesn't the reference alone being non-null guarantee full construction without volatile?* Because
  constructor writes and the reference assignment aren't ordered relative to each other from
  another thread's perspective without a happens-before edge — the JIT is free to reorder them as
  long as single-threaded semantics in the constructing thread are preserved.
- [Intermediate] Why doesn't marking a `List` field `volatile` make operations on that list
  thread-safe? → `volatile` only governs visibility/ordering of the *reference itself* — reads and
  writes of the field that holds the list's address; it says nothing about the internal state of
  the object the reference points to, so concurrent `add()`/`remove()` calls on a plain `ArrayList`
  through that volatile reference are exactly as unsafe as without `volatile`. → Follow-up: *What
  would actually make this safe?* Either use a genuinely thread-safe/concurrent collection
  (`CopyOnWriteArrayList`, `Collections.synchronizedList` with external synchronization on
  iteration), or treat the list as immutable and swap the entire volatile reference to a new list
  atomically on every "mutation."
- [Advanced] Why does `synchronized` also provide visibility, not just mutual exclusion? → The JMM
  defines a happens-before edge between a monitor's `unlock` and any subsequent `lock` of that same
  monitor by any thread — so everything a thread did before releasing a lock is guaranteed visible
  to the next thread that acquires that same lock, exactly analogous to the volatile write-before-
  read rule, just scoped to lock acquisition instead of a single field access. → Follow-up: *Does
  that mean synchronized and volatile are interchangeable for visibility?* For a single flag,
  functionally similar in effect, but `synchronized` additionally provides mutual exclusion (only
  one thread in the critical section at a time) and establishes the happens-before edge for
  *everything* touched inside the block, not just one field — `volatile` is cheaper but narrower.
- [Advanced] Why is reordering legal at all — what is the JMM trying to optimize for by permitting
  it? → The JMM deliberately gives compilers, JITs, and CPUs freedom to reorder, cache, and
  eliminate memory operations for performance (instruction pipelining, register allocation, CPU
  store buffers, out-of-order execution) as long as the reordering is invisible to a
  *single-threaded* observer — correctness for concurrent observers is instead guaranteed only
  where the programmer explicitly establishes happens-before edges (volatile, locks, atomics,
  thread start/join); this trade-off lets the vast majority of sequential, non-shared code run at
  full hardware speed without the JVM having to conservatively insert memory fences everywhere "just
  in case" some other thread might be watching. → Follow-up: *What would happen to performance if
  the JVM guaranteed strict sequential consistency (globally ordered memory operations) for every
  field, always?* Every field access would effectively need a memory fence, eliminating most CPU
  and compiler optimizations (caching in registers, reordering, out-of-order execution, store
  buffering) even for data that's never actually shared across threads — a severe, broad slowdown
  for the common case to protect the comparatively rare case of genuinely shared mutable state.

## 8. Exercises

No dedicated exercises for this topic — the double-checked-locking singleton exercise in
`notes/09-multithreading-concurrency/10-classic-concurrency-problems.md` is a direct, hands-on
application of `volatile` and happens-before.

## 9. Quick recap

- `volatile` guarantees visibility (every read sees the latest write) and forbids certain
  reorderings around the access — it does NOT make compound read-modify-write operations atomic.
- Without `volatile`/locks/atomics establishing a happens-before edge, the JMM makes no guarantee
  at all about what another thread observes — not "usually stale," formally undefined.
- happens-before chains transitively: a plain field written before a volatile write is safely
  visible to any thread that reads that volatile field afterward and observes the new value.
- `synchronized` also creates happens-before edges (unlock-before-lock on the same monitor), which
  is why it provides visibility in addition to mutual exclusion.
- Use `volatile` for simple flags/publication points; use `AtomicInteger`/`synchronized`/`Lock` the
  moment you need atomic compound operations or multi-field consistency.
