package com.gk.study.set.solutions;

import java.util.TreeMap;

/**
 * Reference solution for {@link com.gk.study.set.exercises.KthLargestStream}.
 * A plain TreeSet can't hold duplicate values, so this keeps a {@code TreeMap<value, count>} as a
 * bounded multiset of at most k elements — exactly the counted-key structure a TreeSet is itself
 * built on top of internally (TreeSet just fixes every count at 1). Once the window holds more
 * than k elements, the single smallest one is evicted via {@code firstKey()}.
 */
public final class KthLargestStreamSolution {

    private final int k;
    private final TreeMap<Integer, Integer> window = new TreeMap<>();
    private int windowSize = 0;

    public KthLargestStreamSolution(int k, int[] nums) {
        this.k = k;
        for (int n : nums) {
            add(n);
        }
    }

    public int add(int val) {
        window.merge(val, 1, Integer::sum);
        windowSize++;
        if (windowSize > k) {
            int smallestKey = window.firstKey();
            int count = window.get(smallestKey);
            if (count == 1) {
                window.remove(smallestKey);
            } else {
                window.put(smallestKey, count - 1);
            }
            windowSize--;
        }
        return window.firstKey();
    }
}
