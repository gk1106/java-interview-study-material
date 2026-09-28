package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ContainsNearbyDuplicate} exercise stub. EXPECTED TO FAIL until implemented.
 */
class ContainsNearbyDuplicateTest {

    @Test
    void duplicateWithinWindowReturnsTrue() {
        assertThat(ContainsNearbyDuplicate.solve(new int[] {1, 2, 3, 1}, 3)).isTrue();
    }

    @Test
    void duplicateOutsideWindowReturnsFalse() {
        assertThat(ContainsNearbyDuplicate.solve(new int[] {1, 2, 3, 1, 2, 3}, 2)).isFalse();
    }

    @Test
    void noDuplicatesAtAllReturnsFalse() {
        assertThat(ContainsNearbyDuplicate.solve(new int[] {1, 2, 3, 4}, 3)).isFalse();
    }

    @Test
    void kZeroOnlyMatchesSameIndex() {
        assertThat(ContainsNearbyDuplicate.solve(new int[] {1, 1}, 0)).isFalse();
    }

    @Test
    void emptyArrayReturnsFalse() {
        assertThat(ContainsNearbyDuplicate.solve(new int[0], 3)).isFalse();
    }

    @Test
    void adjacentDuplicatesReturnTrue() {
        assertThat(ContainsNearbyDuplicate.solve(new int[] {1, 0, 1, 1}, 1)).isTrue();
    }
}
