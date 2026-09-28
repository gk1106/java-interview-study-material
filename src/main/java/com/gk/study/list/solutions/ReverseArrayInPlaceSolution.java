package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.ReverseArrayInPlace}.
 * Two pointers converging from both ends, swapping as they go.
 */
public class ReverseArrayInPlaceSolution {

    public static void solve(int[] arr) {
        int left = 0;
        int right = arr.length - 1;
        while (left < right) {
            int tmp = arr[left];
            arr[left] = arr[right];
            arr[right] = tmp;
            left++;
            right--;
        }
    }
}
