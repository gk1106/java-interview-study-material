package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.map.exercises.MajorityElement}.
 *
 * <p>Frequency-map approach: O(n) time, O(n) space. A majority element is guaranteed to exist, so
 * the first entry found exceeding {@code n/2} is returned. See {@link #solveBoyerMoore} for the
 * O(1)-space alternative discussed in the notes.
 */
public final class MajorityElementSolution {

    private MajorityElementSolution() {
    }

    public static int solve(int[] nums) {
        Map<Integer, Integer> freq = new HashMap<>();
        int threshold = nums.length / 2;
        for (int num : nums) {
            int count = freq.merge(num, 1, Integer::sum);
            if (count > threshold) {
                return num;
            }
        }
        throw new IllegalArgumentException("No majority element present");
    }

    /**
     * Boyer-Moore voting: O(n) time, O(1) space. Only correct when a true majority element is
     * guaranteed to exist (otherwise a verification pass is required).
     */
    public static int solveBoyerMoore(int[] nums) {
        int candidate = 0;
        int count = 0;
        for (int num : nums) {
            if (count == 0) {
                candidate = num;
            }
            count += (num == candidate) ? 1 : -1;
        }
        return candidate;
    }
}
