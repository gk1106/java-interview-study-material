package com.gk.study.set.examples;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Demonstrates the iteration-order difference between {@link HashSet}, {@link LinkedHashSet} and
 * {@link TreeSet} for the same input elements, then exercises the {@link NavigableSet} methods
 * (floor/ceiling/higher/lower/headSet/tailSet/pollFirst/pollLast) that only {@code TreeSet}
 * offers.
 *
 * See notes/05-set/01-hashset-linkedhashset-treeset.md for the internals explanation.
 */
public final class HashSetLinkedHashSetTreeSetDemo {

    private HashSetLinkedHashSetTreeSetDemo() {
    }

    public static void main(String[] args) {
        List<Integer> insertionOrder = List.of(50, 20, 90, 10, 30);

        Set<Integer> hash = new HashSet<>(insertionOrder);
        Set<Integer> linked = new LinkedHashSet<>(insertionOrder);
        Set<Integer> tree = new TreeSet<>(insertionOrder);

        System.out.println("insertion order was : " + insertionOrder);
        System.out.println("HashSet       (unspecified bucket order, do not rely on it): " + hash);
        System.out.println("LinkedHashSet (always insertion order)                    : " + linked);
        System.out.println("TreeSet       (always sorted/natural order)               : " + tree);

        System.out.println();
        System.out.println("--- NavigableSet methods on the TreeSet ---");
        NavigableSet<Integer> nav = (NavigableSet<Integer>) tree;
        System.out.println("nav                = " + nav);
        System.out.println("floor(25)          = " + nav.floor(25));    // largest <= 25 -> 20
        System.out.println("ceiling(25)        = " + nav.ceiling(25));  // smallest >= 25 -> 30
        System.out.println("higher(30)         = " + nav.higher(30));   // strictly > 30 -> 50
        System.out.println("lower(30)          = " + nav.lower(30));    // strictly < 30 -> 20
        System.out.println("headSet(30)        = " + nav.headSet(30));       // [10, 20]
        System.out.println("headSet(30, true)  = " + nav.headSet(30, true)); // [10, 20, 30]
        System.out.println("tailSet(30)        = " + nav.tailSet(30));       // [30, 50, 90]
        System.out.println("tailSet(30, false) = " + nav.tailSet(30, false));// [50, 90]

        System.out.println();
        System.out.println("--- pollFirst / pollLast drain the set from both ends ---");
        while (!nav.isEmpty()) {
            System.out.println("pollFirst=" + nav.pollFirst() + ", pollLast=" + nav.pollLast()
                    + ", remaining=" + nav);
        }

        System.out.println();
        System.out.println("--- TreeSet rejects null, HashSet/LinkedHashSet allow one ---");
        hash.add(null);
        linked.add(null);
        System.out.println("HashSet with a null element       : " + hash);
        System.out.println("LinkedHashSet with a null element  : " + linked);
        try {
            new TreeSet<Integer>().add(null);
        } catch (NullPointerException e) {
            System.out.println("TreeSet.add(null) threw NullPointerException, as expected");
        }
    }
}
