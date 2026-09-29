package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FindAllAnagramsInStringSolutionTest {

    @Test
    void typicalInput() {
        assertThat(FindAllAnagramsInStringSolution.solve("cbaebabacd", "abc"))
                .containsExactly(0, 6);
    }

    @Test
    void noMatches() {
        assertThat(FindAllAnagramsInStringSolution.solve("abcdef", "xyz")).isEmpty();
    }

    @Test
    void pLongerThanSReturnsEmpty() {
        assertThat(FindAllAnagramsInStringSolution.solve("ab", "abc")).isEmpty();
    }

    @Test
    void entireStringIsAnAnagram() {
        assertThat(FindAllAnagramsInStringSolution.solve("cba", "abc")).containsExactly(0);
    }

    @Test
    void overlappingMatches() {
        assertThat(FindAllAnagramsInStringSolution.solve("aaaaa", "aa")).containsExactly(0, 1, 2, 3);
    }

    @Test
    void largerInputRepeatingPattern() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb.append("abc");
        }
        // the string is periodic with period 3, so EVERY 3-character window (not just those
        // aligned to a multiple of 3) is some rotation of "abc" ("abc"/"bca"/"cab"), and all
        // three rotations are anagrams of "cab" -> every one of the (n - 3 + 1) windows matches
        String s = sb.toString();
        var result = FindAllAnagramsInStringSolution.solve(s, "cab");
        assertThat(result).hasSize(s.length() - 3 + 1);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualTo(i);
        }
    }
}
