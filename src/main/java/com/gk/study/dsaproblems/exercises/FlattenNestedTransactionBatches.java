package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * E06 [Easy] A batch of transactions may itself contain nested sub-batches to arbitrary depth.
 * Each element of the input is either an {@code Integer} (a single transaction amount) or
 * another {@code List<Object>} (a nested batch, following the same rule recursively). Flatten
 * the whole structure into a single ordered list of amounts, WITHOUT using recursion — use an
 * explicit {@code Deque} as your own call stack instead.
 * Input: [1, [2, 3, [4]], 5] &rarr; Output: [1, 2, 3, 4, 5]
 * Constraint: no recursion (must not rely on the JVM call stack); O(total elements) time.
 * Pattern: explicit stack simulation (iterative DFS)
 * Collections: Deque (used as a stack)
 */
public class FlattenNestedTransactionBatches {

    public static List<Integer> solve(List<Object> nestedBatches) {
        // TODO: implement iteratively using a Deque<Object> as an explicit stack, pushing
        // elements in reverse order so they pop back out in original left-to-right order
        throw new UnsupportedOperationException("TODO");
    }
}
