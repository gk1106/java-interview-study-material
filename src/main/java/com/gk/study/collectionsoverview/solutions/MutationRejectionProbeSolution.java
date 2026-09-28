package com.gk.study.collectionsoverview.solutions;

import java.util.List;

/**
 * Reference solution for E01 (see exercises.MutationRejectionProbe).
 */
public class MutationRejectionProbeSolution {

    public static String probe(List<Integer> list) {
        Integer sentinel = Integer.MIN_VALUE;
        try {
            list.add(sentinel);
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName();
        }
        // Successful add: undo it. Use remove(Object) — NOT remove(int) —
        // to remove the sentinel VALUE, not by index.
        list.remove((Object) sentinel);
        return "mutable";
    }
}
