package com.gk.study.dsaproblems.solutions;

import java.util.List;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.TwoSumSorted}.
 *
 * <p>Two pointers from both ends: if the sum is too small, advance the low pointer (need a
 * bigger value); if too large, retreat the high pointer. O(n) time, O(1) extra space — cheaper
 * than a HashMap-based two-sum because the input is already sorted.
 */
public final class TwoSumSortedSolution {

    private TwoSumSortedSolution() {
    }

    public static int[] solve(List<Integer> sortedAmounts, int target) {
        int lo = 0;
        int hi = sortedAmounts.size() - 1;
        while (lo < hi) {
            int sum = sortedAmounts.get(lo) + sortedAmounts.get(hi);
            if (sum == target) {
                return new int[] {lo, hi};
            } else if (sum < target) {
                lo++;
            } else {
                hi--;
            }
        }
        throw new IllegalArgumentException("No two-sum solution exists for the given input");
    }
}
