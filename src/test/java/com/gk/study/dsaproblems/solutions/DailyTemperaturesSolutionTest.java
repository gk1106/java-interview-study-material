package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DailyTemperaturesSolutionTest {

    @Test
    void typicalInput() {
        int[] temps = {73, 74, 75, 71, 69, 72, 76, 73};
        assertThat(DailyTemperaturesSolution.solve(temps)).containsExactly(1, 1, 4, 2, 1, 1, 0, 0);
    }

    @Test
    void strictlyDecreasingNeverWarmer() {
        int[] temps = {80, 70, 60, 50};
        assertThat(DailyTemperaturesSolution.solve(temps)).containsExactly(0, 0, 0, 0);
    }

    @Test
    void strictlyIncreasingEachWaitsOneDay() {
        int[] temps = {50, 60, 70, 80};
        assertThat(DailyTemperaturesSolution.solve(temps)).containsExactly(1, 1, 1, 0);
    }

    @Test
    void singleDay() {
        assertThat(DailyTemperaturesSolution.solve(new int[] {70})).containsExactly(0);
    }

    @Test
    void equalTemperaturesNeverCountAsWarmer() {
        int[] temps = {70, 70, 70};
        assertThat(DailyTemperaturesSolution.solve(temps)).containsExactly(0, 0, 0);
    }

    @Test
    void largerInput() {
        int n = 3000;
        int[] temps = new int[n];
        for (int i = 0; i < n; i++) {
            temps[i] = n - i;
        }
        int[] expected = new int[n];
        assertThat(DailyTemperaturesSolution.solve(temps)).containsExactly(expected);
    }
}
