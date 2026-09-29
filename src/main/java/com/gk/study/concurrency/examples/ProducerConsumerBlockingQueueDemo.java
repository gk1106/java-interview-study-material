package com.gk.study.concurrency.examples;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The idiomatic, production-recommended producer-consumer implementation: java.util.concurrent's
 * ArrayBlockingQueue already implements the bounded-wait put()/take() semantics correctly and
 * efficiently -- contrast with ProducerConsumerWaitNotifyDemo's hand-rolled equivalent. Uses
 * multiple producers and consumers with a poison-pill shutdown signal, and verifies every produced
 * item was consumed exactly once (no duplicates, no drops) via a deterministic final tally.
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md
 */
public final class ProducerConsumerBlockingQueueDemo {

    private ProducerConsumerBlockingQueueDemo() {
    }

    private static final Integer POISON_PILL = Integer.MIN_VALUE;

    public static void main(String[] args) throws Exception {
        System.out.println("--- Producer-consumer via ArrayBlockingQueue (multiple producers/consumers) ---");
        int producerCount = 4;
        int consumerCount = 4;
        int itemsPerProducer = 250;
        int totalItems = producerCount * itemsPerProducer;
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(16);
        List<Integer> consumedItems = new CopyOnWriteArrayList<>();
        AtomicInteger producedCount = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(producerCount + consumerCount);
        try {
            CountDownLatch producersDone = new CountDownLatch(producerCount);
            for (int p = 0; p < producerCount; p++) {
                int producerIndex = p;
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < itemsPerProducer; i++) {
                            int item = producerIndex * itemsPerProducer + i;
                            queue.put(item);
                            producedCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        producersDone.countDown();
                    }
                });
            }

            CountDownLatch consumersDone = new CountDownLatch(consumerCount);
            for (int c = 0; c < consumerCount; c++) {
                pool.submit(() -> {
                    try {
                        while (true) {
                            Integer item = queue.take();
                            if (item.equals(POISON_PILL)) {
                                break;
                            }
                            consumedItems.add(item);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        consumersDone.countDown();
                    }
                });
            }

            boolean producersFinished = producersDone.await(10, TimeUnit.SECONDS);
            // Once all producers are done, feed exactly one poison pill per consumer to stop them.
            for (int c = 0; c < consumerCount; c++) {
                queue.put(POISON_PILL);
            }
            boolean consumersFinished = consumersDone.await(10, TimeUnit.SECONDS);

            System.out.println("producers finished within bound = " + producersFinished
                    + ", consumers finished within bound = " + consumersFinished);
            System.out.println("produced=" + producedCount.get() + ", consumed=" + consumedItems.size()
                    + ", no duplicates/drops (consumed size matches unique count) = "
                    + (consumedItems.size() == totalItems
                            && consumedItems.stream().distinct().count() == totalItems));
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
