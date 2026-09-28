package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Solution for {@link com.gk.study.map.exercises.TimeBasedKeyValueStore}.
 *
 * <p>One {@link TreeMap} per key, keyed by timestamp. {@code get} uses {@code floorEntry} to find
 * the largest timestamp {@code <= timestamp} in O(log n), thanks to the red-black tree backing.
 */
public final class TimeBasedKeyValueStoreSolution {

    private final Map<String, TreeMap<Long, String>> store = new HashMap<>();

    public void set(String key, String value, long timestamp) {
        store.computeIfAbsent(key, k -> new TreeMap<>()).put(timestamp, value);
    }

    public String get(String key, long timestamp) {
        TreeMap<Long, String> history = store.get(key);
        if (history == null) {
            return "";
        }
        Map.Entry<Long, String> floor = history.floorEntry(timestamp);
        return floor == null ? "" : floor.getValue();
    }
}
