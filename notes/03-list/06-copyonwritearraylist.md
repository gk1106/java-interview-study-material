# CopyOnWriteArrayList

## 1. What it is

`CopyOnWriteArrayList<E>` is a thread-safe `List` where **every mutating operation
(add/set/remove) copies the entire backing array**, mutates the copy, then atomically swaps it
in. Reads (including iteration) never lock and never see partial writes — they either see the
old array or the new one, never a mix. It trades write cost for lock-free, snapshot-consistent
reads.

## 2. How it works internally

- Backing field: `volatile Object[] array` (volatile so a swap is immediately visible to other
  threads without extra synchronization on the read side).
- A single internal lock (e.g. `ReentrantLock`) serializes **writers only** — readers never
  acquire it.

**`add(E e)`** (paraphrased):
```
lock();
try {
    Object[] old = array;
    Object[] copy = Arrays.copyOf(old, old.length + 1);
    copy[old.length] = e;
    array = copy;              // volatile write — publishes the new array atomically
} finally { unlock(); }
```
Every single `add`, even appending one element, is an **O(n) full-array copy** — very different
from `ArrayList`'s amortized O(1) append.

**Iterators are snapshot-based**: `iterator()` captures a reference to the *current* `array` at
call time and iterates only that array, ignoring any later swaps:
```
Thread A: iterator = list.iterator();     // snapshot = array@v1
Thread B: list.add(x);                    // array = array@v2 (copy), v1 untouched
Thread A: iterator.next() ...             // still walking array@v1 — no CME, no new element seen
```
This is why `CopyOnWriteArrayList`'s iterator **cannot throw `ConcurrentModificationException`**
and also **does not support `remove()`/`add()`/`set()` on the iterator itself** (those methods
throw `UnsupportedOperationException`) — the iterator is a read-only view of a frozen array.

