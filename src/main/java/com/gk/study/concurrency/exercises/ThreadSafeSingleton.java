package com.gk.study.concurrency.exercises;

/**
 * E02 [Easy] Implement a lazily-initialized, thread-safe singleton using double-checked locking.
 * Input:  50 threads all call getInstance() for the first time, concurrently (released together
 *         via a start gate, not by chance timing).
 * Output: every thread observes the SAME instance -- exactly one instance is ever constructed.
 * Constraint: construction must be lazy (deferred to first call, not eager at class load), and
 * getInstance() must be O(1) after the first call (a single volatile read on the fast path, no
 * lock re-acquired every time).
 * Pattern: double-checked locking (volatile + synchronized)
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md and
 * notes/09-multithreading-concurrency/03-volatile-and-java-memory-model.md for why the volatile
 * field is required, not optional.
 */
public final class ThreadSafeSingleton {

    // TODO: add a private static volatile ThreadSafeSingleton field to hold the instance.
    // The `volatile` is REQUIRED -- without it, a thread could observe a non-null but
    // partially-constructed instance due to reordering (see the volatile/JMM notes above).

    private ThreadSafeSingleton() {
    }

    /**
     * @return the single shared instance, lazily constructed on first call, thread-safely
     */
    public static ThreadSafeSingleton getInstance() {
        // TODO: implement double-checked locking:
        //   1. first check outside any lock (fast path once initialized)
        //   2. if null, synchronize on the class object
        //   3. re-check inside the lock (second check -- another thread may have just constructed it)
        //   4. construct and assign only if still null
        throw new UnsupportedOperationException("TODO");
    }
}
