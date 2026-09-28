package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FlattenNestedTransactionBatches} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class FlattenNestedTransactionBatchesTest {

    @Test
    void mixedNesting() {
        List<Object> input = List.of(1, List.of(2, 3, List.of(4)), 5);
        assertThat(FlattenNestedTransactionBatches.solve(input)).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void flatListNoNesting() {
        List<Object> input = List.of(1, 2, 3);
        assertThat(FlattenNestedTransactionBatches.solve(input)).containsExactly(1, 2, 3);
    }

    @Test
    void emptyInput() {
        assertThat(FlattenNestedTransactionBatches.solve(List.of())).isEmpty();
    }

    @Test
    void singleElement() {
        assertThat(FlattenNestedTransactionBatches.solve(List.of(42))).containsExactly(42);
    }

    @Test
    void deeplyNestedSingleChain() {
        Object innermost = List.of(99);
        Object level2 = List.of(innermost);
        Object level1 = List.of(level2);
        assertThat(FlattenNestedTransactionBatches.solve(List.of(level1))).containsExactly(99);
    }

    @Test
    void emptyNestedBatchesAreSkipped() {
        List<Object> input = List.of(1, List.of(), 2, List.of(List.of()), 3);
        assertThat(FlattenNestedTransactionBatches.solve(input)).containsExactly(1, 2, 3);
    }
}
