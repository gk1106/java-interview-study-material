package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link AlienDictionary} exercise stub. EXPECTED TO FAIL until implemented.
 */
class AlienDictionaryTest {

    @Test
    void typicalInputFullyDeterminedChain() {
        List<String> words = List.of("wrt", "wrf", "er", "ett", "rftt");
        assertThat(AlienDictionary.solve(words)).isEqualTo("wertf");
    }

    @Test
    void secondKnownExample() {
        List<String> words = List.of("baa", "abcd", "abca", "cab", "cad");
        assertThat(AlienDictionary.solve(words)).isEqualTo("bdac");
    }

    @Test
    void invalidPrefixOrderingReturnsEmpty() {
        assertThat(AlienDictionary.solve(List.of("abc", "ab"))).isEqualTo("");
    }

    @Test
    void cycleReturnsEmpty() {
        assertThat(AlienDictionary.solve(List.of("a", "b", "c", "a"))).isEqualTo("");
    }

    @Test
    void singleWordUsesOnlyItsLetters() {
        String result = AlienDictionary.solve(List.of("abc"));
        assertThat(result).hasSize(3);
        assertThat(result.chars().mapToObj(c -> (char) c)).containsExactlyInAnyOrder('a', 'b', 'c');
    }

    @Test
    void largerInputLinearChainOfManyLetters() {
        // words[i] and words[i+1] each introduce exactly one new later letter: a<b<c<...
        List<String> words = List.of(
                "a", "ab", "abc", "abcd", "abcde", "abcdef", "abcdefg");
        assertThat(AlienDictionary.solve(words)).isEqualTo("abcdefg");
    }
}
