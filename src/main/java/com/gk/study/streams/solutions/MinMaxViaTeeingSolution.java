package com.gk.study.streams.solutions;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
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
        // javac cannot unify T across three nested generic calls (minBy/maxBy/teeing) when they
        // are all inlined in one expression — binding each Collector to an explicit, concrete
        // type first (Collector<Integer, ?, Optional<Integer>>) gives the compiler a fixed target
        // type to check teeing's arguments against, instead of an inference variable per call.
        Collector<Integer, ?, Optional<Integer>> minCollector = Collectors.minBy(Comparator.naturalOrder());
        Collector<Integer, ?, Optional<Integer>> maxCollector = Collectors.maxBy(Comparator.naturalOrder());
        return numbers.stream()
                .collect(Collectors.teeing(
                        minCollector,
                        maxCollector,
                        (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())));
    }
}
