package com.gk.study.foundations.solutions;

import java.util.HashSet;
import java.util.Set;

/** Reference solution for {@code exercises.TwoSumOptimized}. */
public class TwoSumOptimizedSolution {

    public static boolean hasPairWithSum(int[] arr, int target) {
        Set<Integer> seen = new HashSet<>();
        for (int value : arr) {
            if (seen.contains(target - value)) {
                return true;
            }
            seen.add(value);
        }
        return false;
    }
}
