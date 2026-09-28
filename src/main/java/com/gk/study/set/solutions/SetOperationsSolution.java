package com.gk.study.set.solutions;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference solution for {@link com.gk.study.set.exercises.SetOperations}.
 * Standard set algebra via {@code addAll}/{@code retainAll}/{@code removeAll}, always applied to
 * a fresh copy of {@code a} so the caller's inputs are never mutated and the three results don't
 * interfere with each other.
 */
public class SetOperationsSolution {

    public static Set<Integer> union(int[] a, int[] b) {
        Set<Integer> result = toSet(a);
        result.addAll(toSet(b));
        return result;
    }

    public static Set<Integer> intersection(int[] a, int[] b) {
        Set<Integer> result = toSet(a);
        result.retainAll(toSet(b));
        return result;
    }

    public static Set<Integer> difference(int[] a, int[] b) {
        Set<Integer> result = toSet(a);
        result.removeAll(toSet(b));
        return result;
    }

    private static Set<Integer> toSet(int[] arr) {
        Set<Integer> set = new HashSet<>();
        for (int x : arr) {
            set.add(x);
        }
        return set;
    }
}
