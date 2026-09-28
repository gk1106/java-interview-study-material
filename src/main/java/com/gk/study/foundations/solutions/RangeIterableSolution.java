package com.gk.study.foundations.solutions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Reference solution for {@code exercises.RangeIterable}. */
public class RangeIterableSolution implements Iterable<Integer> {
    private final int start;
    private final int end;

    public RangeIterableSolution(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int current = start;

            @Override
            public boolean hasNext() {
                return current < end;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current++;
            }
        };
    }
}
