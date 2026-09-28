# Vector, Stack (legacy) and why ArrayDeque wins

## 1. What it is

`Vector` is `ArrayList`'s synchronized ancestor from Java 1.0. `Stack` extends `Vector` to add
LIFO `push`/`pop`/`peek`. Both predate the Collections Framework's design conventions and are
considered **legacy** — `ArrayDeque` (added in Java 6) is the modern replacement for both
stack and queue use cases.

## 2. How it works internally

`Vector` is structurally almost identical to `ArrayList` (backing `Object[] elementData`, `size`
field, `Arrays.copyOf`-based growth) with one crucial difference: **every public method is
`synchronized`** (`add`, `get`, `size`, ...), acquiring the object's intrinsic monitor on every
call. Its default growth factor is **2x** (doubling) when no explicit `capacityIncrement` is set —
different from `ArrayList`'s 1.5x.

`Stack extends Vector` and adds:
```
push(e)  -> addElement(e)          // append to the end == "top" of the stack
pop()    -> removeElementAt(size-1) after checking non-empty (else EmptyStackException)
peek()   -> elementAt(size-1)
```
Because `Stack` *is a* `Vector` (inheritance, not composition), all of `Vector`'s `List` methods
(`get(0)`, `add(0, e)`, `remove(int)`) remain publicly callable — you can accidentally violate
LIFO discipline by indexing into the "stack" directly, which a well-designed stack type should
prevent entirely.

`ArrayDeque<E>` internals (detailed in module 04): a **circular array** with `head`/`tail`
indices, resizing by doubling when full. No synchronization, no legacy baggage, and it implements
`Deque` so it can be used as *both* a stack (`push`/`pop`/`peek` operate on the head) and a queue
(`offer`/`poll` operate on the tail) with O(1) amortized operations at both ends.

```
Vector/Stack:  every method synchronized -> monitor lock/unlock overhead even single-threaded
ArrayDeque:    unsynchronized, circular array, no inherited List "escape hatches"
```

## 3. Complexity

| Structure | push/pop (top) | add/remove (end) | get(index) | Thread-safe? |
|-----------|-----------------|-------------------|------------|--------------|
| `Vector` | O(1) amortized (as `addElement`) | O(1) amortized | O(1) | yes (synchronized, coarse) |
| `Stack` | O(1) amortized | inherited, same as Vector | O(1) | yes (inherited lock) |
| `LinkedList` as stack | O(1) | O(1) | O(n) | no |
| `ArrayDeque` as stack | O(1) amortized | O(1) amortized | not supported (not a `List`) | no |

`Vector`/`Stack`'s "thread-safe" is **coarse-grained and still not enough** for compound actions
(check-then-act) — see pitfalls.

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/VectorStackArrayDequeDemo.java`

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(1);
stack.push(2);
stack.push(3);
System.out.println(stack.pop());   // 3
System.out.println(stack.peek());  // 2

Deque<Integer> queue = new ArrayDeque<>();
queue.offer(1);
queue.offer(2);
System.out.println(queue.poll());  // 1 (FIFO)
```
Expected console output:
```
3
2
1
```

## 5. When to use / when NOT to use

- **Never choose `Vector` or `Stack` in new code** — they're kept only for backward
  compatibility with pre-Java-5 APIs. For a synchronized list today, use
  `Collections.synchronizedList(new ArrayList<>())` (explicit, same coarse-lock trade-off but
  opt-in) or better, a concurrent collection suited to the access pattern
  (`CopyOnWriteArrayList`, `ConcurrentLinkedDeque`).
- Use `ArrayDeque` for **any** stack or FIFO-queue need — it is faster (no synchronization
  overhead), has a smaller memory footprint than `LinkedList` (no per-node object), and its type
  (`Deque`, not `List`) prevents the "index into my stack" misuse that `Stack extends Vector`
  permits.
- Use `LinkedList` as a `Deque` only when you specifically also need `List` semantics
  (positional access) on the *same* object, which is rare.

## 6. Common pitfalls & gotchas

**`Stack` lets you break LIFO discipline because it's a `Vector`:**
```java
Stack<Integer> stack = new Stack<>();
stack.push(1); stack.push(2); stack.push(3);
stack.get(0);          // legal! reads the BOTTOM of the "stack" — inheritance leak
stack.add(0, 99);       // legal! inserts at the bottom, corrupting LIFO order
// fix: use Deque<Integer> stack = new ArrayDeque<>(); — push/pop/peek only, no index methods
```

**`Vector`'s per-method synchronization doesn't make compound operations safe:**
```java
if (!vector.isEmpty()) {          // check
    Object last = vector.remove(vector.size() - 1); // act — another thread could empty it in between!
}
// fix: synchronize the whole check-then-act block yourself, or use a proper concurrent structure
synchronized (vector) {
    if (!vector.isEmpty()) vector.remove(vector.size() - 1);
}
```

**Assuming `ArrayDeque` allows `null`** — it explicitly forbids `null` elements (throws
`NullPointerException` on `add(null)`), specifically so `peek()`/`poll()` returning `null` can
unambiguously mean "empty". `LinkedList` allows `null`, which is one more reason `ArrayDeque` is
the cleaner choice.

