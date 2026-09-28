package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DedupePreservingOrderSolutionTest {

    @Test
    void preservesFirstSeenOrder() {
        assertThat(DedupePreservingOrderSolution.dedupe(List.of("acc-3", "acc-1", "acc-3", "acc-2", "acc-1")))
                .containsExactly("acc-3", "acc-1", "acc-2");
    }

    @Test
    void simpleCase() {
        assertThat(DedupePreservingOrderSolution.dedupe(List.of("a", "b", "a", "c")))
                .containsExactly("a", "b", "c");
    }

    @Test
    void emptyInput() {
        assertThat(DedupePreservingOrderSolution.dedupe(List.of())).isEmpty();
    }

    @Test
    void noDuplicates() {
        assertThat(DedupePreservingOrderSolution.dedupe(List.of("x", "y", "z")))
                .containsExactly("x", "y", "z");
    }
}
