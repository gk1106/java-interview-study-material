package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.map.exercises.SubarraySumEqualsK}.
 *
 * <p>Prefix-sum + HashMap: at each index, the number of valid subarrays ending here equals the
 * number of earlier prefix sums equal to {@code currentPrefixSum - k}. The map is seeded with
 * {@code {0: 1}} to account for a subarray starting at index 0 that already sums to k. O(n) time,
 * O(n) space. Works correctly even with negative numbers in {@code nums}.
 */
public final class SubarraySumEqualsKSolution {

    private SubarraySumEqualsKSolution() {
    }

    public static int solve(int[] nums, int k) {
        Map<Integer, Integer> prefixSumCounts = new HashMap<>();
        prefixSumCounts.put(0, 1);

        int prefixSum = 0;
        int count = 0;
        for (int num : nums) {
            prefixSum += num;
            count += prefixSumCounts.getOrDefault(prefixSum - k, 0);
            prefixSumCounts.merge(prefixSum, 1, Integer::sum);
        }
        return count;
    }
}
