package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SetOperations} exercise stub. EXPECTED TO FAIL until implemented.
 */
class SetOperationsTest {

    private final int[] a = {1, 2, 3, 4};
    private final int[] b = {3, 4, 5, 6};

    @Test
    void unionCombinesBothArraysWithoutDuplicates() {
        assertThat(SetOperations.union(a, b)).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
    }

    @Test
    void intersectionKeepsOnlyCommonElements() {
        assertThat(SetOperations.intersection(a, b)).containsExactlyInAnyOrder(3, 4);
    }

    @Test
    void differenceKeepsElementsOnlyInA() {
        assertThat(SetOperations.difference(a, b)).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void disjointArraysHaveEmptyIntersection() {
        int[] x = {1, 2};
        int[] y = {3, 4};
        assertThat(SetOperations.intersection(x, y)).isEmpty();
        assertThat(SetOperations.union(x, y)).containsExactlyInAnyOrder(1, 2, 3, 4);
    }

    @Test
    void identicalArraysDifferenceIsEmpty() {
        assertThat(SetOperations.difference(a, a)).isEmpty();
    }
}
