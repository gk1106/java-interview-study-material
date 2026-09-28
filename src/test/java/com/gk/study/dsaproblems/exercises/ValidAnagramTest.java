package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ValidAnagram} exercise stub. EXPECTED TO FAIL until implemented.
 */
class ValidAnagramTest {

    @Test
    void anagramPair() {
        assertThat(ValidAnagram.solve("listen", "silent")).isTrue();
    }

    @Test
    void notAnagram() {
        assertThat(ValidAnagram.solve("rat", "car")).isFalse();
    }

    @Test
    void differentLengths() {
        assertThat(ValidAnagram.solve("a", "ab")).isFalse();
    }

    @Test
    void emptyStrings() {
        assertThat(ValidAnagram.solve("", "")).isTrue();
    }

    @Test
    void singleCharacter() {
        assertThat(ValidAnagram.solve("a", "a")).isTrue();
        assertThat(ValidAnagram.solve("a", "b")).isFalse();
    }

    @Test
    void allSameCharacter() {
        assertThat(ValidAnagram.solve("aaaa", "aaaa")).isTrue();
        assertThat(ValidAnagram.solve("aaaa", "aaab")).isFalse();
    }
}
