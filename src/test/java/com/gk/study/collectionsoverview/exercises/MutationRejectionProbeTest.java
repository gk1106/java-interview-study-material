package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E01 MutationRejectionProbe. Expected to fail until implemented.
 */
class MutationRejectionProbeTest {

    @Test
    void mutableArrayListReportsMutable() {
        List<Integer> list = new ArrayList<>(List.of(1, 2));
        assertThat(MutationRejectionProbe.probe(list)).isEqualTo("mutable");
        assertThat(list).containsExactly(1, 2); // unchanged after the probe
    }

    @Test
    void listOfReportsUnsupportedOperationException() {
        assertThat(MutationRejectionProbe.probe(List.of(1, 2)))
                .isEqualTo("UnsupportedOperationException");
    }

    @Test
    void unmodifiableViewReportsUnsupportedOperationException() {
        List<Integer> view = Collections.unmodifiableList(new ArrayList<>(List.of(1)));
        assertThat(MutationRejectionProbe.probe(view)).isEqualTo("UnsupportedOperationException");
    }

    @Test
    void emptyMutableListStillReportsMutable() {
        List<Integer> list = new ArrayList<>();
        assertThat(MutationRejectionProbe.probe(list)).isEqualTo("mutable");
        assertThat(list).isEmpty();
    }
}
