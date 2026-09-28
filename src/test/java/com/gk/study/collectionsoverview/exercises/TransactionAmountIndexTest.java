package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E04 TransactionAmountIndex. Expected to fail until implemented.
 */
class TransactionAmountIndexTest {

    @Test
    void rangeQueryIsInclusive() {
        TransactionAmountIndex idx = new TransactionAmountIndex(List.of(50, 150, 200, 400, 900));

        assertThat(idx.between(100, 400)).containsExactly(150, 200, 400);
    }

    @Test
    void rangeQueryWithNoMatchesIsEmpty() {
        TransactionAmountIndex idx = new TransactionAmountIndex(List.of(50, 150, 200));

        assertThat(idx.between(0, 49)).isEmpty();
    }

    @Test
    void addExpandsFutureQueries() {
        TransactionAmountIndex idx = new TransactionAmountIndex(List.of(50, 150, 200, 400, 900));
        idx.add(120);

        assertThat(idx.between(100, 400)).containsExactly(120, 150, 200, 400);
    }

    @Test
    void exactBoundaryValuesIncluded() {
        TransactionAmountIndex idx = new TransactionAmountIndex(List.of(100, 200, 300));

        assertThat(idx.between(100, 300)).containsExactly(100, 200, 300);
    }
}
