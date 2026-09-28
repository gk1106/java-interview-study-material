package com.gk.study.streams.exercises;

import java.util.List;

/**
 * H02 [Hard] Parallel-stream correctness pitfall. A junior teammate wrote this to sum the
 * squares of every even number in a large list:
 * <pre>{@code
 * long[] total = {0}; // shared mutable state, no synchronization
 * numbers.parallelStream()
 *         .filter(n -> n % 2 == 0)
 *         .forEach(n -> total[0] += (long) n * n); // BUG: racy read-modify-write from many threads
 * return total[0];
 * }</pre>
 * This compiles and often "looks right" on small inputs, but under real parallelism it loses
 * updates (multiple threads read-modify-write {@code total[0]} without synchronization) and
 * produces a different, wrong answer on almost every run.
 * <p>
 * Input:  [1,2,3,4,5,6], threshold irrelevant
 * Output: 56   (2*2 + 4*4 + 6*6 = 4 + 16 + 36)
 * Constraint: must be correct AND safe to run with {@code .parallel()} on millions of elements —
 * no shared mutable counter, no external synchronization.
 * Pattern: parallel-safe reduction (a primitive stream's {@code sum()}, or
 * {@code Collectors.summingLong})
 */
public class FixParallelStreamSideEffectBug {

    /**
     * @param numbers list of integers (may be large; must be safe to process in parallel)
     * @return sum of the squares of every even number in {@code numbers}
     */
    public static long solve(List<Integer> numbers) {
        // TODO: implement — do NOT use a shared mutable accumulator
        throw new UnsupportedOperationException("TODO");
    }
}
