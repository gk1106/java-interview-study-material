package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link ProducerConsumerWaitNotify} exercise stub. EXPECTED TO FAIL until
 * implemented.
 *
 * <p>Blocking behavior is proven deterministically by polling {@link Thread#getState()} in a
 * bounded loop until the expected state is reached (or the bound is hit) -- never a bare
 * {@code Thread.sleep} used as the only synchronization signal. Every test is wrapped in a JUnit 5
 * {@code @Timeout} so a broken (deadlocking) implementation fails fast.
 */
class ProducerConsumerWaitNotifyTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void singleProducerSingleConsumerPreservesFifoOrderAndCount() throws Exception {
        ProducerConsumerWaitNotify<Integer> buffer = new ProducerConsumerWaitNotify<>(4);
        int itemCount = 500;
        List<Integer> consumedItems = new CopyOnWriteArrayList<>();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < itemCount; i++) {
                    buffer.put(i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < itemCount; i++) {
                    consumedItems.add(buffer.take());
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
        List<Integer> expected = IntStream.range(0, itemCount).boxed().toList();
        assertThat(consumedItems).containsExactlyElementsOf(expected);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void putBlocksWhenFullAndUnblocksAfterATake() throws Exception {
        ProducerConsumerWaitNotify<Integer> buffer = new ProducerConsumerWaitNotify<>(2);
        buffer.put(1);
        buffer.put(2);
        assertThat(buffer.size()).isEqualTo(2);

        Thread blockedProducer = new Thread(() -> {
            try {
                buffer.put(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        blockedProducer.start();

        boolean reachedWaiting = waitUntilState(blockedProducer, Thread.State.WAITING, 2000);
        assertThat(reachedWaiting).as("producer should block on a full buffer").isTrue();

        Integer taken = buffer.take(); // frees a slot -- should unblock the producer
        assertThat(taken).isEqualTo(1);

        blockedProducer.join(2000);
        assertThat(blockedProducer.isAlive()).isFalse();
        assertThat(buffer.size()).isEqualTo(2); // item 2 (remaining) + item 3 (just unblocked)
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void takeBlocksWhenEmptyAndUnblocksAfterAPut() throws Exception {
        ProducerConsumerWaitNotify<Integer> buffer = new ProducerConsumerWaitNotify<>(2);
        assertThat(buffer.size()).isZero();

        final Integer[] result = new Integer[1];
        Thread blockedConsumer = new Thread(() -> {
            try {
                result[0] = buffer.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        blockedConsumer.start();

        boolean reachedWaiting = waitUntilState(blockedConsumer, Thread.State.WAITING, 2000);
        assertThat(reachedWaiting).as("consumer should block on an empty buffer").isTrue();

        buffer.put(99);
        blockedConsumer.join(2000);

        assertThat(blockedConsumer.isAlive()).isFalse();
        assertThat(result[0]).isEqualTo(99);
    }

    /** Polls {@code t.getState()} until it equals {@code expected} or the bound elapses. */
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
