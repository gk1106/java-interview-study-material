package com.gk.study.list.solutions;

import java.util.List;

/**
 * Reference solution for {@link com.gk.study.list.exercises.RemoveDuplicatesSorted}.
 * Two pointers: {@code writeIndex} marks the last confirmed-unique position; {@code readIndex}
 * scans ahead looking for the next distinct value.
 */
public class RemoveDuplicatesSortedSolution {

    public static int solve(List<Integer> list) {
        if (list.isEmpty()) {
            return 0;
        }
        int writeIndex = 0;
        for (int readIndex = 1; readIndex < list.size(); readIndex++) {
            if (!list.get(readIndex).equals(list.get(writeIndex))) {
                writeIndex++;
                list.set(writeIndex, list.get(readIndex));
            }
        }
        return writeIndex + 1;
    }
}
