package com.gk.study.set.solutions;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference solution for {@link com.gk.study.set.exercises.ContainsNearbyDuplicate}.
 * Sliding window of size k held in a HashSet: check membership before inserting the new element
 * (a hit means a duplicate within the window), then evict the element that just slid out of the
 * window once its size would exceed k.
 */
public class ContainsNearbyDuplicateSolution {

    public static boolean solve(int[] nums, int k) {
        Set<Integer> window = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            if (!window.add(nums[i])) {
                return true;
            }
            if (window.size() > k) {
                window.remove(nums[i - k]);
            }
        }
        return false;
    }
}
