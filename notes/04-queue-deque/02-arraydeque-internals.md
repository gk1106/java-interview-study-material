# Deque &amp; ArrayDeque internals

## 1. What it is

`Deque<E>` ("double-ended queue") extends `Queue` with explicit first/last-end operations
(`addFirst`/`addLast`, `removeFirst`/`removeLast`, `peekFirst`/`peekLast`), so it can act as a
**stack** (LIFO, via `push`/`pop` = `addFirst`/`removeFirst`) or a **queue** (FIFO, via
`offer`/`poll` = `addLast`/`removeFirst`) through the same object. `ArrayDeque<E>` is the
resizable-array `Deque` implementation the JDK recommends over both the legacy `Stack` class and
`LinkedList` for stack/queue use.

## 2. How it works internally

Backed by a single `Object[] elements` array treated as a **circular buffer**, with two index
fields:
- `head` -- the index of the current first (front) element.
- `tail` -- the index just past the current last (back) element (the next free slot for
  `addLast`).

Elements logically occupy the range from `head` to `tail`, wrapping around the end of the array
back to index 0 as needed -- hence "circular."

```
capacity 4, elements = [ A | B | C | _ ]
                          ^head       ^tail
addLast(D):  [ A | B | C | D ]     tail wraps to 0 -> array is now full (head==tail after insert)
addFirst(Z): head steps BACKWARD (wrapping past index 0 to the last index) before writing
```

**Insert at either end is O(1)**:
- `addLast(e)`: `elements[tail] = e; tail = (tail + 1) mod capacity;`
- `addFirst(e)`: `head = (head - 1 + capacity) mod capacity; elements[head] = e;`

**Remove at either end is O(1)**:
- `removeFirst()`: read `elements[head]`, null it out (avoid loitering references), then
  `head = (head + 1) mod capacity`.
- `removeLast()`: `tail = (tail - 1 + capacity) mod capacity`, read/null `elements[tail]`.

**Growth (doubling, not 1.5x):** when an insert would make `head == tail` (the array is full --
this state is otherwise ambiguous with "empty," which is also `head == tail`), `ArrayDeque`
reallocates a new array at roughly **double** the old capacity, copies the elements out starting
at `head` so they land at index 0 of the new array in logical order, and resets `head = 0`,
`tail = oldSize`. This is a different growth policy from `ArrayList`'s ~1.5x (topic 03-list) --
doubling trades more wasted headroom for fewer, less-frequent copies.

Conceptually (and true for most JDK versions through Java 17), the backing array's length is
kept a **power of two**, which lets wraparound be computed with a fast **bitmask**
(`index & (capacity - 1)`) instead of a `%` (modulo) operation -- integer division/modulo is
noticeably slower than a bitwise AND on most hardware. Some later JDK revisions have simplified
parts of this internally, but the core idea -- circular array, doubling growth, O(1) at both
ends -- is unchanged across versions. A related fact worth knowing: `ArrayDeque` does **not**
keep a separate `size` field at all in most JDK versions -- `size()` is computed on demand as
`(tail - head) & (capacity - 1)`, relying on the power-of-two capacity.

**Why `ArrayDeque` beats `LinkedList` and `Stack` for stack/queue use:**
- vs. **`LinkedList`**: `LinkedList` allocates a `Node` object (value + two pointers) per
  element -- extra allocations, GC pressure, and pointer-chasing that defeats CPU cache
  locality. `ArrayDeque`'s contiguous backing array means sequential memory access, no per-element
  allocation, and typically 2-3x better throughput in practice for pure stack/queue workloads.
- vs. **`Stack`**: `Stack extends Vector`, which **synchronizes every method** (a legacy design
  from Java 1.0, before `java.util.concurrent` existed) even in single-threaded use --
  pure lock-acquisition overhead with zero benefit if you don't need thread safety. `ArrayDeque`
  has no synchronization at all.
