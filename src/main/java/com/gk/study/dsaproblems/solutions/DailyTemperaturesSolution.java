package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.DailyTemperatures}.
 *
 * <p>A monotonic (decreasing) stack of indices: while the current day's temperature is warmer
 * than the temperature at the index on top of the stack, that stacked day has just found its
 * answer (the wait is {@code today - poppedIndex}), so pop and record it, then repeat. Finally
 * push today's index. Each index is pushed once and popped at most once, so this is O(n)
 * amortized time, O(n) space.
 */
public final class DailyTemperaturesSolution {

    private DailyTemperaturesSolution() {
    }

    public static int[] solve(int[] temperatures) {
        int n = temperatures.length;
        int[] answer = new int[n];
        Deque<Integer> stack = new ArrayDeque<>(); // indices, decreasing temperature top to bottom

        for (int today = 0; today < n; today++) {
            while (!stack.isEmpty() && temperatures[stack.peek()] < temperatures[today]) {
                int waitingIndex = stack.pop();
                answer[waitingIndex] = today - waitingIndex;
            }
            stack.push(today);
        }
        return answer;
    }
}
