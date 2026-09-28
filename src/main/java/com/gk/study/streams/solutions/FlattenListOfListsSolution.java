package com.gk.study.streams.solutions;

import java.util.List;

/**
 * Solution for {@link com.gk.study.streams.exercises.FlattenListOfLists}.
 *
 * <p>flatMap merges the inner streams into one stream of ints, then distinct + sorted dedupe and
 * order it. O(n log n) time, O(n) space.
 */
public final class FlattenListOfListsSolution {

    private FlattenListOfListsSolution() {
    }

    public static List<Integer> solve(List<List<Integer>> lists) {
        return lists.stream()
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .toList();
    }
}
