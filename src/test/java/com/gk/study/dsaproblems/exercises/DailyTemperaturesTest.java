package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link DailyTemperatures} exercise stub. EXPECTED TO FAIL until implemented.
 */
class DailyTemperaturesTest {

    @Test
    void typicalInput() {
        int[] temps = {73, 74, 75, 71, 69, 72, 76, 73};
        assertThat(DailyTemperatures.solve(temps)).containsExactly(1, 1, 4, 2, 1, 1, 0, 0);
    }

    @Test
    void strictlyDecreasingNeverWarmer() {
        int[] temps = {80, 70, 60, 50};
        assertThat(DailyTemperatures.solve(temps)).containsExactly(0, 0, 0, 0);
    }

    @Test
    void strictlyIncreasingEachWaitsOneDay() {
        int[] temps = {50, 60, 70, 80};
        assertThat(DailyTemperatures.solve(temps)).containsExactly(1, 1, 1, 0);
    }

    @Test
    void singleDay() {
        assertThat(DailyTemperatures.solve(new int[] {70})).containsExactly(0);
    }

    @Test
    void equalTemperaturesNeverCountAsWarmer() {
        int[] temps = {70, 70, 70};
        assertThat(DailyTemperatures.solve(temps)).containsExactly(0, 0, 0);
    }

    @Test
    void largerInput() {
        int n = 3000;
        int[] temps = new int[n];
        for (int i = 0; i < n; i++) {
            temps[i] = n - i; // strictly decreasing -> nobody ever finds a warmer future day
        }
        int[] expected = new int[n];
        assertThat(DailyTemperatures.solve(temps)).containsExactly(expected);
    }
}
