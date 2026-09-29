package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TrappingRainWater} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TrappingRainWaterTest {

    @Test
    void typicalInput() {
        int[] height = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
        assertThat(TrappingRainWater.solve(height)).isEqualTo(6);
    }

    @Test
    void flatTerrainTrapsNothing() {
        assertThat(TrappingRainWater.solve(new int[] {3, 3, 3, 3})).isEqualTo(0);
    }

    @Test
    void strictlyIncreasingTrapsNothing() {
        assertThat(TrappingRainWater.solve(new int[] {1, 2, 3, 4})).isEqualTo(0);
    }

    @Test
    void emptyArray() {
        assertThat(TrappingRainWater.solve(new int[] {})).isEqualTo(0);
    }

    @Test
    void singleValley() {
        assertThat(TrappingRainWater.solve(new int[] {3, 0, 3})).isEqualTo(3);
    }

    @Test
    void largerInputSymmetricMountain() {
        int n = 1000;
        int[] height = new int[2 * n + 1];
        for (int i = 0; i <= n; i++) {
            height[i] = i; // rises 0..n
        }
        for (int i = 1; i <= n; i++) {
            height[n + i] = n - i; // falls back down n..0
        }
        // a single symmetric peak traps no water at all
        assertThat(TrappingRainWater.solve(height)).isEqualTo(0);
    }
}
