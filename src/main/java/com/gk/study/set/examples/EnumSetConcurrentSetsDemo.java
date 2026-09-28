package com.gk.study.set.examples;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Demonstrates {@link EnumSet} factory methods and bit-vector-style bulk operations, then the
 * three concurrent {@code Set} options the JDK offers in place of a (nonexistent)
 * {@code ConcurrentHashSet}: {@link CopyOnWriteArraySet}, {@link ConcurrentSkipListSet} and
 * {@link Collections#newSetFromMap(java.util.Map)} wrapping a {@link ConcurrentHashMap}.
 *
 * See notes/05-set/02-enumset-concurrent-sets.md for the internals explanation.
 */
public final class EnumSetConcurrentSetsDemo {

    private enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    private EnumSetConcurrentSetsDemo() {
    }

    public static void main(String[] args) {
        enumSetDemo();
        System.out.println();
        copyOnWriteArraySetDemo();
        System.out.println();
        concurrentSkipListSetDemo();
        System.out.println();
        newSetFromMapDemo();
    }

    private static void enumSetDemo() {
        System.out.println("--- EnumSet: bit-vector backed, always declaration order ---");
        EnumSet<Day> weekdays = EnumSet.range(Day.MON, Day.FRI);
        EnumSet<Day> weekend = EnumSet.complementOf(weekdays);
        EnumSet<Day> chosen = EnumSet.of(Day.MON, Day.WED, Day.FRI);

        System.out.println("weekdays          = " + weekdays);   // [MON, TUE, WED, THU, FRI]
        System.out.println("weekend           = " + weekend);    // [SAT, SUN]
        System.out.println("chosen            = " + chosen);     // [MON, WED, FRI]

        // Bulk set algebra is a single word-level bitwise op internally, not a per-element loop.
        EnumSet<Day> weekdaysMinusChosen = EnumSet.copyOf(weekdays);
        weekdaysMinusChosen.removeAll(chosen);
        System.out.println("weekdays - chosen = " + weekdaysMinusChosen); // [TUE, THU]

        System.out.println("EnumSet.noneOf(Day.class) = " + EnumSet.noneOf(Day.class)); // []
        System.out.println("EnumSet.allOf(Day.class)  = " + EnumSet.allOf(Day.class));
    }

    private static void copyOnWriteArraySetDemo() {
        System.out.println("--- CopyOnWriteArraySet: safe (but O(n)) mutation during iteration ---");
        Set<String> listeners = new CopyOnWriteArraySet<>(List.of("audit", "email"));

        // The iterator snapshots the backing array at creation time; adding "sms" mid-loop is
        // safe (no ConcurrentModificationException) but the new element is NOT seen by this loop.
        for (String listener : listeners) {
            System.out.println("notifying (from snapshot): " + listener);
            listeners.add("sms");
        }
        System.out.println("listeners after the loop = " + listeners); // [audit, email, sms]
    }

    private static void concurrentSkipListSetDemo() {
        System.out.println("--- ConcurrentSkipListSet: concurrent + sorted (NavigableSet) ---");
        NavigableSet<Integer> timestamps = new ConcurrentSkipListSet<>(List.of(500, 100, 900, 300));
        System.out.println("timestamps       = " + timestamps);       // [100, 300, 500, 900]
        System.out.println("ceiling(400)     = " + timestamps.ceiling(400)); // 500
        System.out.println("floor(400)       = " + timestamps.floor(400));   // 300
        System.out.println("headSet(500)     = " + timestamps.headSet(500)); // [100, 300]
    }

    private static void newSetFromMapDemo() {
        System.out.println("--- Collections.newSetFromMap: the idiomatic ConcurrentHashSet ---");
        Set<String> concurrentSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
        concurrentSet.add("AC-1");
        concurrentSet.add("AC-2");
        concurrentSet.add("AC-1"); // duplicate, ignored
        System.out.println("concurrentSet size = " + concurrentSet.size()); // 2
        System.out.println("concurrentSet      = " + concurrentSet);
    }
}
