package com.gk.study.set.exercises;

import java.util.Iterator;

/**
 * B01 [Build it yourself] Implement a hash set from scratch, backed by your own bucket array
 * (separate chaining) rather than delegating to {@code java.util.HashMap}.
 * Constraint: initial capacity 16; resize (double capacity, rehash every entry) once
 * {@code size > capacity * 0.75}; average O(1) {@code add}/{@code remove}/{@code contains}.
 * Pattern: separate-chaining bucket array, hash spreading, load-factor-triggered resize
 *
 * See notes/05-set/01-hashset-linkedhashset-treeset.md for the full design discussion.
 */
public class MyHashSetExercise<T> implements Iterable<T> {

    // TODO: add a private Node<T>[] buckets field (array of chained nodes) and a size field.
    // A private static Node<T> class with (T value, Node<T> next) is a good building block.

    /**
     * Adds {@code element} to the set if not already present.
     *
     * @param element the element to add (implementations should support one {@code null})
     * @return true if the element was newly added, false if it was already present
     */
    public boolean add(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Removes {@code element} from the set.
     *
     * @param element the element to remove
     * @return true if the element was present and removed, false otherwise
     */
    public boolean remove(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param element the element to search for
     * @return true if the set currently contains an element equal to {@code element}
     */
    public boolean contains(T element) {
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

    @Override
    public Iterator<T> iterator() {
        // TODO: implement — walk the bucket array, then each bucket's chain
        throw new UnsupportedOperationException("TODO");
    }
}
