package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArrayIntersectionSolutionTest {

    @Test
    void returnsSortedDeduplicatedIntersection() {
        assertThat(ArrayIntersectionSolution.intersection(new int[] {4, 9, 5, 1}, new int[] {9, 4, 9, 8, 4}))
                .containsExactly(4, 9);
    }

    @Test
    void noCommonElementsReturnsEmpty() {
        assertThat(ArrayIntersectionSolution.intersection(new int[] {1, 2}, new int[] {3, 4})).isEmpty();
    }

    @Test
    void doesNotMutateInputArrays() {
        int[] a = {3, 1, 2};
        int[] b = {2, 3};
        ArrayIntersectionSolution.intersection(a, b);
        assertThat(a).containsExactly(3, 1, 2);
        assertThat(b).containsExactly(2, 3);
    }

    @Test
    void identicalArraysReturnAllUniqueValues() {
        assertThat(ArrayIntersectionSolution.intersection(new int[] {1, 1, 2}, new int[] {1, 2, 2}))
                .containsExactly(1, 2);
    }
}
