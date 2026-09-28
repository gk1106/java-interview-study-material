package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TwoSumHashMapSolutionTest {

    @Test
    void typicalInput() {
        assertThat(TwoSumHashMapSolution.solve(new int[] {2, 7, 11, 15}, 9)).containsExactly(0, 1);
    }

    @Test
    void solutionNotAtTheStart() {
        assertThat(TwoSumHashMapSolution.solve(new int[] {3, 2, 4}, 6)).containsExactly(1, 2);
    }

    @Test
    void duplicateValuesUsedOnce() {
        assertThat(TwoSumHashMapSolution.solve(new int[] {3, 3}, 6)).containsExactly(0, 1);
    }

    @Test
    void negativeNumbers() {
        assertThat(TwoSumHashMapSolution.solve(new int[] {-1, -2, -3, -4, -5}, -8)).containsExactly(2, 4);
    }

    @Test
    void noSolutionThrows() {
        assertThatThrownBy(() -> TwoSumHashMapSolution.solve(new int[] {1, 2, 3}, 100))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void largerInput() {
        int n = 2000;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = i;
        }
        int target = n - 3;
        int[] result = TwoSumHashMapSolution.solve(nums, target);
        assertThat(result).hasSize(2);
        assertThat(result[0]).isNotEqualTo(result[1]);
        assertThat(nums[result[0]] + nums[result[1]]).isEqualTo(target);
    }
}
