package com.gk.study.concurrency.examples;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A hand-rolled bounded-buffer producer-consumer built directly on synchronized/wait/notifyAll --
 * see ProducerConsumerBlockingQueueDemo for the idiomatic, production-recommended alternative
 * using java.util.concurrent.BlockingQueue instead.
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md
 */
public final class ProducerConsumerWaitNotifyDemo {

    private ProducerConsumerWaitNotifyDemo() {
    }

    private static final class BoundedBuffer<T> {
        private final Deque<T> items = new ArrayDeque<>();
        private final int capacity;
        private final Object lock = new Object();

        BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        void put(T item) throws InterruptedException {
            synchronized (lock) {
                while (items.size() == capacity) {
                    lock.wait();
                }
                items.addLast(item);
                lock.notifyAll();
            }
        }

        T take() throws InterruptedException {
            synchronized (lock) {
                while (items.isEmpty()) {
                    lock.wait();
                }
                T item = items.removeFirst();
                lock.notifyAll();
                return item;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("--- Producer-consumer via wait/notifyAll (hand-rolled bounded buffer) ---");
        int capacity = 4;
        int itemCount = 500;
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(capacity);
        AtomicInteger produced = new AtomicInteger();
        AtomicInteger consumed = new AtomicInteger();
        AtomicInteger consumedSum = new AtomicInteger();
        int expectedSum = itemCount * (itemCount - 1) / 2;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> producer = pool.submit(() -> {
                try {
                    for (int i = 0; i < itemCount; i++) {
                        buffer.put(i);
                        produced.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            Future<?> consumer = pool.submit(() -> {
                try {
                    for (int i = 0; i < itemCount; i++) {
                        int item = buffer.take();
                        consumed.incrementAndGet();
                        consumedSum.addAndGet(item);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            producer.get(10, TimeUnit.SECONDS);
            consumer.get(10, TimeUnit.SECONDS);
        } finally {
            shutdownQuietly(pool);
        }

        System.out.println("produced=" + produced.get() + ", consumed=" + consumed.get()
                + ", all items accounted for (sum matches) = " + (consumedSum.get() == expectedSum));
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
