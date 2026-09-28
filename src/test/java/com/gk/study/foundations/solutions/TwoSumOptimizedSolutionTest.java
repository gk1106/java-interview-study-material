package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoSumOptimizedSolutionTest {

    @Test
    void findsPairThatSumsToTarget() {
        assertThat(TwoSumOptimizedSolution.hasPairWithSum(new int[] {2, 7, 11, 15}, 9)).isTrue();
    }

    @Test
    void returnsFalseWhenNoPairSums() {
        assertThat(TwoSumOptimizedSolution.hasPairWithSum(new int[] {1, 2, 3}, 100)).isFalse();
    }

    @Test
    void emptyAndSingleElementArraysHaveNoPair() {
        assertThat(TwoSumOptimizedSolution.hasPairWithSum(new int[] {}, 5)).isFalse();
        assertThat(TwoSumOptimizedSolution.hasPairWithSum(new int[] {5}, 10)).isFalse();
    }

    @Test
    void negativeNumbers() {
        assertThat(TwoSumOptimizedSolution.hasPairWithSum(new int[] {-3, -1, 4, 2}, -4)).isTrue();
    }

    @Test
    void agreesWithBruteForceOnRandomInput() {
        int[] arr = {5, 12, -7, 3, 9, 0, -2, 8, 21, -5};
        for (int target : new int[] {17, -9, 100, 0}) {
            assertThat(TwoSumOptimizedSolution.hasPairWithSum(arr, target))
                    .isEqualTo(TwoSumBruteForceSolution.hasPairWithSum(arr, target));
        }
    }
}
