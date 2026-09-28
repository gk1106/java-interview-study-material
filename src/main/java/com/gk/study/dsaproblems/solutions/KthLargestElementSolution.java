package com.gk.study.dsaproblems.solutions;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.KthLargestElement}.
 *
 * <p>Maintain a min-heap capped at size k: offer every element, and whenever the heap exceeds
 * size k, poll (discard) the smallest. After processing all elements, the heap holds exactly the
 * k largest values, and its root (the smallest of those k) is the kth largest overall. O(n log k)
 * time, O(k) space — cheaper than sorting the whole input when k is small.
 */
public final class KthLargestElementSolution {

    private KthLargestElementSolution() {
    }

    public static int solve(List<Integer> nums, int k) {
        if (k < 1 || k > nums.size()) {
            throw new IllegalArgumentException("k must be between 1 and nums.size()");
        }
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(k);
        for (int num : nums) {
            minHeap.offer(num);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }
        return minHeap.peek();
    }
}
