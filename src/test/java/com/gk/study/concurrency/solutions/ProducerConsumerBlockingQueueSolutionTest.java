package com.gk.study.concurrency.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ProducerConsumerBlockingQueueSolutionTest {

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void everyItemConsumedExactlyOnceWithMultipleProducersAndConsumers() throws Exception {
        ProducerConsumerBlockingQueueSolution pipeline = new ProducerConsumerBlockingQueueSolution();
        int totalItems = 1000;
        int capacity = 16;
        int producerCount = 4;
        int consumerCount = 4;

        List<Integer> consumed = pipeline.run(totalItems, capacity, producerCount, consumerCount);

        assertThat(consumed).hasSize(totalItems);
        List<Integer> expected = IntStream.range(0, totalItems).boxed().toList();
        assertThat(consumed).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(consumed.stream().distinct().collect(Collectors.toList())).hasSize(totalItems);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void worksWithASingleProducerAndConsumer() throws Exception {
        ProducerConsumerBlockingQueueSolution pipeline = new ProducerConsumerBlockingQueueSolution();
        int totalItems = 200;

        List<Integer> consumed = pipeline.run(totalItems, 8, 1, 1);

        assertThat(consumed).containsExactlyInAnyOrderElementsOf(
                IntStream.range(0, totalItems).boxed().toList());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void rejectsUnevenPartitioning() {
        ProducerConsumerBlockingQueueSolution pipeline = new ProducerConsumerBlockingQueueSolution();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pipeline.run(10, 4, 3, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
