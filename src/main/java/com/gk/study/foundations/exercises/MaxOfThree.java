package com.gk.study.foundations.exercises;

/**
 * E01 [Easy] Generic method returning the largest of three Comparable values.
 * Input:  max(3, 7, 5)
 * Output: 7
 * Input:  max("pear", "apple", "banana")
 * Output: "pear"
 * Constraint: O(1) time; must work for ANY type that implements Comparable<T>, not just Integer/String.
 * Pattern: bounded type parameter
 */
public class MaxOfThree {

    public static <T extends Comparable<T>> T max(T a, T b, T c) {
        // TODO: return the largest of a, b, c using compareTo (no casting to Integer/String allowed)
        throw new UnsupportedOperationException("TODO");
    }
}
