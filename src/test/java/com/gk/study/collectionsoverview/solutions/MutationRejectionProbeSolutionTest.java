package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MutationRejectionProbeSolutionTest {

    @Test
    void mutableArrayListReportsMutable() {
        List<Integer> list = new ArrayList<>(List.of(1, 2));
        assertThat(MutationRejectionProbeSolution.probe(list)).isEqualTo("mutable");
        assertThat(list).containsExactly(1, 2);
    }

    @Test
    void listOfReportsUnsupportedOperationException() {
        assertThat(MutationRejectionProbeSolution.probe(List.of(1, 2)))
                .isEqualTo("UnsupportedOperationException");
    }

    @Test
    void unmodifiableViewReportsUnsupportedOperationException() {
        List<Integer> view = Collections.unmodifiableList(new ArrayList<>(List.of(1)));
        assertThat(MutationRejectionProbeSolution.probe(view)).isEqualTo("UnsupportedOperationException");
    }

    @Test
    void emptyMutableListStillReportsMutable() {
        List<Integer> list = new ArrayList<>();
        assertThat(MutationRejectionProbeSolution.probe(list)).isEqualTo("mutable");
        assertThat(list).isEmpty();
    }
}
