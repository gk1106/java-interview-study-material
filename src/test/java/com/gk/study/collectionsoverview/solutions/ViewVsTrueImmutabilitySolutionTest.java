package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViewVsTrueImmutabilitySolutionTest {

    @Test
    void viewReflectsBackingMutationTrulyImmutableDoesNot() {
        ViewVsTrueImmutabilitySolution.Result result =
                ViewVsTrueImmutabilitySolution.run(List.of("a", "b"), "c");

        assertThat(result.view()).containsExactly("a", "b", "c");
        assertThat(result.trulyImmutable()).containsExactly("a", "b");
    }

    @Test
    void worksWithSingleElementInitialContents() {
        ViewVsTrueImmutabilitySolution.Result result =
                ViewVsTrueImmutabilitySolution.run(List.of("only"), "second");

        assertThat(result.view()).containsExactly("only", "second");
        assertThat(result.trulyImmutable()).containsExactly("only");
    }

    @Test
    void worksWithEmptyInitialContents() {
        ViewVsTrueImmutabilitySolution.Result result =
                ViewVsTrueImmutabilitySolution.run(List.of(), "first");

        assertThat(result.view()).containsExactly("first");
        assertThat(result.trulyImmutable()).isEmpty();
    }
}
