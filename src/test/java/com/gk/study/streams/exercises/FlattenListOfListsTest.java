package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FlattenListOfLists} exercise stub. EXPECTED TO FAIL until implemented.
 */
class FlattenListOfListsTest {

    @Test
    void typicalInput() {
        List<Integer> result = FlattenListOfLists.solve(List.of(List.of(3, 1, 2), List.of(2, 4), List.of(5, 1)));
        assertThat(result).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void emptyOuterList() {
        assertThat(FlattenListOfLists.solve(List.of())).isEmpty();
    }

    @Test
    void singleEmptyInnerList() {
        assertThat(FlattenListOfLists.solve(List.of(List.of()))).isEmpty();
    }

    @Test
    void duplicatesWithinOneInnerList() {
        List<Integer> result = FlattenListOfLists.solve(List.of(List.of(4, 4, 4), List.of(1)));
        assertThat(result).containsExactly(1, 4);
    }
}
