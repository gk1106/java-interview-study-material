package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoSumBruteForceSolutionTest {

    @Test
    void findsPairThatSumsToTarget() {
        assertThat(TwoSumBruteForceSolution.hasPairWithSum(new int[] {2, 7, 11, 15}, 9)).isTrue();
    }

    @Test
    void findsPairAtEnds() {
        assertThat(TwoSumBruteForceSolution.hasPairWithSum(new int[] {3, 5, -4, 8, 11, 1, -1, 6}, 10)).isTrue();
    }

    @Test
    void returnsFalseWhenNoPairSums() {
        assertThat(TwoSumBruteForceSolution.hasPairWithSum(new int[] {1, 2, 3}, 100)).isFalse();
    }

    @Test
    void emptyAndSingleElementArraysHaveNoPair() {
        assertThat(TwoSumBruteForceSolution.hasPairWithSum(new int[] {}, 5)).isFalse();
        assertThat(TwoSumBruteForceSolution.hasPairWithSum(new int[] {5}, 10)).isFalse();
    }

    @Test
    void negativeNumbers() {
        assertThat(TwoSumBruteForceSolution.hasPairWithSum(new int[] {-3, -1, 4, 2}, -4)).isTrue();
    }
}
