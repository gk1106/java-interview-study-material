package com.gk.study.set.solutions;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Reference solution for {@link com.gk.study.set.exercises.SmallestRangeKLists}.
 * Keeps one "current pointer" {@code [listIndex, elementIndex]} per list in a TreeSet ordered by
 * value (tie-broken by list index so two lists sharing a value don't collapse into one TreeSet
 * entry). The range's minimum is always {@code pointers.first()}; a running max tracks the
 * largest of the k current pointers. Each of the N total elements is inserted and removed from
 * the TreeSet at most once, so the whole algorithm is O(N log k).
 */
public class SmallestRangeKListsSolution {

    public static int[] solve(List<List<Integer>> lists) {
        int k = lists.size();
        TreeSet<int[]> pointers = new TreeSet<>(
                Comparator.<int[]>comparingInt(p -> lists.get(p[0]).get(p[1]))
                        .thenComparingInt(p -> p[0]));

        int currentMax = Integer.MIN_VALUE;
        for (int listIndex = 0; listIndex < k; listIndex++) {
            pointers.add(new int[] {listIndex, 0});
            currentMax = Math.max(currentMax, lists.get(listIndex).get(0));
        }

        int[] best = null;
        while (true) {
            int[] smallest = pointers.first();
            int listIndex = smallest[0];
            int elementIndex = smallest[1];
            int smallestValue = lists.get(listIndex).get(elementIndex);

            if (best == null || currentMax - smallestValue < best[1] - best[0]) {
                best = new int[] {smallestValue, currentMax};
            }

            if (elementIndex + 1 == lists.get(listIndex).size()) {
                break; // this list is exhausted -- no further range can include all k lists
            }

            pointers.remove(smallest);
            int nextValue = lists.get(listIndex).get(elementIndex + 1);
            pointers.add(new int[] {listIndex, elementIndex + 1});
            currentMax = Math.max(currentMax, nextValue);
        }

        return best;
    }
}
