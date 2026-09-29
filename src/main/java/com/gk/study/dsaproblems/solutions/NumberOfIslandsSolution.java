package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.NumberOfIslands}.
 *
 * <p>Scan every cell; whenever an unvisited {@code '1'} is found, it is the seed of a brand-new
 * island, so run a BFS from it (an {@code ArrayDeque<Integer>} of cell IDs encoded as
 * {@code row * cols + col}) that marks every 4-directionally reachable {@code '1'} as visited in
 * a {@code HashSet<Integer>}, then move on. O(rows * cols) time and space.
 */
public final class NumberOfIslandsSolution {

    private static final int[][] DIRECTIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private NumberOfIslandsSolution() {
    }

    public static int solve(char[][] grid) {
        if (grid.length == 0 || grid[0].length == 0) {
            return 0;
        }
        int rows = grid.length;
        int cols = grid[0].length;
        Set<Integer> visited = new HashSet<>();
        int islands = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int id = r * cols + c;
                if (grid[r][c] == '1' && !visited.contains(id)) {
                    islands++;
                    bfs(grid, rows, cols, r, c, visited);
                }
            }
        }
        return islands;
    }

    private static void bfs(char[][] grid, int rows, int cols, int startR, int startC,
            Set<Integer> visited) {
        Deque<Integer> queue = new ArrayDeque<>();
        visited.add(startR * cols + startC);
        queue.offer(startR * cols + startC);

        while (!queue.isEmpty()) {
            int id = queue.poll();
            int r = id / cols;
            int c = id % cols;
            for (int[] dir : DIRECTIONS) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                int nid = nr * cols + nc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols
                        && grid[nr][nc] == '1' && !visited.contains(nid)) {
                    visited.add(nid);
                    queue.offer(nid);
                }
            }
        }
    }
}
