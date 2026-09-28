package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E01 DedupePreservingOrder. Expected to fail until implemented.
 */
class DedupePreservingOrderTest {

    @Test
    void preservesFirstSeenOrder() {
        assertThat(DedupePreservingOrder.dedupe(List.of("acc-3", "acc-1", "acc-3", "acc-2", "acc-1")))
                .containsExactly("acc-3", "acc-1", "acc-2");
    }

    @Test
    void simpleCase() {
        assertThat(DedupePreservingOrder.dedupe(List.of("a", "b", "a", "c")))
                .containsExactly("a", "b", "c");
    }

    @Test
    void emptyInput() {
        assertThat(DedupePreservingOrder.dedupe(List.of())).isEmpty();
    }

    @Test
    void noDuplicates() {
        assertThat(DedupePreservingOrder.dedupe(List.of("x", "y", "z")))
                .containsExactly("x", "y", "z");
    }
}
