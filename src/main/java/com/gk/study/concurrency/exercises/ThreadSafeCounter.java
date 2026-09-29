package com.gk.study.concurrency.exercises;

/**
 * E01 [Easy] A shared counter that must not lose updates when {@code increment()} is called
 * concurrently from many threads. A naive {@code int count; count++;} implementation is a
 * classic lost-update race (read-modify-write is not atomic) -- fix it so no update is ever lost.
 * Input:  20 threads x 1000 increment() calls each, run concurrently.
 * Output: get() == 20000 afterward, every time, regardless of thread interleaving.
 * Constraint: increment() and get() must both be O(1); no update may ever be lost under any
 * interleaving.
 * Pattern: atomic read-modify-write (CAS-based, or synchronized)
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md (race conditions)
 * and notes/09-multithreading-concurrency/05-atomics-cas.md.
 */
public class ThreadSafeCounter {

    // TODO: add a field that supports atomic increment (e.g. java.util.concurrent.atomic.AtomicInteger),
    // or a plain int guarded by synchronized -- either is an acceptable fix.

    /**
     * Atomically increments the counter by one. Must never lose an update under concurrent calls.
     */
    public void increment() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the current counter value
     */
    public int get() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
