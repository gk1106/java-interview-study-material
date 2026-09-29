package com.gk.study.concurrency.examples;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates intrinsic-lock reentrancy, the fact that a synchronized instance method and a
 * synchronized static method lock two DIFFERENT monitors (no mutual exclusion between them), and
 * a wait/notifyAll bounded buffer built directly on Object's monitor methods. Every proof is
 * deterministic: bounded via Future.get(timeout) / ExecutorService.awaitTermination, never a bare
 * sleep-and-hope.
 *
 * See notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md
 */
public final class SynchronizedWaitNotifyDemo {

    private SynchronizedWaitNotifyDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        reentrancyDemo();
        staticVsInstanceLockDemo();
        waitNotifyBoundedBufferDemo();
    }

    private static final class Reentrant {
        private int holdCount = 0;
        private int observedNestedHoldCount = -1;

        synchronized void outer() {
            holdCount++;
            inner();
        }

        synchronized void inner() {
            holdCount++;
            observedNestedHoldCount = holdCount;
        }
    }

    private static void reentrancyDemo() {
        System.out.println("--- Reentrant synchronized ---");
        Reentrant r = new Reentrant();
        r.outer();
        System.out.println("reentrant synchronized: nested call succeeded without self-deadlock, "
                + "hold count observed = " + r.observedNestedHoldCount);
        System.out.println();
    }

    private static final class LockTargets {
        private static volatile boolean staticSectionEntered = false;
        private volatile boolean instanceSectionEntered = false;

        static synchronized void staticMethod(Runnable onEntered) {
            staticSectionEntered = true;
            onEntered.run();
        }

        synchronized void instanceMethod(Runnable onEntered) {
            instanceSectionEntered = true;
            onEntered.run();
        }
    }

    private static void staticVsInstanceLockDemo() throws InterruptedException {
        System.out.println("--- static synchronized vs instance synchronized: different monitors ---");
        LockTargets target = new LockTargets();
        AtomicInteger concurrentlyInsideBoth = new AtomicInteger(0);
        AtomicInteger peakConcurrent = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> staticCaller = pool.submit(() -> LockTargets.staticMethod(() -> {
                int now = concurrentlyInsideBoth.incrementAndGet();
                peakConcurrent.updateAndGet(prev -> Math.max(prev, now));
                sleepQuietly(200);
                concurrentlyInsideBoth.decrementAndGet();
            }));
            Future<?> instanceCaller = pool.submit(() -> target.instanceMethod(() -> {
                int now = concurrentlyInsideBoth.incrementAndGet();
                peakConcurrent.updateAndGet(prev -> Math.max(prev, now));
                sleepQuietly(200);
                concurrentlyInsideBoth.decrementAndGet();
            }));
            getBounded(staticCaller);
            getBounded(instanceCaller);
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println("both entered concurrently (different monitors, no exclusion) = "
                + (peakConcurrent.get() == 2));
        System.out.println();
    }

    /** Minimal bounded buffer using plain synchronized/wait/notifyAll -- ONE shared wait-set. */
    private static final class WaitNotifyBoundedBuffer<T> {
        private final Deque<T> items = new ArrayDeque<>();
        private final int capacity;
        private final Object lock = new Object();

        WaitNotifyBoundedBuffer(int capacity) {
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

        boolean isEmpty() {
            synchronized (lock) {
                return items.isEmpty();
            }
        }
    }

    private static void waitNotifyBoundedBufferDemo() throws InterruptedException {
        System.out.println("--- wait/notifyAll bounded buffer ---");
        WaitNotifyBoundedBuffer<Integer> buffer = new WaitNotifyBoundedBuffer<>(2);
        int itemCount = 5;
        AtomicInteger produced = new AtomicInteger();
        AtomicInteger consumed = new AtomicInteger();

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
                        buffer.take();
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            getBounded(producer);
            getBounded(consumer);
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println("wait/notifyAll bounded buffer: producer put " + produced.get()
                + " items, consumer took " + consumed.get() + " items, buffer empty at end = "
                + buffer.isEmpty());
        System.out.println();
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static <T> T getBounded(Future<T> future) {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("demo task did not complete within the bound", e);
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
