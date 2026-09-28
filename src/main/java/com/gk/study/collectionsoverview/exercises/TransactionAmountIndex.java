package com.gk.study.collectionsoverview.exercises;

import java.util.Collection;
import java.util.NavigableSet;

/**
 * E04 [Medium] Maintain a set of unique transaction amounts that supports
 * efficient inclusive range queries — "all amounts between lo and hi" —
 * without scanning every element.
 *
 * Input:  a starting collection of int amounts (duplicates collapsed), then
 *         range queries [lo, hi] (inclusive on both ends).
 * Output: a NavigableSet view/copy of the amounts within [lo, hi], in
 *         ascending order.
 *
 * Example:
 *   TransactionAmountIndex idx = new TransactionAmountIndex(List.of(50, 150, 200, 400, 900));
 *   idx.between(100, 400) -&gt; [150, 200, 400]
 *   idx.between(0, 49)    -&gt; []
 *   idx.add(120);
 *   idx.between(100, 400) -&gt; [120, 150, 200, 400]
 *
 * Constraint: between(lo, hi) must run in O(log n + k) where k is the
 * number of results — NOT O(n).
 * Pattern: sorted set / NavigableSet range query
 */
public class TransactionAmountIndex {

    private final NavigableSet<Integer> amounts;

    public TransactionAmountIndex(Collection<Integer> initialAmounts) {
        // TODO: back `amounts` with a TreeSet<Integer> (or another
        // NavigableSet) seeded with initialAmounts.
        this.amounts = null;
        throw new UnsupportedOperationException("TODO");
    }

    public void add(int amount) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public NavigableSet<Integer> between(int lo, int hi) {
        // TODO: implement using NavigableSet.subSet(lo, true, hi, true)
        throw new UnsupportedOperationException("TODO");
    }
}
