package com.gk.study.set.solutions;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference solution for {@link com.gk.study.set.exercises.SingleNumber}.
 * {@link #solveXor} uses XOR cancellation ({@code x ^ x == 0}, XOR with 0 is a no-op) for O(1)
 * space. {@link #solveHashSet} toggles set membership (add on first sighting, remove on second)
 * for O(n) space — kept side by side to make the space/simplicity trade-off concrete.
 */
public class SingleNumberSolution {

    public static int solveXor(int[] nums) {
        int result = 0;
        for (int n : nums) {
            result ^= n;
        }
        return result;
    }

    public static int solveHashSet(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int n : nums) {
            if (!set.add(n)) {
                set.remove(n);
            }
        }
        return set.iterator().next();
    }
}
