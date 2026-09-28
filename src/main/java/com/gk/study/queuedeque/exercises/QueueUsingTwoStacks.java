package com.gk.study.queuedeque.exercises;

import java.util.NoSuchElementException;

/**
 * E02 [Easy] Implement a FIFO queue using only two stacks (LIFO structures) as the internal
 * storage -- no other collection may be used to hold the elements.
 * Input:  enqueue(1); enqueue(2); enqueue(3); dequeue() -&gt; 1; dequeue() -&gt; 2
 * Constraint: enqueue O(1) amortized, dequeue O(1) amortized (each element moves between the two
 * stacks at most once over its lifetime).
 * Pattern: two stacks (amortized cost transfer)
 */
public class QueueUsingTwoStacks<T> {

    // TODO: add two Deque<T> fields to use as stacks (e.g. inStack, outStack).

    /**
     * Adds {@code item} to the back of the queue.
     */
    public void enqueue(T item) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Removes and returns the item at the front of the queue.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    public T dequeue() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the item at the front of the queue without removing it
     * @throws NoSuchElementException if the queue is empty
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
