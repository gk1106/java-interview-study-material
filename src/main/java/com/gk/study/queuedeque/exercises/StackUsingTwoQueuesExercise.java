package com.gk.study.queuedeque.exercises;

import java.util.NoSuchElementException;

/**
 * B03 [Build it yourself] Implement a LIFO stack using only two FIFO queues as internal storage.
 * Input:  push(1); push(2); push(3); pop() -&gt; 3; pop() -&gt; 2
 * Constraint: pick either "costly push, O(1) pop" or "O(1) push, costly pop" -- implement the
 * costly-push version: push is O(n), pop/top are O(1).
 * Pattern: two queues (rotate on push to keep the newest element at the front)
 *
 * See notes/04-queue-deque/06-build-it-yourself-queue-heap.md for the full design discussion.
 */
public class StackUsingTwoQueuesExercise<T> {

    // TODO: add two Queue<T> fields (e.g. java.util.ArrayDeque as a Queue) to use as q1, q2.

    /**
     * Pushes {@code item} so it becomes the new top of the stack.
     */
    public void push(T item) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Removes and returns the current top of the stack.
     *
     * @throws NoSuchElementException if the stack is empty
     */
    public T pop() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the current top of the stack without removing it
     * @throws NoSuchElementException if the stack is empty
     */
    public T top() {
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
