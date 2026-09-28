package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LongestUniqueCharSubstring} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class LongestUniqueCharSubstringTest {

    @Test
    void typicalInput() {
        List<Character> chars = List.of('a', 'b', 'c', 'a', 'b', 'c', 'b', 'b');
        assertThat(LongestUniqueCharSubstring.solve(chars)).isEqualTo(3);
    }

    @Test
    void emptyInputReturnsZero() {
        assertThat(LongestUniqueCharSubstring.solve(List.of())).isEqualTo(0);
    }

    @Test
    void allSameCharacterReturnsOne() {
        List<Character> chars = List.of('x', 'x', 'x', 'x');
        assertThat(LongestUniqueCharSubstring.solve(chars)).isEqualTo(1);
    }

    @Test
    void allUniqueCharactersReturnsFullLength() {
        List<Character> chars = List.of('a', 'b', 'c', 'd', 'e');
        assertThat(LongestUniqueCharSubstring.solve(chars)).isEqualTo(5);
    }

    @Test
    void singleCharacterReturnsOne() {
        assertThat(LongestUniqueCharSubstring.solve(List.of('z'))).isEqualTo(1);
    }

    @Test
    void repeatAtStartRequiresWindowShrink() {
        List<Character> chars = List.of('a', 'a', 'b', 'c', 'd', 'e', 'f');
        assertThat(LongestUniqueCharSubstring.solve(chars)).isEqualTo(6); // "abcdef"
    }
}
