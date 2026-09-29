package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link SimpleThreadPoolExercise} build-it-yourself stub. EXPECTED TO FAIL until
 * implemented.
 *
 * <p>Every test bounds {@code awaitTermination} with a generous, finite timeout and additionally
 * wraps the test in a JUnit 5 {@code @Timeout}, so a broken (deadlocking, or a worker that never
 * exits) implementation fails fast rather than hanging the suite.
 */
class SimpleThreadPoolExerciseTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void everySubmittedTaskRunsExactlyOnce() throws InterruptedException {
        SimpleThreadPoolExercise pool = new SimpleThreadPoolExercise(4);
        int taskCount = 100;
        AtomicInteger executed = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            pool.submit(executed::incrementAndGet);
        }
        pool.shutdown();
        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(terminated).isTrue();
        assertThat(executed.get()).isEqualTo(taskCount);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void submitAfterShutdownIsRejected() throws InterruptedException {
        SimpleThreadPoolExercise pool = new SimpleThreadPoolExercise(2);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThatThrownBy(() -> pool.submit(() -> { }))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shutdownIsGracefulAlreadyQueuedTasksStillRun() throws InterruptedException {
        SimpleThreadPoolExercise pool = new SimpleThreadPoolExercise(2);
        int taskCount = 50;
        AtomicInteger executed = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            pool.submit(() -> {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                executed.incrementAndGet();
            });
        }
        pool.shutdown(); // called immediately -- most tasks are still queued at this point

        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(terminated).isTrue();
        assertThat(executed.get()).isEqualTo(taskCount);
    }
}
