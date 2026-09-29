package com.gk.study.concurrency.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ProducerConsumerWaitNotifySolutionTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void singleProducerSingleConsumerPreservesFifoOrderAndCount() throws Exception {
        ProducerConsumerWaitNotifySolution<Integer> buffer = new ProducerConsumerWaitNotifySolution<>(4);
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
        ProducerConsumerWaitNotifySolution<Integer> buffer = new ProducerConsumerWaitNotifySolution<>(2);
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

        Integer taken = buffer.take();
        assertThat(taken).isEqualTo(1);

        blockedProducer.join(2000);
        assertThat(blockedProducer.isAlive()).isFalse();
        assertThat(buffer.size()).isEqualTo(2);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void takeBlocksWhenEmptyAndUnblocksAfterAPut() throws Exception {
        ProducerConsumerWaitNotifySolution<Integer> buffer = new ProducerConsumerWaitNotifySolution<>(2);
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
