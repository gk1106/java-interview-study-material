package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Solution for {@link com.gk.study.map.exercises.TopKFrequentElements}.
 *
 * <p>Build a frequency map (O(n)), then maintain a min-heap of size k ordered by frequency,
 * popping the smallest whenever the heap exceeds size k (O(n log k) total) — cheaper than sorting
 * all distinct entries when k is small.
 */
public final class TopKFrequentElementsSolution {

    private TopKFrequentElementsSolution() {
    }

    public static int[] solve(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int num : nums) {
            freq.merge(num, 1, Integer::sum);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>((a, b) -> a.getValue() - b.getValue());
        for (Map.Entry<Integer, Integer> entry : freq.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        int[] result = new int[minHeap.size()];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = minHeap.poll().getKey();
        }
        return result;
    }
}
