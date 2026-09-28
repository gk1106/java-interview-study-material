package com.gk.study.list.solutions;

import java.util.List;

/**
 * Reference solution for {@link com.gk.study.list.exercises.IsPalindromeList}.
 * Two pointers converging from both ends; stop early on the first mismatch.
 */
public class IsPalindromeListSolution {

    public static boolean solve(List<Integer> list) {
        int left = 0;
        int right = list.size() - 1;
        while (left < right) {
            if (!list.get(left).equals(list.get(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}
