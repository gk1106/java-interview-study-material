package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * E07 [Easy] Given an ordered list of transaction IDs (some repeated), return the first ID that
 * occurs exactly once, preserving the original order. Return {@code null} if every ID repeats.
 * Input: ["t1","t2","t1","t3"] &rarr; Output: "t2"
 * Constraint: O(n) time; must preserve insertion order while counting.
 * Pattern: order-preserving frequency counting
 * Collections: LinkedHashMap
 */
public class FirstUniqueTransactionId {

    public static String solve(List<String> transactionIds) {
        // TODO: implement using a LinkedHashMap<String, Integer> to count while preserving
        // insertion order, then scan it once for the first entry with count == 1
        throw new UnsupportedOperationException("TODO");
    }
}
