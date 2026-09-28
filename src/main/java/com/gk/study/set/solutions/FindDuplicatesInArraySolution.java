package com.gk.study.set.solutions;

import java.util.HashSet;
import java.util.Set;

/**
 * Reference solution for {@link com.gk.study.set.exercises.FindDuplicatesInArray}.
 * Single pass: {@code Set.add()} returns false exactly when the element was already present, so
 * checking the return value folds the "have I seen this" lookup and the "mark it seen" write
 * into one hash operation instead of a separate contains() + add() pair.
 */
public class FindDuplicatesInArraySolution {

    public static Set<Integer> solve(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = new HashSet<>();
        for (int n : nums) {
            if (!seen.add(n)) {
                duplicates.add(n);
            }
        }
        return duplicates;
    }
}
