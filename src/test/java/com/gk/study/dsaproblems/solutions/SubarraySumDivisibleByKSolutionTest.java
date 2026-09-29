package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubarraySumDivisibleByKSolutionTest {

    @Test
    void typicalInput() {
        assertThat(SubarraySumDivisibleByKSolution.solve(List.of(4, 5, 0, -2, -3, 1), 5))
                .isEqualTo(7);
    }

    @Test
    void emptyInput() {
        assertThat(SubarraySumDivisibleByKSolution.solve(List.of(), 5)).isEqualTo(0);
    }

    @Test
    void singleElementDivisible() {
        assertThat(SubarraySumDivisibleByKSolution.solve(List.of(5), 5)).isEqualTo(1);
    }

    @Test
    void singleElementNotDivisible() {
        assertThat(SubarraySumDivisibleByKSolution.solve(List.of(3), 5)).isEqualTo(0);
    }

    @Test
    void allZerosCountsEverySubarray() {
        List<Integer> zeros = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            zeros.add(0);
        }
        assertThat(SubarraySumDivisibleByKSolution.solve(zeros, 7)).isEqualTo(15);
    }

    @Test
    void largerInputAllOnesDivisibleByFour() {
        List<Integer> ones = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ones.add(1);
        }
        int expected = 0;
        for (int len = 4; len <= 100; len += 4) {
            expected += 100 - len + 1;
        }
        assertThat(SubarraySumDivisibleByKSolution.solve(ones, 4)).isEqualTo(expected);
    }
}
