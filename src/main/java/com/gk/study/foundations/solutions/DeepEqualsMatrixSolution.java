package com.gk.study.foundations.solutions;

/** Reference solution for {@code exercises.DeepEqualsMatrix}. */
public class DeepEqualsMatrixSolution {

    public static boolean matricesEqual(int[][] a, int[][] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int row = 0; row < a.length; row++) {
            if (!rowsEqual(a[row], b[row])) {
                return false;
            }
        }
        return true;
    }

    private static boolean rowsEqual(int[] r1, int[] r2) {
        if (r1 == r2) {
            return true;
        }
        if (r1 == null || r2 == null) {
            return false;
        }
        if (r1.length != r2.length) {
            return false;
        }
        for (int col = 0; col < r1.length; col++) {
            if (r1[col] != r2[col]) {
                return false;
            }
        }
        return true;
    }
}
