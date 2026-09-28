package com.gk.study.streams.examples;

import java.util.List;

/**
 * Tour of the core intermediate operations: {@code map}, {@code filter}, {@code flatMap},
 * {@code distinct}, {@code sorted}, {@code limit}, {@code skip}, {@code peek}.
 */
public class IntermediateOpsDemo {

    public static void main(String[] args) {
        List<String> words = List.of("banana", "apple", "cherry", "apple", "date", "banana", "fig");

        System.out.println("map (uppercase): "
                + words.stream().map(String::toUpperCase).toList());

        System.out.println("filter (length > 4): "
                + words.stream().filter(w -> w.length() > 4).toList());

        System.out.println("distinct: " + words.stream().distinct().toList());

        System.out.println("sorted (natural): " + words.stream().distinct().sorted().toList());

        System.out.println("sorted (by length desc): "
                + words.stream().distinct().sorted((a, b) -> b.length() - a.length()).toList());

        System.out.println("limit(3) after sorted: "
                + words.stream().distinct().sorted().limit(3).toList());

        System.out.println("skip(2) after sorted: "
                + words.stream().distinct().sorted().skip(2).toList());

        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        System.out.println("flatMap (flatten list-of-lists): "
                + nested.stream().flatMap(List::stream).toList());

        // peek — fine for debugging/tracing, NOT for driving business logic side effects.
        List<String> traced = words.stream()
                .distinct()
                .peek(w -> System.out.println("[trace] saw: " + w))
                .filter(w -> w.startsWith("a") || w.startsWith("b"))
                .toList();
        System.out.println("result after traced pipeline: " + traced);
    }
}
