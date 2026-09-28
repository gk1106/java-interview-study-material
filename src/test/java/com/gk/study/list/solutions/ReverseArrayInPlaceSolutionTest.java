package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReverseArrayInPlaceSolutionTest {

    @Test
    void reversesOddLengthArray() {
        int[] arr = {1, 2, 3, 4, 5};
        ReverseArrayInPlaceSolution.solve(arr);
        assertThat(arr).containsExactly(5, 4, 3, 2, 1);
    }

    @Test
    void reversesEvenLengthArray() {
        int[] arr = {1, 2, 3, 4};
        ReverseArrayInPlaceSolution.solve(arr);
        assertThat(arr).containsExactly(4, 3, 2, 1);
    }

    @Test
    void emptyArrayStaysEmpty() {
        int[] arr = {};
        ReverseArrayInPlaceSolution.solve(arr);
        assertThat(arr).isEmpty();
    }

    @Test
    void singleElementUnchanged() {
        int[] arr = {42};
        ReverseArrayInPlaceSolution.solve(arr);
        assertThat(arr).containsExactly(42);
    }

    @Test
    void largerInputReversedCorrectly() {
        int n = 10_000;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        ReverseArrayInPlaceSolution.solve(arr);
        for (int i = 0; i < n; i++) {
            assertThat(arr[i]).isEqualTo(n - 1 - i);
        }
    }
}
