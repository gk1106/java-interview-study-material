package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaxSumSubarrayKSolutionTest {

    @Test
    void typicalInput() {
        int[] arr = {2, 1, 5, 1, 3, 2};
        assertThat(MaxSumSubarrayKSolution.solve(arr, 3)).isEqualTo(9);
    }

    @Test
    void windowSizeOneReturnsMaxElement() {
        int[] arr = {4, 2, 9, 1};
        assertThat(MaxSumSubarrayKSolution.solve(arr, 1)).isEqualTo(9);
    }

    @Test
    void windowSizeEqualsArrayLength() {
        int[] arr = {1, 2, 3, 4};
        assertThat(MaxSumSubarrayKSolution.solve(arr, 4)).isEqualTo(10);
    }

    @Test
    void allNegativeNumbers() {
        int[] arr = {-1, -2, -3, -4};
        assertThat(MaxSumSubarrayKSolution.solve(arr, 2)).isEqualTo(-3);
    }

    @Test
    void largerInput() {
        int n = 5000;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = 1;
        }
        assertThat(MaxSumSubarrayKSolution.solve(arr, 100)).isEqualTo(100);
    }
}
