package com.gk.study.dsaproblems.solutions;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.TrappingRainWater}.
 *
 * <p>Two pointers from both ends, with running {@code leftMax}/{@code rightMax}. Whichever side
 * currently has the smaller max is the side that gets advanced: the water trapped above that
 * position is exactly {@code smallerMax - height[position]}, because — regardless of what lies
 * beyond the other pointer — the smaller of the two maxima is already known to be the limiting
 * wall on both sides of that position. O(n) time, O(1) extra space (no stack needed, unlike the
 * monotonic-stack alternative).
 */
public final class TrappingRainWaterSolution {

    private TrappingRainWaterSolution() {
    }

    public static int solve(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int leftMax = 0;
        int rightMax = 0;
        int trapped = 0;

        while (left < right) {
            if (height[left] <= height[right]) {
                leftMax = Math.max(leftMax, height[left]);
                trapped += leftMax - height[left];
                left++;
            } else {
                rightMax = Math.max(rightMax, height[right]);
                trapped += rightMax - height[right];
                right--;
            }
        }
        return trapped;
    }
}
