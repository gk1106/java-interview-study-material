package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EquilibriumIndexSolutionTest {

    @Test
    void typicalInput() {
        int[] arr = {-7, 1, 5, 2, -4, 3, 0};
        assertThat(EquilibriumIndexSolution.solve(arr)).isEqualTo(3);
    }

    @Test
    void noEquilibriumReturnsMinusOne() {
        int[] arr = {1, 2, 3};
        assertThat(EquilibriumIndexSolution.solve(arr)).isEqualTo(-1);
    }

    @Test
    void singleElementIsAlwaysEquilibrium() {
        int[] arr = {5};
        assertThat(EquilibriumIndexSolution.solve(arr)).isEqualTo(0);
    }

    @Test
    void firstIndexIsEquilibriumWhenRestSumsToZero() {
        int[] arr = {0, -1, 1};
        assertThat(EquilibriumIndexSolution.solve(arr)).isEqualTo(0);
    }

    @Test
    void emptyArrayReturnsMinusOne() {
        int[] arr = {};
        assertThat(EquilibriumIndexSolution.solve(arr)).isEqualTo(-1);
    }
}
