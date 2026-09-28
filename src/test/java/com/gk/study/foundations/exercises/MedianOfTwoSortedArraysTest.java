package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedianOfTwoSortedArraysTest {

    @Test
    void oddTotalLength() {
        assertThat(MedianOfTwoSortedArrays.findMedianSortedArrays(new int[] {1, 3}, new int[] {2}))
                .isEqualTo(2.0);
    }

    @Test
    void evenTotalLength() {
        assertThat(MedianOfTwoSortedArrays.findMedianSortedArrays(new int[] {1, 2}, new int[] {3, 4}))
                .isEqualTo(2.5);
    }

    @Test
    void oneArrayEmpty() {
        assertThat(MedianOfTwoSortedArrays.findMedianSortedArrays(new int[] {}, new int[] {1, 2, 3}))
                .isEqualTo(2.0);
    }
}
