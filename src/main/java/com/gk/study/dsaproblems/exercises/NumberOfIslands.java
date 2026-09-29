package com.gk.study.dsaproblems.exercises;

/**
 * M06 [Medium] Given a grid of {@code '1'} (active branch location) and {@code '0'} (inactive)
 * cells, count the number of islands — 4-directionally connected regions of {@code '1'}s
 * (horizontally/vertically adjacent only, not diagonal).
 * Input:
 * <pre>
 * {'1','1','0','0','0'}
 * {'1','1','0','0','0'}
 * {'0','0','1','0','0'}
 * {'0','0','0','1','1'}
 * </pre>
 * Output: 3
 * Constraint: O(rows * cols) time and space.
 * Pattern: BFS/DFS over a grid
 * Collections: Deque (as the BFS queue) + HashSet (visited cell IDs, not a boolean[][])
 */
public class NumberOfIslands {

    public static int solve(char[][] grid) {
        // TODO: implement by scanning every cell; whenever an unvisited '1' is found, run a BFS
        // (using an ArrayDeque<Integer> of encoded cell IDs, e.g. row * cols + col) that visits
        // every 4-directionally connected '1', recording each visited cell in a HashSet<Integer>
        // so it is never explored again, and increment the island count once per BFS started
        throw new UnsupportedOperationException("TODO");
    }
}
