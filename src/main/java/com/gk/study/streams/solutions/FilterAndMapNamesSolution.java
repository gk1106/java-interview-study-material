package com.gk.study.streams.solutions;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.FilterAndMapNames}.
 *
 * <p>filter + map + collect: split each entry, keep first names starting with a vowel, uppercase
 * and sort. O(n log n) time (dominated by the sort), O(n) space.
 */
public final class FilterAndMapNamesSolution {

    private static final String VOWELS = "aeiouAEIOU";

    private FilterAndMapNamesSolution() {
    }

    public static List<String> solve(List<String> fullNames) {
        return fullNames.stream()
                .map(name -> name.split(" ", 2)[0])
                .filter(firstName -> !firstName.isEmpty() && VOWELS.indexOf(firstName.charAt(0)) >= 0)
                .map(firstName -> firstName.toUpperCase(Locale.ROOT))
                .sorted()
                .collect(Collectors.toList());
    }
}
