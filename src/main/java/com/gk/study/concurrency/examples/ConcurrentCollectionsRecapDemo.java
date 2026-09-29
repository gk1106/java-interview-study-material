package com.gk.study.concurrency.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A recap demo tying together the thread-safe collections covered in earlier modules:
 * ConcurrentHashMap (module 06), CopyOnWriteArrayList (module 03), plus ConcurrentSkipListMap
 * (new here) and a Collections.synchronizedList compound-action race contrasted with a correctly
 * externally-locked fix. The BlockingQueue family (module 04) is exercised directly in
 * ProducerConsumerBlockingQueueDemo rather than repeated here.
 *
 * See notes/09-multithreading-concurrency/09-concurrent-collections-recap.md
 */
public final class ConcurrentCollectionsRecapDemo {

    private ConcurrentCollectionsRecapDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        concurrentHashMapDemo();
        copyOnWriteArrayListDemo();
        concurrentSkipListMapDemo();
        synchronizedWrapperCompoundActionRaceDemo();
    }

    private static void concurrentHashMapDemo() throws InterruptedException {
        System.out.println("--- ConcurrentHashMap: no lost updates under concurrent writers ---");
        int threadCount = 8;
        int putsPerThread = 500;
        Map<String, Integer> map = new ConcurrentHashMap<>();
        CountDownLatch done = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                int threadIndex = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < putsPerThread; i++) {
                            map.put("t" + threadIndex + "-k" + i, i);
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
        int expected = threadCount * putsPerThread;
        System.out.println("ConcurrentHashMap: " + threadCount + " threads x " + putsPerThread
                + " puts each -> size=" + map.size() + " (expected " + expected + ", no lost updates = "
                + (map.size() == expected) + ")");
        System.out.println();
    }

    private static void copyOnWriteArrayListDemo() throws InterruptedException {
        System.out.println("--- CopyOnWriteArrayList: safe concurrent iteration during writes ---");
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(i);
        }
        AtomicInteger concurrentModificationCount = new AtomicInteger(0);
        int writerCount = 4;
        CountDownLatch done = new CountDownLatch(writerCount + 1);

        ExecutorService pool = Executors.newFixedThreadPool(writerCount + 1);
        try {
            pool.submit(() -> {
                try {
                    for (int round = 0; round < 50; round++) {
                        try {
                            for (Integer ignored : list) {
                                // just iterate -- a CopyOnWriteArrayList iterator works off a
                                // fixed snapshot, so concurrent writers below can never invalidate it
                            }
                        } catch (java.util.ConcurrentModificationException e) {
                            concurrentModificationCount.incrementAndGet();
                        }
                    }
                } finally {
                    done.countDown();
                }
            });
            for (int w = 0; w < writerCount; w++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < 50; i++) {
                            list.add(i);
                            if (!list.isEmpty()) {
                                list.remove(0);
                            }
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
        System.out.println("CopyOnWriteArrayList: concurrent iteration during writes completed with "
                + concurrentModificationCount.get() + " ConcurrentModificationException(s) (expected 0)");
        System.out.println();
    }

    private static void concurrentSkipListMapDemo() throws InterruptedException {
        System.out.println("--- ConcurrentSkipListMap: sorted order maintained under concurrent inserts ---");
        ConcurrentSkipListMap<Integer, String> sorted = new ConcurrentSkipListMap<>();
        int threadCount = 6;
        int keysPerThread = 100;
        CountDownLatch done = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                int threadIndex = t;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < keysPerThread; i++) {
                            int key = threadIndex * keysPerThread + i;
                            sorted.put(key, "v" + key);
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
        List<Integer> keys = new ArrayList<>(sorted.keySet());
        boolean ascending = true;
        for (int i = 1; i < keys.size(); i++) {
            if (keys.get(i) < keys.get(i - 1)) {
                ascending = false;
                break;
            }
        }
        System.out.println("ConcurrentSkipListMap: keys after concurrent insert from " + threadCount
                + " threads are in ascending sorted order = " + ascending + " (firstKey=" + sorted.firstKey()
                + ", lastKey=" + sorted.lastKey() + ")");
        System.out.println();
    }

    private static void synchronizedWrapperCompoundActionRaceDemo() throws InterruptedException {
        System.out.println("--- Collections.synchronizedList: compound-action race vs the fix ---");
        int threadCount = 8;
        int callsPerThread = 200;

        // BUG scenario: each individual call is synchronized, but "add if absent" (check-then-act)
        // is not atomic across the two calls -- duplicates or lost adds can occur.
        List<Integer> raceProne = Collections.synchronizedList(new ArrayList<>());
        runAddIfAbsentWithoutExternalLock(raceProne, threadCount, callsPerThread);
        System.out.println("synchronized wrapper compound-action race: final size WITHOUT external lock = "
                + raceProne.size() + " (expected " + callsPerThread
                + "; a size GREATER than expected means duplicate adds slipped through the check-then-act race)");

        // FIX: hold the wrapper's own monitor for the ENTIRE check-then-act sequence.
        List<Integer> fixed = Collections.synchronizedList(new ArrayList<>());
        runAddIfAbsentWithExternalLock(fixed, threadCount, callsPerThread);
        System.out.println("synchronized wrapper compound-action fixed: final size WITH synchronized(list) block = "
                + fixed.size() + " (expected " + callsPerThread + ", matches = " + (fixed.size() == callsPerThread) + ")");
    }

    private static void runAddIfAbsentWithoutExternalLock(List<Integer> list, int threadCount, int callsPerThread)
            throws InterruptedException {
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < callsPerThread; i++) {
                            if (!list.contains(i)) {   // BUG: check-then-act, not atomic as a whole
                                list.add(i);
                            }
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
    }

    private static void runAddIfAbsentWithExternalLock(List<Integer> list, int threadCount, int callsPerThread)
            throws InterruptedException {
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < callsPerThread; i++) {
                            synchronized (list) {                 // FIX: whole check-then-act under one lock
                                if (!list.contains(i)) {
                                    list.add(i);
                                }
                            }
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
