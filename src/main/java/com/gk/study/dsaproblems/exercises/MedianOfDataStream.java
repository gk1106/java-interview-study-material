package com.gk.study.dsaproblems.exercises;

/**
 * H01 [Hard] Support two operations as numbers arrive one at a time from a stream:
 * {@code addNum(int)} adds a number, and {@code findMedian()} returns the median of every number
 * seen so far.
 * Input: addNum(1); addNum(2); findMedian() &rarr; 1.5; addNum(3); findMedian() &rarr; 2.0
 * Constraint: O(log n) per {@code addNum}, O(1) per {@code findMedian}.
 * Pattern: two heaps (max-heap for the lower half, min-heap for the upper half), kept balanced
 * in size so their tops straddle the median
 * Collections: 2x PriorityQueue
 */
public class MedianOfDataStream {

    // TODO: add two PriorityQueue<Integer> fields: a max-heap for the smaller (lower) half of
    // the numbers seen so far, and a min-heap for the larger (upper) half

    public MedianOfDataStream() {
        // TODO: initialize both heaps
    }

    /** Adds {@code num} to the running data stream. */
    public void addNum(int num) {
        // TODO: implement by pushing to one heap then rebalancing so the heaps' sizes never
        // differ by more than 1 and every element in the lower (max) heap is <= every element in
        // the upper (min) heap
        throw new UnsupportedOperationException("TODO");
    }

    /** @return the median of every number added so far */
    public double findMedian() {
        // TODO: implement — if the heaps are equal in size, average their two tops; otherwise
        // return the top of whichever heap holds one more element
        throw new UnsupportedOperationException("TODO");
    }
}
