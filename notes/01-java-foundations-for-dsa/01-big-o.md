# Big-O: Time & Space Complexity, Amortized Analysis

## 1. What it is

Big-O notation describes how the running time (or memory use) of an algorithm
grows as the input size `n` grows, ignoring constant factors and lower-order
terms. It is a way to talk about *scalability*, not a stopwatch measurement.
Amortized analysis is a refinement: it gives the *average* cost per operation
over a worst-case sequence of operations, even when individual operations are
occasionally expensive (e.g. `ArrayList.add`).

## 2. How it works internally

Big-O is defined formally as: `f(n) = O(g(n))` if there exist constants `c > 0`
and `n0` such that `f(n) <= c * g(n)` for all `n >= n0`. In practice, as an
interview engineer you just need to:

- Count the dominant term as `n -> infinity` and drop constants:
  `3n + 100` → `O(n)`; `n^2 + 50n` → `O(n^2)`.
- Multiply complexities of nested loops/work, add complexities of sequential
  blocks of work.
- Recognize that `O`, `Θ` (theta, tight bound) and `Ω` (omega, lower bound)
  are different things — interviewers usually say "Big-O" but mean "worst
  case" (Θ) in practice.

**Amortized analysis** — the classic example is `ArrayList`/dynamic array
`push`. Most pushes are O(1) (just write to the next free slot). Occasionally,
when the backing array is full, a push triggers a resize: allocate a new array
(commonly 1.5x–2x capacity) and copy every existing element — an O(n)
operation. If you only looked at that one expensive push you'd say "O(n) per
push", which is misleading. Spread the cost of each resize evenly across all
the pushes that happened since the *previous* resize (this is the "accounting"
/ "banker's method" of amortized analysis — you pay a little extra on every
cheap operation to "pre-pay" for the next expensive one), and the *average*
cost per push converges to O(1).

```
capacity:      1 -> 2 -> 4 -> 8 -> 16
pushes so far: 1    2    4    8    16
copy cost of
the resize
that grows it:  -    1    2    4    8      (copy old elements into new array)

total copies after 16 pushes = 1 + 2 + 4 + 8 = 15  (< 2 * 16)
=> amortized cost per push = 15 / 16 ≈ 0.94  =>  O(1) amortized
```

Because capacity doubles, the total copy work across all resizes forms a
geometric series that sums to less than `2n` — that's why the growth factor
matters: doubling gives amortized O(1) inserts; growing by a *fixed* amount
(e.g. always +1) gives amortized O(n) inserts (that degrades to the same cost
as a naive array-based list with no slack).

## 3. Complexity

| Class | Name | Example operation |
|-------|------|--------------------|
| O(1) | Constant | array index access `arr[i]`, HashMap `get` (average case) |
| O(log n) | Logarithmic | binary search, balanced BST height, `TreeMap` operations |
| O(n) | Linear | linear scan, finding max, dynamic array amortized `push` |
| O(n log n) | Linearithmic | comparison-based sorting (`Arrays.sort` on objects, merge sort) |
| O(n^2) | Quadratic | nested loops, bubble/insertion sort, naive pair-checking |
| O(2^n) | Exponential | recursive Fibonacci without memoization, generating all subsets |
| O(n!) | Factorial | generating all permutations, brute-force TSP |
| Amortized O(1) | Amortized constant | `ArrayList.add` (append), `HashMap.put` (average, ignoring resize) |

## 4. Example code

Runnable class: `src/main/java/com/gk/study/foundations/examples/BigODemo.java`

```java
int comparisons = 0;
for (int i = 0; i < n; i++) {
    for (int j = i + 1; j < n; j++) {
        comparisons++; // O(n^2): n*(n-1)/2 comparisons
    }
}
```

Expected console output (abridged):
```
=== Big-O demo: counting operations, not wall-clock time ===
n=10   -> bruteForce comparisons=45, optimized comparisons=10
n=100  -> bruteForce comparisons=4950, optimized comparisons=100
n=1000 -> bruteForce comparisons=499500, optimized comparisons=1000

=== Amortized doubling array: 16 pushes ===
capacity progression: 1 -> 2 -> 4 -> 8 -> 16
total element copies during resizes = 15
average copies per push = 0.94 (bounded by < 2.0)
```

## 5. When to use / when NOT to use

- **Use** Big-O to compare algorithm *shapes* at scale, to reason about
  whether code will survive production-sized input (a banking batch job over
  10M transactions), and to justify a data-structure choice in an interview.
- **Don't** treat Big-O as the whole story for small or fixed `n`: an O(n^2)
  algorithm with tiny constants can beat an O(n log n) algorithm with heavy
  constants when `n` is small (e.g. insertion sort beats quicksort/mergesort
  for arrays under ~10-20 elements — this is why `Arrays.sort` and
  `Collections.sort` switch to insertion sort for small sub-arrays/runs).
