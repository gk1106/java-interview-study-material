package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E04 ImmutabilityExceptionClassifier. Expected to fail until implemented.
 */
class ImmutabilityExceptionClassifierTest {

    @Test
    void classifiesAllScenarios() {
        Map<String, String> outcomes = ImmutabilityExceptionClassifier.classify();

        assertThat(outcomes)
                .containsEntry("listOfNull", "NullPointerException")
                .containsEntry("setOfDuplicate", "IllegalArgumentException")
                .containsEntry("unmodifiableWithNullConstruction", "OK")
                .containsEntry("unmodifiableMutation", "UnsupportedOperationException")
                .containsEntry("listOfMutation", "UnsupportedOperationException");
    }

    @Test
    void resultHasExactlyFiveScenarios() {
        assertThat(ImmutabilityExceptionClassifier.classify()).hasSize(5);
    }
}
