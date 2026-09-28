package com.gk.study.collectionsoverview.solutions;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reference solution for E04 (see exercises.MapEntriesSortedByValue).
 */
public class MapEntriesSortedByValueSolution {

    public static List<String> sortedEntries(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
    }
}
