# Module 07 — full problem set (30 problems)

Mixed practice drawing on everything from `List`, `Queue`/`Deque`, `Set` and `Map`
(modules 03-06). Each problem is a TODO stub in
`src/main/java/com/gk/study/dsaproblems/exercises/` with full Javadoc (problem statement,
input/output example, constraints, pattern tag, collection tag) — this file is the index,
not a restatement of that Javadoc. Solve the stub, then compare against
`src/main/java/com/gk/study/dsaproblems/solutions/`.

Grading: **10 Easy, 12 Medium, 8 Hard = 30 problems**, exceeding the module minimum of
3 Easy / 3 Medium / 2 Hard several times over. See `00-pattern-cheatsheet.md` first if a
pattern name below isn't immediately obvious.

## Easy (10)

| # | Level | Title | Pattern | Collection(s) used | File |
|---|-------|-------|---------|---------------------|------|
| E01 | Easy | Two-sum on sorted transaction amounts | two pointers | `List` (sorted, no aux structure) | `exercises/TwoSumSorted.java` |
| E02 | Easy | Valid anagram | frequency map | `HashMap` | `exercises/ValidAnagram.java` |
| E03 | Easy | Majority element (Boyer-Moore voting) | Boyer-Moore / greedy candidate-counting | `List` (no aux structure — O(1) space) | `exercises/MajorityElementBoyerMoore.java` |
| E04 | Easy | Best time to buy/sell a stock | single-pass greedy (min-so-far) | `List` | `exercises/BestTimeToBuyStock.java` |
| E05 | Easy | Contains a duplicate transaction ID within k apart | sliding window + set membership | `HashSet` | `exercises/ContainsDuplicateWithinK.java` |
| E06 | Easy | Flatten nested transaction batches | explicit stack (no recursion) | `Deque` (as a stack) | `exercises/FlattenNestedTransactionBatches.java` |
| E07 | Easy | First unique transaction ID | order-preserving frequency count | `LinkedHashMap` | `exercises/FirstUniqueTransactionId.java` |
| E08 | Easy | Merge overlapping account-hold intervals | sort + linear scan | `List` (sorted) | `exercises/MergeIntervals.java` |
| E09 | Easy | Kth largest transaction amount | min-heap of size k | `PriorityQueue` | `exercises/KthLargestElement.java` |
| E10 | Easy | Valid brackets AND valid XML-like tags | stack matching | `Deque` + `HashMap` (bracket-pair lookup) | `exercises/ValidBracketsAndTags.java` |

## Medium (12)

