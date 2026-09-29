package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link ThreadSafeCounter} exercise stub. EXPECTED TO FAIL until implemented.
 *
 * <p>Deterministic by design: every thread's increments are guaranteed complete before the final
 * assertion, via {@code ExecutorService.awaitTermination} with a generous bound (never a bare
 * {@code Thread.sleep} guess), and the whole test is wrapped in a JUnit 5 {@code @Timeout} so a
 * genuinely broken/deadlocking implementation fails fast instead of hanging the suite.
 */
class ThreadSafeCounterTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void incrementFromManyThreadsLosesNoUpdates() throws InterruptedException, TimeoutException {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        int threadCount = 20;
        int incrementsPerThread = 1000;
        CountDownLatch done = new CountDownLatch(threadCount);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int t = 0; t < threadCount; t++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < incrementsPerThread; i++) {
                            counter.increment();
                        }
                    } finally {
                        done.countDown();
                    }
                });
            }
            boolean finished = done.await(8, TimeUnit.SECONDS);
            if (!finished) {
                throw new TimeoutException("counter threads did not finish within the bound");
            }
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }

        assertThat(counter.get()).isEqualTo(threadCount * incrementsPerThread);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void startsAtZero() {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        assertThat(counter.get()).isZero();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void singleThreadedIncrementsAreExact() {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        for (int i = 0; i < 100; i++) {
            counter.increment();
        }
        assertThat(counter.get()).isEqualTo(100);
    }
}
