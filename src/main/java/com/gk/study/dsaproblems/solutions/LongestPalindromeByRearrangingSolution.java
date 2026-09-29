package com.gk.study.dsaproblems.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.LongestPalindromeByRearranging}.
 *
 * <p>A {@code HashMap<Character, Integer>} counts every character. Each character contributes
 * {@code count - (count % 2)} to the palindrome (its pairs, split evenly across both halves); if
 * any character had an odd count, exactly one of those leftover singles can sit in the middle,
 * adding 1 more. O(n) time, O(alphabet size) space.
 */
public final class LongestPalindromeByRearrangingSolution {

    private LongestPalindromeByRearrangingSolution() {
    }

    public static int solve(String s) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : s.toCharArray()) {
            counts.merge(c, 1, Integer::sum);
        }

        int length = 0;
        boolean hasOdd = false;
        for (int count : counts.values()) {
            length += count - (count % 2);
            if (count % 2 == 1) {
                hasOdd = true;
            }
        }
        return hasOdd ? length + 1 : length;
    }
}
