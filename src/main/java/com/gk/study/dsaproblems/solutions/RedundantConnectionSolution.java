package com.gk.study.dsaproblems.solutions;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.RedundantConnection}.
 *
 * <p>Process edges in input order with Union-Find. For each edge {@code [a, b]}, check whether
 * {@code a} and {@code b} are already in the same set BEFORE unioning them: if so, this edge
 * connects two nodes already connected by earlier edges, so it is the one that closes the cycle
 * — since edges are processed in their original order, this is guaranteed to be the last such
 * edge encountered. Otherwise, union them and continue. O(n * &alpha;(n)) time, O(n) space.
 */
public final class RedundantConnectionSolution {

    private RedundantConnectionSolution() {
    }

    public static int[] solve(int[][] edges) {
        int n = edges.length;
        UnionFind uf = new UnionFind(n + 1); // nodes are labeled 1..n

        for (int[] edge : edges) {
            int a = edge[0];
            int b = edge[1];
            if (uf.find(a) == uf.find(b)) {
                return edge;
            }
            uf.union(a, b);
        }
        throw new IllegalArgumentException("No redundant edge found — input is already a tree");
    }

    /** Minimal Union-Find with path compression and union by rank. */
    private static final class UnionFind {
        private final int[] parent;
        private final int[] rank;

        UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
        }

        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        void union(int a, int b) {
            int rootA = find(a);
            int rootB = find(b);
            if (rootA == rootB) {
                return;
            }
            if (rank[rootA] < rank[rootB]) {
                int t = rootA;
                rootA = rootB;
                rootB = t;
            }
            parent[rootB] = rootA;
            if (rank[rootA] == rank[rootB]) {
                rank[rootA]++;
            }
        }
    }
}
