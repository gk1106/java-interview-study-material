package com.gk.study.dsaproblems.solutions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.LFUCache}.
 *
 * <p>Three structures work together, all O(1) per operation:
 * <ul>
 *   <li>{@code keyToValue} — O(1) value lookup</li>
 *   <li>{@code keyToFrequency} — O(1) lookup of how many times a key has been used</li>
 *   <li>{@code frequencyToKeys}, a {@code Map<Integer, LinkedHashSet<Integer>>} bucketing keys
 *       by their current frequency; within a bucket, the {@code LinkedHashSet}'s iteration order
 *       is insertion order, so its first element is always the least-recently-touched key at
 *       that frequency (exactly the tie-break LFU needs)</li>
 * </ul>
 * A running {@code minFrequency} pointer tracks the lowest frequency currently in use, so
 * eviction (removing the first/oldest key from {@code frequencyToKeys.get(minFrequency)}) never
 * needs to scan for the minimum. Every touch (get, or put on an existing key) moves a key from
 * its old frequency bucket to {@code frequency + 1}, updating {@code minFrequency} if the old
 * bucket becomes empty and was the minimum.
 */
public final class LFUCache {

    private final int capacity;
    private final Map<Integer, Integer> keyToValue = new HashMap<>();
    private final Map<Integer, Integer> keyToFrequency = new HashMap<>();
    private final Map<Integer, LinkedHashSet<Integer>> frequencyToKeys = new HashMap<>();
    private int minFrequency;

    public LFUCache(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be non-negative, was " + capacity);
        }
        this.capacity = capacity;
    }

    public int get(int key) {
        if (!keyToValue.containsKey(key)) {
            return -1;
        }
        touch(key);
        return keyToValue.get(key);
    }

    public void put(int key, int value) {
        if (capacity == 0) {
            return;
        }
        if (keyToValue.containsKey(key)) {
            keyToValue.put(key, value);
            touch(key);
            return;
        }

        if (keyToValue.size() >= capacity) {
            LinkedHashSet<Integer> minBucket = frequencyToKeys.get(minFrequency);
            int evictKey = minBucket.iterator().next();
            minBucket.remove(evictKey);
            if (minBucket.isEmpty()) {
                frequencyToKeys.remove(minFrequency);
            }
            keyToValue.remove(evictKey);
            keyToFrequency.remove(evictKey);
        }

        keyToValue.put(key, value);
        keyToFrequency.put(key, 1);
        frequencyToKeys.computeIfAbsent(1, f -> new LinkedHashSet<>()).add(key);
        minFrequency = 1;
    }

    private void touch(int key) {
        int oldFrequency = keyToFrequency.get(key);
        int newFrequency = oldFrequency + 1;
        keyToFrequency.put(key, newFrequency);

        LinkedHashSet<Integer> oldBucket = frequencyToKeys.get(oldFrequency);
        oldBucket.remove(key);
        if (oldBucket.isEmpty()) {
            frequencyToKeys.remove(oldFrequency);
            if (minFrequency == oldFrequency) {
                minFrequency = newFrequency;
            }
        }
        frequencyToKeys.computeIfAbsent(newFrequency, f -> new LinkedHashSet<>()).add(key);
    }
}
