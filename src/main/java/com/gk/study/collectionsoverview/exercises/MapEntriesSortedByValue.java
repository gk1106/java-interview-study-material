package com.gk.study.collectionsoverview.exercises;

import java.util.List;
import java.util.Map;

/**
 * E04 [Medium] Given a {@code Map<String, Integer>}, produce a
 * {@code List<String>} of {@code "key=value"} strings sorted by value
 * descending, using only the map's collection views (entrySet/keySet/values)
 * — demonstrates that although Map is not a Collection, its views are.
 *
 * Input:  {"checking": 100, "savings": 500, "credit": -50}
 * Output: ["savings=500", "checking=100", "credit=-50"]
 *
 * Tie-break: entries with equal values keep the natural (any consistent)
 * relative order produced by a stable sort.
 *
 * Constraint: O(n log n) time (dominated by the sort), O(n) space.
 * Pattern: Map views -&gt; Collection/Stream pipeline
 */
public class MapEntriesSortedByValue {

    public static List<String> sortedEntries(Map<String, Integer> map) {
        // TODO: use map.entrySet().stream(), sort by value descending via
        // Map.Entry.comparingByValue(Comparator.reverseOrder()), then format
        // each entry as "key=value" and collect to a List.
        throw new UnsupportedOperationException("TODO");
    }
}
