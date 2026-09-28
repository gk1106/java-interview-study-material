package com.gk.study.streams.solutions;

import java.util.stream.Collector;

/**
 * Solution for {@link com.gk.study.streams.exercises.CustomStatsCollector}.
 *
 * <p>A hand-built {@link Collector}, the same four building blocks {@code Collectors.toList()}
 * etc. are built from under the hood:
 * <ul>
 *   <li><b>supplier</b> — creates a fresh, empty mutable {@link Box} per thread/sub-task</li>
 *   <li><b>accumulator</b> — folds one element into a {@link Box}</li>
 *   <li><b>combiner</b> — merges two {@link Box}es from different sub-tasks (used when the
 *       stream runs in parallel and the source was split); must be associative</li>
 *   <li><b>finisher</b> — converts the final {@link Box} into the public {@link Stats} result</li>
 * </ul>
 * O(n) time (one accumulate per element, O(1) combines proportional to the split count), O(1)
 * extra space per partial accumulator.
 */
public final class CustomStatsCollectorSolution {

    public record Stats(long count, long sum, int min, int max) {
    }

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

    private CustomStatsCollectorSolution() {
    }

    public static Collector<Integer, ?, Stats> toStats() {
        return Collector.of(Box::new, Box::accept, Box::combine, Box::toStats);
    }
}
