package com.gk.study.foundations.exercises;

/**
 * E04 [Hard] Recursively compare two 2D int arrays for deep equality WITHOUT calling
 * Arrays.deepEquals, to demonstrate why == and Arrays.equals both fail on nested arrays.
 * Input:  a = {{1,2},{3,4}}, b = {{1,2},{3,4}}
 * Output: true (same shape and same values, even though a != b and a[0] != b[0] by reference)
 * Constraint: O(rows * cols) time; must handle jagged arrays (rows of different lengths), null
 * rows, and both top-level arrays being null (two nulls are "equal"; one null and one non-null
 * are not).
 * Pattern: recursive/deep structural equality
 */
public class DeepEqualsMatrix {

    public static boolean matricesEqual(int[][] a, int[][] b) {
        // TODO: manually compare dimensions and every element recursively (do NOT use
        // Arrays.deepEquals or Arrays.equals on the outer array - only plain loops and, at most,
        // a one-dimensional element-by-element comparison for each row)
        throw new UnsupportedOperationException("TODO");
    }
}
