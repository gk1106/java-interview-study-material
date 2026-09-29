package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * M03 [Medium] Given a list of transaction amounts and a reporting threshold K, count the number
 * of contiguous subarrays whose sum is divisible by K (a sum of 0 counts as divisible).
 * Input: nums=[4,5,0,-2,-3,1], k=5 &rarr; Output: 7
 * Constraint: K &gt;= 1; amounts may be negative; O(n) time. Java's {@code %} operator can return
 * a negative remainder for a negative dividend, so remainders must be normalized into
 * {@code [0, k)} before being used as map keys.
 * Pattern: prefix sum (mod K)
 * Collections: HashMap&lt;Integer, Integer&gt; (remainder &rarr; count of prefixes with that
 * remainder)
 */
public class SubarraySumDivisibleByK {

    public static int solve(List<Integer> nums, int k) {
        // TODO: implement using a running prefix sum mod k and a HashMap<Integer, Integer> that
        // counts how many prefixes so far have produced each remainder; two prefixes sharing the
        // same remainder means the subarray between them sums to a multiple of k. Remember to
        // seed the map with remainder 0 -> count 1 (the empty prefix) and to normalize negative
        // remainders with ((sum % k) + k) % k
        throw new UnsupportedOperationException("TODO");
    }
}
