# DSA patterns with Map (HashMap/TreeMap)

## 1. What it is

A `HashMap` (or `TreeMap` for order-sensitive variants) is the workhorse structure for a huge
class of interview problems: anything that needs "have I seen this before," "how many times have
I seen this," or "what's the complementary value I need" in better than O(n²) time. This topic
collects the canonical patterns and this module's full exercise set.

## 2. How it works internally

### Pattern 1 — Frequency counting

`Map<T, Integer> freq` built by iterating once, doing `freq.merge(item, 1, Integer::sum)` (or the
older `freq.put(item, freq.getOrDefault(item, 0) + 1)`) per element — O(n) to build, O(1) average
per lookup afterward. Backbone of: majority element, anagram checks, top-K frequent, "first
non-repeating character."

### Pattern 2 — Two-sum (complement lookup)

Instead of checking every pair (O(n²)), walk the array once; for each element `x`, check whether
`target - x` was **already seen** (stored in a `Map<value, index>` as you go). If found, you have
your pair in O(1) average lookup; if not, record `x`'s index and continue. Single pass, O(n) time,
O(n) space — the map trades space for collapsing the "search for a partner" step from O(n) to
O(1) average.

```
ASCII trace: arr = [2, 7, 11, 15], target = 9
i=0: x=2, need 7, seen={} -> not found -> seen={2:0}
i=1: x=7, need 2, seen={2:0} -> FOUND at index 0 -> return [0, 1]
```

### Pattern 3 — Group anagrams (canonical-key bucketing)

Map each string to a **canonical form** (sorted characters, or a 26-length character-count
signature) as the map key, and bucket every original string that shares that canonical form into
a `Map<String, List<String>> groups`. O(n * k log k) for sorting-based canonicalization (k =
average string length), or O(n * k) using a count-signature key instead of sorting.

### Pattern 4 — Subarray sum equals K (prefix-sum + HashMap)

The classic "count subarrays summing to K" problem. Key insight: if `prefix[j] - prefix[i] = K`
for some `i < j`, then the subarray `(i, j]` sums to K. Rearranged: `prefix[i] = prefix[j] - K`.
So while scanning left to right and maintaining a running `prefixSum`, at each index check how
many *earlier* prefix sums equal `prefixSum - K` — stored in a `Map<prefixSumValue, count>` built
incrementally. This turns an O(n²) brute-force (every subarray) into a single O(n) pass.

```
ASCII trace: arr = [1, 2, 3], K = 3
running prefixSum, map starts {0: 1} (empty prefix sums to 0, seeds the "whole prefix equals K" case)

i=0: prefixSum=1, need prefixSum-K=-2, map has none -> count+=0; map={0:1, 1:1}
i=1: prefixSum=3, need 0, map has {0:1} -> count+=1 (subarray [1,2]); map={0:1,1:1,3:1}
i=2: prefixSum=6, need 3, map has {3:1} -> count+=1 (subarray [3]);  map={...,6:1}
total count = 2   -> subarrays [1,2] and [3] both sum to 3
```
The `{0: 1}` seed is essential — it accounts for a subarray starting at index 0 whose sum already
equals K exactly (prefix minus "nothing before it" equals K).

### Pattern 5 — Top-K frequent elements (HashMap + heap)

Build a frequency map (Pattern 1), then use a min-heap of size K (or a full sort of the frequency
entries) to extract the K most frequent — O(n) to build the frequency map, O(n log K) to maintain
a size-K heap while scanning all distinct entries (cheaper than sorting all n log n when K is
small).

### Pattern 6 — Sliding window with a HashMap of counts (longest substring with at most K distinct
characters)

Maintain a `Map<Character, Integer> windowCounts` for the current window `[left, right]`; expand
`right`, incrementing counts; whenever `windowCounts.size() > K`, shrink from `left`, decrementing
(and removing at 0) until the distinct-count constraint holds again; track the max window length
seen. O(n) — each pointer moves at most n times total.

### Pattern 7 — Time-based key-value store (TreeMap binary search)

For each key, maintain a `TreeMap<Long timestamp, V value>` of everything ever `set` for that key.
`get(key, timestamp)` is `treeMap.floorEntry(timestamp)` — the value set at the largest timestamp
`<= timestamp` — O(log n) per query thanks to the red-black tree backing (see topic 3), instead of
a linear scan over every historical value for that key.

