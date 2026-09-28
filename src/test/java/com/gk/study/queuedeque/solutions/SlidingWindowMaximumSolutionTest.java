package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlidingWindowMaximumSolutionTest {

    @Test
    void classicExample() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        assertThat(SlidingWindowMaximumSolution.maxSlidingWindow(nums, 3))
                .containsExactly(3, 3, 5, 5, 6, 7);
    }

    @Test
    void windowOfOneReturnsInputUnchanged() {
        int[] nums = {5, 1, 3};
        assertThat(SlidingWindowMaximumSolution.maxSlidingWindow(nums, 1))
                .containsExactly(5, 1, 3);
    }

    @Test
    void windowCoveringWholeArrayReturnsSingleMax() {
        int[] nums = {4, 2, 7, 1};
        assertThat(SlidingWindowMaximumSolution.maxSlidingWindow(nums, 4))
                .containsExactly(7);
    }

    @Test
    void decreasingSequenceKeepsShrinkingMax() {
        int[] nums = {9, 8, 7, 6, 5};
        assertThat(SlidingWindowMaximumSolution.maxSlidingWindow(nums, 2))
                .containsExactly(9, 8, 7, 6);
    }

    @Test
    void largerInputMatchesBruteForceCrossCheck() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7, 2, 9, 0, 4};
        int k = 4;
        int[] optimal = SlidingWindowMaximumSolution.maxSlidingWindow(nums, k);
        int[] bruteForce = SlidingWindowMaxBruteForceSolution.maxSlidingWindowBruteForce(nums, k);
        assertThat(optimal).containsExactly(bruteForce);
    }
}
