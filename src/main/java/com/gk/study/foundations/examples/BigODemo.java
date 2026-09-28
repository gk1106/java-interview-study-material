package com.gk.study.foundations.examples;

import java.util.HashSet;
import java.util.Set;

/**
 * Demonstrates Big-O growth by counting OPERATIONS (not measuring wall-clock
 * time, which is noisy and non-deterministic). Also demonstrates amortized
 * O(1) cost of a doubling dynamic array.
 */
public class BigODemo {

    public static void main(String[] args) {
        System.out.println("=== Big-O demo: counting operations, not wall-clock time ===");
        for (int n : new int[] {10, 100, 1000}) {
            int[] arr = buildArray(n);
            long bruteForce = bruteForceComparisons(arr, Integer.MIN_VALUE); // target never found on purpose
            long optimized = optimizedComparisons(arr, Integer.MIN_VALUE);
            System.out.printf("n=%-4d -> bruteForce comparisons=%d, optimized comparisons=%d%n",
                    n, bruteForce, optimized);
        }

        System.out.println();
        System.out.println("=== Amortized doubling array: 16 pushes ===");
        TinyDoublingArray arr = new TinyDoublingArray();
        StringBuilder capacities = new StringBuilder();
        int lastCapacity = -1;
        for (int i = 0; i < 16; i++) {
            arr.push(i);
            if (arr.capacity() != lastCapacity) {
                if (capacities.length() > 0) {
                    capacities.append(" -> ");
                }
                capacities.append(arr.capacity());
                lastCapacity = arr.capacity();
            }
        }
        System.out.println("capacity progression: " + capacities);
        System.out.println("total element copies during resizes = " + arr.totalCopies());
        double avg = arr.totalCopies() / 16.0;
        System.out.printf("average copies per push = %.2f (bounded by < 2.0)%n", avg);
    }

    /** Counts every pairwise comparison of a naive O(n^2) "does any pair sum to target" check. */
    static long bruteForceComparisons(int[] arr, int target) {
        long comparisons = 0;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                comparisons++;
                if (arr[i] + arr[j] == target) {
                    return comparisons;
                }
            }
        }
        return comparisons;
    }

    /** Counts elements processed by an O(n) hash-set based "does any pair sum to target" check. */
    static long optimizedComparisons(int[] arr, int target) {
        long comparisons = 0;
        Set<Integer> seen = new HashSet<>();
        for (int value : arr) {
            comparisons++;
            if (seen.contains(target - value)) {
                return comparisons;
            }
            seen.add(value);
        }
        return comparisons;
    }

    private static int[] buildArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i * 2 + 1; // arbitrary values, never sums to Integer.MIN_VALUE
        }
        return arr;
    }

    /** Minimal doubling dynamic array used only to illustrate amortized cost in this demo. */
    private static final class TinyDoublingArray {
        private int[] data = new int[1];
        private int size = 0;
        private long totalCopies = 0;

        void push(int value) {
            if (size == data.length) {
                int[] bigger = new int[data.length * 2];
                for (int i = 0; i < size; i++) {
                    bigger[i] = data[i];
                    totalCopies++;
                }
                data = bigger;
            }
            data[size++] = value;
        }

        int capacity() {
            return data.length;
        }

        long totalCopies() {
            return totalCopies;
        }
    }
}
