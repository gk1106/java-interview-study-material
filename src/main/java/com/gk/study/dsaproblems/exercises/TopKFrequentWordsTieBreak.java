package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * M04 [Medium] Given a list of words and an integer k, return the k most frequent words, ordered
 * by frequency descending; on a frequency tie, order alphabetically ascending.
 * Input: words=["i","love","leetcode","i","love","coding"], k=2 &rarr; Output: ["i","love"]
 * (both appear twice; "coding"/"leetcode" appear once each and are excluded)
 * Constraint: 1 &lt;= k &lt;= number of distinct words; target O(n log k) time.
 * Pattern: frequency map + heap with a custom comparator
 * Collections: HashMap + PriorityQueue
 */
public class TopKFrequentWordsTieBreak {

    public static List<String> solve(List<String> words, int k) {
        // TODO: implement using a HashMap<String, Integer> frequency count, then a min-heap of
        // size k ordered by (frequency ascending, word descending) so the heap's root is always
        // the "worst" of the current top-k and gets evicted first; reverse the heap's contents
        // at the end to get (frequency descending, word ascending) order
        throw new UnsupportedOperationException("TODO");
    }
}
