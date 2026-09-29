package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LongestSubstringWithoutRepeatingSolutionTest {

    @Test
    void typicalInput() {
        assertThat(LongestSubstringWithoutRepeatingSolution.solve("abcabcbb")).isEqualTo(3);
    }

    @Test
    void allSameCharacter() {
        assertThat(LongestSubstringWithoutRepeatingSolution.solve("bbbbb")).isEqualTo(1);
    }

    @Test
    void emptyString() {
        assertThat(LongestSubstringWithoutRepeatingSolution.solve("")).isEqualTo(0);
    }

    @Test
    void repeatAtWindowStart() {
        assertThat(LongestSubstringWithoutRepeatingSolution.solve("pwwkew")).isEqualTo(3);
    }

    @Test
    void allUniqueCharacters() {
        assertThat(LongestSubstringWithoutRepeatingSolution.solve("abcdef")).isEqualTo(6);
    }

    @Test
    void largerInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        assertThat(LongestSubstringWithoutRepeatingSolution.solve(sb.toString())).isEqualTo(26);
    }
}
