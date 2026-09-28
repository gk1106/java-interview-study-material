package com.gk.study.collectionsoverview.solutions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reference solution for E03 (see exercises.LruCache).
 */
public class LruCacheSolution<K, V> {

    private final int capacity;
    private final Map<K, V> backing;

    public LruCacheSolution(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        this.backing = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LruCacheSolution.this.capacity;
            }
        };
    }

    public void put(K key, V value) {
        backing.put(key, value);
    }

    public V get(K key) {
        return backing.get(key);
    }

    public int size() {
        return backing.size();
    }
}
