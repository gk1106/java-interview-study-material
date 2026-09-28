package com.gk.study.foundations.exercises;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * E04 [Hard] Implement a fail-safe (snapshot-style) collection: iterator() returns an Iterator
 * over a point-in-time copy of the elements, so later add() calls never throw
 * ConcurrentModificationException and are simply invisible to iterators already in flight
 * (mirrors CopyOnWriteArrayList's iterator).
 * Input:  list.add("a"); Iterator<String> it = list.iterator(); list.add("b"); // during iteration
 * Output: it still yields only "a" (safe to iterate concurrently with add(); no exception thrown).
 * Constraint: add() is O(n) (copies the backing array, like CopyOnWriteArrayList); iterator() is
 * O(1) to create (captures the current snapshot reference, no copy at iterator-creation time);
 * the returned iterator does not support remove().
 * Pattern: copy-on-write / fail-safe iterator
 */
public class SnapshotIterable<T> implements Iterable<T> {
    private volatile Object[] snapshot = new Object[0];

    public synchronized void add(T item) {
        // TODO: create a new array of length snapshot.length + 1, copy every existing element
        // into it, append item at the end, then replace the `snapshot` field with the new array
        // (copy-on-write: never mutate the array `snapshot` currently points to)
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        return snapshot.length;
    }

    @Override
    public Iterator<T> iterator() {
        // TODO: capture the CURRENT `snapshot` array reference into a local variable, then
        // return an Iterator<T> over exactly that array (unaffected by any later add() calls,
        // since add() replaces the field but never mutates the array object itself)
        throw new UnsupportedOperationException("TODO");
    }
}
