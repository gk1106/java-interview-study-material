package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ContainsDuplicateWithinK} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class ContainsDuplicateWithinKTest {

    @Test
    void duplicateWithinWindow() {
        assertThat(ContainsDuplicateWithinK.solve(List.of("A", "B", "C", "A"), 3)).isTrue();
    }

    @Test
    void duplicateOutsideWindow() {
        assertThat(ContainsDuplicateWithinK.solve(List.of("A", "B", "C", "A"), 2)).isFalse();
    }

    @Test
    void emptyList() {
        assertThat(ContainsDuplicateWithinK.solve(List.of(), 3)).isFalse();
    }

    @Test
    void singleElement() {
        assertThat(ContainsDuplicateWithinK.solve(List.of("A"), 1)).isFalse();
    }

    @Test
    void allSameElementAlwaysDuplicate() {
        assertThat(ContainsDuplicateWithinK.solve(List.of("A", "A", "A"), 0)).isFalse();
        assertThat(ContainsDuplicateWithinK.solve(List.of("A", "A", "A"), 1)).isTrue();
    }

    @Test
    void largerInputNoDuplicateWithinWindow() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ids.add("TXN" + i);
        }
        assertThat(ContainsDuplicateWithinK.solve(ids, 5)).isFalse();
    }
}
