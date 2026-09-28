package com.gk.study.foundations.solutions;

/** Reference solution for {@code exercises.RotateArrayInPlace}. */
public class RotateArrayInPlaceSolution {

    public static void rotateRight(int[] arr, int k) {
        if (arr.length == 0) {
            return;
        }
        int n = arr.length;
        int normalizedK = ((k % n) + n) % n; // handles k >= n and defensively negative k

        reverse(arr, 0, n - 1);
        reverse(arr, 0, normalizedK - 1);
        reverse(arr, normalizedK, n - 1);
    }

    private static void reverse(int[] arr, int from, int to) {
        while (from < to) {
            int tmp = arr[from];
            arr[from] = arr[to];
            arr[to] = tmp;
            from++;
            to--;
        }
    }
}
