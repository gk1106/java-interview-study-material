package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * M10 [Medium] Given strings {@code s} and {@code p}, return every starting index in {@code s}
 * where a substring is an anagram of {@code p}.
 * Input: s="cbaebabacd", p="abc" &rarr; Output: [0, 6] ("cba" at 0, "bac" at 6)
 * Constraint: O(s.length()) time (fixed window size = p.length()).
 * Pattern: fixed-size sliding window + frequency map
 * Collections: HashMap&lt;Character, Integer&gt; (running window character counts)
 */
public class FindAllAnagramsInString {

    public static List<Integer> solve(String s, String p) {
        // TODO: implement using a fixed-size window of length p.length() sliding across s;
        // maintain a running count of characters currently in the window (a HashMap or a
        // 26-length int[] both work) and compare it against p's character-count map (or track a
        // single "matches" counter that increments/decrements as characters enter/leave exactly
        // matching counts) to decide whether the window is an anagram
        throw new UnsupportedOperationException("TODO");
    }
}
