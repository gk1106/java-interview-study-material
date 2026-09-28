package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KthLargestStreamSolutionTest {

    @Test
    void classicTraceMatchesExpectedSequence() {
        KthLargestStreamSolution stream = new KthLargestStreamSolution(3, new int[] {4, 5, 8, 2});
        assertThat(stream.add(3)).isEqualTo(4);
        assertThat(stream.add(5)).isEqualTo(5);
        assertThat(stream.add(10)).isEqualTo(5);
        assertThat(stream.add(9)).isEqualTo(8);
        assertThat(stream.add(4)).isEqualTo(8);
    }

    @Test
    void kEqualsOneTracksTheSingleMaximum() {
        KthLargestStreamSolution stream = new KthLargestStreamSolution(1, new int[] {3});
        assertThat(stream.add(5)).isEqualTo(5);
        assertThat(stream.add(2)).isEqualTo(5);
        assertThat(stream.add(10)).isEqualTo(10);
    }

    @Test
    void duplicateValuesAreTrackedIndependently() {
        KthLargestStreamSolution stream = new KthLargestStreamSolution(2, new int[] {5, 5});
        assertThat(stream.add(5)).isEqualTo(5);
    }
}
