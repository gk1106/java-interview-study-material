package com.gk.study.queuedeque.exercises;

/**
 * B01 [Build it yourself] Implement a fixed-capacity circular queue from scratch, backed by a
 * single array (no {@code java.util} collection may be used internally).
 * Input:  MyCircularQueueExercise q = new MyCircularQueueExercise(3);
 *         q.enqueue(1); q.enqueue(2); q.enqueue(3); q.enqueue(4) -&gt; false (full)
 *         q.dequeue() -&gt; true; q.enqueue(4) -&gt; true; q.rear() -&gt; 4
 * Constraint: every operation O(1) time, O(capacity) space.
 * Pattern: circular array (head index + logical count)
 *
 * See notes/04-queue-deque/06-build-it-yourself-queue-heap.md for the full design discussion.
 */
public class MyCircularQueueExercise {

    // TODO: add an int[] data field sized to capacity, plus head and count fields.

    public MyCircularQueueExercise(int capacity) {
        // TODO: implement
    }

    /**
     * @return true if the value was added, false if the queue was already full
     */
    public boolean enqueue(int value) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return true if an element was removed, false if the queue was already empty
     */
    public boolean dequeue() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the front element, or -1 if the queue is empty
     */
    public int front() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the rear (last-added) element, or -1 if the queue is empty
     */
    public int rear() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isEmpty() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isFull() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
