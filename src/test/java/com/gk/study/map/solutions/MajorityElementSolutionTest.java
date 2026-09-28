package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MajorityElementSolutionTest {

    @Test
    void typicalInput() {
        assertThat(MajorityElementSolution.solve(new int[] {2, 2, 1, 1, 1, 2, 2})).isEqualTo(2);
    }

    @Test
    void singleElement() {
        assertThat(MajorityElementSolution.solve(new int[] {5})).isEqualTo(5);
    }

    @Test
    void allSameElement() {
        assertThat(MajorityElementSolution.solve(new int[] {7, 7, 7, 7})).isEqualTo(7);
    }

    @Test
    void boyerMooreAgreesWithHashMapApproach() {
        int[] nums = {6, 5, 5, 6, 6, 6, 5, 6, 6};
        assertThat(MajorityElementSolution.solveBoyerMoore(nums))
                .isEqualTo(MajorityElementSolution.solve(nums));
    }

    @Test
    void largerInput() {
        int n = 10_001;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = (i % 2 == 0) ? 9 : i;
        }
        assertThat(MajorityElementSolution.solve(nums)).isEqualTo(9);
        assertThat(MajorityElementSolution.solveBoyerMoore(nums)).isEqualTo(9);
    }
}
