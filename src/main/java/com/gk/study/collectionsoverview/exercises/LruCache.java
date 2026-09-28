package com.gk.study.collectionsoverview.exercises;

import java.util.Map;

/**
 * E03 [Medium] Build a bounded LRU (least-recently-used) cache on top of
 * {@code LinkedHashMap}'s access-order mode: once the cache exceeds its
 * capacity, the least-recently-accessed entry (by get OR put) is evicted
 * automatically.
 *
 * Input:  a capacity at construction, then a sequence of put/get calls.
 * Output: get(key) returns the value or null if absent/evicted; after
 *         exceeding capacity, the least-recently-used entry is gone.
 *
 * Example:
 *   LruCache&lt;String,Integer&gt; c = new LruCache&lt;&gt;(2);
 *   c.put("a", 1); c.put("b", 2);
 *   c.get("a");          // touches "a" -&gt; now more recent than "b"
 *   c.put("c", 3);        // capacity exceeded -&gt; evicts "b" (least recently used)
 *   c.get("b") -&gt; null
 *   c.get("a") -&gt; 1
 *   c.get("c") -&gt; 3
 *
 * Constraint: O(1) average get/put. Capacity is fixed at construction and
 * must be &gt;= 1.
 * Pattern: LRU via LinkedHashMap access-order mode + removeEldestEntry
 */
public class LruCache<K, V> {

    private final int capacity;
    private final Map<K, V> backing;

    public LruCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        // TODO: initialize `backing` as a LinkedHashMap with accessOrder=true
        // and override removeEldestEntry to evict once size() > capacity.
        this.backing = null;
        throw new UnsupportedOperationException("TODO");
    }

    public void put(K key, V value) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public V get(K key) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
