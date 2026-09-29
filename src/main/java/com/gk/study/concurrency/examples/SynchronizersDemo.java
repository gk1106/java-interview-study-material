package com.gk.study.concurrency.examples;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates CountDownLatch (start gate proving simultaneous release), CyclicBarrier (two
 * rendezvous rounds with a barrier action), Semaphore (proving a permit bound is never exceeded
 * via a live-holder counter), and a brief Phaser phase-advance. Every wait is bounded; every pool
 * is shut down in a finally block.
 *
 * See notes/09-multithreading-concurrency/08-synchronizers.md
 */
public final class SynchronizersDemo {

    private SynchronizersDemo() {
    }

    public static void main(String[] args) throws Exception {
        countDownLatchDemo();
        cyclicBarrierDemo();
        semaphoreDemo();
        phaserDemo();
    }

    private static void countDownLatchDemo() throws InterruptedException {
        System.out.println("--- CountDownLatch: simultaneous start gate ---");
        int workerCount = 5;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch allReady = new CountDownLatch(workerCount);
        CountDownLatch allDone = new CountDownLatch(workerCount);
        AtomicInteger releasedTogether = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(workerCount);
        try {
            for (int i = 0; i < workerCount; i++) {
                pool.submit(() -> {
                    allReady.countDown();
                    try {
                        startGate.await(5, TimeUnit.SECONDS);
                        releasedTogether.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            allReady.await(5, TimeUnit.SECONDS);
            startGate.countDown();
            boolean finished = allDone.await(5, TimeUnit.SECONDS);
            System.out.println("CountDownLatch: all " + workerCount
                    + " workers released simultaneously after countDown() = "
                    + (finished && releasedTogether.get() == workerCount));
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println();
    }

    private static void cyclicBarrierDemo() throws Exception {
        System.out.println("--- CyclicBarrier: repeated rendezvous with a barrier action ---");
        int parties = 4;
        AtomicInteger barrierActionRuns = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(parties, barrierActionRuns::incrementAndGet);
        int rounds = 2;
        CountDownLatch allRoundsDone = new CountDownLatch(parties);

        ExecutorService pool = Executors.newFixedThreadPool(parties);
        try {
            for (int i = 0; i < parties; i++) {
                pool.submit(() -> {
                    try {
                        for (int round = 0; round < rounds; round++) {
                            barrier.await(5, TimeUnit.SECONDS);
                        }
                    } catch (InterruptedException | java.util.concurrent.TimeoutException
                            | BrokenBarrierException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allRoundsDone.countDown();
                    }
                });
            }
            boolean finished = allRoundsDone.await(5, TimeUnit.SECONDS);
            System.out.println("CyclicBarrier: all rounds completed = " + finished
                    + ", barrier action ran " + barrierActionRuns.get() + " times (expected " + rounds + ")");
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println();
    }

    private static void semaphoreDemo() throws InterruptedException {
        System.out.println("--- Semaphore: bounded concurrent holders ---");
        int permits = 3;
        int taskCount = 9;
        Semaphore semaphore = new Semaphore(permits);
        AtomicInteger currentHolders = new AtomicInteger(0);
        AtomicInteger peakHolders = new AtomicInteger(0);
        CountDownLatch allDone = new CountDownLatch(taskCount);

        ExecutorService pool = Executors.newFixedThreadPool(taskCount);
        try {
            for (int i = 0; i < taskCount; i++) {
                pool.submit(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            int now = currentHolders.incrementAndGet();
                            peakHolders.updateAndGet(prev -> Math.max(prev, now));
                            Thread.sleep(50);
                            currentHolders.decrementAndGet();
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            boolean finished = allDone.await(5, TimeUnit.SECONDS);
            System.out.println("Semaphore: all tasks finished = " + finished
                    + ", peak concurrent holders observed = " + peakHolders.get()
                    + " (never exceeded permit count of " + permits + ") = " + (peakHolders.get() <= permits));
        } finally {
            shutdownQuietly(pool);
        }
        System.out.println();
    }

    private static void phaserDemo() throws InterruptedException {
        System.out.println("--- Phaser: dynamic-party phase advance ---");
        int parties = 3;
        Phaser phaser = new Phaser(parties);
        CountDownLatch allDone = new CountDownLatch(parties);
        AtomicInteger observedFinalPhase = new AtomicInteger(-1);

        ExecutorService pool = Executors.newFixedThreadPool(parties);
        try {
            for (int i = 0; i < parties; i++) {
                pool.submit(() -> {
                    int phaseAfterAdvance = phaser.arriveAndAwaitAdvance();
                    observedFinalPhase.set(phaseAfterAdvance);
                    phaser.arriveAndDeregister();
                    allDone.countDown();
                });
            }
            boolean finished = allDone.await(5, TimeUnit.SECONDS);
            System.out.println("Phaser: all parties advanced = " + finished
                    + ", advanced from phase 0 to phase " + observedFinalPhase.get());
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
