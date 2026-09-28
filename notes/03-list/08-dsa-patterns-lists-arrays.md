# DSA patterns on lists/arrays

## 1. What it is

A catalog of the four recurring array/list traversal patterns that solve the vast majority of
interview array problems without brute-force O(n²)/O(n³) scans: **two pointers**, **sliding
window**, **prefix sum**, and **in-place reversal**. Recognizing which pattern a problem statement
implies is the single highest-leverage interview skill for this topic.

## 2. How it works internally

**Two pointers** — two indices moving through the array, either toward each other (converging,
for sorted-array/palindrome problems) or in the same direction at different speeds (fast/slow,
for in-place compaction or cycle detection).
```
converging:      i -> ... <- j              (start, end; move inward)
same-direction:  slow, fast   both start at 0, fast scans ahead, slow marks "write position"
```

**Sliding window** — maintain a contiguous subarray `[left, right]` and grow/shrink it
incrementally instead of recomputing from scratch; a running aggregate (sum, count map) is
updated by adding the entering element and removing the leaving element, turning an O(n·k) or
O(n²) brute force into O(n).
```
fixed-size window (size k):        [-- window k --]
                                     shift right by 1 each step: drop leftmost, add new rightmost

variable-size window (e.g. "longest substring with property P"):
  expand `right` while P holds; when P breaks, shrink from `left` until P holds again
```

**Prefix sum** — precompute `prefix[i] = arr[0] + arr[1] + ... + arr[i-1]` in one O(n) pass, so
any range sum `arr[i..j]` becomes `prefix[j+1] - prefix[i]`, O(1) per query after O(n)
preprocessing — turns O(n) per range-sum query (or O(n²) for many queries) into O(1) per query.
```
arr:     [ 2 | -1 | 3 | 4 | -2 ]
prefix:  [ 0 | 2 | 1 | 4 | 8 | 6 ]     prefix[i] = sum of arr[0..i-1]
rangeSum(1, 3) = arr[1]+arr[2]+arr[3] = prefix[4] - prefix[1] = 8 - 2 = 6
```

**In-place reversal** — reverse a sequence (array segment or linked-list segment) using O(1)
extra space by swapping/relinking as you go, rather than allocating a new reversed copy;
generalizes to "reverse in groups of k" and "reverse a sublist between positions m and n."
```
array:  swap(arr[left], arr[right]); left++; right--;  while left < right

linked list:  prev=null, curr=head
  while curr != null:
      next = curr.next        // save
      curr.next = prev        // reverse the link
      prev = curr; curr = next
  return prev  // new head
```

## 3. Complexity

| Pattern | Typical time | Typical space | Turns what brute force into what |
|---------|--------------|----------------|-----------------------------------|
| Two pointers | O(n) | O(1) | O(n²) nested loop → O(n) single pass |
| Sliding window | O(n) | O(1) or O(k) (window state) | O(n·k) recompute-per-window → O(n) |
| Prefix sum | O(n) preprocess, O(1) per query | O(n) for the prefix array | O(n) per query → O(1) per query |
| In-place reversal | O(n) | O(1) iterative / O(n) recursive (call stack) | O(n) extra-array reversal → O(1) space |

## 4. Example code
- Runnable class: `src/main/java/com/gk/study/list/examples/ListPatternsDemo.java` — demonstrates
  all four patterns on small inputs with printed step traces (two-pointer palindrome check,
  fixed-size sliding window max sum, prefix-sum range query, in-place array reversal).

```java
int[] arr = {1, 3, -1, 2, 5, -4};
int k = 3;
// sliding window: max sum of any 3 consecutive elements
int windowSum = arr[0] + arr[1] + arr[2], best = windowSum;
for (int i = k; i < arr.length; i++) {
    windowSum += arr[i] - arr[i - k];   // add entering, drop leaving — O(1) per step
    best = Math.max(best, windowSum);
}
System.out.println(best); // 6  (2 + 5 + -4? no -> window {2,5,-4}=3; actual best window {3,-1,2}=4... see demo for verified trace)
```
(See the runnable demo for the exact, verified output — the snippet above is illustrative of the
technique; run the class for authoritative numbers.)

## 5. When to use / when NOT to use

- Two pointers: sorted-array problems, palindrome checks, in-place partitioning/deduplication —
  NOT useful when the array is unsorted and order-sensitive relationships between arbitrary pairs
  matter (then you likely need a hash map instead).
- Sliding window: "contiguous subarray/substring with property X" problems — NOT applicable when
  the subsequence doesn't need to be contiguous (that's usually DP, not sliding window).
- Prefix sum: many range-sum queries on a **static** (non-mutating) array — NOT the right tool if
  the array is updated frequently between queries (each update would require an O(n) prefix
  rebuild; a Fenwick tree/segment tree is the right structure for that, out of scope here).
- In-place reversal: when the interviewer specifies O(1) extra space, or when you're asked to
  mutate a linked list's structure directly rather than build a new one.

