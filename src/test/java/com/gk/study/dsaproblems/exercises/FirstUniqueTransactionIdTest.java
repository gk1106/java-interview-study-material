package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FirstUniqueTransactionId} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class FirstUniqueTransactionIdTest {

    @Test
    void typicalInput() {
        assertThat(FirstUniqueTransactionId.solve(List.of("t1", "t2", "t1", "t3")))
                .isEqualTo("t2");
    }

    @Test
    void allUniqueReturnsFirst() {
        assertThat(FirstUniqueTransactionId.solve(List.of("a", "b", "c"))).isEqualTo("a");
    }

    @Test
    void allRepeatedReturnsNull() {
        assertThat(FirstUniqueTransactionId.solve(List.of("a", "a", "b", "b"))).isNull();
    }

    @Test
    void emptyList() {
        assertThat(FirstUniqueTransactionId.solve(List.of())).isNull();
    }

    @Test
    void singleElement() {
        assertThat(FirstUniqueTransactionId.solve(List.of("solo"))).isEqualTo("solo");
    }

    @Test
    void uniqueInTheMiddle() {
        assertThat(FirstUniqueTransactionId.solve(List.of("a", "b", "a", "c", "c")))
                .isEqualTo("b");
    }
}
