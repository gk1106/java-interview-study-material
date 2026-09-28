package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class FirstUniqueTransactionIdSolutionTest {

    @Test
    void typicalInput() {
        assertThat(FirstUniqueTransactionIdSolution.solve(List.of("t1", "t2", "t1", "t3")))
                .isEqualTo("t2");
    }

    @Test
    void allUniqueReturnsFirst() {
        assertThat(FirstUniqueTransactionIdSolution.solve(List.of("a", "b", "c")))
                .isEqualTo("a");
    }

    @Test
    void allRepeatedReturnsNull() {
        assertThat(FirstUniqueTransactionIdSolution.solve(List.of("a", "a", "b", "b"))).isNull();
    }

    @Test
    void emptyList() {
        assertThat(FirstUniqueTransactionIdSolution.solve(List.of())).isNull();
    }

    @Test
    void singleElement() {
        assertThat(FirstUniqueTransactionIdSolution.solve(List.of("solo"))).isEqualTo("solo");
    }

    @Test
    void uniqueInTheMiddle() {
        assertThat(FirstUniqueTransactionIdSolution.solve(List.of("a", "b", "a", "c", "c")))
                .isEqualTo("b");
    }
}
