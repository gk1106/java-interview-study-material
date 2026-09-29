package com.gk.study.dsaproblems.solutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.FindAllAnagramsInString}.
 *
 * <p>A fixed-size window of length {@code p.length()} slides across {@code s}. A
 * {@code HashMap<Character, Integer>} starts holding {@code p}'s character counts; as the window
 * slides, entering a character decrements its count and leaving a character increments it back.
 * The window is an anagram of {@code p} exactly when every count in the map is 0, which is
 * tracked with a single running "unmatched" counter to avoid rescanning the map on every slide.
 * O(s.length()) time, O(alphabet size) space.
 */
public final class FindAllAnagramsInStringSolution {

    private FindAllAnagramsInStringSolution() {
    }

    public static List<Integer> solve(String s, String p) {
        List<Integer> result = new ArrayList<>();
        int windowSize = p.length();
        if (windowSize == 0 || s.length() < windowSize) {
            return result;
        }

        Map<Character, Integer> need = new HashMap<>();
        for (char c : p.toCharArray()) {
            need.merge(c, 1, Integer::sum);
        }
        int unmatched = need.size(); // number of distinct characters not yet at target count

        for (int i = 0; i < s.length(); i++) {
            char enter = s.charAt(i);
            if (need.containsKey(enter)) {
                int updated = need.merge(enter, -1, Integer::sum);
                if (updated == 0) {
                    unmatched--;
                } else if (updated == -1) {
                    unmatched++;
                }
            }

            int windowStart = i - windowSize + 1;
            if (windowStart > 0) {
                char leave = s.charAt(windowStart - 1);
                if (need.containsKey(leave)) {
                    int updated = need.merge(leave, 1, Integer::sum);
                    if (updated == 0) {
                        unmatched--;
                    } else if (updated == 1) {
                        unmatched++;
                    }
                }
            }

            if (windowStart >= 0 && unmatched == 0) {
                result.add(windowStart);
            }
        }
        return result;
    }
}
