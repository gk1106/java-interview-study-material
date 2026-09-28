package com.gk.study.map.examples;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates that ConcurrentHashMap's {@code merge}/{@code compute} are atomic per key under
 * real concurrent access, using a small, bounded, deterministic thread pool so the demo finishes
 * quickly and never hangs.
 *
 * See notes/06-map/04-concurrenthashmap.md for the internals explanation (CAS on empty bins,
 * synchronized-per-bin on collision, helpTransfer, CounterCell striping, null-key/value ban).
 */
public final class ConcurrentHashMapDemo {

    private static final int THREADS = 4;
    private static final int INCREMENTS_PER_THREAD = 1_000;

    private ConcurrentHashMapDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- merge() is atomic per key: no lost updates under contention ---");
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                        counts.merge("shared-key", 1, Integer::sum);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean finished = latch.await(10, TimeUnit.SECONDS);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        int expected = THREADS * INCREMENTS_PER_THREAD;
        System.out.println("all threads finished within timeout: " + finished);
        System.out.println("expected total = " + expected + ", actual total = " + counts.get("shared-key"));
        System.out.println("lost updates?   " + (counts.get("shared-key") != expected));

        System.out.println();
        System.out.println("--- computeIfAbsent() only ever initializes a key once ---");
        ConcurrentHashMap<String, Integer> initCount = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> singleInit = new ConcurrentHashMap<>();
        ExecutorService pool2 = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch2 = new CountDownLatch(THREADS);
        for (int t = 0; t < THREADS; t++) {
            pool2.submit(() -> {
                try {
                    singleInit.computeIfAbsent("resource", k -> {
                        initCount.merge("initializations", 1, Integer::sum);
                        return "initialized-value";
                    });
                } finally {
                    latch2.countDown();
                }
            });
        }
        latch2.await(10, TimeUnit.SECONDS);
        pool2.shutdown();
        pool2.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("threads racing computeIfAbsent = " + THREADS
                + ", actual initializations = " + initCount.getOrDefault("initializations", 0));

        System.out.println();
        System.out.println("--- null key/value both rejected ---");
        try {
            counts.put(null, 1);
        } catch (NullPointerException e) {
            System.out.println("put(null, 1) threw NullPointerException as expected");
        }
        try {
            counts.put("k", null);
        } catch (NullPointerException e) {
            System.out.println("put(\"k\", null) threw NullPointerException as expected");
        }
    }
}
