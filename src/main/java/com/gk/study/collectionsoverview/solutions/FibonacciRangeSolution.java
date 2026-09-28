package com.gk.study.collectionsoverview.solutions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reference solution for E03 (see exercises.FibonacciRange).
 */
public class FibonacciRangeSolution implements Iterable<Integer> {

    private final int bound;

    public FibonacciRangeSolution(int bound) {
        this.bound = bound;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            // a = next value to emit, b = the one after that (classic two-variable Fibonacci)
            private int a = 0;
            private int b = 1;

            @Override
            public boolean hasNext() {
                return a <= bound;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int result = a;
                int newB = a + b;
                a = b;
                b = newB;
                return result;
            }
        };
    }
}
