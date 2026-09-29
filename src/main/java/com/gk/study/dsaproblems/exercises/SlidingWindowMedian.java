package com.gk.study.dsaproblems.exercises;

/**
 * H02 [Hard] Given an array of numbers and a window size k, return the median of every
 * contiguous window of size k as the window slides from left to right.
 * Input: nums=[1,3,-1,-3,5,3,6,7], k=3 &rarr; Output: [1.0,-1.0,-1.0,3.0,5.0,6.0]
 * Constraint: 1 &lt;= k &lt;= nums.length; target O(n log k) time.
 * Pattern: two heaps (as in H01) plus lazy deletion, since a heap cannot remove an arbitrary
 * element in O(log n) directly — instead, mark a value as "pending removal" and only actually
 * discard it once it surfaces at the top of a heap
 * Collections: 2x PriorityQueue + HashMap (pending-removal counts)
 */
public class SlidingWindowMedian {

    public static double[] solve(int[] nums, int k) {
        // TODO: implement using the two-heap median structure from MedianOfDataStream, extended
        // with a HashMap<Integer, Integer> of pending-removal counts: when a value slides out of
        // the window, increment its pending-removal count instead of removing it immediately;
        // before trusting either heap's top (for rebalancing or for reading the median), "prune"
        // by popping-and-discarding any top value that still has a pending removal outstanding
        throw new UnsupportedOperationException("TODO");
    }
}
