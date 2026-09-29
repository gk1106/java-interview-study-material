package com.gk.study.concurrency.examples;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * Demonstrates Java 21 virtual threads: creating them via Thread.ofVirtual() and
 * Executors.newVirtualThreadPerTaskExecutor(), proving thousands of "blocking" tasks complete in
 * roughly the time of ONE task's block duration (not taskCount * blockDuration, which a bounded
 * platform-thread pool would require), and illustrating the synchronized-vs-ReentrantLock pinning
 * gotcha via a wall-clock timing comparison (blocking inside synchronized ties up a carrier thread
 * for the block's duration; blocking while holding a ReentrantLock does not).
 *
 * <p>The pinning comparison is illustrative, not a strict assertion -- exact timings depend on
 * core count and JDK patch version (pinning behavior for synchronized is being progressively
 * relaxed across JDK releases), but the qualitative gap is reliably observable on typical hardware.
 *
 * See notes/09-multithreading-concurrency/11-virtual-threads.md
 */
public final class VirtualThreadsDemo {

    private VirtualThreadsDemo() {
    }

    public static void main(String[] args) throws Exception {
        basicVirtualThreadCreationDemo();
        massConcurrentBlockingDemo();
        pinningComparisonDemo();
    }

    private static void basicVirtualThreadCreationDemo() throws InterruptedException {
        System.out.println("--- Creating a virtual thread ---");
        Thread vt = Thread.ofVirtual().name("demo-virtual-thread").start(() -> {
            // just runs; nothing to print from inside since output ordering isn't guaranteed here
        });
        vt.join(TimeUnit.SECONDS.toMillis(2));
        System.out.println("Thread.ofVirtual() thread name=" + vt.getName() + ", isVirtual()=" + vt.isVirtual());
        System.out.println();
    }

    private static void massConcurrentBlockingDemo() throws Exception {
        System.out.println("--- 10,000 virtual threads, each \"blocking\" for 10ms ---");
        int taskCount = 10_000;
        long start = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Integer>> futures = IntStream.range(0, taskCount)
                    .mapToObj(i -> executor.submit(() -> {
                        Thread.sleep(10);
                        return i;
                    }))
                    .toList();
            int sum = 0;
            for (Future<Integer> f : futures) {
                sum += f.get(30, TimeUnit.SECONDS);
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("started " + taskCount + " virtual threads, each sleeping 10ms -> all "
                    + futures.size() + " completed in ~" + elapsedMs + "ms (NOT " + (taskCount * 10)
                    + "ms, which a naive one-OS-thread-per-task approach would need), checksum=" + sum);
        }
        System.out.println();
    }

    private static void pinningComparisonDemo() throws InterruptedException {
        System.out.println("--- Pinning gotcha: synchronized (pins) vs ReentrantLock (doesn't pin) ---");
        int taskCount = 200;
        long blockMillis = 20;

        long pinnedElapsedMs = timeConcurrentVirtualThreadTasks(taskCount, () -> {
            Object privateLock = new Object(); // uncontended -- isolates the pinning effect itself
            synchronized (privateLock) {
                sleepQuietly(blockMillis); // blocking INSIDE synchronized -- pins the carrier (Java 21)
            }
        });

        long unpinnedElapsedMs = timeConcurrentVirtualThreadTasks(taskCount, () -> {
            ReentrantLock privateLock = new ReentrantLock();
            privateLock.lock();
            try {
                sleepQuietly(blockMillis); // blocking while holding a ReentrantLock -- does NOT pin
            } finally {
                privateLock.unlock();
            }
        });

        System.out.println("synchronized-based: " + taskCount + " tasks x " + blockMillis
                + "ms block each -> elapsed=" + pinnedElapsedMs + "ms (carrier threads tied up while blocked)");
        System.out.println("ReentrantLock-based: " + taskCount + " tasks x " + blockMillis
                + "ms block each -> elapsed=" + unpinnedElapsedMs + "ms (carriers freed while blocked)");
        System.out.println("ReentrantLock version at least as fast as synchronized version = "
                + (unpinnedElapsedMs <= pinnedElapsedMs)
                + " (exact gap depends on core count / JDK patch level)");
    }

    private static long timeConcurrentVirtualThreadTasks(int taskCount, Runnable task) throws InterruptedException {
        long start = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = IntStream.range(0, taskCount)
                    .<Future<?>>mapToObj(i -> executor.submit(task))
                    .toList();
            for (Future<?> f : futures) {
                try {
                    f.get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new IllegalStateException("pinning comparison task did not complete within bound", e);
                }
            }
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
