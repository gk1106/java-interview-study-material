package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkippingIteratorSolutionTest {

    @Test
    void yieldsEveryThirdElement() {
        List<Integer> source = List.of(10, 20, 30, 40, 50, 60);
        SkippingIteratorSolution<Integer> it = new SkippingIteratorSolution<>(source.iterator(), 3);
        List<Integer> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertThat(result).containsExactly(10, 40);
    }

    @Test
    void nOfOneYieldsEveryElement() {
        List<Integer> source = List.of(1, 2, 3);
        SkippingIteratorSolution<Integer> it = new SkippingIteratorSolution<>(source.iterator(), 1);
        List<Integer> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void nLargerThanSizeYieldsOnlyFirstElement() {
        List<Integer> source = List.of(1, 2, 3);
        SkippingIteratorSolution<Integer> it = new SkippingIteratorSolution<>(source.iterator(), 10);
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(1);
        assertThat(it.hasNext()).isFalse();
    }

    @Test
    void exhaustedThrowsNoSuchElement() {
        SkippingIteratorSolution<Integer> it = new SkippingIteratorSolution<>(List.<Integer>of().iterator(), 2);
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void invalidNThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new SkippingIteratorSolution<>(List.of(1).iterator(), 0));
    }
}