## 7. Interview questions

- [Basic] What's the difference between `Vector` and `ArrayList`? → Structurally almost
  identical (array-backed, similar growth strategy) but `Vector`'s methods are all
  `synchronized`, adding lock overhead on every call even in single-threaded code; `ArrayList` is
  unsynchronized. → Follow-up: *Is Vector thread-safe for all use cases then?* No — individual
  method calls are atomic, but compound operations (check-then-act, iterate-then-modify) still
  need external synchronization.
- [Basic] Why is `Stack` considered a design mistake in the JDK? → It extends `Vector`, inheriting
  all of `List`'s index-based methods, which lets callers violate LIFO ordering by reading or
  inserting at arbitrary indices — a stack's whole contract should be "only the top is
  accessible". → Follow-up: *What's the modern replacement?* `Deque<T> stack = new
  ArrayDeque<>();` using only `push`/`pop`/`peek`.
- [Basic] Why is `ArrayDeque` preferred over `LinkedList` for a queue/stack? → No per-node object
  allocation (uses a circular array), better cache locality, no synchronization overhead, and its
  type doesn't leak unrelated `List` methods. → Follow-up: *Does ArrayDeque support indexed
  access like get(i)?* No — it doesn't implement `List` at all, by design.
- [Intermediate] How does `ArrayDeque` achieve O(1) `addFirst` on a plain array (no shifting)? →
  It's a **circular buffer**: `head` and `tail` indices wrap around the array's bounds using
  modulo (bitmask, since capacity is kept a power of two), so adding at the front just decrements
  `head` (wrapping if needed) instead of shifting every element. → Follow-up: *What happens when
  the array fills up?* It doubles in size and re-lays the elements out linearly starting at index
  0, similar in spirit to `ArrayList`'s grow but for a ring buffer.
- [Intermediate] Why does `Vector`'s default doubling (2x) differ from `ArrayList`'s 1.5x, and
  does it matter? → They were designed independently/at different times; doubling wastes more
  average headroom memory for large collections than 1.5x growth, but neither difference matters
  much in practice today since `Vector` shouldn't be used for new code anyway. → Follow-up: *Is
  Vector deprecated?* Not formally `@Deprecated`, but universally documented and treated as
  legacy — kept only for backward compatibility.
- [Intermediate] Give a concrete banking-code smell that signals someone should switch `Stack` to
  `ArrayDeque`. → A class named `TransactionUndoStack` that occasionally does `stack.get(i)` for
  "peek at the Nth-from-top item" — that's a sign the abstraction is leaking; if random access is
  genuinely needed, model it explicitly (e.g. `List` + separate top-pointer), otherwise switch to
  `Deque` and remove the temptation. → Follow-up: *What if you truly need bounded peek-N-deep
  access?* Either keep a small explicit index-accessible buffer for that specific need, or expose
  a `peek(int depthFromTop)` helper method on your own wrapper type instead of the raw structure.
- [Advanced] Why is `synchronized` on every `Vector` method considered coarse-grained locking, and
  what's the performance cost? → Every single call (even reads) acquires/releases the intrinsic
  monitor, which serializes all access across threads (no concurrent reads) and adds
  uncontended-lock overhead (a few nanoseconds, but non-zero and JIT-limits some optimizations)
  even when there's no actual contention (i.e., single-threaded use still pays the tax). →
  Follow-up: *What's a finer-grained alternative for a mostly-read list shared across threads?*
  `CopyOnWriteArrayList` (topic 6) — reads take no lock at all.
- [Advanced] Under what circumstances would `Collections.synchronizedList(new ArrayList<>())`
  still throw `ConcurrentModificationException`? → Iteration is **not** automatically protected
  by the wrapper's per-method locking — the documentation requires the caller to manually
  `synchronized(list) { for (...) ... }` around the entire iteration, otherwise a concurrent
  structural modification from another thread during iteration still trips the fail-fast
  iterator. → Follow-up: *Does CopyOnWriteArrayList have this problem?* No — its iterator is a
  snapshot, immune to concurrent modification by design (see topic 6).

## 8. Exercises

Legacy-class exercises aren't meaningful DSA practice; no dedicated exercise file for this topic.
Use the `ArrayDeque`-as-stack idiom (`push`/`pop`/`peek`) directly in later modules — module
`04-queue-deque` builds full exercises around `ArrayDeque`/`Deque` patterns (valid parentheses,
next-greater-element).

## 9. Quick recap

- `Vector` = synchronized `ArrayList` (2x growth); `Stack` = `Vector` + push/pop/peek — both
  legacy, avoid in new code.
- `Stack extends Vector` leaks index-based `List` methods, breaking LIFO encapsulation.
- `ArrayDeque` (circular array) is the modern stack/queue: faster, less memory, no unsafe leaks,
  rejects `null`.
- Per-method synchronization ≠ safety for compound (check-then-act) operations.
- For shared-across-threads stack/queue needs, prefer purpose-built concurrent structures over
  `Vector`/`Stack`/`synchronizedList`.
