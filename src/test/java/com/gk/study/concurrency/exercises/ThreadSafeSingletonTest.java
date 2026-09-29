package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link ThreadSafeSingleton} exercise stub. EXPECTED TO FAIL until implemented.
 *
 * <p>Deterministic by design: all threads are released simultaneously via a {@code CountDownLatch}
 * start gate (not by hoping thread scheduling lines them up), and the whole test is bounded by a
 * JUnit 5 {@code @Timeout}.
 */
class ThreadSafeSingletonTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void concurrentFirstCallsAllObserveTheSameInstance() throws InterruptedException {
        int threadCount = 50;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch allDone = new CountDownLatch(threadCount);
        Set<ThreadSafeSingleton> observed = Collections.synchronizedSet(new CopyOnWriteArraySet<>());

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    try {
                        startGate.await(5, TimeUnit.SECONDS);
                        observed.add(ThreadSafeSingleton.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            startGate.countDown();
            boolean finished = allDone.await(8, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        }

        assertThat(observed).hasSize(1);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void sequentialCallsReturnTheSameInstance() {
        ThreadSafeSingleton first = ThreadSafeSingleton.getInstance();
        ThreadSafeSingleton second = ThreadSafeSingleton.getInstance();
        assertThat(first).isSameAs(second);
    }
}
