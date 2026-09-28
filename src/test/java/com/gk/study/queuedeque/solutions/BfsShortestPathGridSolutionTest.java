package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BfsShortestPathGridSolutionTest {

    @Test
    void typicalGridWithWalls() {
        int[][] grid = {
                {0, 0, 0},
                {1, 1, 0},
                {0, 0, 0},
        };
        assertThat(BfsShortestPathGridSolution.shortestPath(grid)).isEqualTo(4);
    }

    @Test
    void singleCellGridIsZeroDistance() {
        int[][] grid = {{0}};
        assertThat(BfsShortestPathGridSolution.shortestPath(grid)).isEqualTo(0);
    }

    @Test
    void noPathReturnsMinusOne() {
        int[][] grid = {
                {0, 1},
                {1, 0},
        };
        assertThat(BfsShortestPathGridSolution.shortestPath(grid)).isEqualTo(-1);
    }

    @Test
    void blockedStartReturnsMinusOne() {
        int[][] grid = {
                {1, 0},
                {0, 0},
        };
        assertThat(BfsShortestPathGridSolution.shortestPath(grid)).isEqualTo(-1);
    }

    @Test
    void openGridUsesManhattanDistance() {
        int[][] grid = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
        };
        assertThat(BfsShortestPathGridSolution.shortestPath(grid)).isEqualTo(4);
    }
}
