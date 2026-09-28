package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SumAnyCollectionSolutionTest {

    @Test
    void sumsList() {
        assertThat(SumAnyCollectionSolution.sum(List.of(1, 2, 3))).isEqualTo(6.0, within(1e-9));
    }

    @Test
    void sumsTreeSetOfDoubles() {
        assertThat(SumAnyCollectionSolution.sum(new TreeSet<>(List.of(1.5, 2.5)))).isEqualTo(4.0, within(1e-9));
    }

    @Test
    void sumsDequeOfLongs() {
        assertThat(SumAnyCollectionSolution.sum(new ArrayDeque<>(List.of(1L, 2L, 3L)))).isEqualTo(6.0, within(1e-9));
    }

    @Test
    void emptyCollectionSumsToZero() {
        assertThat(SumAnyCollectionSolution.sum(List.of())).isEqualTo(0.0, within(1e-9));
    }
}
