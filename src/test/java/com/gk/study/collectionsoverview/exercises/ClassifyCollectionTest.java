package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E01 ClassifyCollection. Expected to fail with
 * UnsupportedOperationException until the learner implements the TODO.
 */
class ClassifyCollectionTest {

    @Test
    void classifiesList() {
        assertThat(ClassifyCollection.classify(new ArrayList<Integer>())).isEqualTo("List");
    }

    @Test
    void classifiesSet() {
        assertThat(ClassifyCollection.classify(new HashSet<Integer>())).isEqualTo("Set");
    }

    @Test
    void classifiesDequeAsDequeNotQueue() {
        assertThat(ClassifyCollection.classify(new ArrayDeque<Integer>())).isEqualTo("Deque");
    }

    @Test
    void classifiesPriorityQueueAsQueue() {
        assertThat(ClassifyCollection.classify(new PriorityQueue<Integer>())).isEqualTo("Queue");
    }

    @Test
    void classifiesMap() {
        assertThat(ClassifyCollection.classify(new HashMap<String, Integer>())).isEqualTo("Map");
    }

    @Test
    void classifiesUnrelatedObject() {
        assertThat(ClassifyCollection.classify("hello")).isEqualTo("not a collection type");
    }
}
