package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidAnagramSolutionTest {

    @Test
    void anagramPair() {
        assertThat(ValidAnagramSolution.solve("listen", "silent")).isTrue();
    }

    @Test
    void notAnagram() {
        assertThat(ValidAnagramSolution.solve("rat", "car")).isFalse();
    }

    @Test
    void differentLengths() {
        assertThat(ValidAnagramSolution.solve("a", "ab")).isFalse();
    }

    @Test
    void emptyStrings() {
        assertThat(ValidAnagramSolution.solve("", "")).isTrue();
    }

    @Test
    void singleCharacter() {
        assertThat(ValidAnagramSolution.solve("a", "a")).isTrue();
        assertThat(ValidAnagramSolution.solve("a", "b")).isFalse();
    }

    @Test
    void allSameCharacter() {
        assertThat(ValidAnagramSolution.solve("aaaa", "aaaa")).isTrue();
        assertThat(ValidAnagramSolution.solve("aaaa", "aaab")).isFalse();
    }

    @Test
    void largerInputPermutation() {
        String s = "a".repeat(500) + "b".repeat(500);
        String t = "b".repeat(500) + "a".repeat(500);
        assertThat(ValidAnagramSolution.solve(s, t)).isTrue();
    }
}
