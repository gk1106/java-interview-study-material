package com.gk.study.set.exercises;

import java.util.List;

/**
 * H01 [Hard] Given k sorted (ascending) lists of integers, find the smallest range [a, b] (a
 * closed interval) that contains at least one number from each of the k lists.
 * Input:  [[4,10,15,24,26], [0,9,12,20], [5,18,22,30]]  → Output: [20, 24]
 * Constraint: O(N log k) time, where N is the total element count across all lists; use a
 * TreeSet of size <= k to track the current "pointer" element from each list.
 * Pattern: TreeSet of pointers, sliding min/max
 */
public class SmallestRangeKLists {

    /**
     * @param lists k non-empty lists, each already sorted in ascending order
     * @return a 2-element array {@code [rangeStart, rangeEnd]} for the smallest qualifying range
     *         (if multiple ranges tie for smallest, any one of them is acceptable)
     */
    public static int[] solve(List<List<Integer>> lists) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
