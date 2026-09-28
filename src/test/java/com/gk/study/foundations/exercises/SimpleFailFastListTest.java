package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleFailFastListTest {

    @Test
    void iteratesAllElementsWhenUnmodified() {
        SimpleFailFastList<String> list = new SimpleFailFastList<>();
        list.add("a");
        list.add("b");
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }
        assertThat(sb.toString()).isEqualTo("ab");
    }

    @Test
    void throwsOnConcurrentModification() {
        SimpleFailFastList<Integer> list = new SimpleFailFastList<>();
        list.add(1);
        list.add(2);
        Iterator<Integer> it = list.iterator();
        list.add(3);
        assertThrows(ConcurrentModificationException.class, it::next);
    }
}
