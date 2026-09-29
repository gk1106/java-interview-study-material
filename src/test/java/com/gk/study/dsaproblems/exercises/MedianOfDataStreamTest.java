package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MedianOfDataStream} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MedianOfDataStreamTest {

    @Test
    void typicalInput() {
        MedianOfDataStream stream = new MedianOfDataStream();
        stream.addNum(1);
        stream.addNum(2);
        assertThat(stream.findMedian()).isCloseTo(1.5, within(1e-9));
        stream.addNum(3);
        assertThat(stream.findMedian()).isCloseTo(2.0, within(1e-9));
    }

    @Test
    void singleNumber() {
        MedianOfDataStream stream = new MedianOfDataStream();
        stream.addNum(42);
        assertThat(stream.findMedian()).isCloseTo(42.0, within(1e-9));
    }

    @Test
    void numbersAddedOutOfOrder() {
        MedianOfDataStream stream = new MedianOfDataStream();
        stream.addNum(5);
        stream.addNum(1);
        stream.addNum(3);
        assertThat(stream.findMedian()).isCloseTo(3.0, within(1e-9));
    }

    @Test
    void duplicateNumbers() {
        MedianOfDataStream stream = new MedianOfDataStream();
        stream.addNum(4);
        stream.addNum(4);
        stream.addNum(4);
        assertThat(stream.findMedian()).isCloseTo(4.0, within(1e-9));
    }

    @Test
    void negativeNumbers() {
        MedianOfDataStream stream = new MedianOfDataStream();
        stream.addNum(-5);
        stream.addNum(-1);
        stream.addNum(-10);
        stream.addNum(0);
        assertThat(stream.findMedian()).isCloseTo(-3.0, within(1e-9));
    }

    @Test
    void largerStreamMatchesSortedMedian() {
        MedianOfDataStream stream = new MedianOfDataStream();
        int[] values = new int[1001];
        for (int i = 0; i < values.length; i++) {
            values[i] = (i * 37) % 1001; // pseudo-shuffled but deterministic
            stream.addNum(values[i]);
        }
        int[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        assertThat(stream.findMedian()).isCloseTo(sorted[500], within(1e-9));
    }
}
