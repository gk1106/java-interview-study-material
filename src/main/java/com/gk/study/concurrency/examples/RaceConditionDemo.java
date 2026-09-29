package com.gk.study.concurrency.examples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reproduces a classic lost-update race condition on a plain (unsynchronized) int counter under
 * real concurrent load, then fixes it with AtomicInteger. The buggy version is real, runnable code
 * (not just a commented-out snippet) because a lost update is a silent wrong-answer bug, not a
 * hang -- unlike an actual deadlock, it's safe to demonstrate directly without risking the demo
 * (or a build) never terminating.
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md
 */
public final class RaceConditionDemo {

    private RaceConditionDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        unsafeCounterDemo();
        atomicCounterDemo();
    }

    /** Deliberately unsynchronized -- count++ is read-modify-write, not atomic. */
    private static final class UnsafeCounter {
        private int count = 0;

        void increment() {
            count++;
        }

        int get() {
            return count;
        }
    }

    private static void unsafeCounterDemo() throws InterruptedException {
        System.out.println("--- Race condition: unsynchronized count++ under contention ---");
        int threadCount = 20;
        int incrementsPerThread = 1000;
        int expected = threadCount * incrementsPerThread;
        UnsafeCounter counter = new UnsafeCounter();
        CountDownLatch done = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < incrementsPerThread; i++) {
                            counter.increment();
                        }
                    } finally {
                        done.countDown();
                    }
                });
            }
            done.await(5, TimeUnit.SECONDS);
        } finally {
            shutdownQuietly(pool);
        }
        int actual = counter.get();
        System.out.println("unsynchronized counter: expected=" + expected + ", actual=" + actual
                + (actual < expected ? " (lost updates reproduced, as expected under real contention)"
                        : " (no loss observed this run -- races are timing-dependent, not guaranteed every run)"));
        System.out.println();
    }

    private static void atomicCounterDemo() throws InterruptedException {
        System.out.println("--- Fix: AtomicInteger, no lost updates ---");
        int threadCount = 20;
        int incrementsPerThread = 1000;
        int expected = threadCount * incrementsPerThread;
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch done = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < incrementsPerThread; i++) {
                            counter.incrementAndGet();
                        }
                    } finally {
                        done.countDown();
                    }
                });
            }
            done.await(5, TimeUnit.SECONDS);
        } finally {
            shutdownQuietly(pool);
        }
        int actual = counter.get();
        System.out.println("AtomicInteger counter: expected=" + expected + ", actual=" + actual
                + " (matches = " + (actual == expected) + ")");
    }

    private static void shutdownQuietly(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
