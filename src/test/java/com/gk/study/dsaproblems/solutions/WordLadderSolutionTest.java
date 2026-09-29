package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WordLadderSolutionTest {

    @Test
    void typicalInput() {
        List<String> wordList = List.of("hot", "dot", "dog", "lot", "log", "cog");
        assertThat(WordLadderSolution.solve("hit", "cog", wordList)).isEqualTo(5);
    }

    @Test
    void endWordNotInDictionaryReturnsZero() {
        List<String> wordList = List.of("hot", "dot", "dog", "lot", "log");
        assertThat(WordLadderSolution.solve("hit", "cog", wordList)).isEqualTo(0);
    }

    @Test
    void beginEqualsEndButEndNotInListReturnsZero() {
        assertThat(WordLadderSolution.solve("hit", "hit", List.of("hot"))).isEqualTo(0);
    }

    @Test
    void oneStepTransformation() {
        assertThat(WordLadderSolution.solve("hot", "dot", List.of("hot", "dot"))).isEqualTo(2);
    }

    @Test
    void noPathExists() {
        List<String> wordList = List.of("aaa", "bbb", "ccc");
        assertThat(WordLadderSolution.solve("xxx", "ccc", wordList)).isEqualTo(0);
    }

    @Test
    void largerInputLongChain() {
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
        assertThat(WordLadderSolution.solve(beginWord, endWord, wordList))
                .isEqualTo(chainLength + 1);
    }
}
