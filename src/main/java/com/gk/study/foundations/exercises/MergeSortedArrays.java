package com.gk.study.foundations.exercises;

/**
 * E01 [Easy] Merge two already-sorted int arrays into one sorted array.
 * Input:  a = [1, 3, 5], b = [2, 4, 6]
 * Output: [1, 2, 3, 4, 5, 6]
 * Constraint: O(n + m) time, output array length is exactly n + m; duplicate values across both
 * arrays are all kept (no de-duplication).
 * Pattern: two-pointer merge (the merge step of merge sort)
 */
public class MergeSortedArrays {

    public static int[] merge(int[] a, int[] b) {
        // TODO: classic two-pointer merge - walk a and b together, always taking the smaller
        // current element, then append whatever tail remains from whichever array isn't exhausted
        throw new UnsupportedOperationException("TODO");
    }
}
