package com.gk.study.dsaproblems.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.GroupAnagramCodes}.
 *
 * <p>Every code's sorted-character form is a canonical key shared by all of its anagrams (e.g.
 * "eat" and "tea" both sort to "aet"). Bucket codes into a {@code HashMap<String, List<String>>}
 * keyed by that canonical form, then return the buckets. O(n * k log k) time (k = max code
 * length, from sorting each code), O(n * k) space.
 */
public final class GroupAnagramCodesSolution {

    private GroupAnagramCodesSolution() {
    }

    public static List<List<String>> solve(List<String> codes) {
        Map<String, List<String>> buckets = new HashMap<>();
        for (String code : codes) {
            char[] chars = code.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);
            buckets.computeIfAbsent(key, k -> new ArrayList<>()).add(code);
        }
        return new ArrayList<>(buckets.values());
    }
}
