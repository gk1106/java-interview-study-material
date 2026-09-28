package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GroupAnagramsSolutionTest {

    @Test
    void typicalInputGroupsAnagramsTogether() {
        String[] words = {"eat", "tea", "tan", "ate", "nat", "bat"};
        List<List<String>> groups = GroupAnagramsSolution.solve(words);

        assertThat(groups).hasSize(3);
        assertThat(groups).anySatisfy(g -> assertThat(g).containsExactlyInAnyOrder("eat", "tea", "ate"));
        assertThat(groups).anySatisfy(g -> assertThat(g).containsExactlyInAnyOrder("tan", "nat"));
        assertThat(groups).anySatisfy(g -> assertThat(g).containsExactlyInAnyOrder("bat"));
    }

    @Test
    void emptyArrayReturnsEmptyList() {
        assertThat(GroupAnagramsSolution.solve(new String[0])).isEmpty();
    }

    @Test
    void singleWordReturnsSingleGroup() {
        List<List<String>> groups = GroupAnagramsSolution.solve(new String[] {"solo"});
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).containsExactly("solo");
    }

    @Test
    void noAnagramsEachWordInOwnGroup() {
        String[] words = {"abc", "def", "ghi"};
        assertThat(GroupAnagramsSolution.solve(words)).hasSize(3);
    }

    @Test
    void emptyStringWordsGroupTogether() {
        List<List<String>> groups = GroupAnagramsSolution.solve(new String[] {"", ""});
        assertThat(groups).hasSize(1);
        assertThat(groups.get(0)).containsExactly("", "");
    }
}
