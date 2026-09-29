package com.gk.study.concurrency.exercises;

/**
 * E04 [Medium] Implement a fixed-capacity bounded buffer from scratch, using only intrinsic locks
 * and {@code wait}/{@code notify} (no {@code java.util.concurrent} queue classes internally).
 * {@code put(item)} must block while the buffer is full; {@code take()} must block while the
 * buffer is empty.
 * Input:  a buffer of capacity 2; one producer thread calling put(0..999); one consumer thread
 *         calling take() 1000 times.
 * Output: every produced item is eventually consumed exactly once, in FIFO order; put() never
 *         allows more than `capacity` items to be buffered at once; take() never returns from an
 *         empty buffer.
 * Constraint: put()/take() must each be O(1) amortized (excluding time spent blocked); must never
 * deadlock; must never lose or duplicate an item.
 * Pattern: monitor (synchronized) + wait/notifyAll, two-sided bounded buffer
 *
 * See notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md and
 * notes/09-multithreading-concurrency/10-classic-concurrency-problems.md.
 */
public class ProducerConsumerWaitNotify<T> {

    // TODO: add fields: a FIFO-ordered internal store (e.g. java.util.ArrayDeque<T>), the
    // configured capacity, and a lock object to synchronize on.

    public ProducerConsumerWaitNotify(int capacity) {
        // TODO: implement
    }

    /**
     * Blocks while the buffer is full, then adds {@code item} to the back.
     */
    public void put(T item) throws InterruptedException {
        // TODO: implement (synchronized + while-loop wait on "full", notifyAll after adding)
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Blocks while the buffer is empty, then removes and returns the front item (FIFO).
     */
    public T take() throws InterruptedException {
        // TODO: implement (synchronized + while-loop wait on "empty", notifyAll after removing)
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the current number of buffered items (for tests/diagnostics)
     */
    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
