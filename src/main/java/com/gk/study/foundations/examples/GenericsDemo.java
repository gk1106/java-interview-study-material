package com.gk.study.foundations.examples;

import java.util.ArrayList;
import java.util.List;

/** Demonstrates bounded type parameters, PECS wildcards, and a raw-type ClassCastException trap. */
public class GenericsDemo {

    public static void main(String[] args) {
        System.out.println("max(3, 7) = " + max(3, 7));
        System.out.println("max(\"apple\", \"banana\") = " + max("apple", "banana"));

        List<Integer> source = List.of(1, 2, 3);
        List<Number> destination = new ArrayList<>();
        copy(source, destination);
        System.out.println("PECS copy: source=" + source + " -> destination=" + destination);

        System.out.println("Raw type warning demo: ClassCastException would occur here (caught) - " + rawTypeTrap());
    }

    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    /** PECS: src only produces T (extends), dest only consumes T (super). */
    static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) {
            dest.add(item);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String rawTypeTrap() {
        List<String> strings = new ArrayList<>();
        strings.add("safe");
        List raw = strings;           // raw type reference - disables compile-time checking
        raw.add(42);                   // compiles with an unchecked warning only
        try {
            String s = strings.get(1); // ClassCastException happens HERE, not at raw.add(42)
            return "no exception, got: " + s;
        } catch (ClassCastException e) {
            return "message: " + e.getMessage();
        }
    }
}
