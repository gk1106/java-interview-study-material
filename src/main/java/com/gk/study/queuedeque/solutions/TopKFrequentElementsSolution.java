package com.gk.study.queuedeque.solutions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.TopKFrequentElements}.
 * Counts frequencies with a HashMap (O(n)), then keeps a size-bounded min-heap of (value, count)
 * entries ordered by count: after offering an entry, if the heap grew past size k, evict the
 * smallest-count entry. The heap never holds more than k+1 entries at once, so this is
 * O(n log k) total instead of O(n log n) for sorting every distinct value.
 */
public class TopKFrequentElementsSolution {

    public static List<Integer> topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int n : nums) {
            counts.merge(n, 1, Integer::sum);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : minHeap) {
            result.add(entry.getKey());
        }
        return result;
    }
}
