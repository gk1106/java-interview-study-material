package com.gk.study.set.exercises;

/**
 * H02 [Hard] Design a class that tracks the kth largest element of a growing data stream.
 * Input:  k=3, initial stream=[4,5,8,2], then add(3), add(5), add(10), add(9), add(4)
 * Output of each add(): 4, 5, 5, 8, 8   (the current 3rd-largest value after each addition)
 * Constraint: each {@link #add} call should run in O(log k) time, tracking only k elements at
 * once. Pattern: TreeMap-backed bounded multiset (a plain TreeSet can't hold duplicate values,
 * so this is exactly the structure TreeSet itself is built on internally).
 */
public class KthLargestStream {

    // TODO: add a private TreeMap<Integer, Integer> field (value -> occurrence count) plus
    // fields tracking k and how many elements are currently held in the window.

    /**
     * @param k    how many of the largest elements to track
     * @param nums the initial stream contents, added in order before construction completes
     */
    public KthLargestStream(int k, int[] nums) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Adds {@code val} to the stream and returns the current kth largest element.
     *
     * @param val the new stream value
     * @return the kth largest value among all elements added so far
     */
    public int add(int val) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
