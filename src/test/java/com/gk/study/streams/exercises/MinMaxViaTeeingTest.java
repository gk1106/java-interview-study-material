package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MinMaxViaTeeing} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MinMaxViaTeeingTest {

    @Test
    void typicalInput() {
        MinMaxViaTeeing.MinMax result = MinMaxViaTeeing.solve(List.of(5, 3, 8, 1, 9, 2));
        assertThat(result).isEqualTo(new MinMaxViaTeeing.MinMax(1, 9));
    }

    @Test
    void singleElement() {
        assertThat(MinMaxViaTeeing.solve(List.of(7))).isEqualTo(new MinMaxViaTeeing.MinMax(7, 7));
    }

    @Test
    void emptyListThrows() {
        assertThatThrownBy(() -> MinMaxViaTeeing.solve(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeNumbers() {
        assertThat(MinMaxViaTeeing.solve(List.of(-5, -1, -10))).isEqualTo(new MinMaxViaTeeing.MinMax(-10, -1));
    }
}
