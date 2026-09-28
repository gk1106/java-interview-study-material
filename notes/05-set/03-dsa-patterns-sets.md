# DSA patterns on sets

## 1. What it is

A catalog of the recurring interview patterns that use a `Set` (almost always `HashSet`) to turn
an `O(n²)` brute-force comparison into `O(n)`: **duplicate detection**, **set algebra**
(intersection/union/difference), and the **O(n) longest-consecutive-sequence trick**. The common
thread across all of them is trading `O(n)` extra space for collapsing "is this value present
anywhere else" from an `O(n)` linear scan down to an `O(1)` average hash lookup.

## 2. How it works internally

**Duplicate detection** — the single most common `Set` pattern: scan once, and for each element
try to `add()` it to a "seen" set; if `add` returns `false` (JDK `Set.add` contract: returns
`false` when the element was already present), you've found a duplicate on this exact pass,
without a second nested loop.
```
nums:  [4, 3, 2, 7, 8, 2, 3, 1]
seen:  {}
i=0: add(4) -> true,  seen={4}
i=1: add(3) -> true,  seen={4,3}
i=2: add(2) -> true,  seen={4,3,2}
i=3: add(7) -> true,  seen={4,3,2,7}
i=4: add(8) -> true,  seen={4,3,2,7,8}
i=5: add(2) -> FALSE  -> 2 is a duplicate
i=6: add(3) -> FALSE  -> 3 is a duplicate
```
A close variant, **contains-nearby-duplicate-within-k**, adds a sliding window on top: keep only
the last `k` elements in the set (remove the element leaving the window as you advance), so
`contains()` only ever answers "is this a duplicate *within the last k positions*."

**Set algebra (intersection / union / difference)** — build a `HashSet` from one collection, then
classify each element of the other in O(1) average per element:
```
A = {1,2,3,4},  B = {3,4,5,6}

union        = A ∪ B  -> new HashSet<>(A); result.addAll(B);              -> {1,2,3,4,5,6}
intersection = A ∩ B  -> new HashSet<>(A); result.retainAll(B);           -> {3,4}
difference   = A − B  -> new HashSet<>(A); result.removeAll(B);           -> {1,2}
```
`retainAll`/`removeAll` internally iterate the **smaller** of the two sets when possible and
probe the other with `contains()` — always build the `HashSet` from whichever input lets you
minimize total work; for two arrays, convert the smaller one to a set and probe it while scanning
the larger one directly, avoiding building a second set entirely.

