package com.gk.study.concurrency.solutions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

/**
 * Reference solution for {@code SimpleThreadPoolExercise}: a fixed set of worker threads pulling
 * from a hand-rolled, {@code synchronized}/{@code wait}/{@code notifyAll}-based task queue -- no
 * {@code java.util.concurrent.Executor} classes used internally. Mirrors
 * {@code ThreadPoolExecutor}'s graceful {@code shutdown()} semantics (topic 6): already-queued
 * tasks still run to completion; no new task is accepted afterward; workers exit once the queue is
 * drained AND shutdown has been requested.
 */
public class SimpleThreadPool {

    private final Deque<Runnable> taskQueue = new ArrayDeque<>();
    private final Object lock = new Object();
    private final Thread[] workers;
    private volatile boolean shutdownRequested = false;

    public SimpleThreadPool(int numThreads) {
        if (numThreads <= 0) {
            throw new IllegalArgumentException("numThreads must be positive");
        }
        workers = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new Thread(this::workerLoop, "simple-pool-worker-" + i);
            workers[i].start();
        }
    }

    private void workerLoop() {
        while (true) {
            Runnable task;
            synchronized (lock) {
                while (taskQueue.isEmpty() && !shutdownRequested) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (taskQueue.isEmpty()) {
                    // shutdownRequested is true here (the while loop only exits otherwise when
                    // the queue is non-empty) -- nothing left to do, this worker can exit.
                    return;
                }
                task = taskQueue.pollFirst();
            }
            runQuietly(task);
        }
    }

    private void runQuietly(Runnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            // A task's own exception must not kill the worker thread (the pool would silently
            // shrink) -- swallow it here; production code would route this to a logger/handler.
        }
    }

    public void submit(Runnable task) {
        synchronized (lock) {
            if (shutdownRequested) {
                throw new IllegalStateException("pool has been shut down");
            }
            taskQueue.addLast(task);
            lock.notifyAll();
        }
    }

    public void shutdown() {
        synchronized (lock) {
            shutdownRequested = true;
            lock.notifyAll(); // wake any idle workers so they observe shutdown and can exit
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        for (Thread worker : workers) {
            long remainingMillis = (deadlineNanos - System.nanoTime()) / 1_000_000;
            if (remainingMillis <= 0) {
                return allTerminated();
            }
            worker.join(remainingMillis);
        }
        return allTerminated();
    }

    private boolean allTerminated() {
        for (Thread worker : workers) {
            if (worker.isAlive()) {
                return false;
            }
        }
        return true;
    }
}
