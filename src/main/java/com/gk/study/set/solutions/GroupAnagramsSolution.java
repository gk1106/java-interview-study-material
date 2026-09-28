package com.gk.study.set.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reference solution for {@link com.gk.study.set.exercises.GroupAnagrams}.
 * Canonical-key grouping: sort each word's characters to build a key that's identical for every
 * anagram of a given word, then bucket words into a Map keyed by that canonical form.
 */
public class GroupAnagramsSolution {

    public static List<List<String>> solve(String[] words) {
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
