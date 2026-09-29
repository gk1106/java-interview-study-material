package com.gk.study.dsaproblems.solutions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.TopKFrequentWordsTieBreak}.
 *
 * <p>Build a frequency map, then push every distinct word onto a min-heap ordered by
 * (frequency ascending, word descending) and cap the heap at size k by polling whenever it
 * overflows. That ordering means the heap always evicts the "worst" candidate for the top-k
 * (least frequent, and alphabetically latest on a tie) first, so what remains is exactly the
 * top-k, worst-first; reversing that popped order yields (frequency descending, word ascending).
 * O(n log k) time, O(n) space for the frequency map.
 */
public final class TopKFrequentWordsTieBreakSolution {

    private TopKFrequentWordsTieBreakSolution() {
    }

    public static List<String> solve(List<String> words, int k) {
        Map<String, Integer> frequencies = new HashMap<>();
        for (String word : words) {
            frequencies.merge(word, 1, Integer::sum);
        }

        Comparator<String> byFrequencyAscThenWordDesc = Comparator
                .comparingInt((String w) -> frequencies.get(w))
                .thenComparing(Comparator.<String>naturalOrder().reversed());

        PriorityQueue<String> minHeap = new PriorityQueue<>(byFrequencyAscThenWordDesc);
        for (String word : frequencies.keySet()) {
            minHeap.offer(word);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        List<String> result = new ArrayList<>(minHeap);
        result.sort(Comparator
                .comparingInt((String w) -> -frequencies.get(w))
                .thenComparing(Comparator.naturalOrder()));
        return result;
    }
}