- **Don't** use Big-O alone to predict wall-clock time — cache locality,
  JIT warm-up, GC pauses, autoboxing and I/O dominate in practice.

## 6. Common pitfalls & gotchas

**Pitfall 1 — treating amortized cost as the worst-case cost of a single call.**
A single `ArrayList.add()` call that happens to trigger a resize *is* O(n) for
that call. If you're in a hard real-time / latency-sensitive path (e.g. a
trading tick handler) and cannot tolerate an occasional O(n) spike, amortized
O(1) is not good enough — you need a pre-sized array or a structure with a
true worst-case O(1) bound.

```java
// Buggy assumption: "add is always O(1), so this loop is O(n)"
List<Integer> list = new ArrayList<>(); // starts at capacity 0/10 depending on JDK
for (int i = 0; i < n; i++) {
    list.add(i); // true on AVERAGE, but a handful of these calls are O(k)
}
```
```java
// Fix when a worst-case latency bound matters: pre-size to avoid ANY resize.
List<Integer> list = new ArrayList<>(n); // capacity reserved up front
for (int i = 0; i < n; i++) {
    list.add(i); // now every call is genuinely O(1), no resize can occur
}
```

**Pitfall 2 — quadratic blow-up from String concatenation in a loop.**

```java
// O(n^2): each += creates a new String and copies all previous characters.
String result = "";
for (int i = 0; i < n; i++) {
    result += String.valueOf(i);
}
```
```java
// Fix: StringBuilder amortized O(1) append -> overall O(n).
StringBuilder sb = new StringBuilder();
for (int i = 0; i < n; i++) {
    sb.append(i);
}
String result = sb.toString();
```

**Pitfall 3 — assuming HashMap `get`/`put` is always O(1).** It's O(1)
*average case* with a good hash function; worst case (many colliding keys,
e.g. a poorly-written `hashCode()`) degrades to O(n) per bucket lookup before
Java 8, and O(log n) per bucket in Java 8+ once a bucket treeifies (≥8
entries and table capacity ≥ 64).

## 7. Interview questions

- **[Basic]** What's the difference between O, Θ and Ω? → *O is an upper
  bound (worst case or general bound), Ω is a lower bound (best case), and Θ
  is a tight bound (both upper and lower — the algorithm always takes
  "about" that long). In casual interview speech, "Big-O" is usually used
  loosely to mean "worst case running time."* → Follow-up: *Is `Arrays.sort`
  for primitives Θ(n log n) or does it have a better best case?* (Dual-pivot
  quicksort is Θ(n log n) average/worst for primitives; it doesn't have a
  special-cased better best case like some other sorts.)

- **[Basic]** What is the time complexity of accessing an element by index in
  an `ArrayList` vs a `LinkedList`? → *`ArrayList.get(i)` is O(1) — direct
  array index arithmetic. `LinkedList.get(i)` is O(n) — it must walk node
  links from the head (or tail, whichever is closer) since there's no random
  access.* → Follow-up: *So why would you ever use LinkedList?* (Cheap O(1)
  insert/remove at a known node reference, e.g. implementing a Deque, without
  shifting elements — though `ArrayDeque` is usually preferred in practice.)

- **[Basic]** What does "amortized O(1)" mean for `ArrayList.add`? → *Most
  calls are O(1); occasionally a call triggers a resize that's O(n), but
  because resizes happen exponentially less often as the list grows (doubling
  capacity), the total cost over n adds is O(n), so the average — amortized —
  cost per add is O(1).* → Follow-up: *What if the array grew by a fixed
  amount (e.g. +10) instead of doubling — what's the amortized cost then?*
  (Amortized O(n) per add — you'd resize roughly n/10 times, each copying up
  to n elements, giving O(n^2) total work over n adds.)

- **[Intermediate]** Why does capacity doubling (not tripling, not +1) give
  amortized O(1) inserts? → *The total copy work across all resizes for n
  inserts is a geometric series (1+2+4+...+n/2 ≈ n) that sums to less than
  2n regardless of n, so total work is O(n) and per-insert is O(1). Any
  constant growth factor > 1 gives the same asymptotic result; only the
  constant multiplier changes (bigger growth factor = fewer resizes but more
  wasted memory).* → Follow-up: *Why does `ArrayList` grow by 1.5x instead of
  2x?* (Trade-off between resize frequency and wasted memory; 1.5x wastes
  less memory on average at the cost of slightly more frequent resizes —
  still amortized O(1)).

- **[Intermediate]** Compare O(n log n) sorting vs O(n^2) sorting for a list
  of 20 elements read from a config file at startup — which would you pick
  and why? → *For n=20, constant factors dominate; a simple O(n^2) sort like
  insertion sort can be faster in practice than a heavier O(n log n) sort due
  to lower overhead and good cache behavior, and the code is simpler. This is
  exactly why production sorts (`Arrays.sort`) use insertion sort for small
  runs/sub-arrays even inside an overall O(n log n) algorithm.* → Follow-up:
  *At what rough size does the crossover happen?* (No fixed number — but
  JDK's Arrays.sort switches to insertion sort for runs below ~47 elements
  historically to illustrate the idea, i.e., it's a small double-digit
  threshold, not something to memorize exactly.)

