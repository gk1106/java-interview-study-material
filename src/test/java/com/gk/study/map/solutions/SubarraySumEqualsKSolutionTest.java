package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubarraySumEqualsKSolutionTest {

    @Test
    void typicalInput() {
        assertThat(SubarraySumEqualsKSolution.solve(new int[] {1, 2, 3}, 3)).isEqualTo(2);
    }

    @Test
    void singleElementEqualToK() {
        assertThat(SubarraySumEqualsKSolution.solve(new int[] {5}, 5)).isEqualTo(1);
    }

    @Test
    void noSubarraySums() {
        assertThat(SubarraySumEqualsKSolution.solve(new int[] {1, 2, 3}, 100)).isEqualTo(0);
    }

    @Test
    void negativeNumbers() {
        assertThat(SubarraySumEqualsKSolution.solve(new int[] {1, -1, 1, -1}, 0)).isEqualTo(4);
    }

    @Test
    void largerInput() {
        int n = 5000;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = 1;
        }
        assertThat(SubarraySumEqualsKSolution.solve(nums, 3)).isEqualTo(n - 2);
    }
}