- The JDK's own `ArrayDeque` Javadoc explicitly recommends it over both `Stack` (for LIFO use)
  and `LinkedList` (for FIFO use) "when used as a stack" / "when used as a queue."

**Null elements are forbidden**: `addFirst(null)`/`addLast(null)` throw `NullPointerException`
immediately, for the same reason covered in topic 1 -- `null` is `poll()`/`peek()`'s "empty"
sentinel.

**Not thread-safe**: no internal synchronization; concurrent structural modification from
multiple threads without external synchronization has undefined behaviour (not even reliably
fail-fast). Use `ConcurrentLinkedDeque` (lock-free, unbounded) or `LinkedBlockingDeque` (bounded,
blocking) for concurrent access instead.

## 3. Complexity

| Operation | Time | Space | Notes |
|---|---|---|---|
| `addFirst`/`addLast` | O(1) amortized | O(1) amortized extra | worst case O(n) only on the doubling-growth call |
| `removeFirst`/`removeLast` | O(1) | O(1) | just index arithmetic + null-out |
| `peekFirst`/`peekLast` | O(1) | O(1) | direct array read at `head`/`tail-1` |
| `push`/`pop` (stack use) | O(1) amortized | O(1) | aliases for `addFirst`/`removeFirst` |
| `offer`/`poll` (queue use) | O(1) amortized | O(1) | aliases for `addLast`/`removeFirst` |
| `contains(Object)` / `remove(Object)` | O(n) | O(1) | linear scan, no index-based lookup |
| `size()` | O(1) | O(1) | computed from `head`/`tail`, no stored counter |
| iteration | O(n) | O(1) | cache-friendly (contiguous array) |

## 4. Example code

- Runnable class: `src/main/java/com/gk/study/queuedeque/examples/ArrayDequeInternalsDemo.java`
  -- Part 1 uses the real `ArrayDeque` as both a stack and a queue; Part 2 is a small,
  from-scratch teaching model (`RingBufferModel`, NOT reflection into the real JDK class) that
  makes the `head`/`tail` index arithmetic and doubling growth visible step by step.

Expected console output (Part 2, capacity starts at 4):
```
addLast A,B,C
  raw=[H:A, B, C, T:_] capacity=4 head=0 tail=3 size=3
  logical order=[A, B, C]
removeFirst() drops A, head advances
  raw=[_, H:B, C, T:_] capacity=4 head=1 tail=3 size=2
  logical order=[B, C]
addFirst Z, head steps back by one
  raw=[H:Z, B, C, T:_] capacity=4 head=0 tail=3 size=3
  logical order=[Z, B, C]
addFirst Y, head WRAPS from 0 to capacity-1
  raw=[Z, B, C, H:T:Y] capacity=4 head=3 tail=3 size=4
  logical order=[Y, Z, B, C]
addLast D -> size==capacity triggers doubling growth BEFORE the insert
  raw=[H:Y, Z, B, C, D, T:_, _, _] capacity=8 head=0 tail=5 size=5
  logical order=[Y, Z, B, C, D]
addLast E, plenty of room now
  raw=[H:Y, Z, B, C, D, E, T:_, _] capacity=8 head=0 tail=6 size=6
  logical order=[Y, Z, B, C, D, E]
```
Note the `addFirst Y` step: `head == tail == 3` afterward, yet the deque holds 4 elements, not
0 -- exactly the ambiguity that forces the real `ArrayDeque` to grow **immediately** the instant
an insert makes `head == tail`, rather than waiting for the "next" insert to notice it's full.

## 5. When to use / when NOT to use

- Use `ArrayDeque` as your **default stack and default queue** implementation in new code --
  it's faster than both `Stack` and `LinkedList` for these roles and has no synchronization
  overhead.
- Use `ArrayDeque` for sliding-window algorithms (monotonic deque, topic 5) where you need O(1)
  push/pop at both ends.
- Do NOT use `ArrayDeque` when you need random access by index (`get(i)`) -- `Deque` has no such
  method; use `ArrayList` instead.
