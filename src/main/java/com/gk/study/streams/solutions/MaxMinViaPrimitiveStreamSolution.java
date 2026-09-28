package com.gk.study.streams.solutions;

import java.util.stream.IntStream;

/**
 * Solution for {@link com.gk.study.streams.exercises.MaxMinViaPrimitiveStream}.
 *
 * <p>{@code IntStream} works directly on primitive {@code int}s — no boxing into {@code Integer}
 * for every element, unlike {@code Arrays.stream(numbers).boxed()...}. O(n) time, O(1) space.
 */
public final class MaxMinViaPrimitiveStreamSolution {

    private MaxMinViaPrimitiveStreamSolution() {
    }

    public static int[] solve(int[] numbers) {
        if (numbers.length == 0) {
            throw new IllegalArgumentException("numbers must not be empty");
        }
        int min = IntStream.of(numbers).min().orElseThrow();
        int max = IntStream.of(numbers).max().orElseThrow();
        return new int[] {min, max};
    }
}
