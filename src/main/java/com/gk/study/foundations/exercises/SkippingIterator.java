package com.gk.study.foundations.exercises;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * E03 [Medium] Wrap a delegate Iterator<T> so it yields every Nth element (1-indexed: the 1st,
 * (1+n)th, (1+2n)th element, ...).
 * Input:  delegate = List.of(10,20,30,40,50,60).iterator(), n = 3
 * Output: yields 10, 40  (elements at positions 1 and 4)
 * Constraint: does not materialize the whole sequence up front; O(1) extra space; n >= 1.
 * Pattern: iterator decorator / composition
 */
public class SkippingIterator<T> implements Iterator<T> {
    private final Iterator<T> delegate;
    private final int n;

    public SkippingIterator(Iterator<T> delegate, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be >= 1");
        }
        this.delegate = delegate;
        this.n = n;
    }

    @Override
    public boolean hasNext() {
        // TODO: true iff the delegate still has at least one more element to take
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public T next() {
        // TODO: if !hasNext() throw NoSuchElementException; otherwise take delegate.next() as
        // the value to return, then advance the delegate up to (n - 1) more times (stopping
        // early if it runs out) to skip ahead before returning the value
        throw new UnsupportedOperationException("TODO");
    }
}
