package com.gk.study.dsaproblems.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.LongestSubstringWithoutRepeating}.
 *
 * <p>A variable sliding window [left, right]: a {@code HashMap<Character, Integer>} tracks each
 * character's most recent index. When {@code s.charAt(right)} was last seen inside the current
 * window, jump {@code left} directly to one past that occurrence instead of shrinking one
 * character at a time. O(n) time (each index visited O(1) amortized), O(min(n, alphabet)) space.
 */
public final class LongestSubstringWithoutRepeatingSolution {

    private LongestSubstringWithoutRepeatingSolution() {
    }

    public static int solve(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int longest = 0;
        int left = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            Integer previousIndex = lastSeen.get(c);
            if (previousIndex != null && previousIndex >= left) {
                left = previousIndex + 1;
            }
            lastSeen.put(c, right);
            longest = Math.max(longest, right - left + 1);
        }
        return longest;
    }
}
