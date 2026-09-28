package com.gk.study.dsaproblems.solutions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.MergeIntervals}.
 *
 * <p>Sort by start time (O(n log n)); then a single linear scan either extends the last merged
 * interval's end (if the current interval overlaps/touches it) or starts a new merged interval.
 * O(n log n) time overall, O(n) space for the result.
 */
public final class MergeIntervalsSolution {

    private MergeIntervalsSolution() {
    }

    public static List<int[]> solve(List<int[]> intervals) {
        List<int[]> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingInt(interval -> interval[0]));

        List<int[]> merged = new ArrayList<>();
        for (int[] interval : sorted) {
            if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < interval[0]) {
                merged.add(new int[] {interval[0], interval[1]});
            } else {
                int[] last = merged.get(merged.size() - 1);
                last[1] = Math.max(last[1], interval[1]);
            }
        }
        return merged;
    }
}
