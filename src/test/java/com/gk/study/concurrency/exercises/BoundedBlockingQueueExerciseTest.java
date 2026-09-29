package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link BoundedBlockingQueueExercise} build-it-yourself stub. EXPECTED TO FAIL
 * until implemented.
 *
 * <p>Blocking behavior is proven deterministically by polling {@link Thread#getState()} in a
 * bounded loop (never a bare {@code Thread.sleep} used as the only synchronization signal), and
 * every test is wrapped in a JUnit 5 {@code @Timeout} so a broken (deadlocking) implementation
 * fails fast.
 */
class BoundedBlockingQueueExerciseTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void putAndTakePreserveFifoOrderWithinCapacity() throws InterruptedException {
        BoundedBlockingQueueExercise<Integer> queue = new BoundedBlockingQueueExercise<>(3);
        assertThat(queue.isEmpty()).isTrue();

        queue.put(1);
        queue.put(2);
        queue.put(3);
        assertThat(queue.isFull()).isTrue();
        assertThat(queue.size()).isEqualTo(3);

        assertThat(queue.take()).isEqualTo(1);
        assertThat(queue.take()).isEqualTo(2);
        assertThat(queue.take()).isEqualTo(3);
        assertThat(queue.isEmpty()).isTrue();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void putBlocksWhenFullAndUnblocksAfterATake() throws Exception {
        BoundedBlockingQueueExercise<Integer> queue = new BoundedBlockingQueueExercise<>(2);
        queue.put(1);
        queue.put(2);
        assertThat(queue.isFull()).isTrue();

        Thread blockedPutter = new Thread(() -> {
            try {
                queue.put(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        blockedPutter.start();

        assertThat(waitUntilState(blockedPutter, Thread.State.WAITING, 2000))
                .as("put() should block while the queue is full")
                .isTrue();

        Integer taken = queue.take();
        assertThat(taken).isEqualTo(1);

        blockedPutter.join(2000);
        assertThat(blockedPutter.isAlive()).isFalse();
        assertThat(queue.size()).isEqualTo(2);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void takeBlocksWhenEmptyAndUnblocksAfterAPut() throws Exception {
        BoundedBlockingQueueExercise<Integer> queue = new BoundedBlockingQueueExercise<>(2);
        assertThat(queue.isEmpty()).isTrue();

        final Integer[] result = new Integer[1];
        Thread blockedTaker = new Thread(() -> {
            try {
                result[0] = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        blockedTaker.start();

        assertThat(waitUntilState(blockedTaker, Thread.State.WAITING, 2000))
                .as("take() should block while the queue is empty")
                .isTrue();

        queue.put(42);
        blockedTaker.join(2000);

        assertThat(blockedTaker.isAlive()).isFalse();
        assertThat(result[0]).isEqualTo(42);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void concurrentProducerAndConsumerLoseNoItems() throws Exception {
        BoundedBlockingQueueExercise<Integer> queue = new BoundedBlockingQueueExercise<>(4);
        int itemCount = 500;
        List<Integer> consumed = new CopyOnWriteArrayList<>();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < itemCount; i++) {
                    queue.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < itemCount; i++) {
                    consumed.add(queue.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join(8000);
        consumer.join(8000);

        assertThat(producer.isAlive()).isFalse();
        assertThat(consumer.isAlive()).isFalse();
        List<Integer> expected = java.util.stream.IntStream.range(0, itemCount).boxed().toList();
        assertThat(consumed).containsExactlyElementsOf(expected);
    }

    private static boolean waitUntilState(Thread t, Thread.State expected, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (t.getState() == expected) {
                return true;
            }
            Thread.sleep(10);
        }
        return t.getState() == expected;
    }
}
