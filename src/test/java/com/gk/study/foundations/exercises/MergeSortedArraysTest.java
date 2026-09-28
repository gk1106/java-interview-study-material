package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MergeSortedArraysTest {

    @Test
    void mergesTwoSortedArrays() {
        assertThat(MergeSortedArrays.merge(new int[] {1, 3, 5}, new int[] {2, 4, 6}))
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void oneArrayEmpty() {
        assertThat(MergeSortedArrays.merge(new int[] {}, new int[] {1, 2, 3}))
                .containsExactly(1, 2, 3);
    }

    @Test
    void duplicatesAcrossArraysAreKept() {
        assertThat(MergeSortedArrays.merge(new int[] {1, 2}, new int[] {2, 3}))
                .containsExactly(1, 2, 2, 3);
    }
}
