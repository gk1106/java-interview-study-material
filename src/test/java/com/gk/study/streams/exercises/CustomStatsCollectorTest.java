package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link CustomStatsCollector} exercise stub. EXPECTED TO FAIL until implemented.
 */
class CustomStatsCollectorTest {

    @Test
    void typicalInput() {
        CustomStatsCollector.Stats stats =
                List.of(4, 1, 7, 3, 9, 2).stream().collect(CustomStatsCollector.toStats());
        assertThat(stats).isEqualTo(new CustomStatsCollector.Stats(6, 26, 1, 9));
    }

    @Test
    void singleElement() {
        CustomStatsCollector.Stats stats = List.of(5).stream().collect(CustomStatsCollector.toStats());
        assertThat(stats).isEqualTo(new CustomStatsCollector.Stats(1, 5, 5, 5));
    }

    @Test
    void parallelStreamExercisesTheCombiner() {
        List<Integer> numbers = IntStream.rangeClosed(1, 1000).boxed().toList();
        CustomStatsCollector.Stats stats = numbers.parallelStream().collect(CustomStatsCollector.toStats());
        assertThat(stats).isEqualTo(new CustomStatsCollector.Stats(1000, 500_500, 1, 1000));
    }
}
