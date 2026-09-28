package com.gk.study.streams.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Benchmarks sequential vs parallel streams on a large, CPU-bound synthetic workload, and
 * demonstrates two classic parallel-stream pitfalls: sharing mutable state across threads, and
 * assuming {@code forEach} on a parallel stream preserves encounter order.
 *
 * <p>Timings are printed for interest only — nothing in this class (or its test) asserts on
 * wall-clock time, since relative speed depends on the machine, core count and JIT warm-up.
 */
public class ParallelStreamsDemo {

    public static void main(String[] args) {
        List<Integer> data = IntStream.rangeClosed(1, 5_000_000).boxed().toList();

        long startSeq = System.nanoTime();
        long sumSeq = data.stream().mapToLong(ParallelStreamsDemo::costlyTransform).sum();
        long tookSeq = System.nanoTime() - startSeq;

        long startPar = System.nanoTime();
        long sumPar = data.parallelStream().mapToLong(ParallelStreamsDemo::costlyTransform).sum();
        long tookPar = System.nanoTime() - startPar;

        System.out.println("sequential sum=" + sumSeq + " took=" + (tookSeq / 1_000_000) + "ms");
        System.out.println("parallel   sum=" + sumPar + " took=" + (tookPar / 1_000_000) + "ms");
        System.out.println("both sums equal (correctness, not speed, is what we assert in tests): " + (sumSeq == sumPar));
        System.out.println("common pool parallelism (shared by ALL parallel streams in this JVM): "
                + ForkJoinPool.commonPool().getParallelism());

        // PITFALL 1: shared mutable state without synchronization -> lost updates / wrong result.
        // DO NOT do this:
        List<Integer> unsafeAccumulator = new ArrayList<>();
        data.stream().limit(50_000).parallel().forEach(unsafeAccumulator::add); // racy structural mutation
        System.out.println("unsafe accumulator size (often < 50000, sometimes throws): "
                + unsafeAccumulator.size());

        // FIX: use a proper parallel-safe reduction/collector instead of a shared mutable list.
        List<Integer> safeResult = data.stream().limit(50_000).parallel().collect(Collectors.toList());
        System.out.println("safe collect size (always exactly 50000): " + safeResult.size());

        // FIX (alternative): a concurrency-aware accumulator such as LongAdder for counting.
        LongAdder counter = new LongAdder();
        data.parallelStream().limit(50_000).forEach(n -> counter.increment());
        System.out.println("LongAdder count (always exactly 50000): " + counter.sum());

        // PITFALL 2: forEach on a parallel stream does NOT preserve encounter order.
        // forEachOrdered does, but forces re-synchronization and gives up most of the
        // parallelism benefit — use it only when order genuinely matters.
        System.out.println("parallel forEach order (will likely look shuffled):");
        data.stream().limit(10).parallel().forEach(n -> System.out.print(n + " "));
        System.out.println();
        System.out.println("parallel forEachOrdered (always 1..10 in order):");
        data.stream().limit(10).parallel().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();
    }

    /** Simulates a small amount of CPU-bound work per element so parallelism has a chance to pay off. */
    private static long costlyTransform(int n) {
        long acc = n;
        for (int i = 0; i < 20; i++) {
            acc = (acc * 31 + i) % 1_000_000_007L;
        }
        return acc;
    }
}
