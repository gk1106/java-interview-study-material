package com.gk.study.collectionsoverview.exercises;

import java.util.List;
import java.util.Map;

/**
 * E04 [Medium] Classify how three different "read-only-ish" collection
 * constructions react to hazards: a null element, a duplicate element, and
 * a post-construction mutation attempt. Reports the outcome for each
 * combination as either "OK" (no exception) or the thrown exception's
 * simple class name.
 *
 * Scenarios to classify (each independently, starting from a fresh
 * two-element collection of Strings "a", "b" each time):
 *   1. List.of("a", "b", null)                              -&gt; construction outcome
 *   2. Set.of("a", "a")                                       -&gt; construction outcome
 *   3. Collections.unmodifiableList(new ArrayList&lt;&gt;(List.of("a", null))) -&gt; construction outcome
 *   4. Mutating (add) the result of #3                        -&gt; mutation outcome
 *   5. Mutating (add) List.of("a", "b")                        -&gt; mutation outcome
 *
 * Output: a Map&lt;String, String&gt; keyed by a short scenario label (e.g.
 * "listOfNull", "setOfDuplicate", "unmodifiableWithNullConstruction",
 * "unmodifiableMutation", "listOfMutation") to the outcome string.
 *
 * Example (expected values):
 *   {
 *     "listOfNull": "NullPointerException",
 *     "setOfDuplicate": "IllegalArgumentException",
 *     "unmodifiableWithNullConstruction": "OK",
 *     "unmodifiableMutation": "UnsupportedOperationException",
 *     "listOfMutation": "UnsupportedOperationException"
 *   }
 *
 * Constraint: each scenario's attempt must be isolated in its own
 * try/catch so one failure doesn't prevent classifying the others.
 * Pattern: null-hostility / exception classification
 */
public class ImmutabilityExceptionClassifier {

    public static Map<String, String> classify() {
        // TODO: implement — attempt each scenario above inside its own
        // try/catch(RuntimeException), recording "OK" or the exception's
        // simple class name into the result map under the given labels.
        throw new UnsupportedOperationException("TODO");
    }
}
