package com.gk.study.concurrency.examples;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Demonstrates ReentrantLock (tryLock with timeout), a Condition-based bounded hand-off,
 * ReentrantReadWriteLock concurrent readers vs an exclusive writer, and StampedLock's
 * optimistic-read-then-validate pattern. Every proof is deterministic: concurrency claims are
 * proven with latches/gates, not by hoping timing works out, and every executor is shut down in
 * a finally block with a bounded awaitTermination.
 *
 * See notes/09-multithreading-concurrency/04-locks-reentrantlock-readwritelock-stampedlock.md
 */
public final class LocksDemo {

    private LocksDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        tryLockTimeoutDemo();
        conditionBoundedHandoffDemo();
        readWriteLockDemo();
        stampedLockOptimisticReadDemo();
    }

    private static void tryLockTimeoutDemo() throws InterruptedException {
        System.out.println("--- ReentrantLock.tryLock(timeout) ---");
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> whileHeld = pool.submit(() -> lock.tryLock(300, TimeUnit.MILLISECONDS));
            boolean acquiredWhileHeld = getBounded(whileHeld);
            System.out.println("tryLock(300ms) while main holds the lock -> acquired=" + acquiredWhileHeld);

            lock.unlock();
            Future<Boolean> afterRelease = pool.submit(() -> lock.tryLock(2, TimeUnit.SECONDS));
            boolean acquiredAfterRelease = getBounded(afterRelease);
            System.out.println("tryLock(2s) after main released the lock -> acquired=" + acquiredAfterRelease);
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println();
    }

    /** Minimal bounded buffer using two Conditions on one lock: notFull for producers, notEmpty for consumers. */
    private static final class BoundedBuffer<T> {
        private final Deque<T> items = new ArrayDeque<>();
        private final int capacity;
        private final Lock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        BoundedBuffer(int capacity) {
            this.capacity = capacity;
        }

        void put(T item) throws InterruptedException {
            lock.lock();
            try {
                while (items.size() == capacity) {
                    notFull.await();
                }
                items.addLast(item);
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        T take() throws InterruptedException {
            lock.lock();
            try {
                while (items.isEmpty()) {
                    notEmpty.await();
                }
                T item = items.removeFirst();
                notFull.signal();
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    private static void conditionBoundedHandoffDemo() throws InterruptedException {
        System.out.println("--- Condition-based bounded hand-off (notFull/notEmpty) ---");
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(2);
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
            System.out.println("producer put " + produced.get() + " items, consumer took " + consumed.get() + " items");
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println();
    }

    private static void readWriteLockDemo() throws InterruptedException {
        System.out.println("--- ReadWriteLock: concurrent readers, exclusive writer ---");
        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        int readerCount = 3;
        // If the read lock truly allows concurrency, all readerCount threads can acquire it and
        // count down this latch; if it were exclusive, only one could enter at a time and this
        // latch would never reach zero within the bound -- a deterministic proof of concurrency.
        CountDownLatch allReadersEntered = new CountDownLatch(readerCount);

        ExecutorService readerPool = Executors.newFixedThreadPool(readerCount);
        try {
            for (int i = 0; i < readerCount; i++) {
                readerPool.submit(() -> {
                    rwLock.readLock().lock();
                    try {
                        allReadersEntered.countDown();
                        allReadersEntered.await(2, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        rwLock.readLock().unlock();
                    }
                });
            }
            boolean allEnteredConcurrently = allReadersEntered.await(2, TimeUnit.SECONDS);
            System.out.println("all " + readerCount + " readers held the read lock concurrently = " + allEnteredConcurrently);
        } finally {
            shutdownQuietly(readerPool);
        }

        ExecutorService writerAndReader = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch writerHoldsLock = new CountDownLatch(1);
            CountDownLatch releaseWriter = new CountDownLatch(1);
            Future<?> writer = writerAndReader.submit(() -> {
                rwLock.writeLock().lock();
                try {
                    writerHoldsLock.countDown();
                    releaseWriter.await(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    rwLock.writeLock().unlock();
                }
            });
            writerHoldsLock.await(2, TimeUnit.SECONDS);
            Future<Boolean> readerDuringWrite = writerAndReader.submit(
                    () -> rwLock.readLock().tryLock(300, TimeUnit.MILLISECONDS));
            boolean readerBlockedDuringWrite = getBounded(readerDuringWrite);
            System.out.println("reader tryLock while writer holds the write lock -> acquired=" + readerBlockedDuringWrite);
            releaseWriter.countDown();
            getBounded(writer);
        } finally {
            shutdownQuietly(writerAndReader);
        }
        System.out.println();
    }

    private static void stampedLockOptimisticReadDemo() {
        System.out.println("--- StampedLock: optimistic read invalidated by a concurrent write ---");
        StampedLock stampedLock = new StampedLock();
        int[] sharedValue = {10};

        long stamp = stampedLock.tryOptimisticRead();
        int optimisticSnapshot = sharedValue[0];

        // Simulate a writer intervening between the optimistic read and its validation.
        long writeStamp = stampedLock.writeLock();
        try {
            sharedValue[0] = 20;
        } finally {
            stampedLock.unlockWrite(writeStamp);
        }

        boolean valid = stampedLock.validate(stamp);
        int finalValue = optimisticSnapshot;
        if (!valid) {
            long readStamp = stampedLock.readLock();
            try {
                finalValue = sharedValue[0];
            } finally {
                stampedLock.unlockRead(readStamp);
            }
        }
        System.out.println("optimistic snapshot=" + optimisticSnapshot + ", validate()=" + valid
                + ", value used after fallback=" + finalValue);
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
