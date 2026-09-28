package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MajorityElementBoyerMooreSolutionTest {

    @Test
    void typicalInput() {
        assertThat(MajorityElementBoyerMooreSolution.solve(List.of(2, 2, 1, 1, 1, 2, 2)))
                .isEqualTo(2);
    }

    @Test
    void singleElement() {
        assertThat(MajorityElementBoyerMooreSolution.solve(List.of(7))).isEqualTo(7);
    }

    @Test
    void allSameElement() {
        assertThat(MajorityElementBoyerMooreSolution.solve(List.of(9, 9, 9, 9))).isEqualTo(9);
    }

    @Test
    void majorityAtBoundary() {
        assertThat(MajorityElementBoyerMooreSolution.solve(List.of(3, 3, 3, 4, 4))).isEqualTo(3);
    }

    @Test
    void largerInput() {
        List<Integer> nums = new java.util.ArrayList<>(Collections.nCopies(600, 5));
        nums.addAll(Collections.nCopies(400, 8));
        Collections.shuffle(nums, new java.util.Random(42));
        assertThat(MajorityElementBoyerMooreSolution.solve(nums)).isEqualTo(5);
    }
}
