package com.gk.study.collectionsoverview.solutions;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * Reference solution for E04 (see exercises.TransactionAmountIndex).
 */
public class TransactionAmountIndexSolution {

    private final NavigableSet<Integer> amounts;

    public TransactionAmountIndexSolution(Collection<Integer> initialAmounts) {
        this.amounts = new TreeSet<>(initialAmounts);
    }

    public void add(int amount) {
        amounts.add(amount);
    }

    public NavigableSet<Integer> between(int lo, int hi) {
        return amounts.subSet(lo, true, hi, true);
    }
}
