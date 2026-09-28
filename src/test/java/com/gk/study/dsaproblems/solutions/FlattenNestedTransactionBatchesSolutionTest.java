package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class FlattenNestedTransactionBatchesSolutionTest {

    @Test
    void mixedNesting() {
        List<Object> input = List.of(1, List.of(2, 3, List.of(4)), 5);
        assertThat(FlattenNestedTransactionBatchesSolution.solve(input))
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void flatListNoNesting() {
        List<Object> input = List.of(1, 2, 3);
        assertThat(FlattenNestedTransactionBatchesSolution.solve(input)).containsExactly(1, 2, 3);
    }

    @Test
    void emptyInput() {
        assertThat(FlattenNestedTransactionBatchesSolution.solve(List.of())).isEmpty();
    }

    @Test
    void singleElement() {
        assertThat(FlattenNestedTransactionBatchesSolution.solve(List.of(42)))
                .containsExactly(42);
    }

    @Test
    void deeplyNestedSingleChain() {
        Object innermost = List.of(99);
        Object level2 = List.of(innermost);
        Object level1 = List.of(level2);
        assertThat(FlattenNestedTransactionBatchesSolution.solve(List.of(level1)))
                .containsExactly(99);
    }

    @Test
    void emptyNestedBatchesAreSkipped() {
        List<Object> input = List.of(1, List.of(), 2, List.of(List.of()), 3);
        assertThat(FlattenNestedTransactionBatchesSolution.solve(input))
                .containsExactly(1, 2, 3);
    }

    @Test
    void unsupportedElementTypeThrows() {
        List<Object> input = List.of(1, "not-a-number");
        assertThatThrownBy(() -> FlattenNestedTransactionBatchesSolution.solve(input))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
