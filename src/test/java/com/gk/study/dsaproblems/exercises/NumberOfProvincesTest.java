package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link NumberOfProvinces} exercise stub. EXPECTED TO FAIL until implemented.
 */
class NumberOfProvincesTest {

    @Test
    void typicalInput() {
        int[][] isConnected = {
            {1, 1, 0},
            {1, 1, 0},
            {0, 0, 1}
        };
        assertThat(NumberOfProvinces.solve(isConnected)).isEqualTo(2);
    }

    @Test
    void allSeparateProvinces() {
        int[][] isConnected = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        assertThat(NumberOfProvinces.solve(isConnected)).isEqualTo(3);
    }

    @Test
    void allConnectedIsOneProvince() {
        int[][] isConnected = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        assertThat(NumberOfProvinces.solve(isConnected)).isEqualTo(1);
    }

    @Test
    void singleAccount() {
        int[][] isConnected = {{1}};
        assertThat(NumberOfProvinces.solve(isConnected)).isEqualTo(1);
    }

    @Test
    void transitiveConnectionMergesIntoOneProvince() {
        // 0-1 connected, 1-2 connected, 0-2 not directly connected -> still one province
        int[][] isConnected = {
            {1, 1, 0},
            {1, 1, 1},
            {0, 1, 1}
        };
        assertThat(NumberOfProvinces.solve(isConnected)).isEqualTo(1);
    }

    @Test
    void largerInputManyIsolatedPairs() {
        int n = 100;
        int[][] isConnected = new int[n][n];
        for (int i = 0; i < n; i++) {
            isConnected[i][i] = 1;
        }
        // pair up (0,1), (2,3), (4,5), ... each pair forms its own province
        for (int i = 0; i + 1 < n; i += 2) {
            isConnected[i][i + 1] = 1;
            isConnected[i + 1][i] = 1;
        }
        assertThat(NumberOfProvinces.solve(isConnected)).isEqualTo(n / 2);
    }
}
