package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SmallestRangeKListsSolutionTest {

    @Test
    void typicalInputFindsSmallestRange() {
        List<List<Integer>> lists = List.of(
                List.of(4, 10, 15, 24, 26),
                List.of(0, 9, 12, 20),
                List.of(5, 18, 22, 30));

        assertThat(SmallestRangeKListsSolution.solve(lists)).containsExactly(20, 24);
    }

    @Test
    void singleListRangeIsItsFirstElementOnly() {
        List<List<Integer>> lists = List.of(List.of(1, 2, 3));
        assertThat(SmallestRangeKListsSolution.solve(lists)).containsExactly(1, 1);
    }

    @Test
    void identicalOverlappingListsGiveZeroWidthRange() {
        List<List<Integer>> lists = List.of(List.of(1, 5, 9), List.of(1, 5, 9));
        assertThat(SmallestRangeKListsSolution.solve(lists)).containsExactly(1, 1);
    }

    @Test
    void widelySeparatedListsGiveTheOnlyAchievableWidth() {
        // Best achievable pairing is the largest value from list0 (3) with the smallest value
        // from list1 (100): width = 100 - 3 = 97 -- no pairing can do better since every value
        // in list0 is <= 3 and every value in list1 is >= 100.
        List<List<Integer>> lists = List.of(List.of(1, 2, 3), List.of(100, 101, 102));
        int[] result = SmallestRangeKListsSolution.solve(lists);
        assertThat(result).containsExactly(3, 100);
    }
}
