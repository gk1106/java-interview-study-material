package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RangeIterableTest {

    @Test
    void iteratesHalfOpenRange() {
        List<Integer> collected = new ArrayList<>();
        for (int value : new RangeIterable(2, 6)) {
            collected.add(value);
        }
        assertThat(collected).containsExactly(2, 3, 4, 5);
    }

    @Test
    void emptyRangeYieldsNothing() {
        List<Integer> collected = new ArrayList<>();
        for (int value : new RangeIterable(5, 5)) {
            collected.add(value);
        }
        assertThat(collected).isEmpty();
    }

    @Test
    void nextThrowsWhenExhausted() {
        Iterator<Integer> it = new RangeIterable(0, 1).iterator();
        it.next();
        assertThrows(NoSuchElementException.class, it::next);
    }
}
