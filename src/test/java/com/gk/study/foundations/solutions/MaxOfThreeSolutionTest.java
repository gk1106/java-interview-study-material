package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaxOfThreeSolutionTest {

    @Test
    void findsMaxOfIntegers() {
        assertThat(MaxOfThreeSolution.max(3, 7, 5)).isEqualTo(7);
    }

    @Test
    void findsMaxOfStrings() {
        assertThat(MaxOfThreeSolution.max("pear", "apple", "banana")).isEqualTo("pear");
    }

    @Test
    void handlesAllEqualValues() {
        assertThat(MaxOfThreeSolution.max(4, 4, 4)).isEqualTo(4);
    }

    @Test
    void maxIsFirstArgumentWhenLargest() {
        assertThat(MaxOfThreeSolution.max(9, 2, 1)).isEqualTo(9);
    }

    @Test
    void negativeNumbers() {
        assertThat(MaxOfThreeSolution.max(-5, -1, -10)).isEqualTo(-1);
    }
}
