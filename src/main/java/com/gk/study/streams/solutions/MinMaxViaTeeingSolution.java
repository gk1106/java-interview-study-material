package com.gk.study.streams.solutions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.MinMaxViaTeeing}.
 *
 * <p>{@code Collectors.teeing} fans the same stream out to two downstream collectors
 * (minBy/maxBy) and merges their results in one pass — no need to stream the list twice.
 * O(n) time, O(1) extra space.
 */
public final class MinMaxViaTeeingSolution {

    public record MinMax(int min, int max) {
    }

    private MinMaxViaTeeingSolution() {
    }

    public static MinMax solve(List<Integer> numbers) {
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("numbers must not be empty");
        }
        return numbers.stream()
                .collect(Collectors.teeing(
                        Collectors.minBy(Comparator.naturalOrder()),
                        Collectors.maxBy(Comparator.naturalOrder()),
                        (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())));
    }
}
