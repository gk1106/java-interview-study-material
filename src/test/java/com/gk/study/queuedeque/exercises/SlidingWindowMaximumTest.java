package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SlidingWindowMaximum} exercise stub. EXPECTED TO FAIL until implemented.
 */
class SlidingWindowMaximumTest {

    @Test
    void classicExample() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        assertThat(SlidingWindowMaximum.maxSlidingWindow(nums, 3))
                .containsExactly(3, 3, 5, 5, 6, 7);
    }

    @Test
    void windowOfOneReturnsInputUnchanged() {
        int[] nums = {5, 1, 3};
        assertThat(SlidingWindowMaximum.maxSlidingWindow(nums, 1))
                .containsExactly(5, 1, 3);
    }

    @Test
    void windowCoveringWholeArrayReturnsSingleMax() {
        int[] nums = {4, 2, 7, 1};
        assertThat(SlidingWindowMaximum.maxSlidingWindow(nums, 4))
                .containsExactly(7);
    }

    @Test
    void decreasingSequenceKeepsShrinkingMax() {
        int[] nums = {9, 8, 7, 6, 5};
        assertThat(SlidingWindowMaximum.maxSlidingWindow(nums, 2))
                .containsExactly(9, 8, 7, 6);
    }

    @Test
    void largerInputMatchesBruteForceCrossCheck() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7, 2, 9, 0, 4};
        int k = 4;
        int[] optimal = SlidingWindowMaximum.maxSlidingWindow(nums, k);
        int[] bruteForce = bruteForceReference(nums, k);
        assertThat(optimal).containsExactly(bruteForce);
    }

    private static int[] bruteForceReference(int[] nums, int k) {
        int[] result = new int[nums.length - k + 1];
        for (int i = 0; i <= nums.length - k; i++) {
            int max = nums[i];
            for (int j = i + 1; j < i + k; j++) {
                max = Math.max(max, nums[j]);
            }
            result[i] = max;
        }
        return result;
    }
}
