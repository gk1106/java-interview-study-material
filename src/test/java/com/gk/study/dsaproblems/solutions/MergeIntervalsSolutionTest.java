package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MergeIntervalsSolutionTest {

    @Test
    void typicalInput() {
        List<int[]> result = MergeIntervalsSolution.solve(
                List.of(new int[] {1, 3}, new int[] {2, 6}, new int[] {8, 10}, new int[] {15, 18}));
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly(1, 6);
        assertThat(result.get(1)).containsExactly(8, 10);
        assertThat(result.get(2)).containsExactly(15, 18);
    }

    @Test
    void touchingIntervalsMerge() {
        List<int[]> result =
                MergeIntervalsSolution.solve(List.of(new int[] {1, 4}, new int[] {4, 5}));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly(1, 5);
    }

    @Test
    void emptyInput() {
        assertThat(MergeIntervalsSolution.solve(List.of())).isEmpty();
    }

    @Test
    void singleInterval() {
        List<int[]> result = MergeIntervalsSolution.solve(List.of(new int[] {5, 9}));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly(5, 9);
    }

    @Test
    void unsortedInputStillMergesCorrectly() {
        List<int[]> result = MergeIntervalsSolution.solve(
                List.of(new int[] {15, 18}, new int[] {1, 3}, new int[] {8, 10}, new int[] {2, 6}));
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).containsExactly(1, 6);
    }

    @Test
    void nonOverlappingIntervalsUnchanged() {
        List<int[]> result =
                MergeIntervalsSolution.solve(List.of(new int[] {1, 2}, new int[] {5, 6}));
        assertThat(result).hasSize(2);
    }

    @Test
    void allIntervalsMergeIntoOne() {
        List<int[]> result = MergeIntervalsSolution.solve(
                List.of(new int[] {1, 10}, new int[] {2, 4}, new int[] {3, 6}, new int[] {5, 9}));
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly(1, 10);
    }
}
