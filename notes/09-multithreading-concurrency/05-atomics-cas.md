# Atomics & CAS (AtomicInteger, AtomicLong, AtomicReference, LongAdder)

## 1. What it is

The `java.util.concurrent.atomic` package gives lock-free, thread-safe operations on single
variables (int, long, reference, boolean) using **compare-and-swap (CAS)**, a hardware-level
atomic instruction, instead of `synchronized`/`Lock`. No thread ever blocks waiting for a mutex;
contention shows up as CAS *retries* instead.

## 2. How it works internally

### CAS — the hardware primitive

Compare-and-swap is a single atomic CPU instruction (x86: `CMPXCHG`; ARM: load-linked/
store-conditional pair) with the semantics:

> `CAS(memoryLocation, expectedValue, newValue)`: atomically, if `memoryLocation` currently holds
> `expectedValue`, write `newValue` and return success; otherwise leave it unchanged and return
> failure.

`AtomicInteger`/`AtomicLong`/`AtomicReference` wrap a single `volatile` field and expose CAS on it
via `VarHandle` intrinsics (JIT-compiled straight to the hardware instruction — no JNI/syscall
overhead). `incrementAndGet()`, `updateAndGet(fn)`, `accumulateAndGet(x, fn)` all follow the same
**CAS retry loop** internally:

```
loop:
  1. read current value V  (volatile read)
  2. compute newV = f(V)   (e.g., V + 1)
  3. CAS(field, V, newV)
     success -> return newV
     failure -> another thread changed the field between steps 1 and 3; go to 1 and retry
```

ASCII diagram — the CAS retry loop for `incrementAndGet()` under contention:
```
Thread A                          Thread B
read value = 5                    read value = 5
compute new = 6
                                   compute new = 6
CAS(field, 5, 6) -> SUCCESS
                                   CAS(field, 5, 6) -> FAILS (field is now 6, not 5)
                                   retry: read value = 6
                                   compute new = 7
                                   CAS(field, 6, 7) -> SUCCESS
```
This is **lock-free** (some thread always makes progress on any given step — no thread ever
blocks waiting for another to release something), but under heavy contention many threads can
spin through retries repeatedly, wasting CPU — which is exactly the problem `LongAdder` solves
(below).

### The ABA problem

