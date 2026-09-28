package com.gk.study.set.solutions;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference solution for {@link com.gk.study.set.exercises.LongestConsecutiveSequence}.
 * O(n): put everything in a HashSet, then only expand a run starting at {@code n} when
 * {@code n - 1} is absent from the set. That guard guarantees every element is consumed by the
 * inner expansion loop at most once across the whole algorithm, keeping total work O(n) instead
 * of O(n^2).
 */
public class LongestConsecutiveSequenceSolution {

    public static int solve(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums) {
            set.add(n);
        }

        int longest = 0;
        for (int n : set) {
            if (!set.contains(n - 1)) { // true start of a run
                int length = 1;
                while (set.contains(n + length)) {
                    length++;
                }
                longest = Math.max(longest, length);
            }
        }
        return longest;
    }
}
