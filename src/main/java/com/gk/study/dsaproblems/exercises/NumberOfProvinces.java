package com.gk.study.dsaproblems.exercises;

/**
 * H06 [Hard] Given an {@code n x n} adjacency matrix {@code isConnected} where
 * {@code isConnected[i][j] == 1} means accounts {@code i} and {@code j} are directly connected
 * (and {@code isConnected[i][i] == 1} always), count the number of provinces — groups of
 * accounts connected directly or transitively.
 * Input: [[1,1,0],[1,1,0],[0,0,1]] &rarr; Output: 2 (accounts 0-1 form one province, account 2
 * is its own province)
 * Constraint: O(n^2 * &alpha;(n)) time (n^2 to scan the matrix, near-O(1) per union).
 * Pattern: Union-Find (Disjoint Set Union)
 * Collections: int[] parent / int[] rank arrays (see the module cheat sheet's Union-Find
 * section) — not a java.util collection
 */
public class NumberOfProvinces {

    public static int solve(int[][] isConnected) {
        // TODO: implement using Union-Find: initialize parent[i] = i for every account, then for
        // every pair (i, j) with i < j where isConnected[i][j] == 1, union(i, j). You may add a
        // small private helper (e.g. a nested UnionFind class, or plain find/union methods over
        // int[] arrays) matching the cheat sheet's find (with path compression) and union (by
        // rank) implementation. The answer is the number of distinct roots after all unions
        throw new UnsupportedOperationException("TODO");
    }
}
