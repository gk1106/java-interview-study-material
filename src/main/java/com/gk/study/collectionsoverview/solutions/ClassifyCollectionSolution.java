package com.gk.study.collectionsoverview.solutions;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Reference solution for E01 (see exercises.ClassifyCollection).
 */
public class ClassifyCollectionSolution {

    public static String classify(Object o) {
        if (o instanceof Deque) {
            return "Deque";
        }
        if (o instanceof Queue) {
            return "Queue";
        }
        if (o instanceof List) {
            return "List";
        }
        if (o instanceof Set) {
            return "Set";
        }
        if (o instanceof Map) {
            return "Map";
        }
        if (o instanceof Collection) {
            return "Collection (unspecialized)";
        }
        return "not a collection type";
    }
}
