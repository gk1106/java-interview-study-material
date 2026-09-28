package com.gk.study.foundations.solutions;

import java.util.List;

/** Reference solution for {@code exercises.BoundedNumberSum}. */
public class BoundedNumberSumSolution {

    public static double sumOf(List<? extends Number> numbers) {
        double total = 0.0;
        for (Number n : numbers) {
            total += n.doubleValue();
        }
        return total;
    }
}
