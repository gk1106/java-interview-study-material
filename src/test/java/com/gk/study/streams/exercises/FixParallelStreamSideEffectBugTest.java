package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FixParallelStreamSideEffectBug} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class FixParallelStreamSideEffectBugTest {

    @Test
    void typicalInput() {
        assertThat(FixParallelStreamSideEffectBug.solve(List.of(1, 2, 3, 4, 5, 6))).isEqualTo(56L);
    }

    @Test
    void emptyList() {
        assertThat(FixParallelStreamSideEffectBug.solve(List.of())).isEqualTo(0L);
    }

    @Test
    void noEvenNumbers() {
        assertThat(FixParallelStreamSideEffectBug.solve(List.of(1, 3, 5))).isEqualTo(0L);
    }

    @Test
    void largerInputMatchesIndependentImperativeComputation() {
        List<Integer> numbers = IntStream.rangeClosed(1, 1000).boxed().toList();
        long expected = 0;
        for (int n : numbers) {
            if (n % 2 == 0) {
                expected += (long) n * n;
            }
        }
        assertThat(FixParallelStreamSideEffectBug.solve(numbers)).isEqualTo(expected);
    }
}
