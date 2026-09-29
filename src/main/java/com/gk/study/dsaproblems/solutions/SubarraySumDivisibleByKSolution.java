package com.gk.study.dsaproblems.solutions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.SubarraySumDivisibleByK}.
 *
 * <p>If two prefix sums share the same remainder mod K, the subarray between them sums to a
 * multiple of K. A {@code HashMap<Integer, Integer>} counts how many prefixes seen so far have
 * produced each remainder; for every new prefix, add the count already stored under its
 * remainder (every one of those prefixes pairs with the current index to form a valid subarray),
 * then increment that remainder's count. Seeded with remainder 0 -> 1 for the empty prefix.
 * Negative remainders from Java's {@code %} are normalized into {@code [0, k)}. O(n) time,
 * O(min(n, k)) space.
 */
public final class SubarraySumDivisibleByKSolution {

    private SubarraySumDivisibleByKSolution() {
    }

    public static int solve(List<Integer> nums, int k) {
        if (k < 1) {
            throw new IllegalArgumentException("k must be positive, was " + k);
        }
        Map<Integer, Integer> remainderCounts = new HashMap<>();
        remainderCounts.put(0, 1);

        int runningSum = 0;
        int count = 0;
        for (int num : nums) {
            runningSum += num;
            int remainder = ((runningSum % k) + k) % k;
            count += remainderCounts.getOrDefault(remainder, 0);
            remainderCounts.merge(remainder, 1, Integer::sum);
        }
        return count;
    }
}
