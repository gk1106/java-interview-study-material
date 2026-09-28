package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BoundedNumberSumSolutionTest {

    @Test
    void sumsMixedNumberTypes() {
        List<Number> numbers = List.of(1, 2.5, 3L);
        assertThat(BoundedNumberSumSolution.sumOf(numbers)).isCloseTo(6.5, within(0.0001));
    }

    @Test
    void sumsDoubleList() {
        assertThat(BoundedNumberSumSolution.sumOf(List.of(1.1, 2.2, 3.3))).isCloseTo(6.6, within(0.0001));
    }

    @Test
    void emptyListSumsToZero() {
        assertThat(BoundedNumberSumSolution.sumOf(List.<Integer>of())).isEqualTo(0.0);
    }

    @Test
    void singleElement() {
        assertThat(BoundedNumberSumSolution.sumOf(List.of(42))).isEqualTo(42.0);
    }
}
