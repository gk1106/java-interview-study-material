package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TopKFrequentWordsTieBreakSolutionTest {

    @Test
    void typicalInput() {
        assertThat(TopKFrequentWordsTieBreakSolution.solve(
                        List.of("i", "love", "leetcode", "i", "love", "coding"), 2))
                .containsExactly("i", "love");
    }

    @Test
    void tieBrokenAlphabetically() {
        assertThat(TopKFrequentWordsTieBreakSolution.solve(
                        List.of("the", "day", "is", "sunny", "the", "the", "sunny", "is", "is"), 4))
                .containsExactly("is", "the", "sunny", "day");
    }

    @Test
    void singleWord() {
        assertThat(TopKFrequentWordsTieBreakSolution.solve(List.of("solo"), 1))
                .containsExactly("solo");
    }

    @Test
    void kEqualsDistinctWordCount() {
        assertThat(TopKFrequentWordsTieBreakSolution.solve(List.of("b", "a", "a", "b", "c"), 3))
                .containsExactly("a", "b", "c");
    }

    @Test
    void allWordsUniqueFrequencyTieBreaksAllAlphabetical() {
        assertThat(TopKFrequentWordsTieBreakSolution.solve(List.of("zebra", "apple", "mango"), 3))
                .containsExactly("apple", "mango", "zebra");
    }

    @Test
    void largerInput() {
        List<String> words = List.of(
                "a", "a", "a", "b", "b", "c", "d", "d", "d", "d", "e", "f", "f");
        assertThat(TopKFrequentWordsTieBreakSolution.solve(words, 3)).containsExactly("d", "a", "b");
    }
}
