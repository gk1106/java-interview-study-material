package com.gk.study.streams.exercises;

import java.util.stream.Collector;

/**
 * H01 [Hard] "Build it yourself" for this module: implement a CUSTOM {@link Collector} from
 * scratch (supplier / accumulator / combiner / finisher) that computes count, sum, min and max of
 * a stream of {@code Integer} in a single pass — without using {@code Collectors.summarizingInt}.
 * Input:  Stream.of(4, 1, 7, 3, 9, 2)
 * Output: Stats[count=6, sum=26, min=1, max=9]
 * Constraint: must work correctly whether the source stream is sequential or parallel — the
 * combiner must correctly merge two partial {@link Box} accumulators from different threads.
 * Pattern: {@code Collector.of(supplier, accumulator, combiner, finisher)}
 *
 * <p>The mutable accumulator ({@link Box}) is provided fully implemented below — your job is to
 * wire it into a correct {@link Collector} via {@code toStats()}.
 */
public class CustomStatsCollector {

    public record Stats(long count, long sum, int min, int max) {
    }

    /** Mutable per-thread accumulator used while collecting; provided, not part of the TODO. */
    static final class Box {
        long count = 0;
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        void accept(int value) {
            count++;
            sum += value;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        Box combine(Box other) {
            Box merged = new Box();
            merged.count = this.count + other.count;
            merged.sum = this.sum + other.sum;
            merged.min = Math.min(this.min, other.min);
            merged.max = Math.max(this.max, other.max);
            return merged;
        }

        Stats toStats() {
            return new Stats(count, sum, min, max);
        }
    }

    /**
     * @return a {@link Collector} that reduces a stream of {@code Integer} to a {@link Stats}
     *     in a single pass, using {@link Box} as the mutable intermediate accumulator
     */
    public static Collector<Integer, ?, Stats> toStats() {
        // TODO: implement using Collector.of(Box::new, Box::accept, Box::combine, Box::toStats)
        throw new UnsupportedOperationException("TODO");
    }
}
