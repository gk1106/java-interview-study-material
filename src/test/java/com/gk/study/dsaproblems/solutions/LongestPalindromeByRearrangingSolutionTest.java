package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LongestPalindromeByRearrangingSolutionTest {

    @Test
    void typicalInput() {
        assertThat(LongestPalindromeByRearrangingSolution.solve("abccccdd")).isEqualTo(7);
    }

    @Test
    void allSameCharacter() {
        assertThat(LongestPalindromeByRearrangingSolution.solve("aaaa")).isEqualTo(4);
    }

    @Test
    void emptyString() {
        assertThat(LongestPalindromeByRearrangingSolution.solve("")).isEqualTo(0);
    }

    @Test
    void singleCharacter() {
        assertThat(LongestPalindromeByRearrangingSolution.solve("a")).isEqualTo(1);
    }

    @Test
    void allDistinctCharactersOnlyOneUsable() {
        assertThat(LongestPalindromeByRearrangingSolution.solve("abcdef")).isEqualTo(1);
    }

    @Test
    void caseSensitiveCountsSeparately() {
        assertThat(LongestPalindromeByRearrangingSolution.solve("Aa")).isEqualTo(1);
    }

    @Test
    void largerInputAllPairs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            char c = (char) ('a' + i);
            sb.append(c).append(c);
        }
        assertThat(LongestPalindromeByRearrangingSolution.solve(sb.toString())).isEqualTo(52);
    }
}
