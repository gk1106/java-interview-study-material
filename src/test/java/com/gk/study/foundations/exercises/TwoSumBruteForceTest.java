package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoSumBruteForceTest {

    @Test
    void findsPairThatSumsToTarget() {
        assertThat(TwoSumBruteForce.hasPairWithSum(new int[] {2, 7, 11, 15}, 9)).isTrue();
    }

    @Test
    void returnsFalseWhenNoPairSums() {
        assertThat(TwoSumBruteForce.hasPairWithSum(new int[] {1, 2, 3}, 100)).isFalse();
    }

    @Test
    void emptyArrayHasNoPair() {
        assertThat(TwoSumBruteForce.hasPairWithSum(new int[] {}, 5)).isFalse();
    }
}
