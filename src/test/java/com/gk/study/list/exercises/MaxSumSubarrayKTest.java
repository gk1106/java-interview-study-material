package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MaxSumSubarrayK} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MaxSumSubarrayKTest {

    @Test
    void typicalInput() {
        int[] arr = {2, 1, 5, 1, 3, 2};
        assertThat(MaxSumSubarrayK.solve(arr, 3)).isEqualTo(9);
    }

    @Test
    void windowSizeOneReturnsMaxElement() {
        int[] arr = {4, 2, 9, 1};
        assertThat(MaxSumSubarrayK.solve(arr, 1)).isEqualTo(9);
    }

    @Test
    void windowSizeEqualsArrayLength() {
        int[] arr = {1, 2, 3, 4};
        assertThat(MaxSumSubarrayK.solve(arr, 4)).isEqualTo(10);
    }

    @Test
    void allNegativeNumbers() {
        int[] arr = {-1, -2, -3, -4};
        assertThat(MaxSumSubarrayK.solve(arr, 2)).isEqualTo(-3); // window [-1,-2]
    }

    @Test
    void largerInput() {
        int n = 5000;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = 1; // every window of size k sums to k
        }
        assertThat(MaxSumSubarrayK.solve(arr, 100)).isEqualTo(100);
    }
}
