package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PecsCopySolutionTest {

    @Test
    void copiesIntegersIntoNumberList() {
        List<Integer> src = List.of(1, 2, 3);
        List<Number> dest = new ArrayList<>();
        PecsCopySolution.copy(src, dest);
        assertThat(dest).containsExactly(1, 2, 3);
    }

    @Test
    void appendsRatherThanOverwrites() {
        List<Integer> src = List.of(3, 4);
        List<Number> dest = new ArrayList<>();
        dest.add(1);
        dest.add(2);
        PecsCopySolution.copy(src, dest);
        assertThat(dest).containsExactly(1, 2, 3, 4);
    }

    @Test
    void copyingEmptySourceLeavesDestUnchanged() {
        List<Integer> src = List.of();
        List<Object> dest = new ArrayList<>();
        dest.add("x");
        PecsCopySolution.copy(src, dest);
        assertThat(dest).containsExactly("x");
    }
}
