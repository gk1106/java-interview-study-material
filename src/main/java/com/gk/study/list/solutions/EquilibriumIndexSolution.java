package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.EquilibriumIndex}.
 * Prefix sum, kept as a running total (O(1) space) rather than a materialized array: compute
 * the total sum once, then scan left to right, deriving the right-sum as
 * {@code total - leftSum - current} at each index.
 */
public class EquilibriumIndexSolution {

    public static int solve(int[] arr) {
        long total = 0;
        for (int value : arr) {
            total += value;
        }
        long leftSum = 0;
        for (int i = 0; i < arr.length; i++) {
            long rightSum = total - leftSum - arr[i];
            if (leftSum == rightSum) {
                return i;
            }
            leftSum += arr[i];
        }
        return -1;
    }
}
