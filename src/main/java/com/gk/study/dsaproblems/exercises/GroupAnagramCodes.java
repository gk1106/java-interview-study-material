package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * M02 [Medium] Given a list of alphanumeric merchant/reference codes, group every code that is
 * an anagram of another (same multiset of characters) into the same sub-list. Order of the
 * groups, and order within a group, does not matter.
 * Input: ["eat","tea","tan","ate","nat","bat"] &rarr;
 * Output: [["eat","tea","ate"],["tan","nat"],["bat"]] (any group/element order)
 * Constraint: O(n * k log k) time where k is the max code length (dominated by sorting each
 * code's characters to form its canonical bucket key).
 * Pattern: canonical-key bucketing
 * Collections: HashMap&lt;String, List&lt;String&gt;&gt;
 */
public class GroupAnagramCodes {

    public static List<List<String>> solve(List<String> codes) {
        // TODO: implement using a HashMap<String, List<String>> keyed by each code's sorted-
        // character canonical form; append every code to its bucket, then return all bucket
        // values
        throw new UnsupportedOperationException("TODO");
    }
}
