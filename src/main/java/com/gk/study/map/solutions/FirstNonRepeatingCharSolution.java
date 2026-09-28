package com.gk.study.map.solutions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.map.exercises.FirstNonRepeatingChar}.
 *
 * <p>Two passes: build a frequency map (a {@link LinkedHashMap} here purely to make the demo
 * deterministic to read; a plain HashMap would also work since we re-scan the original string
 * for order, not the map), then scan the original string in order and return the first index
 * whose character has frequency 1. O(n) time.
 */
public final class FirstNonRepeatingCharSolution {

    private FirstNonRepeatingCharSolution() {
    }

    public static int solve(String s) {
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (int i = 0; i < s.length(); i++) {
            freq.merge(s.charAt(i), 1, Integer::sum);
        }
        for (int i = 0; i < s.length(); i++) {
            if (freq.get(s.charAt(i)) == 1) {
                return i;
            }
        }
        return -1;
    }
}
