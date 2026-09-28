package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicArrayAmortizedTest {

    @Test
    void pushGrowsSizeAndPreservesValues() {
        DynamicArrayAmortized arr = new DynamicArrayAmortized();
        for (int i = 0; i < 5; i++) {
            arr.push(i * 10);
        }
        assertThat(arr.size()).isEqualTo(5);
        assertThat(arr.get(0)).isEqualTo(0);
        assertThat(arr.get(4)).isEqualTo(40);
    }

    @Test
    void totalCopiesStayUnderAmortizedBoundForLargeN() {
        DynamicArrayAmortized arr = new DynamicArrayAmortized();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            arr.push(i);
        }
        assertThat(arr.size()).isEqualTo(n);
        assertThat(arr.getTotalElementCopies()).isLessThan(2L * n);
    }

    @Test
    void outOfBoundsGetThrows() {
        DynamicArrayAmortized arr = new DynamicArrayAmortized();
        arr.push(1);
        org.junit.jupiter.api.Assertions.assertThrows(IndexOutOfBoundsException.class, () -> arr.get(5));
    }
}
