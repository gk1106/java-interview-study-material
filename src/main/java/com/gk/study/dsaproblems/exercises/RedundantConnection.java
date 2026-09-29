package com.gk.study.dsaproblems.exercises;

/**
 * H07 [Hard] A graph started as a tree of {@code n} nodes (labeled 1..n) with {@code n - 1}
 * edges, then gained exactly one extra edge, creating exactly one cycle. Given the edges in the
 * order they were added, find the edge that can be removed to restore a tree — if multiple edges
 * could work, return the one that appears LAST in the input.
 * Input: [[1,2],[1,3],[2,3]] &rarr; Output: [2,3] (edges [1,2] and [1,3] form a tree; [2,3] is
 * the extra edge that closes the cycle)
 * Constraint: O(n * &alpha;(n)) time.
 * Pattern: Union-Find (Disjoint Set Union)
 * Collections: int[] parent / int[] rank arrays — not a java.util collection
 */
public class RedundantConnection {

    public static int[] solve(int[][] edges) {
        // TODO: implement using Union-Find: process edges in order, and for each edge [a, b],
        // check find(a) == find(b) BEFORE unioning; if they're already in the same set, this
        // edge is redundant (it closes a cycle) - return it immediately. Otherwise union(a, b)
        // and continue. You may add a small private helper (e.g. a nested UnionFind class)
        throw new UnsupportedOperationException("TODO");
    }
}
