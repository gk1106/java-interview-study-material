package com.gk.study.collectionsoverview.solutions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Reference solution for E01 (see exercises.DedupePreservingOrder).
 */
public class DedupePreservingOrderSolution {

    public static List<String> dedupe(List<String> ids) {
        Set<String> seen = new LinkedHashSet<>(ids);
        return new ArrayList<>(seen);
    }
}
