package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentWordCounterSolutionTest {

    @Test
    void countsWordsAcrossThreads() {
        List<String> words = List.of("a", "b", "a", "c", "b", "a");
        Map<String, Integer> counts = ConcurrentWordCounterSolution.countWords(words, 4);

        assertThat(counts).containsEntry("a", 3).containsEntry("b", 2).containsEntry("c", 1);
    }

    @Test
    void emptyInputProducesEmptyMap() {
        assertThat(ConcurrentWordCounterSolution.countWords(List.of(), 4)).isEmpty();
    }

    @Test
    void largerInputHasCorrectTotals() {
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            words.add("word" + (i % 10));
        }
        Map<String, Integer> counts = ConcurrentWordCounterSolution.countWords(words, 8);

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        assertThat(total).isEqualTo(1000);
        assertThat(counts).hasSize(10);
        for (int i = 0; i < 10; i++) {
            assertThat(counts.get("word" + i)).isEqualTo(100);
        }
    }

    @Test
    void singleThreadStillCorrect() {
        List<String> words = List.of("x", "x", "y");
        Map<String, Integer> counts = ConcurrentWordCounterSolution.countWords(words, 1);

        assertThat(counts).containsEntry("x", 2).containsEntry("y", 1);
    }
}
