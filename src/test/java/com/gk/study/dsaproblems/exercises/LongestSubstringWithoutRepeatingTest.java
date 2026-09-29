package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LongestSubstringWithoutRepeating} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class LongestSubstringWithoutRepeatingTest {

    @Test
    void typicalInput() {
        assertThat(LongestSubstringWithoutRepeating.solve("abcabcbb")).isEqualTo(3);
    }

    @Test
    void allSameCharacter() {
        assertThat(LongestSubstringWithoutRepeating.solve("bbbbb")).isEqualTo(1);
    }

    @Test
    void emptyString() {
        assertThat(LongestSubstringWithoutRepeating.solve("")).isEqualTo(0);
    }

    @Test
    void repeatAtWindowStart() {
        assertThat(LongestSubstringWithoutRepeating.solve("pwwkew")).isEqualTo(3);
    }

    @Test
    void allUniqueCharacters() {
        assertThat(LongestSubstringWithoutRepeating.solve("abcdef")).isEqualTo(6);
    }

    @Test
    void largerInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        assertThat(LongestSubstringWithoutRepeating.solve(sb.toString())).isEqualTo(26);
    }
}
