package com.gk.study.foundations.exercises;

import java.util.List;

/**
 * E02 [Easy] Sum a list of any Number subtype (Integer, Double, Long, ...) as a double.
 * Input:  sumOf(List.of(1, 2.5, 3L))
 * Output: 6.5
 * Constraint: O(n) time, O(1) extra space. Must accept List<Integer>, List<Double>, etc. — a
 * bounded wildcard producer, not an exact List<Number>.
 * Pattern: bounded wildcard (producer / "extends")
 */
public class BoundedNumberSum {

    public static double sumOf(List<? extends Number> numbers) {
        // TODO: accumulate numbers.get(i).doubleValue() over the list
        throw new UnsupportedOperationException("TODO");
    }
}
