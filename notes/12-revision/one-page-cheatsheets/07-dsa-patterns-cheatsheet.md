# Cheat Sheet — 07: DSA Problem Sets (Pattern Recognition)

One-page pre-interview skim. Full notes: `notes/07-dsa-problem-sets/`. This module doesn't add new
structures — it drills matching a problem statement to the right pattern in under a minute.

## Pattern → signal phrase table

| Pattern | Signal phrases | Time | Structure(s) |
|---|---|---|---|
| Two pointers | "sorted array", "pair sums to", "in-place" | O(n) | array/List only |
| Sliding window (variable) | "longest/shortest substring/subarray such that", "at most K distinct" | O(n) | HashMap/HashSet |
| Sliding window (fixed) | "every window of size k" | O(n) | HashMap counts or Deque |
| Prefix sum | "subarray sum equals/divisible by" | O(n) | `HashMap<prefixValue,count>` |
| Fast/slow pointers | "detect a cycle", "middle of a linked list" | O(n)/O(1) | two refs, no structure |
| Monotonic stack/deque | "next greater/smaller", "daily temperatures", "max of every window" | O(n) amortized | Deque |
| BFS (graph/grid) | "shortest path unweighted", "fewest steps", "connected region" | O(V+E) | Queue + HashSet visited |
| DFS/backtracking | "all possible ways", "generate combinations/permutations" | O(branch^depth) | List/Deque as path |
| Topological sort (Kahn's) | "prerequisite", "build order", "valid ordering" | O(V+E) | HashMap adjacency + Queue |
| Heap/top-K | "kth largest/smallest", "top K frequent", "merge K sorted", "running median" | O(n log k) | PriorityQueue (+HashMap) |
| Union-Find | "connected components", "would adding this edge create a cycle" | ~O(α(n)) | `int[] parent`/`rank` |
| Binary search on answer | "minimize the maximum", monotonic feasibility | O(n log range) | index math only |
| Greedy | "max profit one pass", locally-optimal never revisited | O(n) or O(n log n) | List (often sorted) |
| HashMap frequency/lookup | "count occurrences", "anagram", "have I seen this", "complement" | O(n) | HashMap/LinkedHashMap |

**How to use it live**: read the problem once, underline nouns (array/string/grid/graph/stream) and
verbs (sorted/contiguous/shortest/kth/connected). Match the signal phrase. State the pattern and its
complexity **out loud before coding**.

## Union-Find in one box (the module's one genuinely new topic)

```java
int find(int x) {                          // path compression
    if (parent[x] != x) parent[x] = find(parent[x]);
    return parent[x];
}
void union(int a, int b) {                  // union by rank
    int ra = find(a), rb = find(b);
    if (ra == rb) return;                   // this edge would create a cycle
    if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
    parent[rb] = ra;
    if (rank[ra] == rank[rb]) rank[ra]++;
}
```
Amortized O(α(n)) per op — inverse Ackermann, ≤4 for any realistic n.

## The full 30-problem index (Easy 10 / Medium 12 / Hard 8)

### Easy
| # | Title | Pattern | Structure |
|---|---|---|---|
| E01 | Two-sum on sorted transaction amounts | two pointers | List |
| E02 | Valid anagram | frequency map | HashMap |
| E03 | Majority element (Boyer-Moore voting) | greedy candidate-counting | List, O(1) space |
| E04 | Best time to buy/sell a stock | single-pass greedy (min-so-far) | List |
| E05 | Contains duplicate transaction ID within k apart | sliding window + set | HashSet |
| E06 | Flatten nested transaction batches | explicit stack, no recursion | Deque |
| E07 | First unique transaction ID | order-preserving frequency count | LinkedHashMap |
| E08 | Merge overlapping account-hold intervals | sort + linear scan | List (sorted) |
| E09 | Kth largest transaction amount | min-heap of size k | PriorityQueue |
| E10 | Valid brackets AND valid XML-like tags | stack matching | Deque + HashMap |

### Medium
| # | Title | Pattern | Structure |
|---|---|---|---|
| M01 | Longest substring without repeating characters | variable sliding window | HashMap (char→last index) |
| M02 | Group anagram transaction codes | canonical-key bucketing | HashMap<String,List> |
| M03 | Subarrays with sum divisible by K | prefix sum mod K | HashMap<Integer,Integer> |
| M04 | Top-K frequent words, tie-break alphabetically | freq map + heap, custom comparator | HashMap + PriorityQueue |
| M05 | Course schedule (can all finish?) | topo sort (Kahn's BFS) | HashMap + Queue |
| M06 | Number of islands | BFS/DFS over a grid | Deque + HashSet visited |
| M07 | Daily temperatures | monotonic stack | Deque (indices) |
| M08 | Max transaction in every rolling 24h window | monotonic deque | Deque (indices) |
| M09 | Fixed-size MRU transaction log | access-order eviction | LinkedHashMap |
| M10 | Find all anagram start indices in a string | fixed sliding window + freq map | HashMap<Character,Integer> |
| M11 | Meeting rooms II | greedy + min-heap of end times | PriorityQueue + List |
| M12 | Longest palindrome by rearranging | frequency map | HashMap<Character,Integer> |

### Hard
| # | Title | Pattern | Structure |
|---|---|---|---|
| H01 | Median of a data stream | two heaps (max-low, min-high) | 2× PriorityQueue |
| H02 | Sliding window median | two heaps + lazy deletion | 2× PriorityQueue + HashMap |
| H03 | Trapping rain water | two pointers, O(1) space | array only |
| H04 | Word ladder | BFS over implicit word graph | HashSet + Queue |
| H05 | Alien dictionary | topo sort from partial order | HashMap + HashSet + Queue |
| H06 | Number of provinces | Union-Find | int[] parent/rank |
| H07 | Redundant connection | Union-Find | int[] parent/rank |
| H08 | LFU cache | HashMap + frequency buckets | 2×HashMap + LinkedHashSet |

## Top pitfalls

- **Sorting then `limit()` for top-k on a huge stream** — sorts everything (O(n log n)) when a bounded heap gives O(n log k).
- **Forgetting the prefix-sum seed `{0:1}`** for subarray-sum-= K style problems.
- **Marking BFS visited at dequeue instead of enqueue** — inflates the queue with duplicate work.
- **Wrong monotonic-deque pop end** — back = dominated candidates, front = out-of-window; swapping breaks the invariant silently.
- **Top-K must use a MIN-heap bounded to size k**, even when the answer is the k *largest* elements.
