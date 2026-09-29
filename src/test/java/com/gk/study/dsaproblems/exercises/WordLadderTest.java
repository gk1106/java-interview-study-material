package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link WordLadder} exercise stub. EXPECTED TO FAIL until implemented.
 */
class WordLadderTest {

    @Test
    void typicalInput() {
        List<String> wordList = List.of("hot", "dot", "dog", "lot", "log", "cog");
        assertThat(WordLadder.solve("hit", "cog", wordList)).isEqualTo(5);
    }

    @Test
    void endWordNotInDictionaryReturnsZero() {
        List<String> wordList = List.of("hot", "dot", "dog", "lot", "log");
        assertThat(WordLadder.solve("hit", "cog", wordList)).isEqualTo(0);
    }

    @Test
    void beginEqualsEndButEndNotInListReturnsZero() {
        assertThat(WordLadder.solve("hit", "hit", List.of("hot"))).isEqualTo(0);
    }

    @Test
    void oneStepTransformation() {
        assertThat(WordLadder.solve("hot", "dot", List.of("hot", "dot"))).isEqualTo(2);
    }

    @Test
    void noPathExists() {
        List<String> wordList = List.of("aaa", "bbb", "ccc");
        assertThat(WordLadder.solve("xxx", "ccc", wordList)).isEqualTo(0);
    }

    @Test
    void largerInputLongChain() {
        // word_i = i leading 'b's followed by all 'a's; word_i and word_(i+1) differ by exactly
        // one letter (position i+1 flips a -> b), and any non-consecutive pair differs by more
        // than one letter, so no shortcut exists and the shortest path must visit every rung
        int chainLength = 20;
        int wordLength = chainLength;
        StringBuilder beginBuilder = new StringBuilder();
        for (int i = 0; i < wordLength; i++) {
            beginBuilder.append('a');
        }
        String beginWord = beginBuilder.toString();

        List<String> wordList = new ArrayList<>();
        for (int i = 1; i <= chainLength; i++) {
            char[] chars = beginWord.toCharArray();
            for (int p = 0; p < i; p++) {
                chars[p] = 'b';
            }
            wordList.add(new String(chars));
        }
        String endWord = wordList.get(wordList.size() - 1);
        // beginWord + chainLength dictionary words = chainLength + 1 total words in the path
        assertThat(WordLadder.solve(beginWord, endWord, wordList)).isEqualTo(chainLength + 1);
    }
}
