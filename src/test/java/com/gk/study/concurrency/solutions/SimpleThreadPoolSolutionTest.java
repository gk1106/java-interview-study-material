package com.gk.study.concurrency.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class SimpleThreadPoolSolutionTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void everySubmittedTaskRunsExactlyOnce() throws InterruptedException {
        SimpleThreadPool pool = new SimpleThreadPool(4);
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
        SimpleThreadPool pool = new SimpleThreadPool(2);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThatThrownBy(() -> pool.submit(() -> { }))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shutdownIsGracefulAlreadyQueuedTasksStillRun() throws InterruptedException {
        SimpleThreadPool pool = new SimpleThreadPool(2);
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
        pool.shutdown();

        boolean terminated = pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(terminated).isTrue();
        assertThat(executed.get()).isEqualTo(taskCount);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void rejectsNonPositiveThreadCount() {
        assertThatThrownBy(() -> new SimpleThreadPool(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