- Do NOT use `ArrayDeque` across threads without external synchronization or a concurrent
  alternative (`ConcurrentLinkedDeque`, `LinkedBlockingDeque`).
- Do NOT store `null` in an `ArrayDeque` -- it throws `NullPointerException` immediately, by
  design.

## 6. Common pitfalls & gotchas

**Assuming `ArrayDeque` allows `null` like `LinkedList` does:**
```java
Deque<String> d = new ArrayDeque<>();
d.addLast(null);   // throws NullPointerException immediately -- fail fast, not a silent bug
```

**Using the legacy `Stack` class out of habit** (it's synchronized and extends the equally
legacy `Vector`, both relics of pre-collections-framework Java):
```java
Stack<Integer> s = new Stack<>();        // works, but pays synchronization overhead for nothing
Deque<Integer> s = new ArrayDeque<>();   // preferred: push/pop, no locking
```

**Confusing `removeFirst()`/`removeLast()` (throw on empty) with `pollFirst()`/`pollLast()`
(return null on empty)** -- the same throw-vs-sentinel duality from topic 1 applies at both ends
of a `Deque`, not just to the `Queue`-inherited single-ended methods.

**Iterating and mutating concurrently** -- like `ArrayList`, `ArrayDeque`'s iterators are
fail-fast (backed by a `modCount`-equivalent check), so structurally modifying the deque during
a for-each loop throws `ConcurrentModificationException`; use `Iterator.remove()` or collect
indices/values to remove afterward instead.

## 7. Interview questions

- [Basic] What data structure backs `ArrayDeque`, and why is it called "circular"? → A single
  resizable `Object[]` array, with `head` and `tail` index fields; elements wrap around the end
  of the array back to index 0 as items are added/removed at either end, so the "occupied
  region" conceptually forms a ring rather than always starting at index 0. → Follow-up: *Why
  not just shift all elements left/right like `ArrayList` does on insert/remove?* That would
  make `addFirst`/`removeFirst` O(n) (a full shift); the circular design keeps both ends O(1) by
  never needing to move existing elements, only the two index pointers.
- [Basic] Why does the JDK recommend `ArrayDeque` over `Stack` for LIFO use? → `Stack extends
  Vector`, and every `Vector`/`Stack` method is `synchronized`, adding lock-acquisition overhead
  even in single-threaded code with no concurrency benefit; `ArrayDeque` has no synchronization
  at all and is faster. → Follow-up: *Is `Stack` deprecated?* Not formally deprecated, but it's
  a legacy class from Java 1.0 that the Javadoc itself now points away from in favor of `Deque`
  implementations.
- [Basic] Why does the JDK recommend `ArrayDeque` over `LinkedList` for queue use? →
  `LinkedList` allocates a `Node` object per element (extra memory + GC pressure) and accessing
  successive elements means pointer-chasing through scattered heap memory, hurting CPU cache
  locality; `ArrayDeque`'s contiguous array avoids both costs. → Follow-up: *Is there any
  scenario where `LinkedList` is still preferable?* When you need to hold references to interior
  nodes for O(1) removal from the middle given a node reference (not by value/index) -- not a
  common need, and even then a custom structure is often better.
- [Intermediate] How does `ArrayDeque` grow when it's full, and how does that differ from
  `ArrayList`'s growth? → `ArrayDeque` doubles capacity (roughly `oldCapacity * 2`), copying
  elements out starting at `head` so they land at index 0 in logical order in the new array;
  `ArrayList` grows by ~1.5x (`oldCapacity + oldCapacity/2`). → Follow-up: *Why might a
  Deque/stack-like structure prefer more aggressive (2x) growth than a general-purpose list?*
  Stack/queue workloads often grow and shrink repeatedly near a capacity boundary; more headroom
  per growth event amortizes the cost of frequent doubling-growth cycles better, at the expense
  of more wasted memory on average.
