package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkippingIteratorTest {

    @Test
    void yieldsEveryThirdElement() {
        List<Integer> source = List.of(10, 20, 30, 40, 50, 60);
        SkippingIterator<Integer> it = new SkippingIterator<>(source.iterator(), 3);
        List<Integer> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertThat(result).containsExactly(10, 40);
    }

    @Test
    void nOfOneYieldsEveryElement() {
        List<Integer> source = List.of(1, 2, 3);
        SkippingIterator<Integer> it = new SkippingIterator<>(source.iterator(), 1);
        List<Integer> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add(it.next());
        }
        assertThat(result).containsExactly(1, 2, 3);
    }
}
