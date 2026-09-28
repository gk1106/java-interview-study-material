package com.gk.study.collectionsoverview.exercises;

import java.util.Collection;

/**
 * E02 [Easy] Sum a {@code Collection<? extends Number>} regardless of the
 * concrete implementation (List, Set, or Queue) — demonstrates programming
 * to the Collection interface with a bounded wildcard (PECS: producer
 * extends).
 *
 * Input:  a Collection of any Number subtype, e.g. List&lt;Integer&gt;,
 *         TreeSet&lt;Double&gt;, ArrayDeque&lt;Long&gt;.
 * Output: the double sum of all elements. Empty collection -&gt; 0.0.
 *
 * Example:
 *   sum(List.of(1, 2, 3))            -&gt; 6.0
 *   sum(new TreeSet&lt;&gt;(List.of(1.5, 2.5))) -&gt; 4.0
 *   sum(List.of())                   -&gt; 0.0
 *
 * Constraint: O(n) time, O(1) extra space.
 * Pattern: program to the interface / bounded wildcards
 */
public class SumAnyCollection {

    public static double sum(Collection<? extends Number> numbers) {
        // TODO: implement — iterate and accumulate via doubleValue().
        throw new UnsupportedOperationException("TODO");
    }
}
