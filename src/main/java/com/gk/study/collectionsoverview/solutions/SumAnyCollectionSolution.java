package com.gk.study.collectionsoverview.solutions;

import java.util.Collection;

/**
 * Reference solution for E02 (see exercises.SumAnyCollection).
 */
public class SumAnyCollectionSolution {

    public static double sum(Collection<? extends Number> numbers) {
        double total = 0.0;
        for (Number n : numbers) {
            total += n.doubleValue();
        }
        return total;
    }
}
