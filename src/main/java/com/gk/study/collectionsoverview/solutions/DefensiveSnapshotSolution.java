package com.gk.study.collectionsoverview.solutions;

import java.util.List;

/**
 * Reference solution for E03 (see exercises.DefensiveSnapshot).
 */
public class DefensiveSnapshotSolution {

    public static <T> List<T> of(List<T> source) {
        return List.copyOf(source);
    }
}
