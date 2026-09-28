package com.gk.study.queuedeque.solutions;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.BfsShortestPathGrid}.
 * Standard BFS: a queue of (row, col, distance) triples, expanding to the four orthogonal
 * neighbors, marking cells visited the moment they're enqueued (not when dequeued) to avoid
 * enqueuing the same cell multiple times. BFS explores the grid in increasing distance order, so
 * the first time the target cell is dequeued, its recorded distance is guaranteed shortest.
 */
public class BfsShortestPathGridSolution {

    public static int shortestPath(int[][] grid) {
        int rows = grid.length;
        if (rows == 0) {
            return -1;
        }
        int cols = grid[0].length;
        if (grid[0][0] != 0 || grid[rows - 1][cols - 1] != 0) {
            return -1;
        }

        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{0, 0, 0});
        visited[0][0] = true;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            int dist = current[2];
            if (r == rows - 1 && c == cols - 1) {
                return dist;
            }
            for (int[] d : directions) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc] && grid[nr][nc] == 0) {
                    visited[nr][nc] = true;
                    queue.offer(new int[]{nr, nc, dist + 1});
                }
            }
        }
        return -1;
    }
}
