package com.gk.study.streams.examples;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Demonstrates that a stream pipeline does NOTHING until a terminal operation runs, and that
 * short-circuiting terminal ops ({@code findFirst}, {@code anyMatch}, {@code limit}) can stop
 * processing before every element is visited.
 */
public class StreamPipelineLazinessDemo {

    public static void main(String[] args) {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        System.out.println("-- building the pipeline (no terminal op yet) --");
        Stream<Integer> pipeline =
                source.stream()
                        .peek(n -> System.out.println("map stage source: " + n))
                        .map(n -> n * 2)
                        .peek(n -> System.out.println("filter stage input: " + n))
                        .filter(n -> n > 4);
        System.out.println("pipeline built, nothing printed above this line — that's laziness");

        System.out.println("-- now invoking a terminal op: findFirst() --");
        Optional<Integer> first = pipeline.findFirst();
        System.out.println("result = " + first.orElseThrow());

        System.out.println();
        System.out.println("-- short-circuit proof: anyMatch stops as soon as it can answer --");
        boolean found =
                source.stream()
                        .peek(n -> System.out.println("evaluating: " + n))
                        .anyMatch(n -> n == 3);
        System.out.println("anyMatch(== 3) = " + found + " — notice peek never printed past 3");

        System.out.println();
        System.out.println("-- short-circuit proof: limit() truncates an infinite stream --");
        List<Integer> firstFive =
                Stream.iterate(1, n -> n + 1)
                        .peek(n -> System.out.println("generated: " + n))
                        .limit(5)
                        .toList();
        System.out.println("firstFive = " + firstFive);
    }
}
