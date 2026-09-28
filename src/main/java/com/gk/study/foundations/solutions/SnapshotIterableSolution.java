package com.gk.study.foundations.solutions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Reference solution for {@code exercises.SnapshotIterable}. */
public class SnapshotIterableSolution<T> implements Iterable<T> {
    private volatile Object[] snapshot = new Object[0];

    public synchronized void add(T item) {
        Object[] bigger = new Object[snapshot.length + 1];
        System.arraycopy(snapshot, 0, bigger, 0, snapshot.length);
        bigger[snapshot.length] = item;
        snapshot = bigger;
    }

    public int size() {
        return snapshot.length;
    }

    @Override
    public Iterator<T> iterator() {
        Object[] capturedSnapshot = snapshot; // capture the reference at iterator-creation time
        return new Iterator<>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < capturedSnapshot.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) capturedSnapshot[cursor++];
            }
        };
    }
}