- **[Intermediate]** What's the space complexity difference between an
  in-place O(1)-extra-space algorithm and one that allocates a new
  n-sized array? → *In-place means the extra memory used is O(1) —
  constant, independent of input size — beyond the input itself (e.g.
  reversing an array with two pointers). Allocating a new array/list makes
  it O(n) auxiliary space. In interviews, "in-place" almost always means O(1)
  extra space, not "the original array is mutated" alone.* → Follow-up: *Is
  recursion ever a hidden space cost?* (Yes — recursive call stack depth
  counts toward space complexity, e.g. naive recursive factorial/Fibonacci is
  O(n) space from the call stack even though it "looks" like no extra data
  structure was allocated.)

- **[Advanced]** How would you formally justify that `HashMap.put` is
  amortized O(1) even though resizing (rehashing every entry into a bigger
  table) is O(n)? → *Same amortized argument as dynamic arrays: `HashMap`
  doubles its table capacity when the load factor (default 0.75) is
  exceeded. The total rehashing work across n puts is bounded by a geometric
  series summing to O(n), so the amortized cost per put stays O(1) — assuming
  a good hash distribution. This assumption breaks down under adversarial or
  poorly-distributed hash codes.* → Follow-up: *What guarantees the worst
  case even with resizing — could a single put still be O(n)?* (Yes — the
  specific put call that triggers the resize is O(n) for that call; only the
  amortized average across many calls is O(1), same caveat as ArrayList.)

- **[Advanced]** Give an example where an algorithm with better Big-O is
  *slower in practice* than one with worse Big-O, and explain why Big-O
  didn't predict it. → *Comparing a cache-friendly O(n^2) approach on a small
  matrix against a cache-unfriendly O(n log n) approach with poor locality
  and heavy allocation — Big-O ignores constant factors, memory locality,
  branch prediction, and allocation/GC overhead, all of which can dominate at
  the input sizes actually seen in production. Another classic case:
  recursive divide-and-conquer algorithms with heavy object allocation per
  call (e.g., autoboxed generics) losing to a simple iterative loop over
  primitives despite a "better" asymptotic bound.* → Follow-up: *How do you
  decide when it's worth switching to the asymptotically better algorithm?*
  (Profile with realistic production-sized data; only optimize the
  asymptotic behavior when `n` is large enough, or unbounded/growing, that
  the crossover point is actually reached.)

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E01 | Easy | Detect if any two numbers in an array sum to a target, brute force | nested loop / O(n^2) | `exercises/TwoSumBruteForce.java` |
| E02 | Easy | Detect if any two numbers in an array sum to a target, optimized | hash set / O(n) | `exercises/TwoSumOptimized.java` |
| E03 | Medium | Track total element copies of a doubling dynamic array to prove amortized O(1) push | amortized analysis | `exercises/DynamicArrayAmortized.java` |
| E04 | Hard | Find the median of two sorted arrays in O(log(min(m,n))) | binary search on partitions | `exercises/MedianOfTwoSortedArrays.java` |

- **E01 hint:**
  <details><summary>hint</summary>Two nested loops, `i` from 0 to n-1, `j` from i+1 to n-1; check `arr[i] + arr[j] == target`.</summary></details>
- **E02 hint:**
  <details><summary>hint</summary>For each element `x`, check if `target - x` is already in a `HashSet` before adding `x` to the set.</details>
- **E03 hint:**
  <details><summary>hint</summary>Only increment the copy counter inside the resize branch, once per element actually copied into the new array — not on every push.</details>
- **E04 hint:**
  <details><summary>hint</summary>Binary search on the *smaller* array for a partition index `i`; the partition of the other array is determined as `j = (m+n+1)/2 - i`. Grow/shrink `i` based on comparing the boundary elements `nums1[i-1]`, `nums1[i]`, `nums2[j-1]`, `nums2[j]`.</details>

Target complexity — E01: O(n^2) time / O(1) space. E02: O(n) time / O(n)
space. E03: push O(1) amortized time. E04: O(log(min(m,n))) time / O(1)
space. Solutions are in `src/main/java/com/gk/study/foundations/solutions/`
— not shown here; attempt the exercises first.

## 9. Quick recap

- Big-O describes growth trend as `n → ∞`, dropping constants and low-order terms — it is not a stopwatch measurement.
- Amortized analysis averages cost over a *sequence* of operations; a single call can still be expensive (e.g. the resize call itself).
- Doubling (not fixed-increment) growth is what makes dynamic array/HashMap resizing amortized O(1) per operation.
- Constant factors and cache locality can make a "worse" Big-O algorithm faster in practice for small/realistic `n`.
- Recursive call stack depth counts as space complexity — it's easy to forget.
