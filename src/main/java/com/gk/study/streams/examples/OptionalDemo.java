package com.gk.study.streams.examples;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Demonstrates creating and chaining {@link Optional} the way production code should: never
 * calling {@code get()} blind, and preferring {@code map/flatMap/filter} chains over manual
 * null checks.
 */
public class OptionalDemo {

    private static final Map<String, String> CONFIG = Map.of("timeoutMs", "3000");

    public static void main(String[] args) {
        // Creation
        Optional<String> present = Optional.of("hello");
        Optional<String> maybeAbsent = Optional.ofNullable(lookup("missing-key"));
        Optional<String> empty = Optional.empty();
        System.out.println("present=" + present + " maybeAbsent=" + maybeAbsent + " empty=" + empty);

        // map/filter chaining instead of nested null checks
        int timeout = Optional.ofNullable(CONFIG.get("timeoutMs"))
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .orElse(1000);
        System.out.println("timeout = " + timeout);

        // flatMap to avoid Optional<Optional<T>>
        Optional<String> city = findCustomer("C1").flatMap(OptionalDemo::findCity);
        System.out.println("city = " + city.orElse("UNKNOWN"));

        // orElse vs orElseGet: orElse's argument is ALWAYS evaluated eagerly, even when the
        // Optional is present — for a cheap constant that's harmless, for an expensive call it's
        // wasted work. orElseGet's supplier only runs when needed.
        System.out.println("orElse (eager arg always built): " + present.orElse(expensiveDefault()));
        System.out.println("orElseGet (lazy, only runs if empty): " + present.orElseGet(OptionalDemo::expensiveDefault));

        // orElseThrow with a specific exception, not a bare get()
        try {
            empty.orElseThrow(() -> new IllegalStateException("no value configured"));
        } catch (IllegalStateException e) {
            System.out.println("caught expected: " + e.getMessage());
        }

        // ifPresentOrElse for the "do X if present, else do Y" pattern
        empty.ifPresentOrElse(
                v -> System.out.println("has value: " + v),
                () -> System.out.println("ifPresentOrElse: nothing present, ran the else branch"));

        // Do NOT combine list.stream().filter(...).findFirst() with a bare get() — check first.
        List<Integer> numbers = List.of(2, 4, 6);
        Optional<Integer> firstOdd = numbers.stream().filter(n -> n % 2 != 0).findFirst();
        System.out.println("firstOdd present? " + firstOdd.isPresent() + " -> " + firstOdd.orElse(-1));
    }

    private static String lookup(String key) {
        return null; // simulates a lookup that legitimately has no value
    }

    private static Optional<String> findCustomer(String id) {
        return Optional.of("cust-" + id);
    }

    private static Optional<String> findCity(String customerRecord) {
        return Optional.of("Mumbai");
    }

    private static String expensiveDefault() {
        System.out.println("  (expensiveDefault() was actually invoked)");
        return "computed-default";
    }
}
