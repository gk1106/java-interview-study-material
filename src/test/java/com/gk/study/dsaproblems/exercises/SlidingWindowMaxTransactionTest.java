package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SlidingWindowMaxTransaction} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class SlidingWindowMaxTransactionTest {

    @Test
    void typicalInput() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        assertThat(SlidingWindowMaxTransaction.solve(nums, 3)).containsExactly(3, 3, 5, 5, 6, 7);
    }

    @Test
    void windowSizeOneReturnsInputUnchanged() {
        int[] nums = {4, 1, 7, 2};
        assertThat(SlidingWindowMaxTransaction.solve(nums, 1)).containsExactly(4, 1, 7, 2);
    }

    @Test
    void windowSizeEqualsArrayLength() {
        int[] nums = {4, 1, 7, 2};
        assertThat(SlidingWindowMaxTransaction.solve(nums, 4)).containsExactly(7);
    }

    @Test
    void singleElementArray() {
        assertThat(SlidingWindowMaxTransaction.solve(new int[] {9}, 1)).containsExactly(9);
    }

    @Test
    void decreasingSequence() {
        int[] nums = {5, 4, 3, 2, 1};
        assertThat(SlidingWindowMaxTransaction.solve(nums, 2)).containsExactly(5, 4, 3, 2);
    }

    @Test
    void largerInputAscendingSequence() {
        int n = 1000;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = i;
        }
        int k = 10;
        int[] result = SlidingWindowMaxTransaction.solve(nums, k);
        assertThat(result).hasSize(n - k + 1);
        // strictly ascending -> the max of every window is its rightmost (last) element
        for (int i = 0; i < result.length; i++) {
            assertThat(result[i]).isEqualTo(i + k - 1);
        }
    }
}
