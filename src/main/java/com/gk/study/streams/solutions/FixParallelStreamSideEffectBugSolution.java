package com.gk.study.streams.solutions;

import java.util.List;

/**
 * Solution for {@link com.gk.study.streams.exercises.FixParallelStreamSideEffectBug}.
 *
 * <p>Instead of a shared {@code long[]} mutated from many threads via {@code forEach} (a lost-
 * update race), this filters, maps to a primitive {@code long} and calls the built-in,
 * parallel-safe {@code sum()} terminal operation — no external synchronization needed, and it
 * produces the same correct total whether run sequentially or in parallel. O(n) time, O(1) space.
 */
public final class FixParallelStreamSideEffectBugSolution {

    private FixParallelStreamSideEffectBugSolution() {
    }

    public static long solve(List<Integer> numbers) {
        return numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> (long) n * n)
                .sum();
    }
}
