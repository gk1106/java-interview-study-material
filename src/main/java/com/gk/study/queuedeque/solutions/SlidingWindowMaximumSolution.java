package com.gk.study.queuedeque.solutions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.SlidingWindowMaximum}.
 * Maintains a deque of indices whose corresponding values are strictly decreasing front to back
 * -- so the front index always holds the current window's maximum. Before adding index i, pop
 * every trailing index whose value is smaller than nums[i] (they can never be the max again
 * while i is in the window); then drop the front index once it slides out of the window (its
 * position is &lt;= i - k). Each index is pushed once and popped at most once, so the whole scan
 * is O(n) despite computing a max for every window.
 */
public class SlidingWindowMaximumSolution {

    public static int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        if (n == 0 || k <= 0) {
            return new int[0];
        }
        int[] result = new int[n - k + 1];
        Deque<Integer> indices = new ArrayDeque<>(); // holds indices, values strictly decreasing

        for (int i = 0; i < n; i++) {
            while (!indices.isEmpty() && nums[indices.peekLast()] < nums[i]) {
                indices.pollLast();
            }
            indices.offerLast(i);

            if (indices.peekFirst() <= i - k) {
                indices.pollFirst();
            }

            if (i >= k - 1) {
                result[i - k + 1] = nums[indices.peekFirst()];
            }
        }
        return result;
    }
}
