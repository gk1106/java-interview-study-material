# Pattern cheat sheet — module 07 (mixed DSA problem sets)

This module doesn't introduce new data structures; it drills **pattern recognition** — the
skill of reading a problem statement and knowing within seconds which structure(s) and
algorithmic shape it wants. Modules 01-06 taught the structures in isolation; here they get
combined the way real interview (and real production) problems combine them: a `HashMap` next
to a `TreeSet`, a `Deque` next to a `HashSet`, two `PriorityQueue`s at once.

Use this table as a 2-minute pre-interview refresher: read the "signal phrases" column first —
if a phrase in the actual problem statement matches, you've probably found your pattern.

| Pattern | Signal phrases in the problem | Typical time | Example problems (this set) | Collection(s) typically used |
|---|---|---|---|---|
| Two pointers | "sorted array", "pair that sums to", "in-place", "without extra space" | O(n) | `TwoSumSorted`, `TrappingRainWater` | `List`/array only, O(1) extra space |
| Sliding window (variable size) | "longest/shortest substring/subarray such that...", "at most/exactly K distinct" | O(n) | `LongestSubstringWithoutRepeating`, `MinimumWindowSubstring` | `HashMap`/`HashSet` (window contents) |
| Sliding window (fixed size) | "every window of size k", "rolling k days/hours" | O(n) | `FindAllAnagramsInString`, `SlidingWindowMaxTransaction` | `HashMap` (counts) or `Deque` (monotonic) |
| Prefix sum | "subarray sum equals/divisible by", "range sum query" | O(n) | `SubarraySumDivisibleByK` | `HashMap<prefixValue, count>` |
| Fast/slow pointers (cycle detection) | "detect a cycle", "find the duplicate number without extra space", "middle of a linked list" | O(n) time, O(1) space | *(not in this set — needs a linked-list/functional-graph input; see `notes/03-list`)*. Conceptually: `slow` moves 1 step, `fast` moves 2; if they meet, a cycle exists (Floyd's algorithm) | none — two index/reference variables only |
| Monotonic stack/deque | "next greater/smaller element", "daily temperatures", "max of every window" | O(n) amortized | `DailyTemperatures`, `SlidingWindowMaxTransaction` | `Deque` (as a stack or a double-ended index queue) |
| BFS (graph/grid) | "shortest path in unweighted graph", "fewest steps/transformations", "connected region" | O(V+E) or O(rows·cols) | `NumberOfIslands`, `WordLadder`, `CourseScheduleTopoSort` | `Queue`/`Deque` + `HashSet` (visited) |
| DFS / backtracking | "all possible ways to...", "generate all combinations/permutations", "explore then undo a choice" | O(branching^depth) | *(not in this set — see below)*. Conceptually: N-Queens, generate parentheses, subsets — recurse, try a choice, undo it (`state.remove(last)`), try the next | `List`/`Deque` used as an explicit path/stack |
| Topological sort (Kahn's BFS) | "prerequisite", "build order", "valid ordering given dependency constraints" | O(V+E) | `CourseScheduleTopoSort`, `AlienDictionary` | `HashMap` (adjacency + in-degree) + `Queue` |
| Heap / top-K | "kth largest/smallest", "top K frequent", "merge K sorted", "running median" | O(n log k) | `KthLargestElement`, `TopKFrequentWordsTieBreak`, `MedianOfDataStream` | `PriorityQueue` (often paired with `HashMap`) |
| Union-Find (Disjoint Set Union) | "connected components", "will adding this edge create a cycle", "provinces/groups/clusters" | ~O(α(n)) per op (near O(1)) | `NumberOfProvinces`, `RedundantConnection` | `int[] parent` / `int[] rank` (plain arrays, not a JCF collection — see below) |
| Binary search on answer | "minimize the maximum...", "smallest value such that a condition holds", monotonic feasibility | O(n log(range)) | *(not in this set)*. Conceptually: binary-search a candidate answer value (not an index) and use a greedy/simulation check as the O(n) predicate, e.g. "minimum days to ship all packages within D days" | none — index math over a numeric range |
| Greedy | "maximum profit with one pass", "minimum number of X", locally-optimal choice never needs revisiting | O(n) or O(n log n) | `BestTimeToBuyStock`, `MergeIntervals`, `MeetingRoomsII` | `List` (often sorted first) |
| HashMap frequency/lookup | "count occurrences", "anagram", "have I seen this before", "complement" | O(n) | `ValidAnagram`, `GroupAnagramCodes`, `LongestPalindromeByRearranging` | `HashMap` / `LinkedHashMap` |

## Union-Find (Disjoint Set Union) — first appearance in this repo

Union-Find answers one question fast, repeatedly: **"are these two elements in the same
group, and if not, merge their groups."** It underlies "number of connected components",
"will this edge create a cycle", and "are these accounts linked via shared devices/emails"
(a real fraud-detection pattern in banking systems).

It is normally backed by two plain **arrays**, not a `java.util` collection — `parent[i]` is
the parent pointer of element `i` (a tree, not a linked structure with node objects), and
`rank[i]` (or `size[i]`) is a rough height/weight estimate used to keep the trees shallow.

### The two optimizations that make it near-O(1)

**Union by rank/size** — when merging two trees, always attach the *shorter* (lower rank)
tree under the *taller* one's root, not the other way around. This keeps tree height
`O(log n)` instead of degenerating into a linked list.

**Path compression** — every time `find(x)` walks up to the root, re-point every node on that
path directly to the root. Future `find` calls on those nodes become O(1).

```
Before find(E) with path compression:        After find(E):

   A                                              A
   |                                            / | \
   B                                           B  C  D
   |                                              |
   C                                              E
   |
   D
   |
   E

find(E) walks E -> D -> C -> B -> A (root),
then re-parents D, C, B, E ALL directly to A.
Next find(E) is O(1) instead of O(n).
```

Combined, amortized cost per operation is `O(α(n))` — the inverse Ackermann function, which
is `<= 4` for any realistic input size, effectively constant.

```java
int[] parent = new int[n];
int[] rank = new int[n];
// init: parent[i] = i, rank[i] = 0  (n singleton sets)

int find(int x) {
    if (parent[x] != x) {
        parent[x] = find(parent[x]); // path compression
    }
    return parent[x];
}

void union(int a, int b) {
    int rootA = find(a), rootB = find(b);
    if (rootA == rootB) return; // already connected -> this edge would create a cycle
    if (rank[rootA] < rank[rootB]) { int t = rootA; rootA = rootB; rootB = t; }
    parent[rootB] = rootA;          // attach shorter tree under taller
    if (rank[rootA] == rank[rootB]) rank[rootA]++;
}
```

See it exercised in `RedundantConnection` (detect the edge that first creates a cycle) and
`NumberOfProvinces` (count connected components from an adjacency matrix).

## How to use this table in an interview

1. Read the problem once. Underline the nouns (array? string? grid? graph? stream?) and the
   verbs (sorted? contiguous? shortest? kth? connected?).
2. Match against the "signal phrases" column above.
3. State your chosen pattern and its complexity **out loud before coding** — interviewers
   grade the reasoning, not just the final code.
4. If two patterns seem to fit, prefer the one with better complexity, then the one that
   needs less auxiliary space.

See `01-problem-set.md` for the full 30-problem index with per-problem pattern/collection
tags and links to the exercise stubs.
