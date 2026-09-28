package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleFailFastListSolutionTest {

    @Test
    void iteratesAllElementsWhenUnmodified() {
        SimpleFailFastListSolution<String> list = new SimpleFailFastListSolution<>();
        list.add("a");
        list.add("b");
        list.add("c");
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }
        assertThat(sb.toString()).isEqualTo("abc");
    }

    @Test
    void throwsOnConcurrentModification() {
        SimpleFailFastListSolution<Integer> list = new SimpleFailFastListSolution<>();
        list.add(1);
        list.add(2);
        Iterator<Integer> it = list.iterator();
        list.add(3);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void exhaustedIteratorThrowsNoSuchElement() {
        SimpleFailFastListSolution<Integer> list = new SimpleFailFastListSolution<>();
        list.add(1);
        Iterator<Integer> it = list.iterator();
        it.next();
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void growsPastInitialCapacity() {
        SimpleFailFastListSolution<Integer> list = new SimpleFailFastListSolution<>();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }
        assertThat(list.size()).isEqualTo(50);
    }
}
