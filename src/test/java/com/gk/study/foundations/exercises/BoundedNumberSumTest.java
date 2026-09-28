package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class BoundedNumberSumTest {

    @Test
    void sumsMixedNumberTypes() {
        List<Number> numbers = List.of(1, 2.5, 3L);
        assertThat(BoundedNumberSum.sumOf(numbers)).isCloseTo(6.5, within(0.0001));
    }

    @Test
    void sumsIntegerList() {
        assertThat(BoundedNumberSum.sumOf(List.of(1, 2, 3))).isCloseTo(6.0, within(0.0001));
    }

    @Test
    void emptyListSumsToZero() {
        assertThat(BoundedNumberSum.sumOf(List.<Integer>of())).isEqualTo(0.0);
    }
}
