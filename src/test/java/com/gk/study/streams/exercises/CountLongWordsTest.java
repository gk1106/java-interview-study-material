package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link CountLongWords} exercise stub. EXPECTED TO FAIL until implemented.
 */
class CountLongWordsTest {

    @Test
    void typicalInput() {
        assertThat(CountLongWords.solve("the quick brown fox jumps over the lazy dog", 4)).isEqualTo(3);
    }

    @Test
    void emptySentence() {
        assertThat(CountLongWords.solve("", 3)).isEqualTo(0);
    }

    @Test
    void extraWhitespace() {
        assertThat(CountLongWords.solve("  hello   world  ", 3)).isEqualTo(2);
    }

    @Test
    void thresholdAboveEveryWordLength() {
        assertThat(CountLongWords.solve("a bb ccc", 100)).isEqualTo(0);
    }
}
