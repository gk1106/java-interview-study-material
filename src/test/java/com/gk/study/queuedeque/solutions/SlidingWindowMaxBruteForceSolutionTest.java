package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlidingWindowMaxBruteForceSolutionTest {

    @Test
    void classicExample() {
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        assertThat(SlidingWindowMaxBruteForceSolution.maxSlidingWindowBruteForce(nums, 3))
                .containsExactly(3, 3, 5, 5, 6, 7);
    }

    @Test
    void windowOfOneReturnsInputUnchanged() {
        int[] nums = {5, 1, 3};
        assertThat(SlidingWindowMaxBruteForceSolution.maxSlidingWindowBruteForce(nums, 1))
                .containsExactly(5, 1, 3);
    }

    @Test
    void windowCoveringWholeArrayReturnsSingleMax() {
        int[] nums = {4, 2, 7, 1};
        assertThat(SlidingWindowMaxBruteForceSolution.maxSlidingWindowBruteForce(nums, 4))
                .containsExactly(7);
    }

    @Test
    void singleElementArray() {
        int[] nums = {9};
        assertThat(SlidingWindowMaxBruteForceSolution.maxSlidingWindowBruteForce(nums, 1))
                .containsExactly(9);
    }

    @Test
    void allNegativeNumbers() {
        int[] nums = {-4, -2, -5, -1, -3};
        assertThat(SlidingWindowMaxBruteForceSolution.maxSlidingWindowBruteForce(nums, 2))
                .containsExactly(-2, -2, -1, -1);
    }
}
