package com.gk.study.concurrency.exercises;

/**
 * B02 [Build it yourself] Implement a generic, fixed-capacity blocking queue from scratch, using
 * only intrinsic locks and {@code wait}/{@code notify} internally (no
 * {@code java.util.concurrent.BlockingQueue}/{@code Semaphore}/etc.). {@code put(item)} blocks
 * while the queue is full; {@code take()} blocks while the queue is empty. FIFO order.
 * Input:  a queue of capacity 3; put(1), put(2), put(3) all succeed immediately; put(4) blocks
 *         until a take() frees a slot; take() returns 1, 2, 3, 4 in that order.
 * Output: FIFO order preserved; put() never allows more than `capacity` items to be queued at
 *         once; take() never returns from an empty queue; both correctly block/unblock other
 *         threads.
 * Constraint: put()/take()/size()/isEmpty()/isFull() must each be O(1) amortized (excluding
 * blocked time); must never deadlock; must never lose, duplicate, or reorder an item.
 * Pattern: wait/notify-based ring buffer / bounded FIFO
 *
 * See notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md and
 * notes/09-multithreading-concurrency/10-classic-concurrency-problems.md.
 */
public class BoundedBlockingQueueExercise<T> {

    // TODO: add fields: an internal FIFO store (e.g. java.util.ArrayDeque<T>), the configured
    // capacity, and a lock object to synchronize on.

    public BoundedBlockingQueueExercise(int capacity) {
        // TODO: implement
    }

    /**
     * Blocks while the queue is full, then adds {@code item} to the back.
     */
    public void put(T item) throws InterruptedException {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Blocks while the queue is empty, then removes and returns the front item (FIFO).
     */
    public T take() throws InterruptedException {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the current number of queued items
     */
    public int size() {
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
