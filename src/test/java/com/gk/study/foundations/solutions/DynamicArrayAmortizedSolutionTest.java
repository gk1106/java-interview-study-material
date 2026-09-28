package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DynamicArrayAmortizedSolutionTest {

    @Test
    void pushGrowsSizeAndPreservesValues() {
        DynamicArrayAmortizedSolution arr = new DynamicArrayAmortizedSolution();
        for (int i = 0; i < 5; i++) {
            arr.push(i * 10);
        }
        assertThat(arr.size()).isEqualTo(5);
        assertThat(arr.get(0)).isEqualTo(0);
        assertThat(arr.get(4)).isEqualTo(40);
    }

    @Test
    void sixteenPushesCopyExactly15Elements() {
        DynamicArrayAmortizedSolution arr = new DynamicArrayAmortizedSolution();
        for (int i = 0; i < 16; i++) {
            arr.push(i);
        }
        // capacities visited: 1 -> 2 -> 4 -> 8 -> 16, copies at each resize: 1 + 2 + 4 + 8 = 15
        assertThat(arr.getTotalElementCopies()).isEqualTo(15L);
    }

    @Test
    void totalCopiesStayUnderAmortizedBoundForLargeN() {
        DynamicArrayAmortizedSolution arr = new DynamicArrayAmortizedSolution();
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            arr.push(i);
        }
        assertThat(arr.size()).isEqualTo(n);
        assertThat(arr.getTotalElementCopies()).isLessThan(2L * n);
    }

    @Test
    void outOfBoundsGetThrows() {
        DynamicArrayAmortizedSolution arr = new DynamicArrayAmortizedSolution();
        arr.push(1);
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(5));
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1));
    }
}