CAS only checks that the value **looks the same** as before, not that it was **never changed** in
between. Classic failure scenario with `AtomicReference` used to build a lock-free structure (e.g.
a lock-free stack's `head` pointer):

```
Thread T1: reads head = A, plans CAS(head, A, A.next)   [about to pop A]
  -- T1 is preempted here --
Thread T2: pops A (head becomes B), pushes C, then pushes A again (head becomes A, but A.next now
           points to a DIFFERENT node than before)
  -- T1 resumes --
Thread T1: CAS(head, A, A.next) SUCCEEDS because head IS currently A again
           -- but A.next is now stale/wrong, corrupting the stack --
```
T1's CAS "looks" valid (head really is `A`) but the structure underneath changed and changed back
— T1 has no way to know `A` was ever removed and re-added. This matters most in lock-free data
structures (stacks, queues) where nodes/references get reused or logically "come back."

**Fix — `AtomicStampedReference<V>`**: pairs the reference with an `int` stamp (version counter).
CAS now compares **both** the reference *and* the stamp; every logical update increments the
stamp, so even if the reference value cycles A->B->A, the stamp will have moved forward
(e.g., 1->2->3), and a stale CAS attempt using stamp `1` fails even though the reference matches.
`AtomicMarkableReference<V>` is the simpler sibling — pairs the reference with a single `boolean`
mark (e.g., "logically deleted") instead of a full version counter, for cases where you only need
one bit of extra state, not a full ABA-proof version history.

```java
AtomicStampedReference<Node> head = new AtomicStampedReference<>(initial, 0);
int[] stampHolder = new int[1];
Node current = head.get(stampHolder);
int stamp = stampHolder[0];
// ... compute newNode ...
boolean ok = head.compareAndSet(current, newNode, stamp, stamp + 1);  // fails if stamp moved
```

### LongAdder / DoubleAdder — striped counters

Under **high contention**, every thread hitting `AtomicLong.incrementAndGet()` CASes against the
*same* memory location — cache-line ping-ponging between cores (MESI protocol invalidation
traffic) makes most CASes fail and retry, so throughput collapses as thread count grows.
`LongAdder` (Java 8+, the same design idea as `ConcurrentHashMap`'s `CounterCell` striping,
already covered) fixes this by spreading writes across multiple memory locations:

- A `base` field (used directly while there's no contention — cheapest path).
- A lazily-grown `Cell[]` array. Once a CAS on `base` fails (contention detected), the thread
  switches to updating one `Cell`, chosen via a **thread-local probe hash** so different threads
  usually land on different cells and their CASes don't collide.
- `sum()` (and `longValue()`/`intValue()`) walks `base` plus every `Cell` and adds them up — O(number
  of cells), called rarely (typically once, at the end, or for periodic reporting).

```
Thread A increments -> lands on Cell[1]  (own cache line, CAS rarely fails)
Thread B increments -> lands on Cell[3]  (different cache line, no contention with A)
Thread C increments -> lands on Cell[1]  (occasional collision with A, cheap retry)

sum() = base + Cell[0] + Cell[1] + Cell[2] + Cell[3] + ...
```
This trades a more expensive `sum()` (must visit every cell) for dramatically higher-throughput,
low-contention increments — exactly the right trade-off for counters that are written far more
often than read (hit counters, metrics, request tallies).

**When `AtomicLong` is still the better choice**: you need `compareAndSet`/`updateAndGet`
semantics (LongAdder has no CAS API — it's increment/add-only, not a general read-modify-write
primitive), you need the current value cheaply and frequently (every `get()` on `AtomicLong` is a
single volatile read; `LongAdder.sum()` must visit every cell), or contention is low (at low
thread counts the two perform similarly, and `AtomicLong` has a simpler memory footprint — no
lazily-allocated `Cell[]` array).

## 3. Complexity

| Operation | Time | Notes |
|-----------|------|-------|
| `AtomicInteger/Long.get()` | O(1) | single volatile read |
| `incrementAndGet()`/`compareAndSet()` | O(1) amortized | CAS retry loop; retries grow with contention |
| `AtomicReference` CAS | O(1) | same mechanism, reference identity compared |
| `AtomicStampedReference` CAS | O(1) | compares reference + stamp together |
| `LongAdder.add()`/`increment()` | O(1), low contention overhead | lands on base or a striped cell |
| `LongAdder.sum()` | O(number of cells) | not O(1); walks all cells |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/concurrency/examples/AtomicsCasDemo.java` — proves
  `AtomicInteger` loses no updates under real contention, hand-codes an explicit CAS retry loop,
  demonstrates `AtomicStampedReference` catching an ABA change that a plain `AtomicReference`
  misses, and runs a bounded `LongAdder` vs `AtomicLong` comparison.

```java
AtomicInteger counter = new AtomicInteger(0);
int oldVal, newVal;
do {
    oldVal = counter.get();
    newVal = oldVal + 1;
} while (!counter.compareAndSet(oldVal, newVal));   // hand-written CAS retry loop
```
Expected console output (abbreviated):
```
AtomicInteger under contention: expected=8000, actual=8000 (no lost updates)
hand-written CAS retry loop result: 8000
AtomicReference ABA: after A->B->A, compareAndSet(A, C) succeeds even though A changed (ABA occurred)
AtomicStampedReference ABA-guarded: after A->B->A, compareAndSet with stale stamp FAILS (ABA detected)
LongAdder total=8000, AtomicLong total=8000 (both correct; LongAdder designed for lower contention overhead)
```

## 5. When to use / when NOT to use

- Use `AtomicInteger`/`AtomicLong` for simple shared counters/flags where you need `get`/`set`/CAS
  semantics and moderate contention.
- Use `LongAdder` for high-contention, write-heavy, read-rarely counters (metrics, hit counts).
- Use `AtomicReference` for lock-free publish of an immutable object (e.g., swapping a config
  snapshot); use `AtomicStampedReference`/`AtomicMarkableReference` only when building lock-free
  structures where ABA is a real risk (rare in typical application code — most business logic
  never needs this).
- Avoid atomics for compound invariants across *multiple* variables — CAS only guarantees
  atomicity for a single field; coordinating several related fields atomically needs a lock or a
  single `AtomicReference` to an immutable composite object instead.

## 6. Common pitfalls & gotchas

**Non-atomic compound check-then-act even with atomics**:
```java
if (counter.get() < limit) {
    counter.incrementAndGet();     // BUG: another thread can push count over `limit` between
}                                   // the check and the increment
// fix: use a CAS loop that checks-and-updates atomically, e.g.
// counter.updateAndGet(v -> v < limit ? v + 1 : v);  (still racy on "did it actually increment" —
// use compareAndSet in a loop and inspect the return value if you need to know whether it applied)
```

**Assuming `LongAdder.sum()` is a cheap, instantaneous read** — unlike `AtomicLong.get()`, it
visits every striped cell; calling it in a hot loop (instead of once, at the end) defeats the
point of striping.

**Using `AtomicReference` and assuming CAS protects against ABA** — plain `compareAndSet` on an
`AtomicReference` cannot detect that the reference cycled through other values and came back; only
`AtomicStampedReference`/`AtomicMarkableReference` guard against that.

## 7. Interview questions

- [Basic] What is compare-and-swap (CAS), conceptually? → A single atomic hardware instruction:
  compare a memory location's current value to an expected value; if they match, write a new
  value; if not, do nothing and report failure — all without any other thread being able to
  observe an in-between state. → Follow-up: *Is CAS blocking?* No — it's lock-free; a failed CAS
  just means "retry," not "wait."
- [Basic] How does `AtomicInteger.incrementAndGet()` work internally? → It loops: read the current
  value, compute value+1, attempt `compareAndSet(old, new)`; if the CAS fails because another
  thread changed the value first, it retries from the read. → Follow-up: *Can this loop run
  forever?* In theory under extreme contention it could retry many times, but in practice it's
  lock-free and typically very fast; `LongAdder` exists specifically to reduce retries under heavy
  contention.
- [Basic] Is `AtomicInteger` faster than `synchronized` for a simple counter? → Generally yes under
  low-to-moderate contention, since CAS avoids the overhead of acquiring a monitor / potentially
  parking a thread — but under very heavy contention, both degrade, and `LongAdder` typically wins. →
  Follow-up: *Why would synchronized ever be preferable?* When you need to atomically update
  multiple related fields together, not just one.
- [Intermediate] Explain the ABA problem with a concrete example. → Thread T1 reads a value/
  reference `A`, intending to CAS it to something else, but gets preempted; meanwhile other
  threads change it `A -> B -> A`; when T1 resumes, its CAS succeeds because the value looks
  unchanged (`A`), even though it was actually modified and modified back — which can corrupt a
  lock-free structure if intermediate state mattered (e.g. a popped-then-reused node in a lock-free
  stack). → Follow-up: *Does ABA affect a simple counter?* No — for a plain numeric counter, "the
  value is A again" genuinely means it's safe to proceed; ABA only matters when identity/history,
  not just the current value, matters (e.g., pointer/reference-based structures).
- [Intermediate] How does `AtomicStampedReference` fix the ABA problem? → It pairs the reference
  with an integer stamp that must be incremented on every logical update; CAS then compares both
  the reference AND the stamp, so even if the reference cycles back to its original value, the
  stamp will have moved forward and a CAS holding the old stamp fails, correctly detecting that
  something changed in between. → Follow-up: *What's the simpler alternative when you only need
  one bit of extra state, not a full version counter?* `AtomicMarkableReference`, which pairs the
  reference with a single boolean mark.
- [Intermediate] Why does `LongAdder` exist when `AtomicLong` already provides atomic increments? →
  Under high contention, every thread's `AtomicLong.incrementAndGet()` CASes against the exact
  same memory location, so cores constantly invalidate each other's cached copy of that cache line
  (MESI protocol traffic), causing most CASes to fail and retry — throughput collapses as thread
  count grows. `LongAdder` stripes updates across multiple internal cells so concurrent threads
  usually update *different* memory locations, avoiding that contention almost entirely. →
  Follow-up: *What's the cost of that design?* `sum()` is no longer O(1) — it must add up every
  cell — and there's a small extra memory footprint for the cell array, allocated lazily only once
  contention is actually detected.
- [Intermediate] Why can't `LongAdder` support `compareAndSet`? → Its whole value is spread across
  multiple cells (`base` + `Cell[]`); there is no single memory location holding "the" current
  value to compare-and-swap against — the total only exists as a computed sum at read time, which
  is fundamentally incompatible with a single atomic CAS on "the" value. → Follow-up: *So when is
  AtomicLong still strictly necessary over LongAdder?* Whenever you need CAS-based semantics
  (optimistic updates, "only update if still equal to X") or need to read the current value
  cheaply and frequently.
- [Advanced] Walk through why `counter.get() < limit` followed by `counter.incrementAndGet()` is
  still racy even though both calls are individually atomic. → Atomicity of each individual
  operation doesn't compose into atomicity of the *sequence* — between the `get()` check and the
  `incrementAndGet()` call, another thread can run its own get-then-increment, so multiple threads
  can each see `count < limit` and each increment, pushing the total past `limit`. The fix is a
  single atomic read-modify-write over the whole check-and-update, e.g. a CAS loop that only
  applies the increment if the pre-increment value still satisfies the condition, retrying
  otherwise. → Follow-up: *Does `updateAndGet(v -> v < limit ? v+1 : v)` fully fix this?* It fixes
  the atomicity (the function is applied via an internal CAS loop), but the caller can no longer
  tell from the return value alone whether the increment actually happened versus the value was
  already at the limit — inspect whether the returned value actually changed if you need that.
- [Advanced] Contrast how `ConcurrentHashMap.size()`'s `CounterCell` striping and `LongAdder`
  solve the same underlying problem. → They are literally the same design: instead of one hot,
  contended counter that every writer CASes, spread the count across multiple cells so concurrent
  writers usually land on different cells and rarely contend; the aggregate value is only computed
  on demand by summing all cells, accepting a more expensive (but rare) read in exchange for much
  cheaper, non-contending writes. `LongAdder` is effectively the general-purpose, standalone
  version of the same striping trick `ConcurrentHashMap` uses internally for its own count. →
  Follow-up: *Could ConcurrentHashMap have just used a LongAdder field directly for its counter?*
  Conceptually yes — the underlying mechanism (base + striped cells, summed on demand) is
  essentially identical; `ConcurrentHashMap` predates public `LongAdder` in some JDK versions'
  internal design lineage and implements its own cell array, but the ideas are the same.

## 8. Exercises

No dedicated exercises for this topic — see `notes/09-multithreading-concurrency/10-classic-problems.md`
(the synchronized/Atomic/LongAdder race-condition fix exercise lives there) and
`notes/09-multithreading-concurrency/12-build-it-yourself.md` for hands-on practice using these
building blocks.

## 9. Quick recap

- CAS is a single atomic hardware instruction: compare-then-swap, lock-free; failed CAS means
  "retry," never "block."
- `incrementAndGet`/`updateAndGet`/`accumulateAndGet` are all CAS retry loops under the hood.
- ABA: a value cycling A->B->A can fool a naive CAS into thinking nothing changed —
  `AtomicStampedReference` (version counter) / `AtomicMarkableReference` (boolean mark) fix it.
- `LongAdder` stripes updates across multiple cells to avoid single-hot-counter contention,
  trading a more expensive `sum()` for much cheaper concurrent increments — the same idea
  `ConcurrentHashMap` uses for `size()`.
- Individually-atomic operations don't make a *sequence* of them atomic — check-then-act on an
  atomic still needs a CAS loop or external lock to be truly race-free.
