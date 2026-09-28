package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MajorityElement} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MajorityElementTest {

    @Test
    void typicalInput() {
        assertThat(MajorityElement.solve(new int[] {2, 2, 1, 1, 1, 2, 2})).isEqualTo(2);
    }

    @Test
    void singleElement() {
        assertThat(MajorityElement.solve(new int[] {5})).isEqualTo(5);
    }

    @Test
    void majorityAtTheEnd() {
        assertThat(MajorityElement.solve(new int[] {1, 2, 3, 3, 3})).isEqualTo(3);
    }

    @Test
    void allSameElement() {
        assertThat(MajorityElement.solve(new int[] {7, 7, 7, 7})).isEqualTo(7);
    }

    @Test
    void largerInput() {
        int n = 10_001; // odd, guarantees a strict majority for the repeated value
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = (i % 2 == 0) ? 9 : i;
        }
        assertThat(MajorityElement.solve(nums)).isEqualTo(9);
    }
}
