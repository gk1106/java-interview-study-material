package com.gk.study.concurrency.examples;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates a genuine two-lock deadlock, detected deterministically via
 * {@link ThreadMXBean#findDeadlockedThreads()} (real cycle detection over the JVM's lock-ownership
 * graph -- the same mechanism behind jstack's "Found one Java-level deadlock" output), then shows
 * the lock-ordering fix proven safe under real concurrent opposite-direction load within a bounded
 * timeout.
 *
 * <p>The deadlocking threads are started as DAEMON threads specifically so this demo -- and the
 * JVM itself -- can exit normally even though those two threads remain permanently stuck; nothing
 * in this class ever blocks on them.
 *
 * See notes/09-multithreading-concurrency/10-classic-concurrency-problems.md
 */
public final class DeadlockDemo {

    private DeadlockDemo() {
    }

    public static void main(String[] args) throws Exception {
        deadlockDemo();
        lockOrderingFixDemo();
    }

    private static void deadlockDemo() throws InterruptedException {
        System.out.println("--- Deadlock: two threads locking two locks in opposite order ---");
        Object lockA = new Object();
        Object lockB = new Object();
        CountDownLatch bothHoldFirstLock = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                bothHoldFirstLock.countDown();
                sleepQuietly(200); // widen the window so the other thread definitely grabs lockB first
                synchronized (lockB) {
                    // never reached
                }
            }
        }, "deadlock-thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                bothHoldFirstLock.countDown();
                sleepQuietly(200);
                synchronized (lockA) {
                    // never reached
                }
            }
        }, "deadlock-thread-2");

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        bothHoldFirstLock.await(2, TimeUnit.SECONDS);

        // Give the circular wait time to actually form, then ask the JVM to detect it for real.
        Thread.sleep(1000);
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedIds = threadMxBean.findDeadlockedThreads();
        boolean deadlockDetected = deadlockedIds != null && deadlockedIds.length >= 2;
        System.out.println("genuine deadlock detected via ThreadMXBean = " + deadlockDetected
                + " -> " + (deadlockedIds == null ? 0 : deadlockedIds.length) + " thread(s) involved (expected 2)");
        System.out.println("(the two deadlocked threads remain stuck forever, but are daemons -- "
                + "this demo and the JVM proceed/exit normally)");
        System.out.println();
    }

    /** Simple bank account with an id used to establish a consistent global lock order. */
    private static final class Account {
        private final int id;
        private int balance;

        Account(int id, int balance) {
            this.id = id;
            this.balance = balance;
        }

        int getId() {
            return id;
        }

        synchronized int getBalance() {
            return balance;
        }

        void debit(int amount) {
            balance -= amount;
        }

        void credit(int amount) {
            balance += amount;
        }
    }

    /** Always locks the lower-id account first, regardless of the from/to argument order. */
    private static void transferSafely(Account from, Account to, int amount) {
        Account first = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;
        synchronized (first) {
            synchronized (second) {
                from.debit(amount);
                to.credit(amount);
            }
        }
    }

    private static void lockOrderingFixDemo() throws InterruptedException {
        System.out.println("--- Lock-ordering fix: proven deadlock-free under concurrent opposite-direction load ---");
        Account accountA = new Account(1, 100_000);
        Account accountB = new Account(2, 100_000);
        int transfersPerThread = 10_000;

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var future1 = pool.submit(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    transferSafely(accountA, accountB, 1); // "natural" order A->B
                }
            });
            var future2 = pool.submit(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    transferSafely(accountB, accountA, 1); // opposite order B->A, concurrently
                }
            });
            future1.get(5, TimeUnit.SECONDS);
            future2.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("lock-ordering fix did not complete within the bound "
                    + "(would indicate a real deadlock)", e);
        } finally {
            shutdownQuietly(pool);
        }

        int totalBalance = accountA.getBalance() + accountB.getBalance();
        System.out.println("lock-ordering-fixed transfer completed " + (2 * transfersPerThread)
                + " opposite-order transfers within the bound, total balance conserved = "
                + (totalBalance == 200_000) + " (" + totalBalance + ")");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
