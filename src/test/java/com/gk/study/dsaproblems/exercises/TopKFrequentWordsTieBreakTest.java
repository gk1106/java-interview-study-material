package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TopKFrequentWordsTieBreak} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class TopKFrequentWordsTieBreakTest {

    @Test
    void typicalInput() {
        assertThat(TopKFrequentWordsTieBreak.solve(
                        List.of("i", "love", "leetcode", "i", "love", "coding"), 2))
                .containsExactly("i", "love");
    }

    @Test
    void tieBrokenAlphabetically() {
        // frequencies: the=3, is=3, sunny=2, day=1 -> "is" sorts before "the" on the freq-3 tie
        assertThat(TopKFrequentWordsTieBreak.solve(
                        List.of("the", "day", "is", "sunny", "the", "the", "sunny", "is", "is"), 4))
                .containsExactly("is", "the", "sunny", "day");
    }

    @Test
    void singleWord() {
        assertThat(TopKFrequentWordsTieBreak.solve(List.of("solo"), 1)).containsExactly("solo");
    }

    @Test
    void kEqualsDistinctWordCount() {
        assertThat(TopKFrequentWordsTieBreak.solve(List.of("b", "a", "a", "b", "c"), 3))
                .containsExactly("a", "b", "c");
    }

    @Test
    void allWordsUniqueFrequencyTieBreaksAllAlphabetical() {
        assertThat(TopKFrequentWordsTieBreak.solve(List.of("zebra", "apple", "mango"), 3))
                .containsExactly("apple", "mango", "zebra");
    }

    @Test
    void largerInput() {
        List<String> words = List.of(
                "a", "a", "a", "b", "b", "c", "d", "d", "d", "d", "e", "f", "f");
        // frequencies: a=3, b=2, c=1, d=4, e=1, f=2
        assertThat(TopKFrequentWordsTieBreak.solve(words, 3)).containsExactly("d", "a", "b");
    }
}
