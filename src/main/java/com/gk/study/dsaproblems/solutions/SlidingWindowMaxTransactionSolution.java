package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.SlidingWindowMaxTransaction}.
 *
 * <p>A monotonic deque of indices, kept in decreasing order of {@code nums[index]} from front to
 * back. For each new index: pop from the back while its value is dominated (a smaller-or-equal
 * value stacked before a bigger one can never become the max while the bigger one is still in
 * range), evict the front if it has slid out of the current window, then push the new index. The
 * front is always the current window's max once the window has filled (index &gt;= k - 1). Each
 * index is pushed once and popped at most once, so this is O(n) time overall, O(k) space.
 */
public final class SlidingWindowMaxTransactionSolution {

    private SlidingWindowMaxTransactionSolution() {
    }

    public static int[] solve(int[] nums, int k) {
        int n = nums.length;
        int[] result = new int[n - k + 1];
        Deque<Integer> window = new ArrayDeque<>(); // indices, decreasing values front to back

        for (int i = 0; i < n; i++) {
            while (!window.isEmpty() && nums[window.peekLast()] <= nums[i]) {
                window.pollLast();
            }
            window.offerLast(i);

            if (window.peekFirst() <= i - k) {
                window.pollFirst();
            }
            if (i >= k - 1) {
                result[i - k + 1] = nums[window.peekFirst()];
            }
        }
        return result;
    }
}