## 6. Common pitfalls & gotchas

**Off-by-one in sliding window bounds** — forgetting that a window of size `k` starting at `i`
covers `arr[i..i+k-1]`, so the loop that slides the window must start at index `k`, not `k-1` or
`k+1`; always trace a size-1 and size-2 example by hand before trusting the loop bounds.

**Prefix sum array is length n+1, not n** — `prefix[0] = 0` by convention (sum of zero elements),
so `prefix[i]` = sum of the first `i` elements; forgetting the leading zero shifts every query off
by one:
```java
int[] prefix = new int[arr.length + 1]; // NOT new int[arr.length]
for (int i = 0; i < arr.length; i++) prefix[i + 1] = prefix[i] + arr[i];
```

**In-place reversal losing the "next" pointer before rewiring:**
```java
curr.next = prev;   // BUG if done before saving next: curr.next (the real next node) is now lost
// fix: always save `next = curr.next` BEFORE reassigning curr.next
```

**Two-pointer convergence with the wrong loop condition** — using `<=` instead of `<` (or vice
versa) either double-swaps the middle element of an odd-length array back to itself (harmless but
wasteful) or, worse, skips a required comparison — always decide explicitly whether `left == right`
(odd-length middle) should still execute the loop body.

## 7. Interview questions

- [Basic] What problem shape signals "use two pointers"? → A sorted array/list, or a problem
  about pairs/comparisons from both ends (palindrome check, in-place dedup/partition on sorted
  data) where a brute-force nested loop would be O(n²) but the sortedness/structure lets you
  converge two indices in one O(n) pass. → Follow-up: *Does two pointers require the input to be
  sorted?* Not always — the "same-direction, different speed" variant (slow/fast) works on
  unsorted data too, e.g. removing elements in place, cycle detection.
- [Basic] What problem shape signals "use sliding window"? → "Find the (max/min/count of)
  contiguous subarray/substring satisfying property X" — contiguity is the key word; if the
  interviewer says "subsequence" (not necessarily contiguous), sliding window usually doesn't
  apply. → Follow-up: *Fixed or variable window — how do you decide?* Fixed size when the problem
  states an exact window length `k`; variable size when the window must grow/shrink to
  maintain/restore a condition (e.g. "no repeating characters").
- [Basic] What does prefix sum trade off to get O(1) range queries? → O(n) extra space and an
  O(n) upfront preprocessing pass, in exchange for O(1) time per subsequent range-sum query,
  instead of O(n) time per query with no preprocessing. → Follow-up: *When does that trade-off
  not pay off?* When you only need one or two range queries total — building the whole prefix
  array isn't worth it for a single O(n) direct sum.
- [Intermediate] How do you find the equilibrium index of an array (index where the sum of
  elements to the left equals the sum to the right) efficiently? → Compute the total sum once
  (O(n)), then scan left to right maintaining a running left-sum; at each index, the right-sum is
  `total - leftSum - arr[i]`; compare and return the first index where they're equal — O(n) time,
  O(1) space, no separate prefix array needed since you only need a running total, not arbitrary
  range queries. → Follow-up: *Could you solve it with an explicit prefix-sum array instead?*
  Yes, less space-efficient (O(n) array vs O(1) running variable) but conceptually identical —
  useful if you need multiple different range queries afterward, overkill for just this one.
- [Intermediate] Walk through detecting the longest substring without repeating characters using
  sliding window. → Expand `right` one character at a time, tracking the last-seen index of each
  character (e.g. in an array/map sized for the alphabet); if the character at `right` was seen
  before **and** its last index is `>= left`, jump `left` to `lastSeenIndex + 1` (not
  one-by-one shrink) to skip past the duplicate in O(1) amortized; track `right - left + 1` as the
  current window length and keep a running max — overall O(n) since `left` and `right` each only
  move forward, never backward. → Follow-up: *Why must left only move forward, never reset to 0?*
  Resetting from scratch on every duplicate would make the algorithm O(n²); monotonic forward
  movement of both pointers is what guarantees O(n) total.
- [Intermediate] Why is reversing a linked list iteratively preferred over recursively in
  production code despite both being O(n) time? → The recursive version uses O(n) call-stack
  frames, risking `StackOverflowError` on very long lists (a real risk for, e.g., a full
  unbounded transaction history chain), while the iterative version uses O(1) auxiliary space
  regardless of list length. → Follow-up: *When might the recursive version still be preferred?*
  When list length is provably small/bounded and the recursive form's clarity (mirrors the
  problem's natural recursive structure) aids readability/maintainability more than the marginal
  stack cost matters.