- [Intermediate] Why must `ArrayDeque` grow the instant `head == tail` after an insert, rather
  than waiting until the next insert finds it "full"? → Because `head == tail` is used to mean
  **both** "empty" (before any insert) and, if left unchecked, would also result from a full
  buffer wrapping completely around -- an ambiguous state. By growing immediately, the invariant
  "head == tail always means empty, never full" is preserved. → Follow-up: *Could you instead
  keep a separate boolean or counter to disambiguate, avoiding the eager-growth requirement?*
  Yes -- that's exactly what many textbook circular-queue implementations do (and what this
  module's `MyCircularQueue`, topic 6, does with a `count` field) -- it's a valid alternative
  design trade-off (a few extra bytes of state vs. eager reallocation).
- [Intermediate] Can you use `ArrayDeque` as a `Queue` (FIFO) and as a `Deque`/stack (LIFO) at
  the same time on the same instance? → No -- it's the same object with both APIs available, but
  *which* methods you call determines the access pattern; calling `offer`/`poll` (queue) and
  `push`/`pop` (stack) on the same instance interchangeably would mix FIFO and LIFO semantics
  and almost certainly produce logic bugs, even though it compiles. → Follow-up: *Is this a
  design flaw?* Not really -- it reflects that a deque is a strict superset of both stack and
  queue capability; the caller is responsible for using it consistently for one role.
- [Advanced] Why does `ArrayDeque` avoid storing an explicit `size` field, computing it instead
  as `(tail - head) & (capacity - 1)`? → It saves 4 bytes of per-instance state and one field
  update per insert/remove; the computation is cheap (one subtraction and one bitmask) and
  correct precisely because capacity is a power of two, making the bitmask equivalent to modulo
  arithmetic. → Follow-up: *Would this trick still work if capacity weren't a power of two?* No
  -- the bitmask shortcut for modulo (`x & (n-1)` == `x % n`) only holds when `n` is a power of
  two; with an arbitrary capacity you'd need actual `%`, which is slower, or you'd need to keep
  an explicit size field instead.
- [Advanced] Why is a bitmask (`index & (capacity - 1)`) faster than `index % capacity` for
  wraparound, and does the JIT not already optimize this? → Integer division/modulo is one of
  the few arithmetic operations still meaningfully slower than a shift/AND on typical CPUs (no
  single-cycle divider on most architectures, unlike add/AND/shift); the JIT *can* strength-reduce
  `% capacity` to a bitmask **only if it can prove capacity is a power of two at that point**,
  which is easy for a compile-time constant but harder across a mutable field whose value changes
  at runtime (growth events) -- so `ArrayDeque` performs the optimization explicitly in source
  rather than relying on the JIT to infer it. → Follow-up: *Does this level of micro-optimization
  matter for typical application code?* Rarely for application-level logic, but it matters a lot
  for a foundational, extremely hot JDK collection class used everywhere internally (e.g. as the
  default work-stealing deque implementation pattern, iterator machinery, etc.) where the
  operation is called billions of times across the ecosystem.

## 8. Exercises

Deque-specific practice exercises live in
`notes/04-queue-deque/05-dsa-patterns-queue-deque.md` (stack-via-Deque, monotonic deque) and
`notes/04-queue-deque/06-build-it-yourself-queue-heap.md` (`MyCircularQueue`, a from-scratch
circular array).

## 9. Quick recap

- `ArrayDeque` = one `Object[]` array + `head`/`tail` indices, treated as a circular buffer;
  O(1) insert/remove at both ends.
- Growth is **doubling** (not `ArrayList`'s 1.5x), triggered the instant `head == tail` after an
  insert -- because that state would otherwise be ambiguous with "empty."
- Historically capacity is kept a power of two so wraparound uses a fast bitmask instead of
  modulo; `size()` is computed on demand, not stored.
- Preferred over `Stack` (synchronized, legacy `Vector` subclass) for LIFO and over `LinkedList`
  (per-node allocation, poor cache locality) for FIFO -- the JDK's own Javadoc says so.
- Forbids `null` elements (fail-fast `NullPointerException`) and is not thread-safe.
