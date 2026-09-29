package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NumberOfProvincesSolutionTest {

    @Test
    void typicalInput() {
        int[][] isConnected = {
            {1, 1, 0},
            {1, 1, 0},
            {0, 0, 1}
        };
        assertThat(NumberOfProvincesSolution.solve(isConnected)).isEqualTo(2);
    }

    @Test
    void allSeparateProvinces() {
        int[][] isConnected = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        assertThat(NumberOfProvincesSolution.solve(isConnected)).isEqualTo(3);
    }

    @Test
    void allConnectedIsOneProvince() {
        int[][] isConnected = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        assertThat(NumberOfProvincesSolution.solve(isConnected)).isEqualTo(1);
    }

    @Test
    void singleAccount() {
        int[][] isConnected = {{1}};
        assertThat(NumberOfProvincesSolution.solve(isConnected)).isEqualTo(1);
    }

    @Test
    void transitiveConnectionMergesIntoOneProvince() {
        int[][] isConnected = {
            {1, 1, 0},
            {1, 1, 1},
            {0, 1, 1}
        };
        assertThat(NumberOfProvincesSolution.solve(isConnected)).isEqualTo(1);
    }

    @Test
    void largerInputManyIsolatedPairs() {
        int n = 100;
        int[][] isConnected = new int[n][n];
        for (int i = 0; i < n; i++) {
            isConnected[i][i] = 1;
        }
        for (int i = 0; i + 1 < n; i += 2) {
            isConnected[i][i + 1] = 1;
            isConnected[i + 1][i] = 1;
        }
        assertThat(NumberOfProvincesSolution.solve(isConnected)).isEqualTo(n / 2);
    }
}
