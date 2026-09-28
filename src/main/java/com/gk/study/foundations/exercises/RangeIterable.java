package com.gk.study.foundations.exercises;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * E01 [Easy] Implement Iterable<Integer> over a half-open range [start, end).
 * Input:  new RangeIterable(2, 6)
 * Output: iterating yields 2, 3, 4, 5 (end is exclusive)
 * Constraint: O(1) space beyond the iterator itself; O(1) per next()/hasNext() call; next() must
 * throw NoSuchElementException once exhausted; if start >= end, iteration yields nothing.
 * Pattern: custom Iterable/Iterator
 */
public class RangeIterable implements Iterable<Integer> {
    private final int start;
    private final int end;

    public RangeIterable(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Integer> iterator() {
        // TODO: return an Iterator<Integer> that yields start, start+1, ..., end-1, then
        // throws NoSuchElementException from next() if called again
        throw new UnsupportedOperationException("TODO");
    }
}
