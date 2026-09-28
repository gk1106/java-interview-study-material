package com.gk.study.streams.solutions;

import java.util.Arrays;

/**
 * Solution for {@link com.gk.study.streams.exercises.CountLongWords}.
 *
 * <p>split on whitespace, filter blanks (handles repeated/leading/trailing spaces), filter by
 * length, count. O(n) time, O(n) space for the split array.
 */
public final class CountLongWordsSolution {

    private CountLongWordsSolution() {
    }

    public static long solve(String sentence, int minLength) {
        return Arrays.stream(sentence.trim().split("\\s+"))
                .filter(word -> !word.isBlank())
                .filter(word -> word.length() > minLength)
                .count();
    }
}
