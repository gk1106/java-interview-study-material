package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotIterableTest {

    @Test
    void concurrentAddDuringIterationDoesNotThrowAndIsInvisible() {
        SnapshotIterable<String> list = new SnapshotIterable<>();
        list.add("a");
        Iterator<String> it = list.iterator();
        list.add("b"); // must not throw, and must be invisible to `it`
        List<String> seen = new ArrayList<>();
        while (it.hasNext()) {
            seen.add(it.next());
        }
        assertThat(seen).containsExactly("a");
        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void freshIteratorSeesLatestState() {
        SnapshotIterable<String> list = new SnapshotIterable<>();
        list.add("a");
        list.add("b");
        List<String> seen = new ArrayList<>();
        for (String s : list) {
            seen.add(s);
        }
        assertThat(seen).containsExactly("a", "b");
    }
}
