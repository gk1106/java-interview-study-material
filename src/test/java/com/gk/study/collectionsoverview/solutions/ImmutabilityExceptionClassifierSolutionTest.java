package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ImmutabilityExceptionClassifierSolutionTest {

    @Test
    void classifiesAllScenarios() {
        Map<String, String> outcomes = ImmutabilityExceptionClassifierSolution.classify();

        assertThat(outcomes)
                .containsEntry("listOfNull", "NullPointerException")
                .containsEntry("setOfDuplicate", "IllegalArgumentException")
                .containsEntry("unmodifiableWithNullConstruction", "OK")
                .containsEntry("unmodifiableMutation", "UnsupportedOperationException")
                .containsEntry("listOfMutation", "UnsupportedOperationException");
    }

    @Test
    void resultHasExactlyFiveScenarios() {
        assertThat(ImmutabilityExceptionClassifierSolution.classify()).hasSize(5);
    }
}
