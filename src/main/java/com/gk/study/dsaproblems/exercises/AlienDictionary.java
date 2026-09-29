package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * H05 [Hard] A list of words is sorted according to the rules of some unknown alien alphabet
 * (using the same 26 English letters, just in a different order). Derive one valid ordering of
 * that alphabet's letters, or detect that no valid ordering is possible.
 * Input: ["wrt","wrf","er","ett","rftt"] &rarr; Output: "wertf" (one valid ordering; the letters
 * form a single fully-determined chain here, so it is the only correct answer)
 * Constraint: return {@code ""} if the input is contradictory (a cycle) OR invalid (a word
 * appears before a strict prefix of itself, e.g. ["abc","ab"], which can never be valid in any
 * dictionary order). O(C) time where C is the total number of characters across all words.
 * Pattern: topological sort from a partial order (build a "comes before" graph from adjacent
 * word pairs, then topologically sort it)
 * Collections: HashMap (adjacency + in-degree) + HashSet (dedupe edges) + Queue (a
 * PriorityQueue&lt;Character&gt; here, so that when multiple letters are simultaneously free to
 * go next, the alphabetically smallest is chosen — keeping the output deterministic)
 */
public class AlienDictionary {

    public static String solve(List<String> words) {
        // TODO: implement by comparing each pair of adjacent words to find their first
        // differing character (that gives one edge: earlierChar -> laterChar); watch for the
        // invalid case where one word is a strict prefix of an earlier word (e.g. "abc" before
        // "ab") - that makes the input invalid regardless of any edges found. Then run Kahn's
        // topological sort (HashMap adjacency + in-degree, a PriorityQueue<Character> seeded
        // with every in-degree-0 letter that appears in the input) to produce the ordering; if
        // not all letters that appear in the input get placed, a cycle exists -> return ""
        throw new UnsupportedOperationException("TODO");
    }
}
