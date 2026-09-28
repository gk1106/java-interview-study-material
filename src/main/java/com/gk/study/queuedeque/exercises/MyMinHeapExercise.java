package com.gk.study.queuedeque.exercises;

import java.util.NoSuchElementException;

/**
 * B02 [Build it yourself] Implement a generic binary min-heap from scratch: sift-up on insert,
 * sift-down on extractMin, backed by a resizable array-like structure.
 * Input:  insert(5); insert(3); insert(8); insert(1); extractMin() -&gt; 1; extractMin() -&gt; 3
 * Constraint: insert O(log n), extractMin O(log n), peek O(1).
 * Pattern: binary heap (array representation, indices 2i+1 / 2i+2)
 *
 * See notes/04-queue-deque/06-build-it-yourself-queue-heap.md for the full design discussion.
 */
public class MyMinHeapExercise<T extends Comparable<T>> {

    // TODO: add a backing store for the heap (e.g. a List<T> or Object[] + size).

    /**
     * Inserts {@code value}, then restores the heap property by sifting it up.
     */
    public void insert(T value) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Removes and returns the smallest element, then restores the heap property by sifting the
     * replacement root down.
     *
     * @throws NoSuchElementException if the heap is empty
     */
    public T extractMin() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the smallest element without removing it
     * @throws NoSuchElementException if the heap is empty
     */
    public T peek() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isEmpty() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
