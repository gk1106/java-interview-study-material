package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E03 FibonacciRange. Expected to fail until implemented.
 */
class FibonacciRangeTest {

    @Test
    void yieldsFibonacciSequenceUpToBound() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRange(10)) {
            result.add(x);
        }
        assertThat(result).containsExactly(0, 1, 1, 2, 3, 5, 8);
    }

    @Test
    void boundZeroYieldsOnlyZero() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRange(0)) {
            result.add(x);
        }
        assertThat(result).containsExactly(0);
    }

    @Test
    void negativeBoundYieldsNothing() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRange(-5)) {
            result.add(x);
        }
        assertThat(result).isEmpty();
    }

    @Test
    void largerBoundStopsAtOrBelowBound() {
        List<Integer> result = new ArrayList<>();
        for (int x : new FibonacciRange(100)) {
            result.add(x);
        }
        assertThat(result).containsExactly(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89);
    }
}
