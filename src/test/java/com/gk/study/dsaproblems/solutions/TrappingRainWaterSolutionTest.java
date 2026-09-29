package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrappingRainWaterSolutionTest {

    @Test
    void typicalInput() {
        int[] height = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        assertThat(TrappingRainWaterSolution.solve(height)).isEqualTo(6);
    }

    @Test
    void flatTerrainTrapsNothing() {
        assertThat(TrappingRainWaterSolution.solve(new int[] {3, 3, 3, 3})).isEqualTo(0);
    }

    @Test
    void strictlyIncreasingTrapsNothing() {
        assertThat(TrappingRainWaterSolution.solve(new int[] {1, 2, 3, 4})).isEqualTo(0);
    }

    @Test
    void emptyArray() {
        assertThat(TrappingRainWaterSolution.solve(new int[] {})).isEqualTo(0);
    }

    @Test
    void singleValley() {
        assertThat(TrappingRainWaterSolution.solve(new int[] {3, 0, 3})).isEqualTo(3);
    }

    @Test
    void largerInputSymmetricMountain() {
        int n = 1000;
        int[] height = new int[2 * n + 1];
        for (int i = 0; i <= n; i++) {
            height[i] = i;
        }
        for (int i = 1; i <= n; i++) {
            height[n + i] = n - i;
        }
        assertThat(TrappingRainWaterSolution.solve(height)).isEqualTo(0);
    }
}
