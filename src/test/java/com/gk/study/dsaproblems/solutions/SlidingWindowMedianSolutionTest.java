package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class SlidingWindowMedianSolutionTest {

    @Test
    void typicalInput() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        double[] result = SlidingWindowMedianSolution.solve(nums, 3);
        assertThat(result).containsExactly(new double[] {1.0, -1.0, -1.0, 3.0, 5.0, 6.0}, within(1e-9));
    }

    @Test
    void windowSizeOneReturnsInputAsIs() {
        int[] nums = {5, 1, 9, 2};
        double[] result = SlidingWindowMedianSolution.solve(nums, 1);
        assertThat(result).containsExactly(new double[] {5.0, 1.0, 9.0, 2.0}, within(1e-9));
    }

    @Test
    void windowSizeEqualsArrayLengthEvenCount() {
        int[] nums = {1, 2, 3, 4};
        double[] result = SlidingWindowMedianSolution.solve(nums, 4);
        assertThat(result).containsExactly(new double[] {2.5}, within(1e-9));
    }

    @Test
    void windowSizeTwoAveragesConsecutivePairs() {
        int[] nums = {1, 2, 3, 4};
        double[] result = SlidingWindowMedianSolution.solve(nums, 2);
        assertThat(result).containsExactly(new double[] {1.5, 2.5, 3.5}, within(1e-9));
    }

    @Test
    void duplicateValues() {
        int[] nums = {4, 4, 4, 4};
        double[] result = SlidingWindowMedianSolution.solve(nums, 2);
        assertThat(result).containsExactly(new double[] {4.0, 4.0, 4.0}, within(1e-9));
    }

    @Test
    void largerInputConstantWindowOfOnes() {
        int n = 500;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = 1;
        }
        double[] result = SlidingWindowMedianSolution.solve(nums, 5);
        assertThat(result).hasSize(n - 5 + 1);
        for (double median : result) {
            assertThat(median).isCloseTo(1.0, within(1e-9));
        }
    }
}
