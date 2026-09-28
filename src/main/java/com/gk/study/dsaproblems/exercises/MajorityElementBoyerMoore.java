package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * E03 [Easy] Given a list of account-flag codes where one value is guaranteed to appear more
 * than n/2 times, find that majority value using O(1) extra space (Boyer-Moore voting), not a
 * frequency map.
 * Input: [2,2,1,1,1,2,2] &rarr; Output: 2
 * Constraint: a majority element is guaranteed to exist; O(n) time, O(1) space.
 * Pattern: Boyer-Moore voting (candidate + counter)
 * Collections: List (input only — no auxiliary structure; contrast with module 06's
 * HashMap-based majority-element exercise)
 */
public class MajorityElementBoyerMoore {

    public static int solve(List<Integer> nums) {
        // TODO: implement Boyer-Moore voting: track a candidate and a running count
        throw new UnsupportedOperationException("TODO");
    }
}
