package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SmallestRangeKLists} exercise stub. EXPECTED TO FAIL until implemented.
 */
class SmallestRangeKListsTest {

    @Test
    void typicalInputFindsSmallestRange() {
        List<List<Integer>> lists = List.of(
                List.of(4, 10, 15, 24, 26),
                List.of(0, 9, 12, 20),
                List.of(5, 18, 22, 30));

        assertThat(SmallestRangeKLists.solve(lists)).containsExactly(20, 24);
    }

    @Test
    void singleListRangeIsItsFirstElementOnly() {
        List<List<Integer>> lists = List.of(List.of(1, 2, 3));
        assertThat(SmallestRangeKLists.solve(lists)).containsExactly(1, 1);
    }

    @Test
    void identicalOverlappingListsGiveZeroWidthRange() {
        List<List<Integer>> lists = List.of(List.of(1, 5, 9), List.of(1, 5, 9));
        assertThat(SmallestRangeKLists.solve(lists)).containsExactly(1, 1);
    }

    @Test
    void widelySeparatedListsForceALargeRange() {
        // Best achievable pairing is the largest value from list0 (3) with the smallest value
        // from list1 (100): width = 100 - 3 = 97 -- no pairing can do better since every value
        // in list0 is <= 3 and every value in list1 is >= 100.
        List<List<Integer>> lists = List.of(List.of(1, 2, 3), List.of(100, 101, 102));
        int[] result = SmallestRangeKLists.solve(lists);
        assertThat(result[1] - result[0]).isEqualTo(97);
    }
}
