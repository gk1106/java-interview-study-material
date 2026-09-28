package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepEqualsMatrixTest {

    @Test
    void differentInstancesSameContentAreEqual() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 4}};
        assertThat(DeepEqualsMatrix.matricesEqual(a, b)).isTrue();
    }

    @Test
    void differentContentIsNotEqual() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 5}};
        assertThat(DeepEqualsMatrix.matricesEqual(a, b)).isFalse();
    }

    @Test
    void jaggedArraysWithDifferentRowLengths() {
        int[][] a = {{1, 2, 3}, {4}};
        int[][] b = {{1, 2}, {4}};
        assertThat(DeepEqualsMatrix.matricesEqual(a, b)).isFalse();
    }

    @Test
    void bothNullIsEqual() {
        assertThat(DeepEqualsMatrix.matricesEqual(null, null)).isTrue();
    }
}
