package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepEqualsMatrixSolutionTest {

    @Test
    void differentInstancesSameContentAreEqual() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 4}};
        assertThat(DeepEqualsMatrixSolution.matricesEqual(a, b)).isTrue();
    }

    @Test
    void differentContentIsNotEqual() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 5}};
        assertThat(DeepEqualsMatrixSolution.matricesEqual(a, b)).isFalse();
    }

    @Test
    void jaggedArraysWithDifferentRowLengths() {
        int[][] a = {{1, 2, 3}, {4}};
        int[][] b = {{1, 2}, {4}};
        assertThat(DeepEqualsMatrixSolution.matricesEqual(a, b)).isFalse();
    }

    @Test
    void bothNullIsEqual() {
        assertThat(DeepEqualsMatrixSolution.matricesEqual(null, null)).isTrue();
    }

    @Test
    void oneNullOneNonNullIsNotEqual() {
        assertThat(DeepEqualsMatrixSolution.matricesEqual(null, new int[][] {{1}})).isFalse();
        assertThat(DeepEqualsMatrixSolution.matricesEqual(new int[][] {{1}}, null)).isFalse();
    }

    @Test
    void nullRowsHandledSafely() {
        int[][] a = {{1, 2}, null};
        int[][] b = {{1, 2}, null};
        assertThat(DeepEqualsMatrixSolution.matricesEqual(a, b)).isTrue();

        int[][] c = {{1, 2}, {3, 4}};
        assertThat(DeepEqualsMatrixSolution.matricesEqual(a, c)).isFalse();
    }
}
