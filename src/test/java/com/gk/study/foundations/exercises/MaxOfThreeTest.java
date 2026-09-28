package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaxOfThreeTest {

    @Test
    void findsMaxOfIntegers() {
        assertThat(MaxOfThree.max(3, 7, 5)).isEqualTo(7);
    }

    @Test
    void findsMaxOfStrings() {
        assertThat(MaxOfThree.max("pear", "apple", "banana")).isEqualTo("pear");
    }

    @Test
    void handlesAllEqualValues() {
        assertThat(MaxOfThree.max(4, 4, 4)).isEqualTo(4);
    }
}
