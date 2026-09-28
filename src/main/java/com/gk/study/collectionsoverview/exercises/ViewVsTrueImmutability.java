package com.gk.study.collectionsoverview.exercises;

import java.util.List;

/**
 * E02 [Easy] Prove the difference between an unmodifiable VIEW
 * (Collections.unmodifiableList) and a truly immutable collection
 * (List.of): given the same initial contents, mutate the backing list
 * afterward and observe which output reflects the change.
 *
 * Input:  initialContents, e.g. ["a", "b"], and an elementToAppend, e.g. "c".
 * Output: a Result record with two lists: the current contents of the
 *         unmodifiable VIEW (should include the appended element, since the
 *         backing list was mutated), and the current contents of the truly
 *         immutable List.of copy (should NOT include it).
 *
 * Example:
 *   run(List.of("a", "b"), "c")
 *     -&gt; Result[view=[a, b, c], trulyImmutable=[a, b]]
 *
 * Constraint: O(n) time/space where n = initialContents.size() + 1.
 * Pattern: view vs true immutability
 */
public class ViewVsTrueImmutability {

    public record Result(List<String> view, List<String> trulyImmutable) {
    }

    public static Result run(List<String> initialContents, String elementToAppend) {
        // TODO: build a mutable ArrayList seeded with initialContents, wrap
        // it with Collections.unmodifiableList to get `view`, separately
        // build `trulyImmutable` via List.copyOf(initialContents), then
        // append elementToAppend to the BACKING ArrayList (not the view) and
        // return both lists' current contents.
        throw new UnsupportedOperationException("TODO");
    }
}
