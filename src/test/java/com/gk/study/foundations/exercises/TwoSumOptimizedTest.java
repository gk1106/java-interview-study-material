package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoSumOptimizedTest {

    @Test
    void findsPairThatSumsToTarget() {
        assertThat(TwoSumOptimized.hasPairWithSum(new int[] {2, 7, 11, 15}, 9)).isTrue();
    }

    @Test
    void returnsFalseWhenNoPairSums() {
        assertThat(TwoSumOptimized.hasPairWithSum(new int[] {1, 2, 3}, 100)).isFalse();
    }

    @Test
    void singleElementHasNoPair() {
        assertThat(TwoSumOptimized.hasPairWithSum(new int[] {5}, 10)).isFalse();
    }
}
