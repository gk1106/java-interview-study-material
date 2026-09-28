package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class BestTimeToBuyStockSolutionTest {

    @Test
    void typicalInput() {
        assertThat(BestTimeToBuyStockSolution.solve(List.of(7, 1, 5, 3, 6, 4))).isEqualTo(5);
    }

    @Test
    void monotonicDecreasingNoProfit() {
        assertThat(BestTimeToBuyStockSolution.solve(List.of(7, 6, 4, 3, 1))).isZero();
    }

    @Test
    void emptyList() {
        assertThat(BestTimeToBuyStockSolution.solve(List.of())).isZero();
    }

    @Test
    void singlePrice() {
        assertThat(BestTimeToBuyStockSolution.solve(List.of(5))).isZero();
    }

    @Test
    void allSamePrice() {
        assertThat(BestTimeToBuyStockSolution.solve(List.of(4, 4, 4, 4))).isZero();
    }

    @Test
    void bestProfitAtTheEnd() {
        assertThat(BestTimeToBuyStockSolution.solve(List.of(3, 2, 6, 5, 0, 10))).isEqualTo(10);
    }
}
