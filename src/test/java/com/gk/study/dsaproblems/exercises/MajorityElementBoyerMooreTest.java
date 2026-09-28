package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MajorityElementBoyerMoore} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class MajorityElementBoyerMooreTest {

    @Test
    void typicalInput() {
        assertThat(MajorityElementBoyerMoore.solve(List.of(2, 2, 1, 1, 1, 2, 2))).isEqualTo(2);
    }

    @Test
    void singleElement() {
        assertThat(MajorityElementBoyerMoore.solve(List.of(7))).isEqualTo(7);
    }

    @Test
    void allSameElement() {
        assertThat(MajorityElementBoyerMoore.solve(List.of(9, 9, 9, 9))).isEqualTo(9);
    }

    @Test
    void majorityAtBoundary() {
        assertThat(MajorityElementBoyerMoore.solve(List.of(3, 3, 3, 4, 4))).isEqualTo(3);
    }

    @Test
    void largerInput() {
        List<Integer> nums = new java.util.ArrayList<>(Collections.nCopies(600, 5));
        nums.addAll(Collections.nCopies(400, 8));
        Collections.shuffle(nums, new java.util.Random(42));
        assertThat(MajorityElementBoyerMoore.solve(nums)).isEqualTo(5);
    }
}
