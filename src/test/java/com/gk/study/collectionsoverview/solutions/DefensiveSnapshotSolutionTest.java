package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefensiveSnapshotSolutionTest {

    @Test
    void snapshotUnaffectedByLaterMutationOfSource() {
        List<String> source = new ArrayList<>(List.of("a", "b"));
        List<String> snapshot = DefensiveSnapshotSolution.of(source);

        source.add("c");

        assertThat(snapshot).containsExactly("a", "b");
    }

    @Test
    void snapshotRejectsMutation() {
        List<String> snapshot = DefensiveSnapshotSolution.of(new ArrayList<>(List.of("a")));

        assertThatThrownBy(() -> snapshot.add("z"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void worksWhenSourceIsAlreadyImmutable() {
        List<String> snapshot = DefensiveSnapshotSolution.of(List.of("x", "y"));

        assertThat(snapshot).containsExactly("x", "y");
    }

    @Test
    void worksWithEmptySource() {
        assertThat(DefensiveSnapshotSolution.of(new ArrayList<String>())).isEmpty();
    }
}
