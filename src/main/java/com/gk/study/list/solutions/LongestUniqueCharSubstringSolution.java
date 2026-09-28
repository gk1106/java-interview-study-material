package com.gk.study.list.solutions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reference solution for {@link com.gk.study.list.exercises.LongestUniqueCharSubstring}.
 * Variable-size sliding window: track the last-seen index of each character; when a repeat is
 * found inside the current window, jump {@code left} past the previous occurrence in O(1)
 * amortized (never resetting to 0), keeping the whole scan O(n).
 */
public class LongestUniqueCharSubstringSolution {

    public static int solve(List<Character> chars) {
        Map<Character, Integer> lastSeenIndex = new HashMap<>();
        int left = 0;
        int best = 0;
        for (int right = 0; right < chars.size(); right++) {
            Character c = chars.get(right);
            Integer previousIndex = lastSeenIndex.get(c);
            if (previousIndex != null && previousIndex >= left) {
                left = previousIndex + 1;
            }
            lastSeenIndex.put(c, right);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
