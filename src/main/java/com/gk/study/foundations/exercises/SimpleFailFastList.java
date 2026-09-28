package com.gk.study.foundations.exercises;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * E02 [Medium] Implement a minimal fail-fast Iterator for a custom array-backed list, mirroring
 * how ArrayList detects concurrent structural modification via a modCount field.
 * Input:  list.add(1); list.add(2); Iterator<Integer> it = list.iterator(); list.add(3); it.next();
 * Output: the it.next() call above throws ConcurrentModificationException.
 * Constraint: iterator() itself is O(1); each next()/hasNext() call is O(1); no extra copy of the
 * backing array is made when creating the iterator.
 * Pattern: fail-fast iterator via modCount snapshot
 */
public class SimpleFailFastList<T> implements Iterable<T> {
    private Object[] data = new Object[4];
    private int size = 0;
    private int modCount = 0;

    public void add(T item) {
        if (size == data.length) {
            Object[] bigger = new Object[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, size);
            data = bigger;
        }
        data[size++] = item;
        modCount++;
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO: return a fail-fast Iterator<T> that:
        //  - captures modCount at creation time as expectedModCount
        //  - hasNext() returns true iff cursor < size
        //  - next() must first check modCount == expectedModCount (else throw
        //    ConcurrentModificationException), then throw NoSuchElementException if
        //    !hasNext(), otherwise return data[cursor++]
        throw new UnsupportedOperationException("TODO");
    }
}
