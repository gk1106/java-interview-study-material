package com.gk.study.collectionsoverview.exercises;

import java.util.List;

/**
 * E01 [Easy] Deduplicate a list of account IDs while preserving the order
 * in which each ID was first seen. Picking the right Set implementation
 * (order-preserving vs. not) is the whole point of this exercise.
 *
 * Input:  ["acc-3", "acc-1", "acc-3", "acc-2", "acc-1"]
 * Output: ["acc-3", "acc-1", "acc-2"]
 *
 * Example:
 *   dedupe(List.of("a", "b", "a", "c")) -&gt; ["a", "b", "c"]
 *   dedupe(List.of())                    -&gt; []
 *
 * Constraint: O(n) time, O(n) space. Must preserve first-seen order —
 * a plain HashSet does NOT guarantee this.
 * Pattern: insertion-order dedupe
 */
public class DedupePreservingOrder {

    public static List<String> dedupe(List<String> ids) {
        // TODO: implement using a Set implementation that both dedupes in
        // O(1) average and preserves insertion order.
        throw new UnsupportedOperationException("TODO");
    }
}
