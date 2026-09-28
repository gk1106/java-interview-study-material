package com.gk.study.dsaproblems.solutions;

import java.util.List;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.BestTimeToBuyStock}.
 *
 * <p>Single pass tracking the minimum price seen so far; at each day, the best possible profit
 * if selling today is {@code price - minSoFar}. O(n) time, O(1) space.
 */
public final class BestTimeToBuyStockSolution {

    private BestTimeToBuyStockSolution() {
    }

    public static int solve(List<Integer> prices) {
        int minSoFar = Integer.MAX_VALUE;
        int maxProfit = 0;
        for (int price : prices) {
            minSoFar = Math.min(minSoFar, price);
            maxProfit = Math.max(maxProfit, price - minSoFar);
        }
        return maxProfit;
    }
}
