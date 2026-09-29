package com.gk.study.dsaproblems.solutions;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.NumberOfProvinces}.
 *
 * <p>Union every directly-connected pair of accounts via a small Union-Find (Disjoint Set Union)
 * helper backed by plain {@code int[]} parent/rank arrays (path compression in {@code find},
 * union by rank in {@code union} — see the module cheat sheet). After processing the whole
 * matrix, the number of distinct roots is the number of provinces. O(n^2 * &alpha;(n)) time,
 * O(n) space.
 */
public final class NumberOfProvincesSolution {

    private NumberOfProvincesSolution() {
    }

    public static int solve(int[][] isConnected) {
        int n = isConnected.length;
        UnionFind uf = new UnionFind(n);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (isConnected[i][j] == 1) {
                    uf.union(i, j);
                }
            }
        }

        int provinces = 0;
        for (int i = 0; i < n; i++) {
            if (uf.find(i) == i) {
                provinces++;
            }
        }
        return provinces;
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
