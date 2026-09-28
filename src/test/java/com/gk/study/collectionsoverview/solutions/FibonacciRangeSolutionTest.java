package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FibonacciRangeSolutionTest {

    @Test
    void yieldsFibonacciSequenceUpToBound() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRangeSolution(10)) {
            result.add(x);
        }
        assertThat(result).containsExactly(0, 1, 1, 2, 3, 5, 8);
    }

    @Test
    void boundZeroYieldsOnlyZero() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRangeSolution(0)) {
            result.add(x);
        }
        assertThat(result).containsExactly(0);
    }

    @Test
    void negativeBoundYieldsNothing() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRangeSolution(-5)) {
            result.add(x);
        }
        assertThat(result).isEmpty();
    }

    @Test
    void largerBoundStopsAtOrBelowBound() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRangeSolution(100)) {
            result.add(x);
        }
        assertThat(result).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89);
    }
}
