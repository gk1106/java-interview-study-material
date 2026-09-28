package com.gk.study.foundations.solutions;

import java.util.Arrays;

/** Reference solution for {@code exercises.ArrayIntersection}. */
public class ArrayIntersectionSolution {

    public static int[] intersection(int[] a, int[] b) {
        int[] sortedA = a.clone();
        int[] sortedB = b.clone();
        Arrays.sort(sortedA);
        Arrays.sort(sortedB);

        int[] buffer = new int[Math.min(sortedA.length, sortedB.length)];
        int count = 0;
        int i = 0;
        int j = 0;
        while (i < sortedA.length && j < sortedB.length) {
            if (sortedA[i] == sortedB[j]) {
                if (count == 0 || buffer[count - 1] != sortedA[i]) {
                    buffer[count++] = sortedA[i];
                }
                i++;
                j++;
            } else if (sortedA[i] < sortedB[j]) {
                i++;
            } else {
                j++;
            }
        }
        return Arrays.copyOf(buffer, count);
    }
}
