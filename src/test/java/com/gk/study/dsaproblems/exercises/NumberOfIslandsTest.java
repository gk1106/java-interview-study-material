package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link NumberOfIslands} exercise stub. EXPECTED TO FAIL until implemented.
 */
class NumberOfIslandsTest {

    @Test
    void typicalInput() {
        char[][] grid = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
        };
        assertThat(NumberOfIslands.solve(grid)).isEqualTo(3);
    }

    @Test
    void allWater() {
        char[][] grid = {
            {'0', '0'},
            {'0', '0'}
        };
        assertThat(NumberOfIslands.solve(grid)).isEqualTo(0);
    }

    @Test
    void allLandIsOneIsland() {
        char[][] grid = {
            {'1', '1'},
            {'1', '1'}
        };
        assertThat(NumberOfIslands.solve(grid)).isEqualTo(1);
    }

    @Test
    void singleCellIsland() {
        char[][] grid = {{'1'}};
        assertThat(NumberOfIslands.solve(grid)).isEqualTo(1);
    }

    @Test
    void diagonalCellsAreNotConnected() {
        char[][] grid = {
            {'1', '0'},
            {'0', '1'}
        };
        assertThat(NumberOfIslands.solve(grid)).isEqualTo(2);
    }

    @Test
    void largerGridCheckerboardPattern() {
        int size = 20;
        char[][] grid = new char[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = (r + c) % 2 == 0 ? '1' : '0';
            }
        }
        // every '1' is diagonally isolated from every other '1' -> each is its own island
        int expected = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == '1') {
                    expected++;
                }
            }
        }
        assertThat(NumberOfIslands.solve(grid)).isEqualTo(expected);
    }
}
