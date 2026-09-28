package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SetOperationsSolutionTest {

    private final int[] a = {1, 2, 3, 4};
    private final int[] b = {3, 4, 5, 6};

    @Test
    void unionCombinesBothArraysWithoutDuplicates() {
        assertThat(SetOperationsSolution.union(a, b)).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
    }

    @Test
    void intersectionKeepsOnlyCommonElements() {
        assertThat(SetOperationsSolution.intersection(a, b)).containsExactlyInAnyOrder(3, 4);
    }

    @Test
    void differenceKeepsElementsOnlyInA() {
        assertThat(SetOperationsSolution.difference(a, b)).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void disjointArraysHaveEmptyIntersection() {
        int[] x = {1, 2};
        int[] y = {3, 4};
        assertThat(SetOperationsSolution.intersection(x, y)).isEmpty();
        assertThat(SetOperationsSolution.union(x, y)).containsExactlyInAnyOrder(1, 2, 3, 4);
    }

    @Test
    void identicalArraysDifferenceIsEmpty() {
        assertThat(SetOperationsSolution.difference(a, a)).isEmpty();
    }

    @Test
    void inputArraysAreNotMutated() {
        int[] copyOfA = a.clone();
        SetOperationsSolution.union(a, b);
        SetOperationsSolution.intersection(a, b);
        SetOperationsSolution.difference(a, b);
        assertThat(a).isEqualTo(copyOfA);
    }
}
