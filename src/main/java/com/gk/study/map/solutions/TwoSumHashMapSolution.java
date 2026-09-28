package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.map.exercises.TwoSumHashMap}.
 *
 * <p>Single pass: for each element, check whether its complement was already seen (stored as
 * value -> index). O(n) time, O(n) space.
 */
public final class TwoSumHashMapSolution {

    private TwoSumHashMapSolution() {
    }

    public static int[] solve(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            Integer complementIndex = seen.get(complement);
            if (complementIndex != null) {
                return new int[] {complementIndex, i};
            }
            seen.put(nums[i], i);
        }
        throw new IllegalArgumentException("No two-sum solution exists for the given input");
    }
}
