package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LongestSubstringKDistinct} exercise stub. EXPECTED TO FAIL until implemented.
 */
class LongestSubstringKDistinctTest {

    @Test
    void typicalInput() {
        assertThat(LongestSubstringKDistinct.solve("eceba", 2)).isEqualTo(3);
    }

    @Test
    void kZeroReturnsZero() {
        assertThat(LongestSubstringKDistinct.solve("abc", 0)).isEqualTo(0);
    }

    @Test
    void emptyStringReturnsZero() {
        assertThat(LongestSubstringKDistinct.solve("", 2)).isEqualTo(0);
    }

    @Test
    void kGreaterThanDistinctCharsReturnsWholeString() {
        assertThat(LongestSubstringKDistinct.solve("aabbcc", 10)).isEqualTo(6);
    }

    @Test
    void allSameCharacter() {
        assertThat(LongestSubstringKDistinct.solve("aaaa", 1)).isEqualTo(4);
    }
}
