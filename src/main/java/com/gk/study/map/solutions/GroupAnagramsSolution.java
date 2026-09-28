package com.gk.study.map.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.map.exercises.GroupAnagrams}.
 *
 * <p>Canonical-key bucketing: each word maps to its sorted-character form, which is identical for
 * every anagram of that word. O(n * k log k) time (k = average word length), O(n * k) space.
 */
public final class GroupAnagramsSolution {

    private GroupAnagramsSolution() {
    }

    public static List<List<String>> solve(List<String> words) {
        Map<String, List<String>> groups = new HashMap<>();
        for (String word : words) {
            char[] chars = word.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(word);
        }
        return new ArrayList<>(groups.values());
    }
}
