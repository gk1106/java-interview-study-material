package com.gk.study.concurrency.solutions;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Reference solution for {@code ProducerConsumerBlockingQueue}: multiple producers/consumers
 * sharing a single bounded {@link ArrayBlockingQueue}, with a poison-pill sentinel (one per
 * consumer) enqueued only after every producer has finished, so every consumer stops cleanly
 * instead of blocking forever on an empty queue. See
 * notes/09-multithreading-concurrency/10-classic-concurrency-problems.md.
 */
public class ProducerConsumerBlockingQueueSolution {

    private static final Integer POISON_PILL = Integer.MIN_VALUE;

    public List<Integer> run(int totalItems, int capacity, int producerCount, int consumerCount)
            throws InterruptedException {
        if (totalItems % producerCount != 0) {
            throw new IllegalArgumentException("totalItems must be evenly divisible by producerCount");
        }
        int itemsPerProducer = totalItems / producerCount;
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(capacity);
        List<Integer> consumed = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(producerCount + consumerCount);
        try {
            CountDownLatch producersDone = new CountDownLatch(producerCount);
            for (int p = 0; p < producerCount; p++) {
                int producerIndex = p;
                pool.submit(() -> {
                    try {
                        int start = producerIndex * itemsPerProducer;
                        for (int i = 0; i < itemsPerProducer; i++) {
                            queue.put(start + i);
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
                            consumed.add(item);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        consumersDone.countDown();
                    }
                });
            }

            producersDone.await(10, TimeUnit.SECONDS);
            for (int c = 0; c < consumerCount; c++) {
                queue.put(POISON_PILL);
            }
            consumersDone.await(10, TimeUnit.SECONDS);
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }
        return consumed;
    }
}
