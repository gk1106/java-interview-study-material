package com.gk.study.concurrency.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for the {@link ProducerConsumerBlockingQueue} exercise stub. EXPECTED TO FAIL until
 * implemented.
 *
 * <p>Order is intentionally NOT asserted (multiple producers/consumers race, so any interleaving
 * is valid) -- correctness is verified as a set: every expected value present exactly once, no
 * drops, no duplicates. Bounded by {@code @Timeout} so a broken (deadlocking/leaking)
 * implementation fails fast instead of hanging the suite.
 */
class ProducerConsumerBlockingQueueTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void everyItemConsumedExactlyOnceWithMultipleProducersAndConsumers() throws Exception {
        ProducerConsumerBlockingQueue pipeline = new ProducerConsumerBlockingQueue();
        int totalItems = 1000;
        int capacity = 16;
        int producerCount = 4;
        int consumerCount = 4;

        List<Integer> consumed = pipeline.run(totalItems, capacity, producerCount, consumerCount);

        assertThat(consumed).hasSize(totalItems);
        List<Integer> expected = IntStream.range(0, totalItems).boxed().toList();
        assertThat(consumed).containsExactlyInAnyOrderElementsOf(expected);
        // no duplicates, expressed independently of the containsExactlyInAnyOrder check above
        assertThat(consumed.stream().distinct().collect(Collectors.toList())).hasSize(totalItems);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void worksWithASingleProducerAndConsumer() throws Exception {
        ProducerConsumerBlockingQueue pipeline = new ProducerConsumerBlockingQueue();
        int totalItems = 200;

        List<Integer> consumed = pipeline.run(totalItems, 8, 1, 1);

        assertThat(consumed).containsExactlyInAnyOrderElementsOf(
                IntStream.range(0, totalItems).boxed().toList());
    }
}
