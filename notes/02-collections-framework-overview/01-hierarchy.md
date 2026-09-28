# Collections Framework Hierarchy

## 1. What it is

The Java Collections Framework (JCF) is a unified set of interfaces and
implementations for storing and manipulating groups of objects. Everything
that can be iterated with a for-each loop implements `Iterable<E>`;
`Collection<E>` adds the bulk-operation contract (add/remove/size/contains);
`List`, `Set`, and `Queue` are the three specializations of `Collection`.
`Map<K,V>` is deliberately **not** a `Collection` — it is a separate root
for key→value associations, though it exposes collection *views*
(`keySet()`, `values()`, `entrySet()`).

## 2. How it works internally

### 2.1 The interface hierarchy (ASCII diagram)

```
java.lang.Iterable<E>
        |
   java.util.Collection<E>              (size, add, remove, contains, iterator, stream)
        |
   -----------------------------------------------------------
   |                    |                                    |
 List<E>              Set<E>                              Queue<E>
   |                    |                                    |
   | (ordered,          | (no dupes,                         | (FIFO-ish,
   |  index access,      |  equals/hashCode                   |  offer/poll/peek)
   |  dupes allowed)      |  based dedupe)                     |
   |                    |                                    |
   |               SortedSet<E>                          Deque<E>
   |                    |                              (double-ended queue;
   |               NavigableSet<E>                      also usable as a Stack)
   |             (floor/ceiling/higher/lower,
   |              descendingSet, subSet)

Well-known implementations:

List<E>
 +-- ArrayList              (resizable array, RandomAccess)
 +-- LinkedList             (doubly linked list; ALSO implements Deque<E>)
 +-- Vector (legacy)        (synchronized resizable array)
       +-- Stack (legacy)   (LIFO on top of Vector)
 +-- CopyOnWriteArrayList   (java.util.concurrent; snapshot-iterator)
 +-- Arrays.asList(...)     (fixed-size view backed by an array)
 +-- List.of(...)           (truly immutable, JDK 9+)

Set<E>
 +-- HashSet                (backed by a HashMap<E,Object>, no order guarantee)
 +-- LinkedHashSet           (HashSet + insertion-order doubly linked list)
 +-- TreeSet                (NavigableSet, backed by a TreeMap -> Red-Black tree)
 +-- EnumSet (abstract)      (bit-vector, only for enum types)
 +-- CopyOnWriteArraySet     (java.util.concurrent, backed by COWArrayList)
 +-- Set.of(...)             (truly immutable, JDK 9+)

Queue<E>
 +-- PriorityQueue           (binary heap, NOT a Deque, no ordering iterator)
 +-- ArrayDeque              (circular array; implements Deque<E>; also a stack)
 +-- LinkedList              (implements both List and Deque)
 +-- java.util.concurrent.BlockingQueue<E>  (separate sub-hierarchy)
       +-- ArrayBlockingQueue, LinkedBlockingQueue, PriorityBlockingQueue,
           SynchronousQueue, DelayQueue

Map<K,V>   <-- NOT a Collection. Separate root interface entirely.
 +-- HashMap                 (buckets of nodes, hash + treeified bins >= 8)
       +-- LinkedHashMap     (HashMap + insertion/access-order linked list)
 +-- SortedMap<K,V>
       +-- NavigableMap<K,V>
             +-- TreeMap     (Red-Black tree)
 +-- Hashtable (legacy)      (synchronized, no null key/value)
       +-- Properties
 +-- WeakHashMap, IdentityHashMap, EnumMap (specialized, see module 06)
 +-- ConcurrentMap<K,V> (java.util.concurrent)
       +-- ConcurrentHashMap
 +-- Map.of(...)             (truly immutable, JDK 9+)
```

Key structural facts to memorize:

- `Iterable` is the *only* thing every one of these has in common with `Map`
  — and `Map` doesn't even implement `Iterable` directly; you iterate its
  **views** (`keySet()`, `values()`, `entrySet()`), which themselves are
  `Set`/`Collection` and thus `Iterable`.
- `Queue` and `List` are siblings under `Collection`; `Deque extends Queue`,
  not the other way around. `LinkedList` is unusual in implementing *both*
  `List` and `Deque`.
