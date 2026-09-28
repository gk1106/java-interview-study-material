package com.gk.study.dsaproblems.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.ValidAnagram}.
 *
 * <p>Increment counts for every character in {@code s}, decrement for every character in
 * {@code t}; the strings are anagrams iff every count nets to zero. O(n) time, O(distinct
 * characters) space.
 */
public final class ValidAnagramSolution {

    private ValidAnagramSolution() {
    }

    public static boolean solve(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : s.toCharArray()) {
            counts.merge(c, 1, Integer::sum);
        }
        for (char c : t.toCharArray()) {
            counts.merge(c, -1, Integer::sum);
        }
        for (int count : counts.values()) {
            if (count != 0) {
                return false;
            }
        }
        return true;
    }
}
