package com.gk.study.dsaproblems.exercises;

/**
 * M08 [Medium] Given a sequence of transaction amounts and a window size k, return the maximum
 * amount in every contiguous window of size k as the window slides from left to right.
 * Input: nums=[1,3,-1,-3,5,3,6,7], k=3 &rarr; Output: [3,3,5,5,6,7]
 * Constraint: 1 &lt;= k &lt;= nums.length; O(n) time overall (amortized O(1) per element).
 * Pattern: monotonic (decreasing) deque of indices
 * Collections: Deque (holding indices)
 */
public class SlidingWindowMaxTransaction {

    public static int[] solve(int[] nums, int k) {
        // TODO: implement using an ArrayDeque<Integer> of indices whose values are in
        // decreasing order from front to back; for each new index, pop from the back while its
        // value is >= the back's value, pop from the front if the front index has fallen out of
        // the current window, then push the new index; the front of the deque is always the
        // current window's max once the window is full (index >= k - 1)
        throw new UnsupportedOperationException("TODO");
    }
}
