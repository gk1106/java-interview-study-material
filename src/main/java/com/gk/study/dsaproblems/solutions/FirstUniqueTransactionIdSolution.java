package com.gk.study.dsaproblems.solutions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.FirstUniqueTransactionId}.
 *
 * <p>A {@code LinkedHashMap} counts occurrences while preserving insertion order in one pass;
 * a second pass over the (already order-preserving) entry set finds the first count-1 entry.
 * O(n) time, O(distinct ids) space.
 */
public final class FirstUniqueTransactionIdSolution {

    private FirstUniqueTransactionIdSolution() {
    }

    public static String solve(List<String> transactionIds) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String id : transactionIds) {
            counts.merge(id, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() == 1) {
                return entry.getKey();
            }
        }
        return null;
    }
}
