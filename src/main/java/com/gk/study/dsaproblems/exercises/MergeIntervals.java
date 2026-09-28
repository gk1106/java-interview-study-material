package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * E08 [Easy] Given a list of account-hold intervals as {@code [start, end]} pairs (inclusive),
 * merge all overlapping or touching intervals into the minimal set that covers the same ranges.
 * Input: [[1,3],[2,6],[8,10],[15,18]] &rarr; Output: [[1,6],[8,10],[15,18]]
 * Constraint: intervals may be given in any order; O(n log n) time (dominated by the sort).
 * Pattern: sort + linear scan (greedy merge)
 * Collections: List (sorted in place / into a new result list)
 */
public class MergeIntervals {

    public static List<int[]> solve(List<int[]> intervals) {
        // TODO: sort intervals by start, then walk once, merging into (or starting) the
        // last interval in the result list
        throw new UnsupportedOperationException("TODO");
    }
}