ASCII diagram of a write while a reader iterates:
```
Reader holds:      array@v1 = [A, B, C]              (still iterating this)
Writer calls add(D):
   copy = [A, B, C, D]   (new array allocated)
   array = copy            (volatile swap; array@v2 now current for NEW readers)
Reader's iterator keeps walking array@v1 = [A, B, C]  -- unaffected, consistent, no D seen
```

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `get(index)` | O(1) | O(1) | reads current array, no lock |
| `add` / `remove` / `set` | O(n) | O(n) | full array copy every time |
| iteration | O(n) | O(n) snapshot held | never blocks, never throws CME |
| `contains` / `indexOf` | O(n) | O(1) | linear scan over the snapshot array |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/CopyOnWriteArrayListDemo.java`

```java
List<String> list = new CopyOnWriteArrayList<>(List.of("A", "B", "C"));
Iterator<String> it = list.iterator();
list.add("D");                 // mutation happens while `it` is "live"
while (it.hasNext()) {
    System.out.println(it.next());  // still only prints A, B, C — snapshot at iterator() call
}
System.out.println(list);      // [A, B, C, D] — the list itself did change
```
Expected console output:
```
A
B
C
[A, B, C, D]
```

## 5. When to use / when NOT to use

- Use for **read-heavy, write-rare** collections shared across threads — classic examples:
  a list of registered event listeners, a small, rarely-changing configuration/allow-list read by
  many threads on every request (e.g. a cached list of active bank branch codes refreshed
  occasionally).
- Do NOT use for write-heavy workloads (e.g. an order book, a live transaction log appended to
  continuously) — every write is O(n), so throughput collapses as the list grows; use
  `ConcurrentLinkedQueue`/`BlockingQueue`/a proper concurrent structure instead.
- Do NOT use expecting `size()` immediately after concurrent adds to reflect every write in
  real time from every thread's perspective mid-iteration — iteration is a **stale-but-consistent
  snapshot** by design, which is the correct trade-off for listener lists but wrong if you need
  "read must see the latest write" semantics.

## 6. Common pitfalls & gotchas

**Calling `iterator.remove()` throws, unlike `ArrayList`:**
```java
Iterator<String> it = copyOnWriteList.iterator();
it.next();
it.remove();  // UnsupportedOperationException — the iterator is read-only
// fix: mutate the list directly, e.g. copyOnWriteList.removeIf(predicate);
```

**Using it as a general-purpose, write-heavy list** silently degrades performance — nothing
throws, it just gets slower and slower (O(n) per write, more GC churn from repeated array
allocation) as the list grows, which can be a subtle production perf regression if someone
swaps `ArrayList` for `CopyOnWriteArrayList` "to be thread-safe" without checking the write
frequency.

**Expecting iterators to reflect concurrent writes:**
```java
List<String> alerts = new CopyOnWriteArrayList<>();
Iterator<String> it = alerts.iterator();  // snapshot taken NOW, empty
alerts.add("fraud-check-failed");
it.hasNext(); // false — the snapshot was empty at iterator() time, this is correct/by-design
```

## 7. Interview questions

- [Basic] What does "copy-on-write" mean for this list? → Every structural mutation
  (add/remove/set) allocates a brand-new backing array, copies the existing elements into it,
  applies the change, and atomically publishes the new array via a `volatile` field write — reads
  never block and never see a torn/partial array. → Follow-up: *Is get() also copying?* No —
  `get` just reads the current array reference and indexes into it, O(1), no copy, no lock.
- [Basic] Why doesn't `CopyOnWriteArrayList`'s iterator throw `ConcurrentModificationException`?
  → Because it iterates a frozen snapshot array captured at `iterator()` call time; concurrent
  writes create an entirely new array and never touch the one the iterator holds, so there's
  nothing to detect a "concurrent modification" of. → Follow-up: *Does that mean it's always safe
  to use in any concurrent context?* Safe from CME and torn reads, yes, but you must accept
  potentially stale data during iteration — that's a semantic trade-off, not a bug.
- [Basic] Can you call `iterator.remove()` on a `CopyOnWriteArrayList`? → No — it throws
  `UnsupportedOperationException`; the iterator is intentionally read-only. → Follow-up: *How do
  you remove elements then?* Call mutating methods directly on the list (`remove`, `removeIf`,
  `removeAll`), each of which does its own internal copy-lock-swap.
- [Intermediate] Why is every write O(n) instead of amortized O(1) like `ArrayList`? → Because
  correctness (readers never seeing a partially-updated array, and iterators being stable
  snapshots) is achieved by *never mutating a published array in place* — every write must
  produce a whole new array of the correct final size and swap it in atomically; there's no
  "leave spare capacity and just increment size" trick like `ArrayList` uses. → Follow-up: *Could
  the JDK optimize batch writes to copy less often?* Methods like `addAll` do a single copy for
  the whole batch rather than one copy per element, which is why batching writes matters
  performance-wise.
- [Intermediate] Compare `CopyOnWriteArrayList` to `Collections.synchronizedList(new
  ArrayList<>())` for a read-heavy workload. → `synchronizedList` requires every read (`get`,
  iteration) to acquire the same lock as writers — reads serialize against each other and against
  writers; `CopyOnWriteArrayList` reads take no lock at all and never block, making it strictly
  better for read-heavy, write-rare access patterns, at the cost of O(n) writes and higher memory
  churn. → Follow-up: *What about a write-heavy workload?* `synchronizedList` wins there —
  O(1)-ish locked writes beat O(n) copy-on-every-write.
- [Intermediate] Why must the backing array field be `volatile`? → So that when a writer swaps in
  the new array reference, other threads reading the field are guaranteed (by the Java Memory
  Model's happens-before rules for volatile writes/reads) to see the up-to-date reference rather
  than a stale cached one — without `volatile`, a reader thread could keep seeing the old array
  reference indefinitely. → Follow-up: *Does volatile alone make the whole class thread-safe?*
  No — volatile only guarantees visibility of the reference swap; the internal lock is still
  needed to serialize writers against each other so two concurrent `add()` calls don't both read
  the same old array and race to publish, losing one of the writes.
- [Advanced] Why is `CopyOnWriteArrayList` a good fit for a listener/observer list but a bad fit
  for a shared work queue? → Listener lists are registered rarely (write-rare) and iterated on
  every event dispatch (read-heavy), exactly the profile copy-on-write optimizes for, and losing
  the "see the latest listener immediately" guarantee mid-dispatch is harmless; a work queue is
  write-heavy (constant enqueue/dequeue) where O(n)-per-write kills throughput, and "read the
  latest state" is usually a correctness requirement, not just a nicety. → Follow-up: *What would
  you use for a shared work queue instead?* A `BlockingQueue` implementation
  (`LinkedBlockingQueue`/`ArrayBlockingQueue`), covered in module 04.
- [Advanced] How does `CopyOnWriteArrayList` avoid lost updates when two threads call `add()`
  concurrently? → The internal lock (e.g. a `ReentrantLock`) is acquired for the duration of the
  read-copy-modify-swap sequence, so the two `add()` calls are fully serialized against each
  other even though reads are lock-free; only one writer at a time can be "in flight" mutating and
  publishing a new array. → Follow-up: *So writers don't get any concurrency benefit at all?*
  Correct — writers are fully serialized (like a synchronized list); the entire benefit of this
  structure is on the read side.

## 8. Exercises

No dedicated pattern exercise for this topic — the concept (snapshot iteration, copy-on-write
trade-offs) is best internalized by reading and running
`CopyOnWriteArrayListDemo.java` and reasoning about the interview questions above.

## 9. Quick recap

- Every write copies the whole backing array and atomically (volatile) swaps it in; reads never
  lock.
- Iterators are snapshots of the array at `iterator()` time — never throw CME, never see later
  writes, and are read-only (`remove()` on the iterator throws).
- Best for read-heavy/write-rare shared lists (listener lists, small rarely-changing config);
  wrong choice for write-heavy structures (O(n) per write).
- A single internal lock still fully serializes writers against each other — no write-side
  concurrency benefit.
- Contrast with `Collections.synchronizedList`: that one locks reads too; this one never locks
  reads but pays O(n) per write.
