package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MaxMinViaPrimitiveStream} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MaxMinViaPrimitiveStreamTest {

    @Test
    void typicalInput() {
        assertThat(MaxMinViaPrimitiveStream.solve(new int[] {7, 2, 9, 4, 1})).containsExactly(1, 9);
    }

    @Test
    void singleElement() {
        assertThat(MaxMinViaPrimitiveStream.solve(new int[] {5})).containsExactly(5, 5);
    }

    @Test
    void emptyArrayThrows() {
        assertThatThrownBy(() -> MaxMinViaPrimitiveStream.solve(new int[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeNumbers() {
        assertThat(MaxMinViaPrimitiveStream.solve(new int[] {-3, -7, -1})).containsExactly(-7, -1);
    }
}
