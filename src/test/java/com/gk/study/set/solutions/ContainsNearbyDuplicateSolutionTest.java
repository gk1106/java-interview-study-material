package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContainsNearbyDuplicateSolutionTest {

    @Test
    void duplicateWithinWindowReturnsTrue() {
        assertThat(ContainsNearbyDuplicateSolution.solve(new int[] {1, 2, 3, 1}, 3)).isTrue();
    }

    @Test
    void duplicateOutsideWindowReturnsFalse() {
        assertThat(ContainsNearbyDuplicateSolution.solve(new int[] {1, 2, 3, 1, 2, 3}, 2)).isFalse();
    }

    @Test
    void noDuplicatesAtAllReturnsFalse() {
        assertThat(ContainsNearbyDuplicateSolution.solve(new int[] {1, 2, 3, 4}, 3)).isFalse();
    }

    @Test
    void kZeroOnlyMatchesSameIndex() {
        assertThat(ContainsNearbyDuplicateSolution.solve(new int[] {1, 1}, 0)).isFalse();
    }

    @Test
    void emptyArrayReturnsFalse() {
        assertThat(ContainsNearbyDuplicateSolution.solve(new int[0], 3)).isFalse();
    }

    @Test
    void adjacentDuplicatesReturnTrue() {
        assertThat(ContainsNearbyDuplicateSolution.solve(new int[] {1, 0, 1, 1}, 1)).isTrue();
    }
}
