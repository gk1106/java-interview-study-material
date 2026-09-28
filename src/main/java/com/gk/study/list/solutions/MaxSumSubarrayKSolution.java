package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.MaxSumSubarrayK}.
 * Fixed-size sliding window: compute the first window directly, then slide one step at a time
 * by adding the entering element and subtracting the leaving element (O(1) per step).
 */
public class MaxSumSubarrayKSolution {

    public static int solve(int[] arr, int k) {
        int windowSum = 0;
        for (int i = 0; i < k; i++) {
            windowSum += arr[i];
        }
        int best = windowSum;
        for (int i = k; i < arr.length; i++) {
            windowSum += arr[i] - arr[i - k];
            best = Math.max(best, windowSum);
        }
        return best;
    }
}