## 3. Complexity

| Pattern | Time | Space | Notes |
|---------|------|-------|-------|
| Frequency counting | O(n) | O(distinct elements) | one pass, `merge`/`getOrDefault` |
| Two-sum | O(n) | O(n) | single pass, complement lookup |
| Group anagrams | O(n * k log k) or O(n * k) | O(n * k) | k = avg string length; count-signature avoids the sort |
| Subarray sum = K | O(n) | O(n) | prefix-sum counts in a map, seeded with `{0:1}` |
| Top-K frequent | O(n log K) | O(n) | frequency map + size-K heap |
| Longest substring ≤ K distinct | O(n) | O(K) | sliding window, map bounded to K+1 distinct chars |
| Time-based KV store | O(log n) per `get`, O(1) per `set` | O(n) | `TreeMap` per key, `floorEntry` |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/map/examples/MapPatternsDemo.java` — runs each
  pattern above against a small hard-coded input and prints the result.

```java
int[] result = TwoSumHashMapSolution.solve(new int[]{2, 7, 11, 15}, 9);
System.out.println(Arrays.toString(result));   // [0, 1]

int count = SubarraySumEqualsKSolution.solve(new int[]{1, 2, 3}, 3);
System.out.println(count);                     // 2
```
Expected console output:
```
[0, 1]
2
```

## 5. When to use / when NOT to use

- Reach for a `HashMap` pattern whenever brute force is "for every element, scan the rest of the
  array/string" (O(n²)) — almost always collapsible to O(n) by remembering what you've already
  seen.
- Use `TreeMap`/prefix-sum-with-`TreeMap` when the problem needs an *ordered* or *nearest-match*
  lookup (time-based store, range sums) rather than exact-match (`HashMap` suffices for exact
  match only).
- Don't reach for a map when a fixed small alphabet (e.g. lowercase a-z) makes a plain
  `int[26]` array strictly faster and simpler than `Map<Character, Integer>` — arrays avoid
  boxing and hashing overhead entirely when the key space is small and dense.

## 6. Common pitfalls & gotchas

**Off-by-one on the prefix-sum seed** — forgetting `map.put(0, 1)` before the scan undercounts
subarrays that start at index 0 and already sum to K.

**Using the wrong canonical key for group-anagrams** on Unicode input — sorting characters works
for any Unicode string, but a fixed `int[26]` count signature silently breaks for non-a-z input;
know which one your input guarantees before choosing the cheaper array-based signature.

**Two-sum: checking the complement before recording the current element in the wrong order**
if a problem allows using the same index twice by mistake — always check "seen before this
index," then record the current element, in that order, so an element never pairs with itself
unless the problem explicitly allows it.

**Autoboxed `Integer` keys and `==`** — iterating a frequency map and comparing counts with `==`
instead of `.equals()`/`intValue()` breaks silently outside the `Integer` cache range `[-128,
127]` (see `notes/01-java-foundations-for-dsa`); always compare boxed values with `.equals()` or
unbox first.

## 7. Interview questions

- [Basic] Why is a HashMap the standard tool for two-sum instead of nested loops? → Nested loops
  are O(n²) (check every pair); a single pass storing "value seen so far -> index" in a map turns
  the search for a partner into an O(1) average lookup, giving O(n) total. → Follow-up: *What if
  the array is already sorted?* A two-pointer approach also achieves O(n) with O(1) extra space,
  no map needed — sortedness lets you avoid the hash map entirely.
- [Basic] How do you count word/character frequencies efficiently? → One pass, `map.merge(item, 1,
  Integer::sum)` per element — O(n) time, O(distinct items) space. → Follow-up: *What's the
  fixed-array alternative when counting lowercase letters only?* `int[26]`, indexed by
  `ch - 'a'` — faster than a `Map<Character,Integer>` since it avoids boxing and hashing for a
  known small dense key space.
- [Basic] Majority element (appears more than n/2 times) — HashMap approach vs Boyer-Moore voting?
  → HashMap: frequency-count every element, O(n) time, O(n) space, works for any threshold (not
  just majority). Boyer-Moore: maintain a `candidate` and a `count`; increment count when seeing
  the candidate, decrement otherwise, swap candidate when count hits 0 — O(n) time, **O(1) space**,
  but only guaranteed correct when a true majority element is known to exist (needs a verification
  pass otherwise). → Follow-up: *When would you still prefer the HashMap approach?* When you need
  the full frequency distribution anyway, or when no true majority is guaranteed to exist and you
  need exact counts rather than just a majority check.
- [Intermediate] Explain the prefix-sum + HashMap technique for "subarray sum equals K." → Track a
  running prefix sum while scanning left to right, and a map of `prefixSumValue -> how many times
  seen so far`. At each index, the number of valid subarrays ending here equals the number of
  earlier prefix sums equal to `currentPrefixSum - K` (since their difference is exactly K); seed
  the map with `{0: 1}` to account for subarrays starting at index 0. One O(n) pass total. →
  Follow-up: *Does this work if the array contains negative numbers?* Yes — unlike a sliding-window
  approach (which requires monotonic prefix sums, i.e. non-negative numbers), the prefix-sum+map
  technique works for any integers, including negatives, since it doesn't rely on shrinking/
  growing a window monotonically.
- [Intermediate] How would you find the top-K most frequent elements efficiently? → Build a
  frequency map in O(n), then maintain a min-heap of size K while scanning the map's entries:
  push each entry, and if the heap exceeds size K, pop the smallest — O(n log K) total, better
  than sorting all distinct entries (O(n log n)) when K is small. → Follow-up: *Is there a way to
  do it in O(n) average time?* Yes, via bucket sort on frequency (frequencies range from 1 to n,
  so bucket entries by frequency into an array of lists, then walk from the highest-frequency
  bucket down, collecting K elements) — O(n) but with more implementation complexity.
- [Intermediate] Walk through "longest substring with at most K distinct characters" using a
  sliding window + HashMap. → Maintain `windowCounts: Map<Character,Integer>` for the current
  window; expand the right pointer, incrementing the entering character's count; whenever
  `windowCounts.size()` exceeds K, shrink from the left, decrementing the leaving character's
  count and removing it from the map once it hits 0, until the window is valid again (≤ K distinct
  chars); track the max `(right - left + 1)` seen throughout. Each pointer only moves forward, so
  total work is O(n). → Follow-up: *What changes if K = 0?* The window can never contain any
  characters, so the answer is trivially 0 (or the loop naturally never expands a valid window) —
  worth explicitly handling/testing as an edge case.
- [Advanced] Design a time-based key-value store: `set(key, value, timestamp)` (timestamps
  strictly increasing per key) and `get(key, timestamp)` returns the value set at the largest
  timestamp `<= timestamp`, or `""`/empty if none. → Maintain `Map<String, TreeMap<Long, String>>`
  — one `TreeMap` per key, keyed by timestamp. `set` is `O(log n)` (or O(1) amortized if you can
  guarantee append-only strictly-increasing timestamps and use a plain list + binary search
  instead — the TreeMap version handles out-of-order timestamps generically too).
  `get` is `outerMap.get(key).floorEntry(timestamp)` — O(log n) via the red-black tree's floor
  search, returning `""` if no such entry (or the map/key doesn't exist). → Follow-up: *Could you
  use a plain ArrayList + binary search instead of TreeMap per key?* Yes, if `set` calls are
  guaranteed strictly increasing in timestamp per key (as most versions of this problem specify) —
  append to a list and binary-search (`Collections.binarySearch` variant, or manual) for the floor
  index; that avoids per-entry tree node overhead, trading `TreeMap`'s generality (handles
  out-of-order inserts) for a leaner structure when the ordering guarantee holds.
- [Advanced] Why does the "group anagrams" problem's choice of canonical key matter for both
  correctness and performance? → Correctness: the canonical key must map every anagram of a word
  to the *same* key and no non-anagram to that same key — sorted characters or a full character-
  count signature both satisfy this; a partial/lossy signature (e.g. just the character set,
  ignoring counts) would incorrectly group "aab" with "ab". Performance: sorting each string costs
  O(k log k) (k = string length), while building a fixed-size count signature (e.g. `int[26]`
  turned into a string/array key) costs O(k) — for large inputs with long strings, the
  count-signature approach is asymptotically faster, at the cost of only working cleanly for a
  known bounded alphabet (lowercase a-z), not arbitrary Unicode. → Follow-up: *How would you adapt
  the count-signature approach for arbitrary Unicode input?* Use a `Map<Character,Integer>`
  per-string signature (e.g. serialized to a canonical string via sorted entries) instead of a
  fixed `int[26]` array, trading some of the performance win for generality.

## 8. Exercises

This table covers the full exercise set for module 06-map (all topics' exercises are grouped
here per the module's package layout).

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| E1 | Easy | Two-sum: return indices of the two numbers that add up to a target | complement lookup | `exercises/TwoSumHashMap.java` |
| E2 | Easy | First non-repeating character in a string | frequency counting | `exercises/FirstNonRepeatingChar.java` |
| E3 | Easy | Majority element (appears more than n/2 times) | frequency counting (+ Boyer-Moore discussed in notes) | `exercises/MajorityElement.java` |
| M1 | Medium | Group anagrams from a list of strings | canonical-key bucketing | `exercises/GroupAnagrams.java` |
| M2 | Medium | Count subarrays whose sum equals K | prefix-sum + HashMap | `exercises/SubarraySumEqualsK.java` |
| M3 | Medium | Top-K most frequent elements | frequency map + heap | `exercises/TopKFrequentElements.java` |
| H1 | Hard | Longest substring with at most K distinct characters | sliding window + HashMap | `exercises/LongestSubstringKDistinct.java` |
| H2 | Hard | Time-based key-value store (`set`/`get` by timestamp) | `TreeMap` `floorKey` binary search | `exercises/TimeBasedKeyValueStore.java` |
| B1 | Build-it-yourself | Implement `MyHashMap<K,V>` from scratch (separate chaining, 0.75 load factor resize) | hashing + dynamic bucket array | `exercises/MyHashMapExercise.java` |
| B2 | Build-it-yourself | Implement an O(1) get/put `LRUCache<K,V>` from your own HashMap + doubly linked list (not a `LinkedHashMap` wrapper) | HashMap + intrusive doubly linked list | `exercises/LRUCacheExercise.java` |

**E1 — Two-sum**
- Input: `nums=[2,7,11,15], target=9` → Output: `[0,1]`
- Constraint: O(n) time, one pass, each input has exactly one solution, may not use the same
  element twice.
- <details><summary>Hint</summary>Map each seen value to its index as you scan; before recording
  the current element, check whether `target - current` is already in the map.</details>
- Target complexity: O(n) time, O(n) space.

**E2 — First non-repeating character**
- Input: `"leetcode"` → Output: `0` (index of `'l'`); Input: `"aabb"` → Output: `-1`
- Constraint: O(n) time; lowercase English letters (but the solution should not assume this to
  earn full marks — using a `Map<Character,Integer>` generalizes beyond a-z).
- <details><summary>Hint</summary>Two passes: first build a frequency map, then scan the string
  again in order and return the first index whose character has frequency 1.</details>
- Target complexity: O(n) time, O(1) extra space if bounded to a fixed alphabet, else
  O(distinct chars).

**E3 — Majority element**
- Input: `[2,2,1,1,1,2,2]` → Output: `2`
- Constraint: a majority element (appears `> n/2` times) is guaranteed to exist.
- <details><summary>Hint</summary>Implement with a frequency `HashMap` first; as a stretch, also
  implement the O(1)-space Boyer-Moore voting variant and compare — see notes section 7.</details>
- Target complexity: O(n) time; O(n) space (HashMap) or O(1) space (Boyer-Moore).

**M1 — Group anagrams**
- Input: `["eat","tea","tan","ate","nat","bat"]` → Output: `[["eat","tea","ate"],["tan","nat"],
  ["bat"]]` (any order of groups/within groups)
- Constraint: lowercase English letters only.
- <details><summary>Hint</summary>Map each string to a canonical key (sorted chars, or a 26-length
  count signature) and bucket into `Map<String, List<String>>`.</details>
- Target complexity: O(n * k log k) (sort-based) or O(n * k) (count-signature-based), k = avg
  string length.

**M2 — Subarray sum equals K**
- Input: `nums=[1,2,3], k=3` → Output: `2` (subarrays `[1,2]` and `[3]`)
- Constraint: array may contain negative numbers; O(n) time required.
- <details><summary>Hint</summary>Running prefix sum + `Map<prefixSum, count>`, seeded with
  `{0: 1}`; at each index add `map.getOrDefault(prefixSum - k, 0)` to the running total.</details>
- Target complexity: O(n) time, O(n) space.

**M3 — Top-K frequent elements**
- Input: `nums=[1,1,1,2,2,3], k=2` → Output: `[1,2]` (any order)
- Constraint: O(n log k) target; answer is unique for the given input.
- <details><summary>Hint</summary>Frequency map first, then a min-heap (`PriorityQueue`) of size
  k ordered by frequency, popping the smallest whenever the heap exceeds size k.</details>
- Target complexity: O(n log k) time, O(n) space.

**H1 — Longest substring with at most K distinct characters**
- Input: `s="eceba", k=2` → Output: `3` (substring `"ece"`)
- Constraint: O(n) time required; `k >= 0`.
- <details><summary>Hint</summary>Sliding window with a `Map<Character,Integer>` of counts for
  the current window; shrink from the left whenever `map.size() > k`.</details>
- Target complexity: O(n) time, O(k) space.

**H2 — Time-based key-value store**
- Operations: `set(key, value, timestamp)`, `get(key, timestamp)` → returns the value set at the
  largest timestamp `<= timestamp` for that key, or `""` if none exists. Timestamps for `set`
  calls on the same key are strictly increasing.
- Constraint: `get` should be O(log n); `set` should be O(log n) or better.
- <details><summary>Hint</summary>`Map<String, TreeMap<Long, String>>` — one `TreeMap` per key;
  `get` is `floorEntry(timestamp)` on that key's `TreeMap`.</details>
- Target complexity: O(log n) per `get`, O(log n) (or amortized O(1) with a list + binary search
  variant) per `set`.

**B1 — MyHashMap&lt;K,V&gt;**
- Implement: `put(K,V)`, `get(K)`, `remove(K)`, `containsKey(K)`, `size()`, `isEmpty()`.
- Constraint: separate chaining via your own `Node<K,V>[] table`; resize (double capacity, rehash
  every entry) once `size > capacity * 0.75`; O(1) average get/put.
- <details><summary>Hint</summary>Spread the hash the same way the JDK does
  (`h ^ (h >>> 16)`) before masking with `(capacity - 1)`; on resize, either fully rehash or
  implement the low/high-list split optimization described in
  `notes/06-map/01-hashmap-internals.md`.</details>
- Target complexity: O(1) average get/put/remove, O(n) amortized total resize cost.

**B2 — LRUCache&lt;K,V&gt;**
- Implement: `get(K)` (returns value or a sentinel/`null`, and marks the key as most recently
  used), `put(K,V)` (inserts/updates and marks most recently used, evicting the least-recently-
  used entry if over capacity).
- Constraint: **must not** wrap `LinkedHashMap` — build it from your own `HashMap<K, Node>` plus a
  hand-rolled doubly linked list with sentinel head/tail nodes; O(1) for both operations.
- <details><summary>Hint</summary>On `get`/`put`-on-existing-key, unlink the node and re-append it
  just before the tail sentinel; on `put` past capacity, remove the node right after the head
  sentinel (the least recently used) and remove it from the backing HashMap too.</details>
- Target complexity: O(1) worst case for both `get` and `put`.

Solutions for all ten exercises are in the `solutions` package (`TwoSumHashMapSolution.java`
through `LRUCache.java`) — attempt each stub in `exercises/` first.

## 9. Quick recap

- Frequency counting and complement lookup (two-sum) are the two most common HashMap patterns —
  both collapse an O(n²) brute force into O(n) by remembering what's already been seen.
- Prefix-sum + HashMap (subarray sum = K) works even with negative numbers, unlike a sliding
  window, because it doesn't rely on monotonic prefix sums — always seed with `{0: 1}`.
- Group-anagrams and top-K-frequent both layer a second structure (a bucket map, or a heap) on top
  of a first pass that builds a `HashMap`.
- Sliding window + HashMap-of-counts handles "at most K distinct" style substring problems in
  O(n) by only ever moving each pointer forward.
- `TreeMap.floorEntry`/`floorKey` is the right tool whenever a problem needs "nearest value at or
  before X" rather than an exact match — a plain `HashMap` cannot do this efficiently.
