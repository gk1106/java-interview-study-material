package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TwoSumSorted} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TwoSumSortedTest {

    @Test
    void typicalInput() {
        assertThat(TwoSumSorted.solve(List.of(1, 3, 5, 7, 9), 12)).containsExactly(1, 4);
    }

    @Test
    void adjacentPair() {
        assertThat(TwoSumSorted.solve(List.of(2, 3, 5, 8), 5)).containsExactly(0, 1);
    }

    @Test
    void duplicateValues() {
        assertThat(TwoSumSorted.solve(List.of(2, 2), 4)).containsExactly(0, 1);
    }

    @Test
    void negativeAmounts() {
        assertThat(TwoSumSorted.solve(List.of(-5, -3, 0, 2, 6), 3)).containsExactly(1, 3);
    }

    @Test
    void largerInput() {
        int n = 2000;
        Integer[] amounts = new Integer[n];
        for (int i = 0; i < n; i++) {
            amounts[i] = i;
        }
        int[] result = TwoSumSorted.solve(List.of(amounts), n - 3);
        assertThat(result).hasSize(2);
        assertThat(amounts[result[0]] + amounts[result[1]]).isEqualTo(n - 3);
    }
}
