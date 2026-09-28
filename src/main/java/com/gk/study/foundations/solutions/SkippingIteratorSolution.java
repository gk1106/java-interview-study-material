package com.gk.study.foundations.solutions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Reference solution for {@code exercises.SkippingIterator}. */
public class SkippingIteratorSolution<T> implements Iterator<T> {
    private final Iterator<T> delegate;
    private final int n;

    public SkippingIteratorSolution(Iterator<T> delegate, int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be >= 1");
        }
        this.delegate = delegate;
        this.n = n;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        T value = delegate.next();
        for (int i = 1; i < n && delegate.hasNext(); i++) {
            delegate.next();
        }
        return value;
    }
}
