package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.map.exercises.LongestSubstringKDistinct}.
 *
 * <p>Sliding window with a HashMap of character counts for the current window. Expand the right
 * pointer; whenever the window has more than k distinct characters, shrink from the left until
 * valid again. Each pointer only ever moves forward, so total work is O(n).
 */
public final class LongestSubstringKDistinctSolution {

    private LongestSubstringKDistinctSolution() {
    }

    public static int solve(String s, int k) {
        if (k == 0 || s.isEmpty()) {
            return 0;
        }

        Map<Character, Integer> windowCounts = new HashMap<>();
        int left = 0;
        int maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            windowCounts.merge(c, 1, Integer::sum);

            while (windowCounts.size() > k) {
                char leftChar = s.charAt(left);
                int newCount = windowCounts.merge(leftChar, -1, Integer::sum);
                if (newCount == 0) {
                    windowCounts.remove(leftChar);
                }
                left++;
            }

            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }
}
