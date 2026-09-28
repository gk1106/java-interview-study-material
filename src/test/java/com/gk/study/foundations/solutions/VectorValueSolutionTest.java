package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VectorValueSolutionTest {

    @Test
    void differentArrayInstancesSameContentAreEqual() {
        VectorValueSolution a = new VectorValueSolution(new int[] {1, 2, 3});
        VectorValueSolution b = new VectorValueSolution(new int[] {1, 2, 3});
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentContentIsNotEqual() {
        VectorValueSolution a = new VectorValueSolution(new int[] {1, 2, 3});
        VectorValueSolution b = new VectorValueSolution(new int[] {1, 2, 4});
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void worksAsHashSetKey() {
        Set<VectorValueSolution> set = new HashSet<>();
        set.add(new VectorValueSolution(new int[] {1, 2}));
        set.add(new VectorValueSolution(new int[] {1, 2}));
        assertThat(set).hasSize(1);
    }

    @Test
    void defensiveCopyPreventsExternalMutation() {
        int[] source = {1, 2, 3};
        VectorValueSolution v = new VectorValueSolution(source);
        source[0] = 99;
        assertThat(v).isEqualTo(new VectorValueSolution(new int[] {1, 2, 3}));
    }
}
