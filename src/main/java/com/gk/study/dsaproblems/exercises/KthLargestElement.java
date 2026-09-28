package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * E09 [Easy] Given a list of transaction amounts and an integer k, return the kth largest
 * amount (1st largest = the maximum).
 * Input: nums=[3,2,1,5,6,4], k=2 &rarr; Output: 5
 * Constraint: 1 &lt;= k &lt;= nums.size(); target O(n log k) time (better than an O(n log n)
 * full sort when k is small).
 * Pattern: min-heap of size k
 * Collections: PriorityQueue
 */
public class KthLargestElement {

    public static int solve(List<Integer> nums, int k) {
        // TODO: maintain a min-heap of size k over the elements seen so far; the heap's root
        // is the kth largest once every element has been processed
        throw new UnsupportedOperationException("TODO");
    }
}
