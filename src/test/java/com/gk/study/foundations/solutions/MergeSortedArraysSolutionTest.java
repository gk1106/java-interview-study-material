package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MergeSortedArraysSolutionTest {

    @Test
    void mergesTwoSortedArrays() {
        assertThat(MergeSortedArraysSolution.merge(new int[] {1, 3, 5}, new int[] {2, 4, 6}))
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void bothArraysEmpty() {
        assertThat(MergeSortedArraysSolution.merge(new int[] {}, new int[] {})).isEmpty();
    }

    @Test
    void oneArrayEmpty() {
        assertThat(MergeSortedArraysSolution.merge(new int[] {}, new int[] {1, 2, 3}))
                .containsExactly(1, 2, 3);
    }

    @Test
    void duplicatesAcrossArraysAreKept() {
        assertThat(MergeSortedArraysSolution.merge(new int[] {1, 2}, new int[] {2, 3}))
                .containsExactly(1, 2, 2, 3);
    }

    @Test
    void negativeNumbers() {
        assertThat(MergeSortedArraysSolution.merge(new int[] {-5, -1, 3}, new int[] {-3, 0, 2}))
                .containsExactly(-5, -3, -1, 0, 2, 3);
    }
}
