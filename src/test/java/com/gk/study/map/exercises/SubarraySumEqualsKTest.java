package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SubarraySumEqualsK} exercise stub. EXPECTED TO FAIL until implemented.
 */
class SubarraySumEqualsKTest {

    @Test
    void typicalInput() {
        assertThat(SubarraySumEqualsK.solve(new int[] {1, 2, 3}, 3)).isEqualTo(2);
    }

    @Test
    void singleElementEqualToK() {
        assertThat(SubarraySumEqualsK.solve(new int[] {5}, 5)).isEqualTo(1);
    }

    @Test
    void noSubarraySums() {
        assertThat(SubarraySumEqualsK.solve(new int[] {1, 2, 3}, 100)).isEqualTo(0);
    }

    @Test
    void negativeNumbers() {
        assertThat(SubarraySumEqualsK.solve(new int[] {1, -1, 1, -1}, 0)).isEqualTo(4);
    }

    @Test
    void largerInput() {
        int n = 5000;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = 1; // every subarray of length k sums to k
        }
        assertThat(SubarraySumEqualsK.solve(nums, 3)).isEqualTo(n - 2);
    }
}
