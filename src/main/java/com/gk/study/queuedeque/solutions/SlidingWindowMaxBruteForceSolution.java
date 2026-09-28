package com.gk.study.queuedeque.solutions;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.SlidingWindowMaxBruteForce}.
 * For each of the {@code n - k + 1} windows, scans all k elements to find the max: O(n*k) time,
 * O(1) extra space. Compare against
 * {@link SlidingWindowMaximumSolution#maxSlidingWindow(int[], int)}, the O(n) monotonic-deque
 * version of the same problem.
 */
public class SlidingWindowMaxBruteForceSolution {

    public static int[] maxSlidingWindowBruteForce(int[] nums, int k) {
        int n = nums.length;
        if (n == 0 || k <= 0) {
            return new int[0];
        }
        int[] result = new int[n - k + 1];
        for (int i = 0; i <= n - k; i++) {
            int max = nums[i];
            for (int j = i + 1; j < i + k; j++) {
                max = Math.max(max, nums[j]);
            }
            result[i] = max;
        }
        return result;
    }
}
