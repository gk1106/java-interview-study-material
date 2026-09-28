package com.gk.study.streams.examples;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * {@code IntStream}/{@code LongStream}/{@code DoubleStream}, {@code boxed()}, and
 * {@code Stream.iterate}/{@code Stream.generate} (bounded and infinite).
 */
public class PrimitiveStreamsDemo {

    public static void main(String[] args) {
        // IntStream avoids boxing every element into an Integer.
        int sum = IntStream.rangeClosed(1, 100).sum();
        System.out.println("sum 1..100 via IntStream = " + sum);

        IntStream.of(4, 1, 7, 3).summaryStatistics();
        var stats = IntStream.of(4, 1, 7, 3).summaryStatistics();
        System.out.println("stats: min=" + stats.getMin() + " max=" + stats.getMax()
                + " avg=" + stats.getAverage() + " count=" + stats.getCount());

        // boxed() converts IntStream -> Stream<Integer> only when you actually need objects
        // (e.g. to put them in a List<Integer> or pass to a generic Collectors method).
        List<Integer> boxedList = IntStream.range(0, 5).boxed().toList();
        System.out.println("boxed list: " + boxedList);

        // Stream.iterate with a seed + UnaryOperator — classic infinite stream, needs limit().
        List<Integer> powersOfTwo = Stream.iterate(1, n -> n * 2).limit(8).toList();
        System.out.println("powers of two: " + powersOfTwo);

        // Stream.iterate(seed, hasNext, next) — Java 9+ bounded form, self-terminating,
        // equivalent to a classic for-loop, no limit() needed.
        List<Integer> under50 = Stream.iterate(1, n -> n < 50, n -> n * 2).toList();
        System.out.println("iterate with predicate (< 50): " + under50);

        // Stream.generate takes a Supplier with no relation between elements — must be limited
        // manually, there is no natural stopping point.
        List<Double> fixedNoise = Stream.generate(() -> 0.5).limit(4).toList();
        System.out.println("generate (constant supplier) x4: " + fixedNoise);

        // Avoiding autoboxing overhead: prefer IntStream.sum()/average() over
        // Stream<Integer>.reduce(Integer::sum), which boxes every intermediate result.
        int viaIntStream = IntStream.rangeClosed(1, 1_000_000).sum();
        long viaBoxedReduce = Stream.iterate(1, n -> n + 1)
                .limit(1_000_000)
                .mapToInt(Integer::intValue) // demonstrate converting back down when needed
                .sum();
        System.out.println("viaIntStream=" + viaIntStream + " viaBoxedReduce=" + viaBoxedReduce);
    }
}
