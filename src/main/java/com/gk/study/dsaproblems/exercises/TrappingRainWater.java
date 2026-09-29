package com.gk.study.dsaproblems.exercises;

/**
 * H03 [Hard] Given an array representing a bar-height elevation map (each bar has width 1),
 * compute how much water it traps after raining.
 * Input: [0,1,0,2,1,0,1,3,2,1,2,1] &rarr; Output: 6
 * Constraint: O(n) time, O(1) extra space (an O(n)-space monotonic-stack approach also exists —
 * see the note below — but the target here is the two-pointer O(1)-space version).
 * Pattern: two pointers, tracking running left/right maxima
 * Collections: none — array only (contrast with the monotonic-stack O(n)-space alternative,
 * which would use a Deque of indices)
 */
public class TrappingRainWater {

    public static int solve(int[] height) {
        // TODO: implement using two pointers starting at both ends and two running maxima
        // (leftMax, rightMax); at each step, advance whichever side currently has the smaller
        // max (its trapped water at that position is exactly that max minus its own height,
        // because the smaller side's max is guaranteed to be the limiting wall on both sides)
        throw new UnsupportedOperationException("TODO");
    }
}
