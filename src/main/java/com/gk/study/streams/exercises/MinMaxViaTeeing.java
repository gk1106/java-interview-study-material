package com.gk.study.streams.exercises;

import java.util.List;

/**
 * M03 [Medium] Given a non-empty list of integers, compute the minimum and maximum in a single
 * pass using {@code Collectors.teeing} (Java 12+).
 * Input:  [5,3,8,1,9,2]
 * Output: MinMax[min=1, max=9]
 * Constraint: throw IllegalArgumentException on an empty list.
 * Pattern: Collectors.teeing(minBy, maxBy, combiner)
 */
public class MinMaxViaTeeing {

    public record MinMax(int min, int max) {
    }

    /**
     * @param numbers non-empty list of integers
     * @return the min and max, computed in one pass over the stream
     * @throws IllegalArgumentException if {@code numbers} is empty
     */
    public static MinMax solve(List<Integer> numbers) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
