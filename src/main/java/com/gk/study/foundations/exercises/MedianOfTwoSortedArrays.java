package com.gk.study.foundations.exercises;

/**
 * E04 [Hard] Find the median of two sorted int arrays without merging them.
 * Input:  nums1 = [1, 3], nums2 = [2]
 * Output: 2.0
 * Input:  nums1 = [1, 2], nums2 = [3, 4]
 * Output: 2.5
 * Constraint: O(log(min(m, n))) time, O(1) extra space. Arrays may be empty (but not both at once).
 * Pattern: binary search on partition index
 */
public class MedianOfTwoSortedArrays {

    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // TODO: binary search for a partition index `i` in the SMALLER array such that the
        // partition `j = (m + n + 1) / 2 - i` in the other array balances the "left half" and
        // "right half" element counts and every left-side element <= every right-side element.
        throw new UnsupportedOperationException("TODO");
    }
}
