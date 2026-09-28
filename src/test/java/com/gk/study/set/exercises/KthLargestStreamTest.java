package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link KthLargestStream} exercise stub. EXPECTED TO FAIL until implemented.
 */
class KthLargestStreamTest {

    @Test
    void classicTraceMatchesExpectedSequence() {
        KthLargestStream stream = new KthLargestStream(3, new int[] {4, 5, 8, 2});
        assertThat(stream.add(3)).isEqualTo(4);
        assertThat(stream.add(5)).isEqualTo(5);
        assertThat(stream.add(10)).isEqualTo(5);
        assertThat(stream.add(9)).isEqualTo(8);
        assertThat(stream.add(4)).isEqualTo(8);
    }

    @Test
    void kEqualsOneTracksTheSingleMaximum() {
        KthLargestStream stream = new KthLargestStream(1, new int[] {3});
        assertThat(stream.add(5)).isEqualTo(5);
        assertThat(stream.add(2)).isEqualTo(5);
        assertThat(stream.add(10)).isEqualTo(10);
    }

    @Test
    void duplicateValuesAreTrackedIndependently() {
        KthLargestStream stream = new KthLargestStream(2, new int[] {5, 5});
        assertThat(stream.add(5)).isEqualTo(5);
    }
}
