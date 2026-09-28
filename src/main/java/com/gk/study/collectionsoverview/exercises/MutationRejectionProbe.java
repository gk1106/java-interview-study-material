package com.gk.study.collectionsoverview.exercises;

import java.util.List;

/**
 * E01 [Easy] Detect whether a given List rejects structural mutation, and
 * classify what happens without permanently mutating the list passed in
 * (undo any successful test mutation before returning).
 *
 * Input:  any List&lt;Integer&gt; (mutable or immutable).
 * Output: "mutable" if a no-op add+remove round-trip succeeds; otherwise
 *         the simple class name of the exception thrown by add()
 *         (e.g. "UnsupportedOperationException").
 *
 * Example:
 *   probe(new ArrayList&lt;&gt;(List.of(1, 2)))  -&gt; "mutable"
 *   probe(List.of(1, 2))                        -&gt; "UnsupportedOperationException"
 *   probe(Collections.unmodifiableList(new ArrayList&lt;&gt;(List.of(1)))) -&gt; "UnsupportedOperationException"
 *
 * Constraint: O(1) extra work beyond the list's own add/remove cost. The
 * input list's final observable contents must be unchanged after the probe.
 * Pattern: mutation probing via try/catch
 */
public class MutationRejectionProbe {

    public static String probe(List<Integer> list) {
        // TODO: attempt list.add(0) (or any sentinel value), catch
        // RuntimeException, return its simple class name if thrown, or
        // "mutable" after undoing the successful add (list.remove(...)).
        throw new UnsupportedOperationException("TODO");
    }
}
