package com.gk.study.list.exercises;

import java.util.Iterator;

/**
 * B01 [Build it yourself] Implement a resizable, array-backed list from scratch.
 * Constraint: {@code add}/{@code get} amortized O(1) / O(1); growth factor ~1.5x
 * ({@code oldCapacity + (oldCapacity >> 1)}), matching real ArrayList; proper bounds checking
 * ({@code IndexOutOfBoundsException} on invalid index); implements {@code Iterable<T>}.
 * Pattern: manual array growth, generic array creation via {@code Object[]} cast
 *
 * See notes/03-list/09-build-it-yourself-mylist.md for the full design discussion.
 */
public class MyArrayListExercise<T> implements Iterable<T> {

    // TODO: add a private Object[] elements field, a size field, and a modCount field.

    /**
     * Appends {@code element} to the end of the list, growing the backing array if needed.
     *
     * @param element the element to append
     * @return true (per the {@code List.add} convention)
     */
    public boolean add(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Inserts {@code element} at {@code index}, shifting subsequent elements right.
     *
     * @param index   insertion index, {@code 0 <= index <= size()}
     * @param element the element to insert
     */
    public void add(int index, T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param index a valid index, {@code 0 <= index < size()}
     * @return the element at {@code index}
     */
    public T get(int index) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Replaces the element at {@code index}.
     *
     * @param index   a valid index, {@code 0 <= index < size()}
     * @param element the new element
     * @return the previous element at {@code index}
     */
    public T set(int index, T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Removes and returns the element at {@code index}, shifting subsequent elements left.
     *
     * @param index a valid index, {@code 0 <= index < size()}
     * @return the removed element
     */
    public T remove(int index) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the number of elements currently stored
     */
    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return true if {@link #size()} is 0
     */
    public boolean isEmpty() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param element the element to search for
     * @return true if some element in the list is equal to {@code element}
     */
    public boolean contains(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param element the element to search for
     * @return the index of the first occurrence of {@code element}, or -1 if not present
     */
    public int indexOf(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterator<T> iterator() {
        // TODO: implement a fail-fast iterator (checks modCount on each next())
        throw new UnsupportedOperationException("TODO");
    }
}
