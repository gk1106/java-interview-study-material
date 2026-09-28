package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContainsDuplicateWithinKSolutionTest {

    @Test
    void duplicateWithinWindow() {
        assertThat(ContainsDuplicateWithinKSolution.solve(List.of("A", "B", "C", "A"), 3))
                .isTrue();
    }

    @Test
    void duplicateOutsideWindow() {
        assertThat(ContainsDuplicateWithinKSolution.solve(List.of("A", "B", "C", "A"), 2))
                .isFalse();
    }

    @Test
    void emptyList() {
        assertThat(ContainsDuplicateWithinKSolution.solve(List.of(), 3)).isFalse();
    }

    @Test
    void singleElement() {
        assertThat(ContainsDuplicateWithinKSolution.solve(List.of("A"), 1)).isFalse();
    }

    @Test
    void allSameElementAlwaysDuplicate() {
        assertThat(ContainsDuplicateWithinKSolution.solve(List.of("A", "A", "A"), 0)).isFalse();
        assertThat(ContainsDuplicateWithinKSolution.solve(List.of("A", "A", "A"), 1)).isTrue();
    }

    @Test
    void negativeKThrows() {
        assertThatThrownBy(() -> ContainsDuplicateWithinKSolution.solve(List.of("A"), -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void largerInputNoDuplicateWithinWindow() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ids.add("TXN" + i);
        }
        assertThat(ContainsDuplicateWithinKSolution.solve(ids, 5)).isFalse();
    }
}
