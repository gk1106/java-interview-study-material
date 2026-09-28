package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class LongestUniqueCharSubstringSolutionTest {

    @Test
    void typicalInput() {
        List<Character> chars = List.of('a', 'b', 'c', 'a', 'b', 'c', 'b', 'b');
        assertThat(LongestUniqueCharSubstringSolution.solve(chars)).isEqualTo(3);
    }

    @Test
    void emptyInputReturnsZero() {
        assertThat(LongestUniqueCharSubstringSolution.solve(List.of())).isEqualTo(0);
    }

    @Test
    void allSameCharacterReturnsOne() {
        List<Character> chars = List.of('x', 'x', 'x', 'x');
        assertThat(LongestUniqueCharSubstringSolution.solve(chars)).isEqualTo(1);
    }

    @Test
    void allUniqueCharactersReturnsFullLength() {
        List<Character> chars = List.of('a', 'b', 'c', 'd', 'e');
        assertThat(LongestUniqueCharSubstringSolution.solve(chars)).isEqualTo(5);
    }

    @Test
    void singleCharacterReturnsOne() {
        assertThat(LongestUniqueCharSubstringSolution.solve(List.of('z'))).isEqualTo(1);
    }

    @Test
    void repeatAtStartRequiresWindowShrink() {
        List<Character> chars = List.of('a', 'a', 'b', 'c', 'd', 'e', 'f');
        assertThat(LongestUniqueCharSubstringSolution.solve(chars)).isEqualTo(6);
    }
}
