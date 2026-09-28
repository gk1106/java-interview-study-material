package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LongestConsecutiveSequenceSolutionTest {

    @Test
    void typicalInputFindsLongestRun() {
        assertThat(LongestConsecutiveSequenceSolution.solve(new int[] {100, 4, 200, 1, 3, 2})).isEqualTo(4);
    }

    @Test
    void emptyArrayReturnsZero() {
        assertThat(LongestConsecutiveSequenceSolution.solve(new int[0])).isEqualTo(0);
    }

    @Test
    void singleElementReturnsOne() {
        assertThat(LongestConsecutiveSequenceSolution.solve(new int[] {7})).isEqualTo(1);
    }

    @Test
    void duplicatesDoNotInflateRunLength() {
        assertThat(LongestConsecutiveSequenceSolution.solve(new int[] {1, 2, 2, 3})).isEqualTo(3);
    }

    @Test
    void negativeNumbersFormARun() {
        assertThat(LongestConsecutiveSequenceSolution.solve(new int[] {0, -1})).isEqualTo(2);
    }

    @Test
    void noConsecutiveNumbersReturnsOne() {
        assertThat(LongestConsecutiveSequenceSolution.solve(new int[] {10, 30, 50})).isEqualTo(1);
    }
}
