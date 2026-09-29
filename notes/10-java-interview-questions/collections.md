# Collections framework interview questions

Cross-cutting drill on List/Set/Map/Queue — the deep internals (bucket arrays, red-black trees,
resize algorithms, circular buffers) live in `notes/03-list/` through `notes/06-map/`; this file is
the rapid-fire interview version plus the comparisons interviewers love to ask across collection
types. Tags: `[Basic]` / `[Intermediate]` / `[Advanced]`.

Sections: [List](#1-list) · [Set](#2-set) · [Map](#3-map) · [Queue/Deque](#4-queuedeque)
· [Iteration, fail-fast vs fail-safe](#5-iteration-fail-fast-vs-fail-safe)
· [Comparable vs Comparator](#6-comparable-vs-comparator) · [Choosing a collection](#7-choosing-the-right-collection)
· [Predict-the-output puzzles](#8-predict-the-output-puzzles)

---

## 1. List

**[Basic] `ArrayList` vs `LinkedList` — how do you decide, and why do most people over-reach for
`LinkedList`?**
`ArrayList` is backed by a resizable array: O(1) amortized append, O(1) random access (`get(i)`),
but O(n) insert/remove at an arbitrary index (must shift elements) — it grows by roughly 1.5x when
full via `System.arraycopy`. `LinkedList` is a doubly-linked list: O(1) insert/remove **once you
already have a reference to the node** (e.g. via a `ListIterator`), but O(n) random access
(`get(i)` walks from whichever end is closer) and worse cache locality (nodes scattered across the
heap vs a contiguous array, so `ArrayList` iteration is dramatically faster in practice due to CPU
cache prefetching, despite both being "O(n)" for a full scan). In practice `ArrayList` wins the
overwhelming majority of real use cases — default to it; reach for `LinkedList` only when you
specifically need cheap insert/remove at both ends with iterator-based access patterns (and even
then, `ArrayDeque` usually beats it — see below).
*Follow-up: is `LinkedList` still commonly used in modern code?* Rarely — `ArrayDeque` is faster for
stack/queue use (no per-node object overhead, better cache locality) and is now the JDK's own
recommended replacement for both `Stack` and most `LinkedList` use cases.

**[Basic] Why does `ArrayList.remove(int)` behave differently from `ArrayList.remove(Object)`, and
what's the classic bug this causes?**
`remove(int index)` removes the element **at that index**; `remove(Object o)` removes the **first
element equal to `o`**. With a `List<Integer>`, calling `list.remove(1)` is ambiguous to a human but
not to the compiler — `1` is a primitive `int` literal, so it resolves to the `remove(int index)`
overload (removes index 1), **not** `remove(Object)` (which would remove the boxed value `1` if it
appeared). To remove the *value* `1` from a `List<Integer>`, you must box it explicitly:
`list.remove(Integer.valueOf(1))` or `list.remove((Integer) 1)`.
*Follow-up: does this ambiguity exist for `List<String>`?* No — there's no overload conflict there
because a `String` can never satisfy the `int` parameter, so `list.remove("x")` unambiguously calls
`remove(Object)`.

**[Basic] What does `Arrays.asList(...)` actually return, and what's the classic gotcha?**
It returns a **fixed-size** `List` view **backed directly by the given array** — not a real
`ArrayList`, and not resizable: calling `.add()` or `.remove()` on it throws
`UnsupportedOperationException`. Because it's a *view*, mutating an element through the list
(`set(i, x)`) writes through to the backing array, and vice versa — a rarely-intended side effect.
```java
Integer[] arr = {1, 2, 3};
List<Integer> list = Arrays.asList(arr);
list.set(0, 99);
System.out.println(arr[0]);   // 99 -- the list is a live view over the array
list.add(4);                  // throws UnsupportedOperationException
```
*Follow-up: how do you get a genuinely independent, mutable `ArrayList` from an array?* Wrap it:
`new ArrayList<>(Arrays.asList(arr))`, or Java 10+: `new ArrayList<>(List.of(arr))`.

**[Intermediate] How does `ArrayList` grow internally, and what's the cost of that growth
amortized?**
When the backing array is full and a new element is added, `ArrayList` allocates a **new** array —
capacity roughly **1.5x** the old capacity (`oldCapacity + (oldCapacity >> 1)`, not 2x like some
other languages' dynamic arrays) — and copies every existing element into it via
`System.arraycopy` (a fast native bulk copy), then discards the old array. A single grow-and-copy
is O(n), but because growth happens exponentially less often as the list gets larger, the
**amortized** cost per `add()` across many additions works out to O(1) — the same amortized
argument used for `HashMap` resizing.
*Follow-up: how would you avoid several of these resize-copies if you know you'll add 10,000
elements?* Pre-size it: `new ArrayList<>(10_000)`, which allocates the backing array at that
capacity up front, avoiding every intermediate grow-and-copy step.

**[Basic] `List.of(...)` vs `Arrays.asList(...)` vs `new ArrayList<>(...)` — three different
mutability levels, what are they?**
`List.of(...)` (Java 9+) returns a genuinely **immutable** list — `add`, `remove`, and `set` all
throw `UnsupportedOperationException`, and unlike `Arrays.asList`, it also rejects `null` elements
outright (`NullPointerException` at creation). `Arrays.asList(...)` returns a **fixed-size**
mutable-in-place view backed by the array — `set(i, x)` works and writes through to the array, but
`add`/`remove` throw (size can't change). `new ArrayList<>(...)` is a fully independent, fully
mutable copy — no ties back to any source array. Use `List.of` for constants/defensive API returns,
`new ArrayList<>(...)` when the caller needs to actually mutate the list.
*Follow-up: does `List.of()` allocate memory proportional to the number of elements even for a
0- or 1-element list?* No — the JDK provides specialized zero-arg, one-arg, and two-arg
implementations (`ImmutableCollections.List0/List1/List2`) to avoid unnecessary array allocation for
the common small-list case, falling back to a general array-backed implementation for larger lists.

**[Intermediate] What's the `subList` gotcha that catches people off guard?**
`list.subList(from, to)` returns a **view** backed by the original list, not an independent copy —
structural modifications made through the sublist (add/remove) are written through to the original
list (and vice versa), and any **structural** modification made directly to the original list
outside the sublist's own methods invalidates the sublist view, throwing
`ConcurrentModificationException` on its next use.
```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
List<Integer> view = list.subList(1, 4);   // view over [2,3,4]
list.add(99);                               // structural change to the PARENT list
view.get(0);                                // throws ConcurrentModificationException
```
*Follow-up: how do you get an independent copy instead of a view?* Wrap it: `new
ArrayList<>(list.subList(1, 4))`.

---

## 2. Set

**[Basic] `HashSet` vs `LinkedHashSet` vs `TreeSet` — what does each guarantee and cost?**
`HashSet` — backed by a `HashMap<E, Object>` internally (each element stored as a key mapped to a
shared dummy value); O(1) average add/remove/contains; **no** ordering guarantee at all, and
iteration order can change across resizes. `LinkedHashSet` — extends `HashSet`, additionally
threads a doubly-linked list through entries to preserve **insertion order**; same O(1) average
operations, small constant-factor overhead for the extra linking. `TreeSet` — backed by a
`TreeMap` (red-black tree); O(log n) add/remove/contains, but always iterates in **sorted** order
(natural ordering via `Comparable`, or a supplied `Comparator`) and additionally implements
`NavigableSet` (`floor`, `ceiling`, `higher`, `lower`, `first`, `last`, range views).
*Follow-up: if you need both insertion-order iteration AND O(1) lookups, which do you pick?*
`LinkedHashSet` — it's the only one of the three giving both.

**[Basic] Can you put a mutable object in a `HashSet` safely?**
Only if you never mutate the fields it uses for `hashCode()`/`equals()` while it's a member of the
set. Mutating those fields in place after insertion strands the object in the bucket computed from
its **old** hash — subsequent `contains()`/`remove()` calls (which recompute the hash from the
object's **current** state) look in the wrong bucket and silently fail to find it, even though
iterating the set will still show the object sitting there. This mirrors the identical `HashMap`
mutable-key pitfall (see `notes/06-map/01-hashmap-internals.md`) since `HashSet` is a thin wrapper
over `HashMap`.
*Follow-up: what's the safe pattern?* Use immutable value objects (records, or classes with only
`final` fields relevant to `equals`/`hashCode`) as set elements, or remove-mutate-reinsert instead
of mutating in place.

**[Intermediate] How would you find the intersection, union, and difference of two `Set`s using
only the `Set` API (no manual loops)?**
```java
Set<Integer> a = new HashSet<>(List.of(1, 2, 3));
Set<Integer> b = new HashSet<>(List.of(2, 3, 4));
Set<Integer> union = new HashSet<>(a);        union.addAll(b);        // {1,2,3,4}
Set<Integer> intersection = new HashSet<>(a); intersection.retainAll(b); // {2,3}
Set<Integer> difference = new HashSet<>(a);   difference.removeAll(b);  // {1}
```
Always copy into a new `Set` first (`new HashSet<>(a)`) before calling `retainAll`/`removeAll`/
`addAll` destructively — mutating `a` or `b` in place is usually not what you want and surprises
callers holding those references.
*Follow-up: what's the time complexity of `retainAll` here?* Roughly O(size of the smaller set),
since it needs to check membership of each of its own elements in the other set — each check is an
O(1) average `HashSet.contains()`.

**[Advanced] Why does `EnumSet` outperform `HashSet<SomeEnum>` so dramatically, and what's the
trade-off?**
`EnumSet` is implemented internally as a **bit vector** (a `long` for up to 64 enum constants —
`RegularEnumSet`; a `long[]` for more — `JumboEnumSet`), where each enum constant's `ordinal()`
maps directly to a bit position. Add/remove/contains become single bitwise operations (`OR`, `AND
NOT`, bit test) — extremely fast, and the whole set uses a handful of bytes regardless of how many
constants are present, versus `HashSet`'s per-element `Node` object overhead and hashing cost. The
trade-off: it only works for `enum` types (the API is generic over `Enum<E>` specifically), and
iteration order always follows the enum's natural declaration order, not insertion order.
*Follow-up: is `EnumSet` thread-safe?* No — like `HashSet`, it requires external synchronization for
concurrent mutation (`Collections.synchronizedSet(EnumSet...)` if needed).

---

## 3. Map

**[Basic] Walk through what happens on `map.put(key, value)` for a `HashMap` at a high level (see
`notes/06-map/01-hashmap-internals.md` for the full byte-by-byte version).**
Compute a spread hash from `key.hashCode()`, derive a bucket index via `(capacity-1) & hash`,
insert into that bucket (directly if empty, appended to a chain/tree if occupied, or overwrite the
value if an equal key is already there), then resize (double capacity) if the map's size now
exceeds `capacity * loadFactor` (default 0.75). A single bucket's chain treeifies into a red-black
tree once it reaches 8 entries (if total capacity ≥ 64), bounding worst-case lookup at O(log n)
instead of O(n) for a pathologically hash-colliding key set.
*Follow-up: what's the point of the load factor being 0.75 rather than, say, 1.0?* It's a
memory/collision-rate trade-off — a load factor of 1.0 would pack the table denser (less wasted
array space) but sharply increase average chain length and thus average lookup cost; 0.75 keeps
average chain length short while not wasting too much space, and is the JDK's long-standing tuned
default.

**[Basic] Why can't `HashMap` have more than one `null` key, but can have many `null` values?**
A `Map`'s keys form a set by contract — duplicate keys aren't meaningful, `put(null, x)` twice just
overwrites the single `null`-keyed entry's value, exactly like any other key. `HashMap` special-cases
a `null` key by forcing its hash to `0` (bucket 0), since `null.hashCode()` would NPE. Values have no
such uniqueness constraint at all — any number of distinct keys can map to `null` values, or to the
same non-null value.
*Follow-up: does `Hashtable` or `ConcurrentHashMap` allow null keys/values?* No — both reject `null`
keys and `null` values outright (`NullPointerException` on `put`), specifically because in a
concurrent map, `map.get(key) == null` is ambiguous between "no such mapping" and "mapping to a
null value," and resolving that ambiguity safely under concurrency (re-checking with `containsKey`)
isn't atomic — so the JDK designers simply forbade nulls in the concurrent maps to remove the
ambiguity entirely.

**[Intermediate] `HashMap` vs `Hashtable` vs `ConcurrentHashMap` — compare thread-safety strategy
and performance.**
`HashMap` — not thread-safe at all, no synchronization; fastest for single-threaded use.
`Hashtable` — legacy (pre-Java-1.2), every method `synchronized` on the whole table object — safe
but a coarse global lock serializes *all* access, even concurrent reads, making it a throughput
bottleneck under contention; also forbids null keys/values. `ConcurrentHashMap` — modern (Java 8+)
concurrent map using much finer-grained synchronization: CAS (compare-and-swap) operations for
uncontended bucket-head insertion, and `synchronized` only on the **specific bin (bucket)** being
modified when a collision occurs — reads are largely lock-free. This gives dramatically better
throughput under concurrent access than `Hashtable` or a globally `synchronized` `HashMap` wrapper,
while still being fully thread-safe. See `notes/06-map/04-concurrenthashmap.md` for the full
internals (including why iterators are weakly consistent, not fail-fast).
*Follow-up: is `Collections.synchronizedMap(new HashMap<>())` the same as `Hashtable`?* Functionally
similar (both use one coarse lock for the whole map), but `synchronizedMap` is a wrapper you apply
explicitly to any `Map`, and compound operations (`if (!map.containsKey(k)) map.put(k, v)`) still
need external synchronization on the returned map's own monitor to be atomic — same caveat applies
to `Hashtable`.

**[Intermediate] `HashMap` vs `TreeMap` vs `LinkedHashMap` — when do you pick each?**
`HashMap` — default choice, O(1) average, no ordering. `LinkedHashMap` — O(1) average like
`HashMap`, plus predictable iteration order (insertion order by default, or **access order** if
constructed with `accessOrder=true`, which is exactly the building block for an LRU cache via
overriding `removeEldestEntry`). `TreeMap` — O(log n) operations (red-black tree), always iterates
in **sorted key order**, and implements `NavigableMap` (`floorKey`, `ceilingKey`, `firstEntry`,
`headMap`/`tailMap`/`subMap` range views) — use it when you need sorted iteration or range queries,
not just fast point lookups.
*Follow-up: how would you build a simple LRU cache using just JDK classes?* `new LinkedHashMap<K,V>
(initialCapacity, 0.75f, true)` (the third arg enables access-order) and override
`removeEldestEntry(Map.Entry eldest)` to return `true` once `size() > maxCapacity` — every `get`
moves the accessed entry to the end, and the map auto-evicts the oldest on the next `put` once over
capacity. See `notes/06-map/02-linkedhashmap-lru.md`.

**[Advanced] Why does `ConcurrentHashMap` forbid `null` keys and values, when a plain `HashMap`
allows a single `null` key?**
Because in a genuinely concurrent map, `map.get(key)` returning `null` is ambiguous — it could mean
"no mapping exists for this key" or "the key maps to a stored `null` value." In a **single-threaded**
`HashMap`, you can disambiguate safely by following up with `containsKey(key)`, because nothing else
can have mutated the map between the two calls. In a **concurrent** map, another thread could
insert or remove the key between your `get` and your `containsKey` check, so that
disambiguation pattern is inherently racy/non-atomic — rather than leave a trap, the JDK designers
simply disallowed `null` as a key or value in every concurrent `Map` implementation (`Hashtable`,
`ConcurrentHashMap`, `ConcurrentSkipListMap`), forcing a `NullPointerException` immediately at
`put()` time instead of a subtle race later.
*Follow-up: what would you use instead of storing `null` as a "no value" marker in a
`ConcurrentHashMap`?* A sentinel object (e.g. an `Optional.empty()` value, or a dedicated `NULL`
marker constant), or simply not inserting the key at all and treating absence as the signal.

---

## 4. Queue/Deque

**[Basic] `offer`/`poll`/`peek` vs `add`/`remove`/`element` on `Queue` — what's the actual
difference?**
Both pairs do the same logical operation (insert, remove-and-return-head, view-head-without-removing),
but differ in **failure behavior**: `add`/`remove`/`element` **throw** an exception on failure
(`IllegalStateException` if a capacity-restricted queue is full on `add`, `NoSuchElementException`
if `remove`/`element` is called on an empty queue), while `offer`/`poll`/`peek` return a **special
value** (`false` from `offer` if it couldn't insert, `null` from `poll`/`peek` if the queue is
empty) instead of throwing. Prefer the `offer`/`poll`/`peek` family for capacity-bounded queues
(e.g. a `BlockingQueue` used as a work queue) where "full" or "empty" is a normal, expected
condition, not an exceptional one.
*Follow-up: what does `poll()` return on an empty `LinkedList` used as a `Queue`, versus
`remove()`?* `poll()` returns `null`; `remove()` throws `NoSuchElementException` — same underlying
empty-queue condition, different reporting contract.

**[Basic] Why is `ArrayDeque` generally preferred over both `Stack` and `LinkedList` for
stack/queue use today?**
`Stack` is a legacy class (extends the synchronized `Vector`), carrying unnecessary synchronization
overhead for the overwhelmingly common single-threaded case, plus an awkward API inherited from
`Vector` (it exposes index-based operations that don't belong on a "pure" stack abstraction).
`ArrayDeque` is backed by a resizable **circular array** (no per-element node/pointer overhead
unlike `LinkedList`, better cache locality), supports O(1) amortized push/pop/offer/poll at **both**
ends, and is unsynchronized (faster for the common single-threaded case). The JDK's own
documentation explicitly recommends `ArrayDeque` over both `Stack` (for stack use, via
`push`/`pop`) and `LinkedList` (for queue use) in modern code.
*Follow-up: does `ArrayDeque` allow `null` elements?* No — `null` is used internally as a sentinel
to mark empty slots in the circular array, so inserting `null` is explicitly disallowed
(`NullPointerException`).

**[Intermediate] How does `PriorityQueue` guarantee O(log n) insert and O(log n) remove, and why
is `peek()` O(1)?**
It's backed by a **binary heap** stored implicitly in an array (no explicit tree nodes/pointers) —
for a node at array index `i`, its children live at `2i+1` and `2i+2`. Insert (`offer`) appends the
new element at the end of the array, then **sifts it up**: repeatedly swap with its parent while it
violates heap order (default: min-heap, smallest at the root, per natural ordering or a supplied
`Comparator`), at most O(log n) swaps for a tree of height log n. Remove (`poll`) takes the root
(the min/max), moves the **last** array element into the root position, shrinks the array by one,
then **sifts it down**: repeatedly swap with the smaller (min-heap) child while it violates heap
order, again at most O(log n) swaps. `peek()` is simply reading array index 0 — O(1), no traversal
needed, since heap order guarantees the min (or max, per comparator) is always at the root.
*Follow-up: is a `PriorityQueue`'s iterator ordered by priority?* No — iterating a `PriorityQueue`
visits elements in the internal **array's** order (heap layout), not priority/sorted order; only
repeated `poll()` calls give you elements out in priority order.

**[Intermediate] What's the difference between `LinkedBlockingQueue` and `ArrayBlockingQueue`, and
when would you choose each for a producer-consumer setup?**
`ArrayBlockingQueue` is backed by a fixed-size circular array, **must** be given a bounded capacity
at construction, and uses a **single lock** shared by both put and take operations (so a producer
and a consumer contend for the same lock even though they touch conceptually different ends of the
queue). `LinkedBlockingQueue` is backed by linked nodes, can be unbounded (default) or bounded, and
uses **two separate locks** — one for `put`, one for `take` — allowing a producer and a consumer to
proceed truly concurrently without contending on the same lock (better throughput under heavy
concurrent producer+consumer load), at the cost of per-node allocation overhead `ArrayBlockingQueue`
avoids.
*Follow-up: why would you deliberately choose a bounded queue over an unbounded one for a
producer-consumer work queue?* An unbounded queue lets a fast producer/slow consumer pair grow the
queue without limit, risking `OutOfMemoryError` under sustained backpressure — a bounded queue
forces **backpressure**: `put()` blocks the producer once full, which is usually the desired,
self-throttling behavior in a real system (this is exactly why `ThreadPoolExecutor`'s own work
queue sizing matters — see `notes/10-java-interview-questions/concurrency.md`).

**[Intermediate] What is `WeakHashMap` for, and how does it differ from a regular `HashMap` in terms
of when entries disappear?**
`WeakHashMap` holds its **keys** via `WeakReference`s — an entry is automatically and silently
removed (during a GC cycle) once its key is no longer strongly reachable from anywhere else in the
application, even though the map itself still technically "holds" it. This makes it useful for
building caches keyed by objects whose lifecycle you don't want to control/extend (e.g. metadata
associated with a class, or a listener registry keyed by the listener object) — the mapping
disappears naturally once nothing else references the key, without needing explicit cleanup/eviction
logic.
*Follow-up: does the same weak-reference behavior apply to a WeakHashMap's values?* No — only keys
are held weakly by default; a value is kept alive as long as its entry exists, and a value that
itself strongly references its own key back can actually prevent that key from ever becoming weakly
reachable, defeating the intended cleanup (a subtle, documented gotcha).

---

## 5. Iteration, fail-fast vs fail-safe

**[Basic] What is `ConcurrentModificationException`, and why does it happen even in
single-threaded code?**
Most JDK collections (`ArrayList`, `HashMap`, `HashSet`, etc.) are **fail-fast**: they track a
`modCount` (structural modification counter) that increments on every structural change (add,
remove — not a `set(i, x)` element replacement, which isn't "structural"). An `Iterator` captures
`modCount`'s value when created and checks it on every `next()`/`remove()` call; if the collection
was structurally modified through any path **other than the iterator's own `remove()`** since the
iterator was created, it throws `ConcurrentModificationException` immediately — this happens in
single-threaded code just as easily as multi-threaded, e.g. calling `list.remove(x)` directly
inside a `for-each` loop over `list`, because the for-each loop is really iterator-based under the
hood.
```java
List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
for (Integer i : list) {
    if (i == 2) list.remove(i);   // structural change NOT via the iterator -> CME on next hasNext()/next()
}
```
*Follow-up: how do you correctly remove elements while iterating?* Use the iterator's own
`Iterator.remove()` method (`it.remove()` inside a manual `while (it.hasNext())` loop — this
updates `modCount` in a way the iterator itself tracks, so no mismatch is detected), or use
`Collection.removeIf(predicate)`, which handles this internally and safely.

**[Intermediate] Fail-fast vs fail-safe iterators — name a concrete example of each and describe
their actual guarantees (or lack thereof).**
**Fail-fast** (`ArrayList`, `HashMap`, `HashSet`, most `java.util` collections) — detects
concurrent structural modification via `modCount` checking and throws `ConcurrentModificationException`
as a **best-effort** debugging aid, not a hard guarantee (the JDK explicitly documents this
detection is not bulletproof — it's meant to surface bugs, not to be relied on for correctness).
**Fail-safe** (a slight misnomer — better called "weakly consistent"), e.g.
`CopyOnWriteArrayList`, `ConcurrentHashMap`'s iterators, `ConcurrentSkipListMap` — never throw
`ConcurrentModificationException`, because they either iterate over a **snapshot** (`CopyOnWriteArrayList`
takes a copy of the backing array at iterator-creation time — later mutations to the live list are
simply invisible to an iterator already in flight) or tolerate concurrent structural changes by
design (`ConcurrentHashMap`'s iterator reflects the state of the map at some point during (or after)
the iteration's construction, may or may not see a given concurrent update, but never throws and
never corrupts).
*Follow-up: does "fail-safe" mean the iterator always sees a fully up-to-date, consistent view?* No
— it means the iterator won't crash or corrupt state, but it makes **no promise** the view it sees
reflects every concurrent mutation; `CopyOnWriteArrayList` in particular guarantees you see exactly
the snapshot from iterator-creation time, potentially "stale" relative to changes made mid-iteration.

**[Advanced] Is `ConcurrentModificationException` detection reliable enough to depend on for
correctness in concurrent code? Why or why not?**
No — the JDK's own Javadoc explicitly warns that fail-fast behavior is "best-effort" and should
never be relied upon for program correctness, only for bug detection during development. The
underlying `modCount` check is not synchronized/atomic itself; under genuine concurrent
modification (multiple threads), it's entirely possible for the check to race and **not** detect a
concurrent structural change (a classic example: `HashMap`'s pre-Java-8 concurrent-resize bug could
corrupt the map's structure — infinite loop on `get()` — without ever throwing
`ConcurrentModificationException` at all, because the corruption happened at a lower level than the
`modCount` check). Never use a plain (non-concurrent) collection across threads and rely on catching
`ConcurrentModificationException` as your safety net — use a genuinely thread-safe/concurrent
collection instead.
*Follow-up: does `CopyOnWriteArrayList` solve this by being "safer," or by avoiding the problem
category entirely?* It avoids the category entirely — mutations always create a brand-new backing
array (copy-on-write), so an in-flight iterator over the old array snapshot can never observe
"structural modification" in the first place; there's nothing to detect because nothing it's
iterating over ever changes underneath it.

**[Intermediate] Does `BlockingQueue` add anything to plain `Queue` beyond just "thread-safe"?**
Yes — beyond thread safety, `BlockingQueue` adds genuinely **blocking** operations: `put(e)` blocks
the calling thread until space becomes available (for a bounded queue that's currently full), and
`take()` blocks until an element becomes available (for a queue that's currently empty) — this is
categorically different from a merely thread-safe `Queue` (e.g. `ConcurrentLinkedQueue`, which is
non-blocking: `offer`/`poll` return immediately, `poll` returning `null` on an empty queue rather
than waiting). Blocking behavior is exactly what makes `BlockingQueue` implementations
(`ArrayBlockingQueue`, `LinkedBlockingQueue`, etc.) the natural building block for
producer-consumer designs, where a consumer genuinely should pause (not spin-poll) until work
arrives.
*Follow-up: what's the non-blocking, lock-free alternative when you specifically don't want
blocking semantics for a concurrent queue?* `ConcurrentLinkedQueue` — a non-blocking, CAS-based
unbounded queue implementing plain `Queue` (not `BlockingQueue`), appropriate when you want
thread-safe concurrent access without ever wanting a thread to pause waiting.

---

## 6. Comparable vs Comparator

**[Basic] `Comparable` vs `Comparator` — what's the structural difference?**
`Comparable<T>` is implemented **by the class itself** (`compareTo(T other)`), defining that type's
single **natural ordering** — a class can have at most one. `Comparator<T>` is a separate,
standalone object implementing `compare(T a, T b)`, passed **into** a sorting/ordering method
(`Collections.sort(list, comparator)`, `list.sort(comparator)`, `TreeMap`'s constructor) — a type
can have as many different `Comparator`s as you want, letting you sort/order the same objects
differently in different contexts without touching the class itself (useful for types you don't
own, or multiple valid orderings, e.g. sort `Employee` by name in one place and by salary in
another).
*Follow-up: which one does `Collections.sort(list)` (single-arg) use?* `Comparable.compareTo` — the
type's natural ordering; the type must implement `Comparable` or it throws `ClassCastException` at
runtime.

**[Intermediate] Build a `Comparator` that sorts `Employee` by department (ascending), then by
salary (descending) within department, nulls-last on department — using the modern fluent API.**
```java
Comparator<Employee> cmp = Comparator
    .comparing(Employee::getDepartment, Comparator.nullsLast(Comparator.naturalOrder()))
    .thenComparing(Employee::getSalary, Comparator.reverseOrder());
employees.sort(cmp);
```
`Comparator.comparing(keyExtractor)` builds a comparator from a key-extraction lambda/method
reference; `.thenComparing(...)` chains a tie-breaker only consulted when the first comparator
returns `0`; `Comparator.nullsLast`/`nullsFirst` wrap another comparator to push nulls to one end
instead of NPE-ing on them; `.reverseOrder()`/`.reversed()` flip a comparator's direction.
*Follow-up: what happens if you call `.reversed()` on the whole chained comparator instead of just
the salary part?* It reverses the **entire** composed ordering, including the department ordering
(department descending, then salary ascending within it) — not just the last `thenComparing` clause
— so `.reversed()` on the final chain and negating just one stage are very different operations.

**[Advanced] What contract must `compareTo`/`compare` satisfy, and what breaks if you violate it
(e.g. inconsistent with `equals`)?**
Must be antisymmetric (`sign(compare(a,b)) == -sign(compare(b,a))`), transitive
(`compare(a,b)>0 && compare(b,c)>0` implies `compare(a,c)>0`), and consistent (repeated calls with
unchanged objects return the same result). "Consistent with `equals`" (`compareTo(x)==0` should
imply `x.equals(other)`) is **strongly recommended but not strictly required** by the interface
contract — however, violating it produces genuinely surprising behavior specifically in
**sorted collections**: a `TreeSet`/`TreeMap` uses `compareTo`/`compare` (not `equals`/`hashCode`) to
determine element/key **uniqueness** — two objects that `compareTo` says are "equal" (`0`) will be
treated as duplicates by a `TreeSet` even if `.equals()` says they're different, silently dropping
one on insert (`BigDecimal`'s natural ordering vs `equals` is the JDK's own canonical example of
this trap: `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` is `false` (different scale) but
`compareTo` treats them as equal, so a `TreeSet<BigDecimal>` keeps only one of them).
*Follow-up: give the canonical broken-contract bug in a comparator implementation.* Using subtraction
for numeric comparison, `(a, b) -> a.getX() - b.getX()`, silently overflows for large or
opposite-signed `int` values, producing a wrong sign and breaking transitivity — use
`Integer.compare(a.getX(), b.getX())` instead.

---

## 7. Choosing the right collection

**[Intermediate] Give a quick decision framework: how do you pick a collection type for a new
requirement in an interview, out loud?**
Ask, in order: (1) Key-value pairs, or just values? → `Map` vs `Collection`. (2) Duplicates allowed?
→ `List`/`Map` (yes) vs `Set` (no). (3) Ordering requirement — none, insertion order, sorted, or
priority/FIFO/LIFO? → plain `HashMap`/`HashSet` (none) vs `LinkedHashMap`/`LinkedHashSet` (insertion)
vs `TreeMap`/`TreeSet` (sorted, + range queries) vs `PriorityQueue` (priority) vs `ArrayDeque`
(FIFO/LIFO). (4) Access pattern — mostly random-access reads, or mostly insert/remove at ends? →
`ArrayList` (random access) vs `ArrayDeque` (ends). (5) Concurrent access needed? →
`ConcurrentHashMap`/`CopyOnWriteArrayList`/`BlockingQueue` family instead of the plain versions.
Walking through this checklist out loud (rather than jumping straight to an answer) is itself often
what the interviewer is scoring — it shows you're reasoning about trade-offs, not pattern-matching a
memorized answer.
*Follow-up: for a read-heavy, rarely-written cache of configuration values shared across threads,
what would you pick and why?* `CopyOnWriteArrayList`/`CopyOnWriteArraySet` if it's list/set-shaped,
or an immutable `Map` swapped via an `AtomicReference` on update — writes are rare and expensive
(full copy) but reads are lock-free and extremely fast, matching the read-heavy/write-rare access
pattern exactly.

**[Advanced] When would `CopyOnWriteArrayList` be a genuinely bad choice, even though it's
thread-safe?**
Any workload with **frequent writes** (adds/removes) relative to reads, or with a **large**
underlying list — every single mutating operation copies the **entire** backing array (O(n) time
and O(n) extra memory per write), so a write-heavy or large-list workload turns every `add()` into
an expensive full copy, unlike `ArrayList` (O(1) amortized) or even a lock-based
`Collections.synchronizedList` (O(1) per write, just serialized). It's specifically designed for the
narrow case of read-dominated, rarely-mutated, typically-small lists shared across threads (the
canonical example: a list of event listeners, added/removed rarely but iterated frequently and
concurrently).
*Follow-up: what happens to an iterator created before a concurrent write completes?* Nothing
changes for it — it keeps iterating its own snapshot array from creation time, completely unaware
the underlying list has since been replaced by a new array; this is a feature (no
`ConcurrentModificationException`, ever), not a bug, but it means the iterator can show
increasingly "stale" data the longer it's held open across concurrent writes.

---

## 8. Predict-the-output puzzles

**Puzzle 1 — `ConcurrentModificationException` on direct removal in a for-each loop**
```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);
    }
}
System.out.println(list);
```
**Output:** throws `ConcurrentModificationException` (does not print anything).
**Why:** The for-each loop desugars to an `Iterator`-based loop. `list.remove(s)` is called directly
on the `List`, not on the iterator, incrementing the list's `modCount` without the iterator knowing
— the iterator's next `hasNext()`/`next()` call detects the mismatch and throws. (Note: for this
specific 3-element case where the removed element happens to be the second-to-last, some
size/index combinations can *appear* to work by luck depending on which element triggers the final
`hasNext()` check — but relying on that is exactly the bug; the safe fix is `list.removeIf(s ->
s.equals("b"))` or an explicit `Iterator.remove()`.)

**Puzzle 2 — mutable key silently "disappearing" from a `HashSet`**
```java
class Point {
    int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }
    public boolean equals(Object o) {
        if (!(o instanceof Point p)) return false;
        return x == p.x && y == p.y;
    }
    public int hashCode() { return Objects.hash(x, y); }
}
Set<Point> set = new HashSet<>();
Point p = new Point(1, 2);
set.add(p);
p.x = 99;                                   // mutate a field used in hashCode()/equals()
System.out.println(set.contains(p));        // same object reference!
System.out.println(set.contains(new Point(99, 2)));
```
**Output:** `false`, then `false`.
**Why:** `set.add(p)` placed `p` into the bucket computed from `hashCode()` when `x=1`. After
`p.x = 99`, `set.contains(p)` recomputes the hash with the **new** state (`x=99`) and looks in a
**different** bucket — `p` is still physically sitting in the old bucket, so it's not found there
either. `set.contains(new Point(99, 2))` also fails for the same reason: it looks in the bucket for
`hashCode()` of `(99,2)`, but the actual stored object is still parked in the bucket for `(1,2)`.
The entry is effectively "lost" (unreachable via lookup) while still technically present — iterating
the set would still show it. Lesson: never mutate fields used in `equals`/`hashCode` once an object
is a `HashSet` element or `HashMap` key.
