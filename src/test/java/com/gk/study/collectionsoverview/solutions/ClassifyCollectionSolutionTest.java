package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import static org.assertj.core.api.Assertions.assertThat;

class ClassifyCollectionSolutionTest {

    @Test
    void classifiesList() {
        assertThat(ClassifyCollectionSolution.classify(new ArrayList<Integer>())).isEqualTo("List");
    }

    @Test
    void classifiesSet() {
        assertThat(ClassifyCollectionSolution.classify(new HashSet<Integer>())).isEqualTo("Set");
    }

    @Test
    void classifiesDequeAsDequeNotQueue() {
        assertThat(ClassifyCollectionSolution.classify(new ArrayDeque<Integer>())).isEqualTo("Deque");
    }

    @Test
    void classifiesPriorityQueueAsQueue() {
        assertThat(ClassifyCollectionSolution.classify(new PriorityQueue<Integer>())).isEqualTo("Queue");
    }

    @Test
    void classifiesMap() {
        assertThat(ClassifyCollectionSolution.classify(new HashMap<String, Integer>())).isEqualTo("Map");
    }

    @Test
    void classifiesUnrelatedObject() {
        assertThat(ClassifyCollectionSolution.classify("hello")).isEqualTo("not a collection type");
    }
}
