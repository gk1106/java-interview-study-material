package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link GroupAnagramCodes} exercise stub. EXPECTED TO FAIL until implemented.
 *
 * <p>Group order and within-group order are unspecified, so assertions compare the result as a
 * set of sets rather than relying on any particular ordering.
 */
class GroupAnagramCodesTest {

    private static Set<Set<String>> asSetOfSets(List<List<String>> groups) {
        return groups.stream().map(HashSet::new).collect(Collectors.toSet());
    }

    @Test
    void typicalInput() {
        List<List<String>> result =
                GroupAnagramCodes.solve(List.of("eat", "tea", "tan", "ate", "nat", "bat"));
        assertThat(asSetOfSets(result))
                .isEqualTo(Set.of(
                        Set.of("eat", "tea", "ate"),
                        Set.of("tan", "nat"),
                        Set.of("bat")));
    }

    @Test
    void emptyInput() {
        assertThat(GroupAnagramCodes.solve(List.of())).isEmpty();
    }

    @Test
    void singleCode() {
        List<List<String>> result = GroupAnagramCodes.solve(List.of("abc"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly("abc");
    }

    @Test
    void noAnagramsEachInOwnGroup() {
        List<List<String>> result = GroupAnagramCodes.solve(List.of("abc", "def", "ghi"));
        assertThat(result).hasSize(3);
    }

    @Test
    void emptyStringCodesGroupTogether() {
        List<List<String>> result = GroupAnagramCodes.solve(List.of("", ""));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly("", "");
    }

    @Test
    void largerInputPreservesTotalCount() {
        List<String> codes = List.of(
                "abc", "bca", "cab", "xyz", "zyx", "def", "fed", "aabb", "bbaa", "abab");
        List<List<String>> result = GroupAnagramCodes.solve(codes);
        int totalGrouped = result.stream().mapToInt(List::size).sum();
        assertThat(totalGrouped).isEqualTo(codes.size());
        assertThat(result).hasSize(4); // {abc,bca,cab}, {xyz,zyx}, {def,fed}, {aabb,bbaa,abab}
    }
}
