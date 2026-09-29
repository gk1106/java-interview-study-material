package com.gk.study.dsaproblems.exercises;

/**
 * M07 [Medium] Given a list of daily temperatures, return an array where {@code answer[i]} is
 * the number of days you'd have to wait after day {@code i} for a strictly warmer temperature;
 * if there is no future warmer day, {@code answer[i] = 0}.
 * Input: [73,74,75,71,69,72,76,73] &rarr; Output: [1,1,4,2,1,1,0,0]
 * Constraint: O(n) time (amortized — each index is pushed and popped at most once).
 * Pattern: monotonic (decreasing) stack of indices
 * Collections: Deque (holding indices, used as a stack)
 */
public class DailyTemperatures {

    public static int[] solve(int[] temperatures) {
        // TODO: implement using a Deque<Integer> of indices whose temperatures are in
        // decreasing order from bottom to top; for each new day, pop every stacked index whose
        // temperature is lower than today's (today is their answer, at distance i - poppedIndex),
        // then push today's index
        throw new UnsupportedOperationException("TODO");
    }
}
