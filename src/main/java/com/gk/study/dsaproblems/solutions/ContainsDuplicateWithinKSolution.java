package com.gk.study.dsaproblems.solutions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.ContainsDuplicateWithinK}.
 *
 * <p>Slide a {@code HashSet} window of size at most k across the list: before checking index i,
 * evict the id that has fallen more than k positions behind (index {@code i - k - 1}); if
 * {@code set.add(id)} returns false, that id is already present within the window. O(n) time,
 * O(min(n, k)) space.
 */
public final class ContainsDuplicateWithinKSolution {

    private ContainsDuplicateWithinKSolution() {
    }

    public static boolean solve(List<String> transactionIds, int k) {
        if (k < 0) {
            throw new IllegalArgumentException("k must be >= 0");
        }
        Set<String> window = new HashSet<>();
        for (int i = 0; i < transactionIds.size(); i++) {
            if (i > k) {
                window.remove(transactionIds.get(i - k - 1));
            }
            if (!window.add(transactionIds.get(i))) {
                return true;
            }
        }
        return false;
    }
}
