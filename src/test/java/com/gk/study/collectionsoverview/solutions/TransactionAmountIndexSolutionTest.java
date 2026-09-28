package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionAmountIndexSolutionTest {

    @Test
    void rangeQueryIsInclusive() {
        TransactionAmountIndexSolution idx = new TransactionAmountIndexSolution(List.of(50, 150, 200, 400, 900));

        assertThat(idx.between(100, 400)).containsExactly(150, 200, 400);
    }

    @Test
    void rangeQueryWithNoMatchesIsEmpty() {
        TransactionAmountIndexSolution idx = new TransactionAmountIndexSolution(List.of(50, 150, 200));

        assertThat(idx.between(0, 49)).isEmpty();
    }

    @Test
    void addExpandsFutureQueries() {
        TransactionAmountIndexSolution idx = new TransactionAmountIndexSolution(List.of(50, 150, 200, 400, 900));
        idx.add(120);

        assertThat(idx.between(100, 400)).containsExactly(120, 150, 200, 400);
    }

    @Test
    void exactBoundaryValuesIncluded() {
        TransactionAmountIndexSolution idx = new TransactionAmountIndexSolution(List.of(100, 200, 300));

        assertThat(idx.between(100, 300)).containsExactly(100, 200, 300);
    }
}
