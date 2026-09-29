package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link RedundantConnection} exercise stub. EXPECTED TO FAIL until implemented.
 */
class RedundantConnectionTest {

    @Test
    void typicalInput() {
        int[][] edges = {{1, 2}, {1, 3}, {2, 3}};
        assertThat(RedundantConnection.solve(edges)).containsExactly(2, 3);
    }

    @Test
    void redundantEdgeAtTheEnd() {
        int[][] edges = {{1, 2}, {2, 3}, {3, 4}, {1, 4}, {1, 5}};
        assertThat(RedundantConnection.solve(edges)).containsExactly(1, 4);
    }

    @Test
    void smallestTriangle() {
        int[][] edges = {{1, 2}, {2, 3}, {1, 3}};
        assertThat(RedundantConnection.solve(edges)).containsExactly(1, 3);
    }

    @Test
    void redundantEdgeIsADuplicate() {
        int[][] edges = {{1, 2}, {2, 3}, {3, 1}};
        assertThat(RedundantConnection.solve(edges)).containsExactly(3, 1);
    }

    @Test
    void starShapedGraph() {
        int[][] edges = {{1, 2}, {1, 3}, {1, 4}, {2, 3}};
        assertThat(RedundantConnection.solve(edges)).containsExactly(2, 3);
    }

    @Test
    void largerInputLongChainPlusOneClosingEdge() {
        int n = 100;
        int[][] edges = new int[n][2];
        for (int i = 1; i < n; i++) {
            edges[i - 1] = new int[] {i, i + 1}; // chain 1-2-3-...-n
        }
        edges[n - 1] = new int[] {1, n}; // closes the chain into a cycle
        assertThat(RedundantConnection.solve(edges)).containsExactly(1, n);
    }
}
