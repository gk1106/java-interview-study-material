package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LongestConsecutiveSequence} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class LongestConsecutiveSequenceTest {

    @Test
    void typicalInputFindsLongestRun() {
        assertThat(LongestConsecutiveSequence.solve(new int[] {100, 4, 200, 1, 3, 2})).isEqualTo(4);
    }

    @Test
    void emptyArrayReturnsZero() {
        assertThat(LongestConsecutiveSequence.solve(new int[0])).isEqualTo(0);
    }

    @Test
    void singleElementReturnsOne() {
        assertThat(LongestConsecutiveSequence.solve(new int[] {7})).isEqualTo(1);
    }

    @Test
    void duplicatesDoNotInflateRunLength() {
        assertThat(LongestConsecutiveSequence.solve(new int[] {1, 2, 2, 3})).isEqualTo(3);
    }

    @Test
    void negativeNumbersFormARun() {
        assertThat(LongestConsecutiveSequence.solve(new int[] {0, -1})).isEqualTo(2);
    }

    @Test
    void noConsecutiveNumbersReturnsOne() {
        assertThat(LongestConsecutiveSequence.solve(new int[] {10, 30, 50})).isEqualTo(1);
    }
}
