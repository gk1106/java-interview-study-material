package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link GroupAnagrams} exercise stub. EXPECTED TO FAIL until implemented.
 */
class GroupAnagramsTest {

    @Test
    void typicalInput() {
        List<List<String>> groups =
                GroupAnagrams.solve(List.of("eat", "tea", "tan", "ate", "nat", "bat"));
        assertThat(groups).hasSize(3);
        assertThat(groups).anySatisfy(g -> assertThat(g).containsExactlyInAnyOrder("eat", "tea", "ate"));
        assertThat(groups).anySatisfy(g -> assertThat(g).containsExactlyInAnyOrder("tan", "nat"));
        assertThat(groups).anySatisfy(g -> assertThat(g).containsExactlyInAnyOrder("bat"));
    }

    @Test
    void emptyInput() {
        assertThat(GroupAnagrams.solve(List.of())).isEmpty();
    }

    @Test
    void allDistinctWords() {
        List<List<String>> groups = GroupAnagrams.solve(List.of("abc", "def", "ghi"));
        assertThat(groups).hasSize(3);
    }

    @Test
    void allAnagramsOfEachOther() {
        List<List<String>> groups = GroupAnagrams.solve(List.of("abc", "bca", "cab"));
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).containsExactlyInAnyOrder("abc", "bca", "cab");
    }

    @Test
    void singleEmptyStringGroupsWithOtherEmptyStrings() {
        List<List<String>> groups = GroupAnagrams.solve(List.of("", ""));
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).containsExactly("", "");
    }
}
