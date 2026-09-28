package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MedianOfTwoSortedArraysSolutionTest {

    @Test
    void oddTotalLength() {
        assertThat(MedianOfTwoSortedArraysSolution.findMedianSortedArrays(new int[] {1, 3}, new int[] {2}))
                .isEqualTo(2.0);
    }

    @Test
    void evenTotalLength() {
        assertThat(MedianOfTwoSortedArraysSolution.findMedianSortedArrays(new int[] {1, 2}, new int[] {3, 4}))
                .isEqualTo(2.5);
    }

    @Test
    void oneArrayEmpty() {
        assertThat(MedianOfTwoSortedArraysSolution.findMedianSortedArrays(new int[] {}, new int[] {1, 2, 3}))
                .isEqualTo(2.0);
        assertThat(MedianOfTwoSortedArraysSolution.findMedianSortedArrays(new int[] {2}, new int[] {}))
                .isEqualTo(2.0);
    }

    @Test
    void largerMixedArrays() {
        assertThat(MedianOfTwoSortedArraysSolution.findMedianSortedArrays(
                new int[] {1, 3, 8, 9, 15}, new int[] {7, 11, 18, 19, 21, 25}))
                .isEqualTo(11.0);
    }

    @Test
    void argumentOrderDoesNotMatter() {
        double a = MedianOfTwoSortedArraysSolution.findMedianSortedArrays(new int[] {1, 2}, new int[] {3, 4});
        double b = MedianOfTwoSortedArraysSolution.findMedianSortedArrays(new int[] {3, 4}, new int[] {1, 2});
        assertThat(a).isEqualTo(b);
    }
}
