package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E02 ViewVsTrueImmutability. Expected to fail until implemented.
 */
class ViewVsTrueImmutabilityTest {

    @Test
    void viewReflectsBackingMutationTrulyImmutableDoesNot() {
        ViewVsTrueImmutability.Result result = ViewVsTrueImmutability.run(List.of("a", "b"), "c");

        assertThat(result.view()).containsExactly("a", "b", "c");
        assertThat(result.trulyImmutable()).containsExactly("a", "b");
    }

    @Test
    void worksWithSingleElementInitialContents() {
        ViewVsTrueImmutability.Result result = ViewVsTrueImmutability.run(List.of("only"), "second");

        assertThat(result.view()).containsExactly("only", "second");
        assertThat(result.trulyImmutable()).containsExactly("only");
    }

    @Test
    void worksWithEmptyInitialContents() {
        ViewVsTrueImmutability.Result result = ViewVsTrueImmutability.run(List.of(), "first");

        assertThat(result.view()).containsExactly("first");
        assertThat(result.trulyImmutable()).isEmpty();
    }
}
