package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link RemoveDuplicatesSorted} exercise stub. These are EXPECTED TO FAIL until
 * the TODO in {@link RemoveDuplicatesSorted#solve} is implemented — that is intentional.
 */
class RemoveDuplicatesSortedTest {

    @Test
    void removesDuplicatesFromTypicalInput() {
        List<Integer> list = new ArrayList<>(List.of(1, 1, 2, 3, 3));
        int newLength = RemoveDuplicatesSorted.solve(list);
        assertThat(newLength).isEqualTo(3);
        assertThat(list.subList(0, newLength)).containsExactly(1, 2, 3);
    }

    @Test
    void emptyListReturnsZero() {
        List<Integer> list = new ArrayList<>();
        assertThat(RemoveDuplicatesSorted.solve(list)).isEqualTo(0);
    }

    @Test
    void singleElementReturnsOne() {
        List<Integer> list = new ArrayList<>(List.of(5));
        assertThat(RemoveDuplicatesSorted.solve(list)).isEqualTo(1);
    }

    @Test
    void allDuplicatesCollapseToOne() {
        List<Integer> list = new ArrayList<>(List.of(7, 7, 7, 7));
        int newLength = RemoveDuplicatesSorted.solve(list);
        assertThat(newLength).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(7);
    }

    @Test
    void noDuplicatesLeavesLengthUnchanged() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        assertThat(RemoveDuplicatesSorted.solve(list)).isEqualTo(5);
    }

    @Test
    void largerInputHandledCorrectly() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i / 2); // each value repeated twice: 0,0,1,1,2,2,...
        }
        int newLength = RemoveDuplicatesSorted.solve(list);
        assertThat(newLength).isEqualTo(500);
        for (int i = 0; i < newLength; i++) {
            assertThat(list.get(i)).isEqualTo(i);
        }
    }
}
