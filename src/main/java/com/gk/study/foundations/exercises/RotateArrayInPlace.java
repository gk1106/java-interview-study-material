package com.gk.study.foundations.exercises;

/**
 * E03 [Medium] Rotate an int array to the right by k positions, in place.
 * Input:  arr = [1, 2, 3, 4, 5, 6, 7], k = 3
 * Output: arr becomes [5, 6, 7, 1, 2, 3, 4]
 * Constraint: O(n) time, O(1) extra space (no second array); k may be >= arr.length or negative-safe
 * is not required, but must handle k == 0 and k >= arr.length via k % arr.length.
 * Pattern: in-place rotation via triple reversal
 */
public class RotateArrayInPlace {

    public static void rotateRight(int[] arr, int k) {
        // TODO: normalize k = k % arr.length (guard against arr.length == 0), then:
        // 1) reverse the whole array
        // 2) reverse the first k elements
        // 3) reverse the remaining (arr.length - k) elements
        throw new UnsupportedOperationException("TODO");
    }
}