**Longest consecutive sequence — the O(n) HashSet "start of sequence" trick** — the brute-force
approach (sort, then scan for runs) is `O(n log n)`. The `O(n)` trick: put every number in a
`HashSet`, then for each number, only start counting a run from it if `num - 1` is **not** in the
set (i.e., it's the start of a run) — every number is only ever the start of *at most one* run
across the whole algorithm, so the total work across all runs is `O(n)` even though it looks like
a nested loop.
```
nums = [100, 4, 200, 1, 3, 2]
set  = {100, 4, 200, 1, 3, 2}

num=100: 99 not in set -> start of a run. count: 100(len1) -> 101? not in set -> run length 1
num=4:    3 in set -> NOT a start, skip (it'll be counted from 1's run)
num=200: 199 not in set -> start. 200(len1) -> 201? not in set -> run length 1
num=1:    0 not in set -> start. 1,2,3,4 all in set, 5 not -> run length 4  <-- longest
num=3:    2 in set -> NOT a start, skip
num=2:    1 in set -> NOT a start, skip

longest = 4  (the run {1,2,3,4})
```
Why this is `O(n)` total and not `O(n²)`: every number is visited by the inner "extend the run"
loop **at most once** across the *entire* algorithm's lifetime — once `4` has been consumed while
extending the run starting at `1`, it will never be re-walked, because it's never a run start
(its predecessor `3` is always in the set). Summing the lengths of all runs across the whole input
is bounded by `n`.

## 3. Complexity

| Pattern | Time | Space | Turns what brute force into what |
|---------|------|-------|-----------------------------------|
| Duplicate detection (`add()` returns false) | O(n) | O(n) | O(n²) pairwise comparison → O(n) single pass |
| Contains-nearby-duplicate-within-k (sliding window + Set) | O(n) | O(min(n, k)) | O(n·k) windowed comparison → O(n) |
| Union / intersection / difference | O(n + m) | O(min(n, m)) if you set-ify only the smaller side, else O(n + m) | O(n·m) nested-loop comparison → O(n + m) |
| Longest consecutive sequence | O(n) | O(n) | O(n log n) sort-based scan → O(n), and O(n²) naive-expand → O(n) |
| Group anagrams (canonical-key grouping) | O(n · k log k), k = max word length | O(n · k) | O(n² · k) pairwise anagram check → O(n · k log k) |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/set/examples/SetPatternsDemo.java` — walks through
  duplicate detection, union/intersection/difference, and the longest-consecutive-sequence trick
  on small sample inputs with printed step traces.

```java
int[] nums = {100, 4, 200, 1, 3, 2};
Set<Integer> set = new HashSet<>();
for (int n : nums) set.add(n);

int longest = 0;
for (int n : set) {
    if (!set.contains(n - 1)) {               // only enter from a run's true start
        int length = 1;
        while (set.contains(n + length)) length++;
        longest = Math.max(longest, length);
    }
}
System.out.println(longest); // 4  (the run {1,2,3,4})
```

## 5. When to use / when NOT to use

- Duplicate detection via `Set`: any "have I seen this before" question — NOT the right tool when
  you also need the **count** of occurrences (use a `Map<T, Integer>` frequency count instead) or
  need duplicates in their **original positions preserved with order** for output (pair a
  `LinkedHashSet` with your duplicate-tracking set instead).
- Set algebra: comparing two collections for overlap — NOT worth it for a single membership check
  against a *tiny* fixed collection (a handful of `||`/`==` comparisons can beat the overhead of
  building a `HashSet` for 3-4 elements).
- Longest consecutive sequence trick: specifically the "longest run of consecutive integers,
  order doesn't matter in the input" shape — NOT applicable if the sequence must be *contiguous in
  the original array* (that's a sliding-window problem, not this one) or if elements aren't
  integers with a well-defined "+1 neighbor" relationship.

## 6. Common pitfalls & gotchas

**Forgetting `add()`'s boolean return value is the whole trick** — re-implementing duplicate
detection with a separate `contains()` check plus a follow-up `add()` call works but does two
hash lookups instead of one:
```java
if (seen.contains(n)) { duplicates.add(n); } else { seen.add(n); }  // 2 lookups worst case
if (!seen.add(n)) { duplicates.add(n); }                             // 1 lookup, same result
```

**Mutating a set while iterating it directly** (rather than the collection you're comparing it
against) throws `ConcurrentModificationException` — always iterate a *copy* or the *other*
collection when computing set algebra in place:
```java
for (Integer x : a) { if (b.contains(x)) a.remove(x); }  // BUG: mutates `a` while iterating `a`
// fix: a.retainAll(b);  or iterate a copy: for (Integer x : new HashSet<>(a)) { ... }
```

**Not checking `num - 1` before expanding a run** — omitting the "start of sequence" guard turns
the `O(n)` longest-consecutive-sequence algorithm back into `O(n²)` in the worst case (every
number re-walks the same run from every position inside it):
```java
for (int n : set) {                       // BUG: no guard -> every element re-scans its whole run
    int length = 1;
    while (set.contains(n + length)) length++;
}
// fix: skip n entirely unless set.contains(n - 1) is false
```

**Using `array.length`/list size instead of the union/intersection set's size when the inputs
themselves contain duplicates** — `new HashSet<>(list)` silently collapses duplicates within a
single input *before* any set algebra even runs; if the problem cares about multiplicities (e.g.
"how many times does each element appear in both"), a `Set`-only approach is the wrong tool — use
a frequency `Map` (a multiset) instead.

## 7. Interview questions

- [Basic] Why is checking `Set.add()`'s return value the idiomatic way to detect duplicates in one
  pass? → `add()` is documented to return `false` if the element was already present (and the set
  was therefore unchanged) — checking it folds the "have I seen this" lookup and the "mark it
  seen" write into a single hash operation instead of a separate `contains()` + `add()` pair. →
  Follow-up: *Does this work identically for `TreeSet`?* Yes, same `add()` contract on the `Set`
  interface, just O(log n) per call instead of O(1) average.
- [Basic] How do you compute the intersection of two `List<Integer>` using sets, and what's the
  time complexity? → Convert one list (ideally the smaller) to a `HashSet`, then either call
  `retainAll` on a copy or scan the other list checking `contains()` per element and collecting
  matches — O(n + m) total, versus the O(n·m) nested-loop brute force. → Follow-up: *Does it
  matter which list you convert to a set first?* Yes for performance (convert the smaller one to
  minimize the set-build cost) but not for correctness — the mathematical result is the same
  either way.
- [Basic] What's the brute-force time complexity for "longest run of consecutive integers" and
  what does the `HashSet` trick reduce it to? → Naive brute force (for each number, walk forward
  checking `arr[i]+1`, `arr[i]+2`, ... by linear scanning the *array* each time) is O(n²) to
  O(n³) depending on implementation; sorting first gets O(n log n); the `HashSet`
  "only expand from a true start" trick gets O(n). → Follow-up: *Why can't you just sort and get
  O(n) instead of O(n log n)?* Comparison-based sorting is fundamentally O(n log n); you'd need a
  non-comparison sort (counting/radix, bounded input range) to beat that, which the `HashSet`
  approach sidesteps entirely by never sorting.
- [Intermediate] Walk through why the longest-consecutive-sequence `HashSet` algorithm is O(n) and
  not O(n²) despite the nested `while` loop. → Every number can only ever be the **start** of a
  run if its predecessor (`num - 1`) is absent from the set — that guard means the expensive inner
  `while` loop only ever executes starting from true run-starts; every element of the input is
  consumed by the inner loop of at most one run's expansion across the entire algorithm, so the
  total iterations of the inner loop, summed across every outer iteration, is bounded by n, not
  n². → Follow-up: *What breaks the O(n) guarantee if you remove the `num - 1` check?* Every
  element would try to expand its own run from scratch, re-walking runs it's already inside
  multiple times — worst case (all n numbers consecutive) becomes O(n²).
- [Intermediate] How would you find duplicates within a sliding window of size k (contains-nearby-
  duplicate) efficiently? → Maintain a `HashSet` representing exactly the last k elements seen; for
  each new element, check `contains()` first (O(1) — a duplicate within the window means answer
  found), then `add()` it, and if the window now exceeds size k, `remove()` the element that just
  slid out of the window (`arr[i - k]`) — O(n) total, O(min(n, k)) space. → Follow-up: *Why not
  just use a plain HashSet over the whole array without evicting old elements?* That answers "is
  there any duplicate anywhere," not "within a window of k" — a duplicate more than k apart must
  not count, so eviction is essential to correctness, not just an optimization.
- [Intermediate] How would you group anagrams using a `Set`/`Map`-based canonical key? → For each
  word, compute a canonical form that's identical for all anagrams of each other — either the
  sorted character sequence (`"eat"` → `"aet"`) or a fixed-length character-frequency signature
  (26 counts for lowercase English) — then group words into a `Map<String, List<String>>` (or
  `Map<CanonicalKey, Set<String>>`) keyed by that canonical form; words sharing a key are
  anagrams. Sorting-based key is O(k log k) per word (k = word length); frequency-signature key is
  O(k) per word but with a larger constant (building/comparing a 26-length array/string). →
  Follow-up: *Which canonical-key strategy is faster for very long words?* The frequency-count
  signature, since it's O(k) instead of O(k log k) per word — the sort-based key only wins on
  simplicity/readability for short words.
- [Intermediate] Single number: compare solving it with XOR versus with a `HashSet`. → XOR
  approach: XOR every element together; pairs cancel to 0 (`x ^ x == 0`) and XOR with 0 is a
  no-op, so the final accumulated value is exactly the element that appears once — O(n) time,
  **O(1) space**, single pass, no auxiliary structure at all. `HashSet` approach: `add()` each
  element, and if `add()` returns false (already present) `remove()` it instead — whatever's left
  in the set at the end is the singleton — also O(n) time, but O(n) space for the set. → Follow-up:
  *When would you still prefer the HashSet approach over XOR despite it needing more space?* When
  the problem isn't "exactly one element appears once, rest appear exactly twice" but a looser
  variant (e.g. "rest appear an unknown/variable number of times, find the one with odd
  frequency," or you need to identify *which* frequency each element has) — XOR's cancellation
  trick only cleanly solves the exact "pairs cancel" shape.
- [Advanced] Design "smallest range covering at least one element from each of K sorted lists"
  using a `TreeSet`. → Insert one `(value, listIndex)` pointer per list into a `TreeSet<int[]>`
  ordered by value, and track the running maximum value among all current pointers; the current
  range's minimum is always `treeSet.first()` (smallest of the k pointers) and its maximum is the
  tracked running max — repeatedly: record `(first, runningMax)` as a candidate if it's the
  tightest range seen; remove `first`, advance that list's pointer to its next element, insert the
  new pointer, update the running max; stop once any list runs out of elements (no valid range can
  include all k lists anymore). Each of the N total elements across all lists is inserted and
  removed from the `TreeSet` at most once, each op O(log k), so overall O(N log k). → Follow-up:
  *Why a `TreeSet` here instead of a min-heap (`PriorityQueue`)?* A `PriorityQueue` gives you the
  minimum in O(1)/O(log k) removal just as well, and is the more common textbook answer — a
  `TreeSet` works identically here since you only ever need "smallest current pointer," and adds
  the option of `pollFirst()`/`first()` semantics if you also wanted range/navigation queries mid-
  algorithm, which a heap can't offer.
- [Advanced] How would you find the top-K frequent elements in a **live, unbounded data stream**
  (can't sort at the end) using a `TreeSet`/`TreeMap`-based structure? → Maintain a frequency
  `Map<T, Integer>` updated on every incoming element, plus a bounded structure holding only the
  current top-K by frequency; because a plain `TreeSet` can't hold two elements with the same sort
  key (frequency) without collapsing them, use a `TreeMap<Integer, Set<T>>` (frequency → set of
  elements with that frequency) as a "bucketed multiset": on each update, remove the element from
  its old frequency bucket (and drop the bucket if now empty), increment its count, reinsert into
  the new frequency bucket, and if the total tracked element count exceeds K, evict one element
  from the *lowest* frequency bucket (`treeMap.firstEntry()`) — every update touches O(log K)
  buckets. → Follow-up: *Why not just re-sort the whole frequency map after every new stream
  element?* That's O(n log n) per single new element, which is disastrous for a high-throughput
  stream — the bucketed-`TreeMap` approach keeps each update to O(log K), independent of how many
  distinct elements have been seen historically.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E2 | Easy | Union, intersection and difference of two integer arrays | Set algebra | `exercises/SetOperations.java` |
| E3 | Easy | Contains duplicate within distance k | Sliding window + Set | `exercises/ContainsNearbyDuplicate.java` |
| M1 | Medium | Longest consecutive sequence | O(n) HashSet "start of run" trick | `exercises/LongestConsecutiveSequence.java` |
| M2 | Medium | Group anagrams | Canonical-key grouping via Map/Set | `exercises/GroupAnagrams.java` |
| M3 | Medium | Single number (exactly one element appears once, rest twice) | XOR vs HashSet comparison | `exercises/SingleNumber.java` |

**E2 — Union, intersection, difference**
- Input: `a = [1,2,3,4]`, `b = [3,4,5,6]` → Output: union `{1,2,3,4,5,6}`, intersection `{3,4}`,
  difference (a−b) `{1,2}`
- Constraint: O(n + m) time.
- <details><summary>Hint</summary>Build one `HashSet` from `a`; use `addAll`/`retainAll`/
  `removeAll` against a set built from `b` (or against `b` directly for `addAll`) — remember
  `retainAll`/`removeAll` mutate the receiver, so work on a fresh copy per result you need.</details>

**E3 — Contains nearby duplicate within k**
- Input: `nums = [1,2,3,1]`, `k = 3` → Output: `true` (two `1`s at indices 0 and 3, distance 3 ≤ k)
- Input: `nums = [1,2,3,1,2,3]`, `k = 2` → Output: `false` (nearest repeat distance is 3 > k)
- Constraint: O(n) time, O(min(n, k)) space.
- <details><summary>Hint</summary>Keep a `HashSet` holding only the last `k` elements; check
  `contains` before adding each new element, and remove `nums[i - k]` once the window would
  exceed size k.</details>

**M1 — Longest consecutive sequence**
- Input: `[100, 4, 200, 1, 3, 2]` → Output: `4` (the run `{1,2,3,4}`)
- Constraint: O(n) time, O(n) space — no sorting allowed.
- <details><summary>Hint</summary>Put everything in a `HashSet`; only start expanding a run from
  `n` when `n - 1` is absent from the set, then count forward (`n+1`, `n+2`, ...) while present.</details>

**M2 — Group anagrams**
- Input: `["eat", "tea", "tan", "ate", "nat", "bat"]` → Output:
  `[["eat","tea","ate"], ["tan","nat"], ["bat"]]` (group order and in-group order not significant)
- Constraint: O(n · k log k) time (k = max word length), O(n · k) space.
- <details><summary>Hint</summary>Map each word to a canonical key (its characters sorted into a
  `String`, or a 26-length frequency signature) and group words into a
  `Map<String, List<String>>` keyed by that canonical form.</details>

**M3 — Single number**
- Input: `[4, 1, 2, 1, 2]` → Output: `4`
- Constraint: O(n) time. Implement both the XOR (O(1) space) and HashSet (O(n) space) approaches
  and compare.
- <details><summary>Hint</summary>XOR every element together — every value that appears exactly
  twice cancels itself out (`x ^ x == 0`), leaving only the singleton. For the HashSet variant,
  `add()` on first sighting, `remove()` on second sighting — whatever remains is the answer.</details>

Solutions are in the `solutions` package (`SetOperationsSolution`, `ContainsNearbyDuplicateSolution`,
`LongestConsecutiveSequenceSolution`, `GroupAnagramsSolution`, `SingleNumberSolution`) — attempt
the stubs first.

## 9. Quick recap

- Duplicate detection: check `Set.add()`'s boolean return value — `false` means "already present,"
  folding lookup + insert into one hash operation.
- Set algebra: `addAll` = union, `retainAll` = intersection, `removeAll` = difference; always work
  on a fresh copy since all three mutate the receiving set in place.
- Longest consecutive sequence: put everything in a `HashSet`, only expand a run from `n` when
  `n - 1` is absent — that single guard is what keeps the algorithm O(n) instead of O(n²).
- Group anagrams: canonical key (sorted chars, or a frequency signature) groups words into
  buckets in a `Map`, turning an O(n²) pairwise-comparison problem into O(n · k log k).
- Single number: XOR beats HashSet when the problem shape is "pairs cancel" (O(1) space vs O(n)) —
  know both, but recognize when HashSet is still the right (or only) tool for a looser variant.
