package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SubarraySumDivisibleByK} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class SubarraySumDivisibleByKTest {

    @Test
    void typicalInput() {
        assertThat(SubarraySumDivisibleByK.solve(List.of(4, 5, 0, -2, -3, 1), 5)).isEqualTo(7);
    }

    @Test
    void emptyInput() {
        assertThat(SubarraySumDivisibleByK.solve(List.of(), 5)).isEqualTo(0);
    }

    @Test
    void singleElementDivisible() {
        assertThat(SubarraySumDivisibleByK.solve(List.of(5), 5)).isEqualTo(1);
    }

    @Test
    void singleElementNotDivisible() {
        assertThat(SubarraySumDivisibleByK.solve(List.of(3), 5)).isEqualTo(0);
    }

    @Test
    void allZerosCountsEverySubarray() {
        List<Integer> zeros = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            zeros.add(0);
        }
        // every one of the 5*6/2 = 15 contiguous subarrays sums to 0, divisible by any k
        assertThat(SubarraySumDivisibleByK.solve(zeros, 7)).isEqualTo(15);
    }

    @Test
    void largerInputAllOnesDivisibleByFour() {
        List<Integer> ones = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ones.add(1);
        }
        // subarrays of length that is a multiple of 4: lengths 4,8,...,100 -> for each length L
        // there are (100 - L + 1) subarrays
        int expected = 0;
        for (int len = 4; len <= 100; len += 4) {
            expected += 100 - len + 1;
        }
        assertThat(SubarraySumDivisibleByK.solve(ones, 4)).isEqualTo(expected);
    }
}
