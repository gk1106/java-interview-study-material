package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * E05 [Easy] Given a stream of transaction IDs, determine whether the same ID appears twice
 * within {@code k} positions of each other (a cheap duplicate-submission guard).
 * Input: ids=["A","B","C","A"], k=3 &rarr; Output: true ("A" repeats 3 apart);
 * ids=["A","B","C","A"], k=2 &rarr; Output: false (repeat is 3 apart, > k)
 * Constraint: k &gt;= 0; O(n) time, O(min(n, k)) space.
 * Pattern: fixed-size sliding window + set membership
 * Collections: HashSet
 */
public class ContainsDuplicateWithinK {

    public static boolean solve(List<String> transactionIds, int k) {
        // TODO: maintain a HashSet of the last k ids seen; slide it forward one position at a
        // time, removing the id that falls out of the window before checking/adding the new one
        throw new UnsupportedOperationException("TODO");
    }
}
