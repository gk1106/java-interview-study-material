package com.gk.study.collectionsoverview.exercises;

import java.util.List;

/**
 * E03 [Medium] Write a defensive-copy utility that snapshots a
 * caller-supplied list into a truly immutable one, so that future
 * mutations to the caller's original list never affect the returned
 * snapshot — the standard fix for the "leaked mutable reference" bug.
 *
 * Input:  any List&lt;T&gt; (possibly mutable, possibly already immutable).
 * Output: an immutable List&lt;T&gt; with the same contents at the moment of
 *         the call; later mutations to the input list must NOT be
 *         reflected in the returned snapshot, and mutation attempts on
 *         the returned snapshot must throw UnsupportedOperationException.
 *
 * Example:
 *   List&lt;String&gt; source = new ArrayList&lt;&gt;(List.of("a", "b"));
 *   List&lt;String&gt; snap = DefensiveSnapshot.of(source);
 *   source.add("c");
 *   snap -&gt; still ["a", "b"]
 *   snap.add("d") -&gt; throws UnsupportedOperationException
 *
 * Constraint: O(n) time/space for the copy. Must work correctly even when
 * the input is already an immutable List.of instance (no unnecessary
 * re-copy is required, but correctness matters more than avoiding it).
 * Pattern: defensive copying
 */
public class DefensiveSnapshot {

    public static <T> List<T> of(List<T> source) {
        // TODO: implement using List.copyOf(source).
        throw new UnsupportedOperationException("TODO");
    }
}
