package com.gk.study.set.exercises;

/**
 * M03 [Medium] Given an array where every element appears exactly twice except for one element
 * that appears exactly once, find that single element. Implement it two ways to compare the
 * trade-off.
 * Input:  [4, 1, 2, 1, 2]  → Output: 4
 * Constraint: O(n) time. {@link #solveXor} must use O(1) extra space; {@link #solveHashSet} may
 * use O(n) extra space.
 * Pattern: XOR cancellation vs HashSet add/remove toggling
 */
public class SingleNumber {

    /**
     * O(1) space solution: XOR every element together — paired values cancel to 0.
     *
     * @param nums the input array
     * @return the element that appears exactly once
     */
    public static int solveXor(int[] nums) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * O(n) space solution: toggle membership in a HashSet — whatever remains is the answer.
     *
     * @param nums the input array
     * @return the element that appears exactly once
     */
    public static int solveHashSet(int[] nums) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
