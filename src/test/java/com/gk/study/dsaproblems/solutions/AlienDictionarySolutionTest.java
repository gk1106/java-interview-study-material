package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class AlienDictionarySolutionTest {

    @Test
    void typicalInputFullyDeterminedChain() {
        List<String> words = List.of("wrt", "wrf", "er", "ett", "rftt");
        assertThat(AlienDictionarySolution.solve(words)).isEqualTo("wertf");
    }

    @Test
    void secondKnownExample() {
        List<String> words = List.of("baa", "abcd", "abca", "cab", "cad");
        assertThat(AlienDictionarySolution.solve(words)).isEqualTo("bdac");
    }

    @Test
    void invalidPrefixOrderingReturnsEmpty() {
        assertThat(AlienDictionarySolution.solve(List.of("abc", "ab"))).isEqualTo("");
    }

    @Test
    void cycleReturnsEmpty() {
        assertThat(AlienDictionarySolution.solve(List.of("a", "b", "c", "a"))).isEqualTo("");
    }

    @Test
    void singleWordUsesOnlyItsLetters() {
        String result = AlienDictionarySolution.solve(List.of("abc"));
        assertThat(result).hasSize(3);
        assertThat(result.chars().mapToObj(c -> (char) c)).containsExactlyInAnyOrder('a', 'b', 'c');
    }

    @Test
    void largerInputLinearChainOfManyLetters() {
        List<String> words = List.of(
                "a", "ab", "abc", "abcd", "abcde", "abcdef", "abcdefg");
        assertThat(AlienDictionarySolution.solve(words)).isEqualTo("abcdefg");
    }
}
