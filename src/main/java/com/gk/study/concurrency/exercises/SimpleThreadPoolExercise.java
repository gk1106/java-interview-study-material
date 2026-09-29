package com.gk.study.concurrency.exercises;

import java.util.concurrent.TimeUnit;

/**
 * B01 [Build it yourself] Implement a fixed-size thread pool from scratch: your own worker
 * threads and your own internal task queue (no {@code java.util.concurrent.Executor}/
 * {@code ExecutorService}/{@code BlockingQueue} classes used internally -- a hand-rolled
 * {@code synchronized}/{@code wait}/{@code notify} queue is the point of this exercise).
 * Input:  a pool of 4 worker threads; 100 submit()-ted Runnable tasks, each incrementing a shared
 *         counter.
 * Output: after shutdown() + awaitTermination(), every submitted task has run exactly once (the
 *         counter equals 100); submit() after shutdown() is rejected.
 * Constraint: shutdown() must be GRACEFUL -- already-queued tasks still run to completion; no new
 * task may be accepted after shutdown() is called; awaitTermination(timeout, unit) must return
 * promptly once every worker has exited, and must not exceed the given timeout.
 * Pattern: fixed worker threads + hand-rolled monitor-based task queue
 *
 * See notes/09-multithreading-concurrency/06-executor-framework-thread-pools.md.
 */
public class SimpleThreadPoolExercise {

    // TODO: add fields: an internal FIFO task queue (e.g. java.util.ArrayDeque<Runnable>) guarded
    // by a lock, a fixed array/list of worker Threads, and a volatile "shutdown requested" flag.

    public SimpleThreadPoolExercise(int numThreads) {
        // TODO: implement -- create and start `numThreads` worker threads, each looping: wait for
        // a task (or for shutdown with an empty queue), run it, repeat.
    }

    /**
     * Submits a task for execution by one of the pool's worker threads. Throws
     * {@link IllegalStateException} if called after {@link #shutdown()}.
     */
    public void submit(Runnable task) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Stops accepting new tasks; already-queued tasks are still run to completion by the worker
     * threads before they exit. Does not block -- use {@link #awaitTermination} to wait.
     */
    public void shutdown() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Blocks until either every worker thread has exited or the timeout elapses.
     *
     * @return true if all workers terminated before the timeout, false if the timeout elapsed first
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