- `SortedSet`/`NavigableSet` and `SortedMap`/`NavigableMap` are the only
  places ordering-by-comparison is part of the *interface contract* (not
  just an implementation detail like `LinkedHashSet`'s insertion order).
- `PriorityQueue` is a `Queue` but **not** a `Deque` — you cannot push/pop
  from both ends, and its iterator does not return elements in priority
  order (only `poll()` does).
- Legacy classes (`Vector`, `Stack`, `Hashtable`) predate the 1.2 framework
  and are retrofitted onto it; they're synchronized on every method, which
  is both their selling point (thread-safety) and their weakness (throughput).

### 2.2 Why `Map` is not a `Collection`

A `Collection<E>` models "a group of *individual* elements of type E".
A `Map<K,V>` models "a set of *pairs*" — there's no single natural element
type to hand back from `iterator()`: is it the key, the value, or the pair?
The JCF designers avoided this ambiguity by making `Map` its own interface
with three distinct collection-view methods, each answering the question
explicitly:

```
Map<String, Integer> balances = ...;
for (String acct   : balances.keySet())   { ... }   // Set<String>
for (Integer amount : balances.values())  { ... }   // Collection<Integer>
for (Map.Entry<String,Integer> e : balances.entrySet()) { ... } // Set<Entry>
```

These views are *backed by* the map — mutating the view (e.g.
`keySet().remove(k)`) mutates the map, and vice versa.

## 3. Complexity

| Operation | Time | Space | Notes |
|-----------|------|-------|-------|
| `instanceof` interface check | O(1) | O(1) | JVM interface-table lookup, effectively constant |
| Iterating via `Iterable`/`iterator()` | O(n) total | O(1) extra (O(n) for a snapshot iterator like COW) | cost is dominated by the concrete implementation, not the interface |
| `Map.keySet()` / `values()` / `entrySet()` | O(1) to obtain the view | O(1), view not a copy | walking the view is still O(n) |
| Polymorphic dispatch through an interface reference | O(1) | O(1) | one extra indirection vs. a concrete type, negligible in practice |

The hierarchy itself has no runtime cost beyond ordinary virtual dispatch —
this section is about *design*, not algorithmic complexity; the complexity
tables that matter live in the per-implementation topics (03-list, 04-queue,
05-set, 06-map).

## 4. Example code

Runnable class: `src/main/java/com/gk/study/collectionsoverview/examples/HierarchyDemo.java`

```java
List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
Set<Integer> set = new TreeSet<>(list);
Queue<Integer> queue = new ArrayDeque<>(list);
Map<String, Integer> map = new HashMap<>();

System.out.println(list instanceof Collection);   // true
System.out.println(map instanceof Collection);     // false  <-- the whole point
System.out.println(queue instanceof Deque);         // true (ArrayDeque)
System.out.println(new PriorityQueue<>(list) instanceof Deque); // false
```

Expected console output (abridged, full output documented in the class):
```
list instanceof Collection : true
map  instanceof Collection : false
queue instanceof Deque      : true
priorityQueue instanceof Deque : false
map.entrySet() instanceof Collection : true
```

## 5. When to use / when NOT to use

Program **against the interface**, not the implementation:

- Use `List<T>` as the field/parameter/return type unless you specifically
  need `ArrayList`-only methods (you almost never do) — this lets you swap
  `ArrayList` for `LinkedList` or `CopyOnWriteArrayList` later without
  touching calling code.
- Use `Collection<T>` as a parameter type when a method only needs to
  iterate/add/size — it accepts `List`, `Set`, or `Queue` implementations,
  maximizing reuse.
- Use `Map<K,V>` (never `HashMap` in signatures) unless ordering
  (`LinkedHashMap`/`TreeMap`) is part of the contract you're promising callers.
- Do NOT expose `Iterable<T>` when the caller genuinely needs random access
  or size — that forces an O(n) walk just to find `size()` on some custom
  `Iterable`s. Expose `Collection<T>` or `List<T>` instead.
- Do NOT assume `Queue`'s `add`/`remove`/`element` will throw when full/empty
  vs `offer`/`poll`/`peek` returning `null`/`false` — that's a per-method
  contract choice inside `Queue`, not a hierarchy issue, but it trips people
  up right where hierarchy questions get asked. See module 04.

## 6. Common pitfalls & gotchas

**Pitfall 1 — treating a `Map` as if it were iterable directly.**

```java
Map<String, Integer> m = new HashMap<>();
for (String key : m) { ... }   // COMPILE ERROR: Map<K,V> is not Iterable<K>
```

Fix: iterate a view.

```java
for (String key : m.keySet()) { ... }
for (Map.Entry<String, Integer> e : m.entrySet()) { ... }
```

**Pitfall 2 — assuming `PriorityQueue`'s iterator gives sorted order.**

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 3));
for (int x : pq) System.out.print(x + " "); // NOT guaranteed 1 3 5 — heap array order
```

Fix: drain with `poll()` to get sorted order, or copy into a `List` and sort.

```java
List<Integer> sorted = new ArrayList<>();
while (!pq.isEmpty()) sorted.add(pq.poll()); // 1 3 5, guaranteed
```

**Pitfall 3 — assuming every `List` supports structural mutation.**

```java
List<Integer> fixed = Arrays.asList(1, 2, 3);
fixed.add(4); // UnsupportedOperationException — it's List-typed but fixed-size
```

`Arrays.asList` and `List.of` are both statically typed as `List<E>`, but the
interface type tells you nothing about mutability — see topic 03 (Immutable
collections) for the full picture.

## 7. Interview questions

- [Basic] Q: Why doesn't `Map` extend `Collection`?
  A: `Collection<E>` represents a group of single elements; `Map<K,V>` stores
  key-value *pairs*, so there's no unambiguous single "element type" to
  iterate. Instead `Map` exposes three collection views — `keySet()`,
  `values()`, `entrySet()` — each explicit about what you're iterating.
  Follow-up: Are those views live or copies? (Live — backed by the map;
  removing from `keySet()` removes the entry from the map too, but adding
  is usually unsupported except via `entrySet()`'s `setValue`.)

- [Basic] Q: What's the top of the Collections Framework hierarchy?
  A: `java.lang.Iterable<E>` — anything for-each-able. `Collection<E>`
  extends it and adds size/add/remove/contains. `List`, `Set`, `Queue`
  extend `Collection`.
  Follow-up: Why is `Iterable` in `java.lang` and not `java.util`? (So core
  language features like the enhanced for-loop can reference it without
  depending on the collections package.)

- [Basic] Q: Does `Set` allow duplicates?
  A: No — `Set` is defined by the *no duplicate elements* contract, enforced
  via `equals()`/`hashCode()` (or `compareTo` for `TreeSet`).
  Follow-up: What happens if you add a mutable object to a `HashSet` and
  then mutate a field involved in `hashCode()`? (You get a "lost" element —
  `contains()`/`remove()` may fail to find it because it's now in the wrong
  bucket. Covered in module 05.)

- [Intermediate] Q: Is `LinkedList` a `List` or a `Queue`?
  A: Both — it implements `List<E>` and `Deque<E>` (which extends `Queue`)
  simultaneously, so it can be used as an index-based list, a stack, or a
  double-ended queue, though `ArrayDeque` is generally preferred over it for
  pure queue/stack use because it avoids per-node allocation.
  Follow-up: Why prefer `ArrayDeque` over `LinkedList` for a stack/queue?
  (Better cache locality, no per-node object overhead, amortized O(1) both
  ends via a circular array — see module 04.)

- [Intermediate] Q: Why is `PriorityQueue` not a `Deque`?
  A: A `Deque` promises ordered access from both ends that the caller
  controls; a `PriorityQueue` reorders elements internally by priority, so
  "the front" is always the current minimum (or per-comparator smallest),
  not something the caller can push/pop arbitrarily from either end — the
  two contracts are incompatible.
  Follow-up: What data structure backs `PriorityQueue`? (A binary heap
  stored in a resizable array — see module 04.)

- [Intermediate] Q: What's the difference between `SortedSet` and
  `NavigableSet`?
  A: `NavigableSet` (added in Java 6) extends `SortedSet` and adds
  navigation methods — `floor`, `ceiling`, `higher`, `lower`,
  `pollFirst`/`pollLast`, `descendingSet()` — for finding neighbors of a
  given value without a full traversal. `TreeSet` implements
  `NavigableSet`, so in practice you almost always get the richer interface.
  Follow-up: Give a use case for `ceiling()`. (Finding the smallest
  transaction amount >= a threshold without scanning the whole set.)

- [Intermediate] Q: Can you have a `Map` as an element of a `Set`?
  A: Yes — a `Set<Map<K,V>>` is legal; the `Set` treats each `Map` as one
  opaque element and dedupes using `Map`'s own `equals()`/`hashCode()`
  (which `AbstractMap` implements as content-based equality). It's rarely
  useful because mutating one of the maps after insertion can break the
  set's internal invariants (same mutable-key problem as `HashSet`).
  Follow-up: Would this be safe with `HashMap` elements versus
  `ConcurrentHashMap` elements? (Neither is inherently "safer" for this —
  the hazard is mutating any map's content after hashing, not its
  thread-safety.)

- [Advanced] Q: Why does the JCF favor "program to the interface" so
  heavily — what would break if all your APIs took `ArrayList` instead of
  `List`?
  A: You'd lose the ability to swap implementations (e.g., switch to
  `CopyOnWriteArrayList` under concurrent reads, or `LinkedList` if you
  suddenly need O(1) head inserts) without changing every call site's
  declared type; you'd also leak implementation-specific behavior (like
  `ArrayList`'s fail-fast iterator quirks) into your public contract,
  making future refactors binary/semantically breaking changes.
  Follow-up: When *is* it acceptable to expose a concrete type? (Rarely —
  mainly in tightly scoped internal/private code where the concrete
  behavior, e.g. `ArrayDeque`'s O(1) both-ends access, is itself the
  contract you're promising.)

- [Advanced] Q: `Collection` declares `add(E e)` as an optional operation
  that may throw `UnsupportedOperationException`. Why put an "optional"
  method in an interface instead of splitting into a mutable/immutable
  interface hierarchy?
  A: This was a historical trade-off in Java 1.2 — a `MutableCollection`
  vs `ImmutableCollection` split (as Scala/Kotlin later did) would have
  doubled the interface surface and broken simplicity of "one Collection
  type per shape". Instead the JCF documents "optional operations" per
  method and throws at runtime. JDK 9's `List.of()`/`Set.of()`/`Map.of()`
  partially address this by giving you collections that are *documented* to
  always reject mutation, but the underlying interface contract is
  unchanged — see topic 03.
  Follow-up: What's the practical downside of runtime-only enforcement?
  (No compile-time safety — passing an immutable list to code that calls
  `.add()` compiles fine and blows up only when executed, possibly deep in
  production. Defensive copying and clear API docs are the mitigations.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Classify an arbitrary object into List/Set/Queue/Deque/Map/"none of these" using `instanceof` | interface classification | `ClassifyCollection.java` |
| E02 | Easy | Sum a `Collection<? extends Number>` regardless of concrete type (List, Set, or Queue) | program-to-interface / bounded wildcards | `SumAnyCollection.java` |
| E03 | Medium | Implement a custom `Iterable<Integer>` (`FibonacciRange`) that lazily yields Fibonacci numbers up to a bound, usable in a for-each loop | custom Iterable / lazy iterator | `FibonacciRange.java` |
| E04 | Medium | Given a `Map<String,Integer>`, produce a `List<String>` of `"key=value"` entries sorted by value descending, using only the map's collection views | Map views -> Collection pipeline | `MapEntriesSortedByValue.java` |

Each exercise: full problem statement, sample input/output, constraints, and
a collapsible hint live in the exercise file's Javadoc and in the matching
test class. Solutions live only in
`src/main/java/com/gk/study/collectionsoverview/solutions/` — not shown here.

<details>
<summary>E01 hint</summary>
Check the most specific interfaces first if you want a single label (e.g.
check `Deque` before `Queue`, since every `Deque` is also a `Queue`).
</details>

<details>
<summary>E02 hint</summary>
The parameter type should be <code>Collection&lt;? extends Number&gt;</code> —
this is the PECS "producer extends" rule from module 01.
</details>

<details>
<summary>E03 hint</summary>
Implement <code>Iterable&lt;Integer&gt;</code> by returning a custom
<code>Iterator&lt;Integer&gt;</code> whose <code>hasNext()</code>/<code>next()</code>
compute the next Fibonacci number on demand — don't precompute the whole list.
</details>

<details>
<summary>E04 hint</summary>
Use <code>entrySet().stream()</code>, sort with
<code>Map.Entry.comparingByValue(Comparator.reverseOrder())</code>, then map
each entry to a formatted string and collect to a <code>List</code>.
</details>

## 9. Quick recap

- `Iterable -> Collection -> {List, Set, Queue}`; `Deque extends Queue`;
  `Map` is a separate root, exposed only through `keySet()`/`values()`/`entrySet()` views.
- `List` = ordered + duplicates allowed + index access. `Set` = no
  duplicates (equals/hashCode or compareTo driven). `Queue`/`Deque` =
  ordered for removal from one or both ends.
- `LinkedList` uniquely implements both `List` and `Deque`; prefer
  `ArrayDeque` for pure stack/queue use.
- `PriorityQueue` is a `Queue` but not a `Deque`, and its iterator order is
  not sorted order — only `poll()` gives you sorted order.
- Always declare fields/params/returns as the interface (`List`, `Map`,
  `Collection`), never the concrete class, unless you specifically need
  implementation-only behavior.