| # | Level | Title | Pattern | Collection(s) used | File |
|---|-------|-------|---------|---------------------|------|
| M01 | Medium | Longest substring without repeating characters | variable sliding window | `HashMap` (char → last index) | `exercises/LongestSubstringWithoutRepeating.java` |
| M02 | Medium | Group anagram transaction codes | canonical-key bucketing | `HashMap<String,List<String>>` | `exercises/GroupAnagramCodes.java` |
| M03 | Medium | Count subarrays of transactions with sum divisible by K | prefix sum (mod K) | `HashMap<Integer,Integer>` | `exercises/SubarraySumDivisibleByK.java` |
| M04 | Medium | Top-K frequent words, tie-break alphabetically | frequency map + heap w/ custom comparator | `HashMap` + `PriorityQueue` | `exercises/TopKFrequentWordsTieBreak.java` |
| M05 | Medium | Course schedule (can all courses be finished?) | topological sort (Kahn's BFS) | `HashMap` (adjacency + in-degree) + `Queue` (`ArrayDeque`) | `exercises/CourseScheduleTopoSort.java` |
| M06 | Medium | Number of islands (connected branch regions on a grid) | BFS/DFS over a grid | `Deque` (BFS queue) + `HashSet` (visited) | `exercises/NumberOfIslands.java` |
| M07 | Medium | Daily temperatures (days until a warmer day) | monotonic stack | `Deque` (indices) | `exercises/DailyTemperatures.java` |
| M08 | Medium | Max transaction in every rolling 24h window | monotonic deque | `Deque` (indices) | `exercises/SlidingWindowMaxTransaction.java` |
| M09 | Medium | Fixed-size most-recently-used transaction log | access-order eviction | `LinkedHashMap` | `exercises/FixedSizeMRUTransactionLog.java` |
| M10 | Medium | Find all anagram start indices in a string | fixed sliding window + frequency map | `HashMap<Character,Integer>` | `exercises/FindAllAnagramsInString.java` |
| M11 | Medium | Meeting rooms II (min rooms for overlapping meetings) | greedy + min-heap of end times | `PriorityQueue` + `List` (sorted starts) | `exercises/MeetingRoomsII.java` |
| M12 | Medium | Longest palindrome buildable by rearranging characters | frequency map | `HashMap<Character,Integer>` | `exercises/LongestPalindromeByRearranging.java` |

## Hard (8)

| # | Level | Title | Pattern | Collection(s) used | File |
|---|-------|-------|---------|---------------------|------|
| H01 | Hard | Median of a data stream | two heaps (max-heap low half, min-heap high half) | 2x `PriorityQueue` | `exercises/MedianOfDataStream.java` |
| H02 | Hard | Sliding window median | two heaps + lazy deletion | 2x `PriorityQueue` + `HashMap` (pending-removal counts) | `exercises/SlidingWindowMedian.java` |
| H03 | Hard | Trapping rain water | two pointers (O(1) space) | none — array only, contrast with the monotonic-stack O(n)-space approach | `exercises/TrappingRainWater.java` |
| H04 | Hard | Word ladder (fewest transformations begin → end word) | BFS over an implicit word graph | `HashSet` (dictionary + visited) + `Queue` (`ArrayDeque`) | `exercises/WordLadder.java` |
| H05 | Hard | Alien dictionary (derive letter order from sorted words) | topological sort from a partial order | `HashMap` (adjacency + in-degree) + `HashSet` (edges) + `Queue` | `exercises/AlienDictionary.java` |
| H06 | Hard | Number of provinces (connected groups of accounts) | Union-Find | `int[]` parent/rank arrays (see cheat sheet) | `exercises/NumberOfProvinces.java` |
| H07 | Hard | Redundant connection (edge that first creates a cycle) | Union-Find | `int[]` parent/rank arrays | `exercises/RedundantConnection.java` |
| H08 | Hard | LFU cache (evict least-frequently-used, ties broken by least-recently-used) | HashMap + frequency buckets | `HashMap` x2 + `LinkedHashSet` (per-frequency bucket, preserves recency order) | `exercises/LFUCache.java` |

---

## Per-problem context

**E01 — Two-sum on sorted transaction amounts.** A sorted list of transaction amounts and a
target refund total; return the 0-based indices of the two amounts that sum to the target.
Because the input is already sorted, two pointers beat a `HashMap` lookup — O(1) extra space
instead of O(n).

**E02 — Valid anagram.** Two strings; are they anagrams of each other (same multiset of
characters)? A single frequency map (increment for one string, decrement for the other,
check all zero) settles it in one pass.

**E03 — Majority element.** A list of account-flag codes where one value is guaranteed to
appear more than n/2 times; find it in O(1) space using Boyer-Moore voting instead of a
frequency map (contrast with module 06's HashMap-based majority-element exercise).

**E04 — Best time to buy/sell a stock.** A list of daily prices; one buy and one sell
(buy before sell) — maximize profit. Track the minimum price seen so far in a single pass.

**E05 — Contains duplicate transaction ID within k apart.** Given a stream of transaction
IDs, detect whether the same ID appears twice within `k` positions of each other — a
fixed-size sliding window of a `HashSet`.

**E06 — Flatten nested transaction batches.** Batches can contain either individual
transaction amounts or nested sub-batches (arbitrary depth); flatten to a single ordered
list without recursion, using an explicit `Deque` as the call stack.

**E07 — First unique transaction ID.** Given an ordered list of transaction IDs (duplicates
allowed), return the first ID that occurs exactly once, preserving original order —
`LinkedHashMap` keeps both the count and the insertion order in one structure.

**E08 — Merge overlapping account-hold intervals.** Given a list of `[start, end]` hold
periods on an account, merge all overlapping/touching intervals into the minimal set.

**E09 — Kth largest transaction amount.** Return the kth largest value in a list using a
size-k min-heap (`O(n log k)`) instead of a full sort (`O(n log n)`).

**E10 — Valid brackets and valid tags.** Two related stack-matching problems in one class:
classic bracket-pair validation (`()[]{}`) and validating simple, unattributed XML-like tags
(`<a><b></b></a>`) — both reduce to "push on open, pop-and-match on close."

**M01 — Longest substring without repeating characters.** Classic variable sliding window;
a `HashMap<Character, Integer>` tracks each character's last-seen index so the window's left
edge can jump directly past a repeat instead of shrinking one step at a time.

**M02 — Group anagram transaction codes.** Bucket a list of alphanumeric merchant/reference
codes by canonical form (sorted characters) so every group of codes that are anagrams of
each other ends up together.

**M03 — Subarrays divisible by K.** Count contiguous transaction windows whose sum is
divisible by a reporting threshold K, using prefix-sum-mod-K counts in a `HashMap` (handles
negative remainders carefully — see the exercise Javadoc).

**M04 — Top-K frequent words, tie-break alphabetically.** Build a frequency map, then use a
heap with a comparator that orders by frequency ascending and, on a tie, by word descending
(so popping the k smallest via a min-heap yields the correct top-k when reversed).

**M05 — Course schedule.** Given `numCourses` and a list of `[course, prerequisite]` pairs,
determine whether all courses can be completed (no cyclic dependency) using Kahn's
in-degree-based BFS topological sort.

**M06 — Number of islands.** A grid of `'1'`/`'0'` cells representing active/inactive branch
locations; count the number of 4-directionally connected regions of `'1'`s using BFS with an
explicit `HashSet` of visited cell IDs (not just a `boolean[][]`, to exercise `Set` usage
directly).

**M07 — Daily temperatures.** For each day, how many days until a strictly warmer day? A
monotonic (decreasing) stack of indices resolves every element in amortized O(1).

**M08 — Max transaction in every rolling window.** Given a sequence of transaction amounts
and a window size, return the maximum in every window of that size — a monotonic deque of
indices keeps the current window's maximum accessible in O(1) at all times.

**M09 — Fixed-size MRU transaction log.** A fixed-capacity log that always evicts the
least-recently-*touched* entry when full; built on `LinkedHashMap`'s access-order mode
(contrast with module 06's from-scratch `LRUCache` build-it-yourself exercise).

**M10 — Find all anagram start indices.** Given strings `s` and `p`, return every starting
index in `s` where a substring is an anagram of `p` — a fixed-size sliding window comparing
running character-count maps.

**M11 — Meeting rooms II.** Given meeting `[start, end]` intervals, compute the minimum
number of rooms needed — sort by start time, and track currently-occupied rooms via a
min-heap of end times (a room frees up once its top end time <= the next meeting's start).

**M12 — Longest palindrome by rearranging.** Given a string, what's the longest palindrome
you can build using its characters in any order? A frequency map: use all even counts fully,
plus one odd-count character in the middle if any odd count exists.

**H01 — Median of a data stream.** Support `addNum` and `findMedian` as numbers arrive
one at a time, maintaining O(log n) insert and O(1) median lookup via a max-heap for the
lower half and a min-heap for the upper half, kept balanced in size.

**H02 — Sliding window median.** The median of every window of size k as it slides across
an array — the two-heap approach from H01 plus **lazy deletion** (a `HashMap` of
pending-removal counts, since heaps can't remove an arbitrary element in O(log n) directly).

**H03 — Trapping rain water.** Given a bar-height elevation map, compute how much water it
traps after rain, using two pointers and running left/right maxima in O(1) extra space
(the notes also describe the alternative monotonic-stack O(n)-space approach for contrast).

**H04 — Word ladder.** Fewest single-letter transformations to turn `beginWord` into
`endWord`, every intermediate step a valid dictionary word — BFS layer by layer over an
implicit graph (edges = "differs by one letter"), with a `HashSet` dictionary for O(1)
membership checks and as the visited set.

**H05 — Alien dictionary.** Given a list of words sorted according to some unknown alien
alphabet's ordering, derive one valid ordering of the alphabet (or detect that none exists) —
build a "comes before" graph from adjacent word pairs and topologically sort it.

**H06 — Number of provinces.** Given an `n x n` adjacency matrix of directly-connected
accounts, count the number of connected groups (provinces) using Union-Find — union every
directly-connected pair, then count distinct roots.

**H07 — Redundant connection.** Given a list of edges that formed a tree plus exactly one
extra edge (creating exactly one cycle), find that extra edge — union edges one at a time;
the edge whose endpoints are already connected (`find(a) == find(b)`) before the union is the
answer.

**H08 — LFU cache.** `get`/`put` with capacity eviction, but unlike LRU, evict the
**least-frequently-used** entry (ties broken by least-recently-used) — a harder variant
combining a `HashMap<key, Node>` for O(1) lookup, a `HashMap<frequency, LinkedHashSet<key>>`
of frequency buckets (the `LinkedHashSet` preserves recency order within a frequency for
tie-breaking), and a running `minFrequency` pointer, all operations O(1).

---

Solutions for all 30 exercises are in `src/main/java/com/gk/study/dsaproblems/solutions/`
(class names end in `Solution`, except `LFUCache`/etc. where the exercise itself is the
data-structure name — check each solution file's Javadoc `@link` back to its exercise stub).
Attempt every stub yourself first.
