package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RangeIterableSolutionTest {

    @Test
    void iteratesHalfOpenRange() {
        List<Integer> collected = new ArrayList<>();
        for (int value : new RangeIterableSolution(2, 6)) {
            collected.add(value);
        }
        assertThat(collected).containsExactly(2, 3, 4, 5);
    }

    @Test
    void emptyRangeYieldsNothing() {
        List<Integer> collected = new ArrayList<>();
        for (int value : new RangeIterableSolution(5, 5)) {
            collected.add(value);
        }
        assertThat(collected).isEmpty();
    }

    @Test
    void negativeRange() {
        List<Integer> collected = new ArrayList<>();
        for (int value : new RangeIterableSolution(-2, 2)) {
            collected.add(value);
        }
        assertThat(collected).containsExactly(-2, -1, 0, 1);
    }

    @Test
    void nextThrowsWhenExhausted() {
        Iterator<Integer> it = new RangeIterableSolution(0, 1).iterator();
        it.next();
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void freshIteratorEachCall() {
        RangeIterableSolution range = new RangeIterableSolution(0, 2);
        Iterator<Integer> it1 = range.iterator();
        Iterator<Integer> it2 = range.iterator();
        it1.next();
        assertThat(it2.next()).isEqualTo(0); // it2 unaffected by it1's progress
    }
}
