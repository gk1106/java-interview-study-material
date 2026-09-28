package com.gk.study.foundations.solutions;

/** Reference solution for {@code exercises.MaxOfThree}. */
public class MaxOfThreeSolution {

    public static <T extends Comparable<T>> T max(T a, T b, T c) {
        T largest = a;
        if (b.compareTo(largest) > 0) {
            largest = b;
        }
        if (c.compareTo(largest) > 0) {
            largest = c;
        }
        return largest;
    }
}
