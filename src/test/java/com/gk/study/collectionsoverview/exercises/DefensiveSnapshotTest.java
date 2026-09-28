package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for E03 DefensiveSnapshot. Expected to fail until implemented.
 */
class DefensiveSnapshotTest {

    @Test
    void snapshotUnaffectedByLaterMutationOfSource() {
        List<String> source = new ArrayList<>(List.of("a", "b"));
        List<String> snapshot = DefensiveSnapshot.of(source);

        source.add("c");

        assertThat(snapshot).containsExactly("a", "b");
    }

    @Test
    void snapshotRejectsMutation() {
        List<String> snapshot = DefensiveSnapshot.of(new ArrayList<>(List.of("a")));

        assertThatThrownBy(() -> snapshot.add("z"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void worksWhenSourceIsAlreadyImmutable() {
        List<String> snapshot = DefensiveSnapshot.of(List.of("x", "y"));

        assertThat(snapshot).containsExactly("x", "y");
    }

    @Test
    void worksWithEmptySource() {
        assertThat(DefensiveSnapshot.of(new ArrayList<String>())).isEmpty();
    }
}
