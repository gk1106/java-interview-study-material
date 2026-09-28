package com.gk.study.collectionsoverview.exercises;

/**
 * E01 [Easy] Classify an arbitrary object into the most specific Collections
 * Framework interface it implements.
 *
 * Input:  a {@code List}, {@code Set}, {@code Deque}, {@code Queue} (non-Deque),
 *         {@code Map}, or an unrelated object (e.g. a {@code String}).
 * Output: one of "List", "Set", "Deque", "Queue", "Map",
 *         "Collection (unspecialized)", or "not a collection type".
 *
 * Example:
 *   classify(new ArrayList&lt;&gt;())      -&gt; "List"
 *   classify(new ArrayDeque&lt;&gt;())      -&gt; "Deque"   (NOT "Queue" — Deque is more specific)
 *   classify(new PriorityQueue&lt;&gt;())   -&gt; "Queue"   (it is a Queue but not a Deque)
 *   classify(new HashMap&lt;&gt;())         -&gt; "Map"
 *   classify("hello")                    -&gt; "not a collection type"
 *
 * Constraint: O(1) time — a chain of instanceof checks, most specific first.
 * Pattern: interface classification via instanceof
 */
public class ClassifyCollection {

    public static String classify(Object o) {
        // TODO: implement — check Deque before Queue, since every Deque is
        // also a Queue; check List/Set/Map; fall back appropriately.
        throw new UnsupportedOperationException("TODO");
    }
}
