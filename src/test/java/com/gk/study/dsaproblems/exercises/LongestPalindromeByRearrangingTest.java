package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LongestPalindromeByRearranging} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class LongestPalindromeByRearrangingTest {

    @Test
    void typicalInput() {
        assertThat(LongestPalindromeByRearranging.solve("abccccdd")).isEqualTo(7);
    }

    @Test
    void allSameCharacter() {
        assertThat(LongestPalindromeByRearranging.solve("aaaa")).isEqualTo(4);
    }

    @Test
    void emptyString() {
        assertThat(LongestPalindromeByRearranging.solve("")).isEqualTo(0);
    }

    @Test
    void singleCharacter() {
        assertThat(LongestPalindromeByRearranging.solve("a")).isEqualTo(1);
    }

    @Test
    void allDistinctCharactersOnlyOneUsable() {
        assertThat(LongestPalindromeByRearranging.solve("abcdef")).isEqualTo(1);
    }

    @Test
    void caseSensitiveCountsSeparately() {
        assertThat(LongestPalindromeByRearranging.solve("Aa")).isEqualTo(1);
    }

    @Test
    void largerInputAllPairs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            char c = (char) ('a' + i);
            sb.append(c).append(c); // every letter appears exactly twice
        }
        assertThat(LongestPalindromeByRearranging.solve(sb.toString())).isEqualTo(52);
    }
}
