package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SingleNumber} exercise stub. EXPECTED TO FAIL until implemented.
 */
class SingleNumberTest {

    @Test
    void xorApproachFindsTheSingleton() {
        assertThat(SingleNumber.solveXor(new int[] {4, 1, 2, 1, 2})).isEqualTo(4);
    }

    @Test
    void hashSetApproachFindsTheSingleton() {
        assertThat(SingleNumber.solveHashSet(new int[] {4, 1, 2, 1, 2})).isEqualTo(4);
    }

    @Test
    void xorApproachHandlesNegativeNumbers() {
        assertThat(SingleNumber.solveXor(new int[] {-1, -1, -2})).isEqualTo(-2);
    }

    @Test
    void hashSetApproachHandlesNegativeNumbers() {
        assertThat(SingleNumber.solveHashSet(new int[] {-1, -1, -2})).isEqualTo(-2);
    }

    @Test
    void singleElementArrayReturnsThatElement() {
        assertThat(SingleNumber.solveXor(new int[] {42})).isEqualTo(42);
        assertThat(SingleNumber.solveHashSet(new int[] {42})).isEqualTo(42);
    }
}
