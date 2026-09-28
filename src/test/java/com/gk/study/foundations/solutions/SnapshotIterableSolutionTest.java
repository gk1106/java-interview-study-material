package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotIterableSolutionTest {

    @Test
    void concurrentAddDuringIterationDoesNotThrowAndIsInvisible() {
        SnapshotIterableSolution<String> list = new SnapshotIterableSolution<>();
        list.add("a");
        Iterator<String> it = list.iterator();
        list.add("b");
        List<String> seen = new ArrayList<>();
        while (it.hasNext()) {
            seen.add(it.next());
        }
        assertThat(seen).containsExactly("a");
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void freshIteratorSeesLatestState() {
        SnapshotIterableSolution<String> list = new SnapshotIterableSolution<>();
        list.add("a");
        list.add("b");
        List<String> seen = new ArrayList<>();
        for (String s : list) {
            seen.add(s);
        }
        assertThat(seen).containsExactly("a", "b");
    }

    @Test
    void emptyListIterationYieldsNothing() {
        SnapshotIterableSolution<String> list = new SnapshotIterableSolution<>();
        List<String> seen = new ArrayList<>();
        for (String s : list) {
            seen.add(s);
        }
        assertThat(seen).isEmpty();
    }
}
