package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RedundantConnectionSolutionTest {

    @Test
    void typicalInput() {
        int[][] edges = {{1, 2}, {1, 3}, {2, 3}};
        assertThat(RedundantConnectionSolution.solve(edges)).containsExactly(2, 3);
    }

    @Test
    void redundantEdgeAtTheEnd() {
        int[][] edges = {{1, 2}, {2, 3}, {3, 4}, {1, 4}, {1, 5}};
        assertThat(RedundantConnectionSolution.solve(edges)).containsExactly(1, 4);
    }

    @Test
    void smallestTriangle() {
        int[][] edges = {{1, 2}, {2, 3}, {1, 3}};
        assertThat(RedundantConnectionSolution.solve(edges)).containsExactly(1, 3);
    }

    @Test
    void redundantEdgeIsADuplicate() {
        int[][] edges = {{1, 2}, {2, 3}, {3, 1}};
        assertThat(RedundantConnectionSolution.solve(edges)).containsExactly(3, 1);
    }

    @Test
    void starShapedGraph() {
        int[][] edges = {{1, 2}, {1, 3}, {1, 4}, {2, 3}};
        assertThat(RedundantConnectionSolution.solve(edges)).containsExactly(2, 3);
    }

    @Test
    void largerInputLongChainPlusOneClosingEdge() {
        int n = 100;
        int[][] edges = new int[n][2];
        for (int i = 1; i < n; i++) {
            edges[i - 1] = new int[] {i, i + 1};
        }
        edges[n - 1] = new int[] {1, n};
        assertThat(RedundantConnectionSolution.solve(edges)).containsExactly(1, n);
    }
}