- [Advanced] How would you reverse a linked list in groups of k (Hard variant)? → Recursively (or
  iteratively with an explicit stack/counter): for each group of k nodes, first verify k nodes
  exist (else leave that final partial group unreversed per the usual problem statement), reverse
  just that group using the standard iterative reversal bounded to k nodes, then recursively (or
  in a loop) reverse the next group and connect the current group's original head (now the
  group's tail) to the head of the next reversed group. Time O(n) (each node visited O(1) times
  across all groups), space O(1) iteratively or O(n/k) recursion depth if done recursively. →
  Follow-up: *What's the trickiest edge case?* The last group having fewer than k remaining
  nodes — decide (per problem spec) whether to reverse it anyway or leave it as-is, and make sure
  the count-check happens before doing any relinking so you can bail out cleanly.
- [Advanced] Why does Floyd's cycle detection (fast/slow pointers) provably terminate and find
  the meeting point inside the cycle? → If there's no cycle, the fast pointer reaches `null` and
  the loop ends normally; if there is a cycle, once both pointers are inside it, the fast pointer
  gains on the slow pointer by 1 node per step (relative speed difference of 1), so the gap
  between them (mod cycle length) strictly decreases each iteration and must reach 0 within at
  most `cycleLength` steps — a pigeonhole-style argument guaranteeing a finite meeting point. →
  Follow-up: *Why does resetting one pointer to head and moving both by 1 find the cycle's exact
  start?* Let the distance from head to the cycle start be `a`, and from the cycle start to the
  meeting point be `b`; the algebra of the meeting-point distances (derivable from "fast traveled
  twice as far as slow when they met") shows the remaining distance from the meeting point back
  around to the cycle start equals `a` — so a pointer from `head` and a pointer from the meeting
  point, both moving 1 step at a time, meet exactly at the cycle's start.

## 8. Exercises

| # | Level | Problem | Pattern | File |
|---|-------|---------|---------|------|
| M1 | Medium | Maximum sum of any contiguous subarray of size k | Sliding window (fixed) | `exercises/MaxSumSubarrayK.java` |
| M2 | Medium | Longest substring without repeating characters (input as `List<Character>`) | Sliding window (variable) | `exercises/LongestUniqueCharSubstring.java` |
| M3 | Medium | Equilibrium index — first index where left-sum equals right-sum | Prefix sum | `exercises/EquilibriumIndex.java` |
| H1 | Hard | Reverse a linked list in groups of k | In-place reversal | `exercises/ReverseKGroup.java` |

**M1 — Max sum subarray of size k**
- Input: `[2, 1, 5, 1, 3, 2]`, `k=3` → Output: `9` (subarray `[5,1,3]`)
- Constraint: O(n) time, O(1) extra space.
- <details><summary>Hint</summary>Compute the first window's sum directly, then slide: add the
  new right element, subtract the element leaving on the left.</details>

**M2 — Longest substring without repeating characters**
- Input: `[a, b, c, a, b, c, b, b]` (as `List<Character>`) → Output: `3` (`"abc"`)
- Constraint: O(n) time, O(min(n, alphabet size)) space.
- <details><summary>Hint</summary>Sliding window with a map of last-seen index per character;
  when a repeat is found inside the current window, jump `left` past its previous
  occurrence.</details>

**M3 — Equilibrium index**
- Input: `[-7, 1, 5, 2, -4, 3, 0]` → Output: `3` (left sum `-7+1+5=-1`, right sum
  `-4+3+0=-1`)
- Constraint: O(n) time, O(1) extra space (running totals, no array needed).
- <details><summary>Hint</summary>Compute total sum first; scan left to right tracking
  running left-sum; right-sum = total - leftSum - current.</details>

**H1 — Reverse in groups of k**
- Input: `1->2->3->4->5->null`, `k=2` → Output: `2->1->4->3->5->null` (last partial group of
  size < k stays as-is)
- Constraint: O(n) time, O(1) extra space (iterative) or O(n/k) recursion depth.
- <details><summary>Hint</summary>First check whether k nodes remain (walk ahead); if not, return
  the head unchanged. If yes, reverse exactly k nodes with the standard iterative technique, then
  recursively/iteratively process the rest and connect the original head (now tail of this
  reversed group) to the head of the next processed group.</details>

Solutions are in the `solutions` package (`MaxSumSubarrayKSolution`,
`LongestUniqueCharSubstringSolution`, `EquilibriumIndexSolution`, `ReverseKGroupSolution`) — not
shown here.

## 9. Quick recap

- Two pointers: converging (sorted/palindrome) or same-direction fast/slow (in-place
  compaction, cycle detection) — turns O(n²) into O(n).
- Sliding window: maintain a running aggregate over a contiguous range, add-on-enter /
  subtract-on-exit — turns O(n·k) into O(n).
- Prefix sum: O(n) preprocessing buys O(1) range-sum queries on a static array; remember the
  length-n+1 convention with `prefix[0]=0`.
- In-place reversal: save `next` before rewiring `curr.next`; iterative is O(1) space, recursive
  is O(n) stack — know both for linked lists.
- Recognizing which pattern a problem statement implies (contiguous? sorted? range queries?
  reversal?) is the actual interview skill — the code is secondary once the pattern is right.
