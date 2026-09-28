package com.gk.study.collectionsoverview.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates the difference between an unmodifiable VIEW
 * (Collections.unmodifiableList) over a mutable backing list, and a truly
 * immutable collection (List.of / List.copyOf), plus the exceptions each
 * throws on mutation attempts.
 *
 * Run with: java -cp target/classes com.gk.study.collectionsoverview.examples.ImmutableCollectionsDemo
 */
public final class ImmutableCollectionsDemo {

    private ImmutableCollectionsDemo() {
    }

    public static void main(String[] args) {
        List<String> backing = new ArrayList<>(List.of("a", "b"));
        List<String> view = Collections.unmodifiableList(backing);
        List<String> truly = List.of("a", "b");

        backing.add("c"); // mutate the backing list directly

        System.out.println("view after backing.add(\"c\") : " + view);
        System.out.println("truly (List.of) unaffected  : " + truly);

        try {
            view.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("view.add rejected: " + e.getClass().getSimpleName());
        }

        try {
            truly.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("truly.add rejected: " + e.getClass().getSimpleName());
        }

        try {
            List.of(1, null);
        } catch (NullPointerException e) {
            System.out.println("List.of(1, null) rejected at creation: " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("-- Arrays.asList quirk --");
        List<Integer> fixedSize = java.util.Arrays.asList(1, 2, 3);
        fixedSize.set(0, 99); // allowed: fixed-size, not fixed-content
        System.out.println("Arrays.asList after set(0, 99): " + fixedSize);
        try {
            fixedSize.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("Arrays.asList.add rejected: " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("-- List.copyOf defensive snapshot --");
        List<String> mutable = new ArrayList<>(List.of("x", "y"));
        List<String> snapshot = List.copyOf(mutable);
        mutable.add("z");
        System.out.println("mutable after add(\"z\")   : " + mutable);
        System.out.println("snapshot (List.copyOf)    : " + snapshot);
    }
}
