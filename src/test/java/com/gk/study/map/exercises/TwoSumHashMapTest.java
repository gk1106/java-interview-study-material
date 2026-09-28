package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TwoSumHashMap} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TwoSumHashMapTest {

    @Test
    void typicalInput() {
        assertThat(TwoSumHashMap.solve(new int[] {2, 7, 11, 15}, 9)).containsExactly(0, 1);
    }

    @Test
    void solutionNotAtTheStart() {
        assertThat(TwoSumHashMap.solve(new int[] {3, 2, 4}, 6)).containsExactly(1, 2);
    }

    @Test
    void duplicateValuesUsedOnce() {
        assertThat(TwoSumHashMap.solve(new int[] {3, 3}, 6)).containsExactly(0, 1);
    }

    @Test
    void negativeNumbers() {
        assertThat(TwoSumHashMap.solve(new int[] {-1, -2, -3, -4, -5}, -8)).containsExactly(2, 4);
    }

    @Test
    void largerInput() {
        int n = 2000;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = i;
        }
        int target = n - 3;
        int[] result = TwoSumHashMap.solve(nums, target);
        assertThat(result).hasSize(2);
        assertThat(result[0]).isNotEqualTo(result[1]);
        assertThat(nums[result[0]] + nums[result[1]]).isEqualTo(target);
    }
}
