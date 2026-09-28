package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LongestSubstringKDistinctSolutionTest {

    @Test
    void typicalInput() {
        assertThat(LongestSubstringKDistinctSolution.solve("eceba", 2)).isEqualTo(3);
    }

    @Test
    void kZeroReturnsZero() {
        assertThat(LongestSubstringKDistinctSolution.solve("abc", 0)).isEqualTo(0);
    }

    @Test
    void emptyStringReturnsZero() {
        assertThat(LongestSubstringKDistinctSolution.solve("", 2)).isEqualTo(0);
    }

    @Test
    void kGreaterThanDistinctCharsReturnsWholeString() {
        assertThat(LongestSubstringKDistinctSolution.solve("aabbcc", 10)).isEqualTo(6);
    }

    @Test
    void allSameCharacter() {
        assertThat(LongestSubstringKDistinctSolution.solve("aaaa", 1)).isEqualTo(4);
    }
}
