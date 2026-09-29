package com.gk.study.concurrency.examples;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates the double-checked-locking singleton pattern (with the required volatile field) and
 * the initialization-on-demand holder idiom, each proven to construct exactly one instance despite
 * many threads racing to call getInstance() for the first time simultaneously (via a CountDownLatch
 * start gate, not timing luck).
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md
 */
public final class ThreadSafeSingletonDemo {

    private ThreadSafeSingletonDemo() {
    }

    /** Double-checked locking -- the `volatile` on `instance` is required, not decorative. */
    static final class DoubleCheckedSingleton {
        private static volatile DoubleCheckedSingleton instance;

        private DoubleCheckedSingleton() {
        }

        static DoubleCheckedSingleton getInstance() {
            DoubleCheckedSingleton result = instance;
            if (result == null) {
                synchronized (DoubleCheckedSingleton.class) {
                    result = instance;
                    if (result == null) {
                        instance = result = new DoubleCheckedSingleton();
                    }
                }
            }
            return result;
        }
    }

    /** Initialization-on-demand holder idiom -- lazy and thread-safe via class-loading guarantees. */
    static final class HolderSingleton {
        private HolderSingleton() {
        }

        private static final class Holder {
            static final HolderSingleton INSTANCE = new HolderSingleton();
        }

        static HolderSingleton getInstance() {
            return Holder.INSTANCE;
        }
    }

    public static void main(String[] args) throws Exception {
        runSingletonRaceDemo("double-checked locking", DoubleCheckedSingleton::getInstance);
        runSingletonRaceDemo("holder idiom", HolderSingleton::getInstance);
    }

    private static void runSingletonRaceDemo(String label, java.util.function.Supplier<Object> getInstance)
            throws InterruptedException {
        int threadCount = 50;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch allDone = new CountDownLatch(threadCount);
        Set<Object> observedInstances = Collections.synchronizedSet(new CopyOnWriteArraySet<>());

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    try {
                        startGate.await(5, TimeUnit.SECONDS);
                        observedInstances.add(getInstance.get());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            startGate.countDown(); // release all 50 threads at once to genuinely race getInstance()
            boolean finished = allDone.await(5, TimeUnit.SECONDS);
            System.out.println(label + ": all threads finished within bound = " + finished
                    + " -> unique instance count across " + threadCount + " threads = "
                    + observedInstances.size() + " (expected 1)");
        } finally {
            shutdownQuietly(pool);
        }
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
