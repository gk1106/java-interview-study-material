package com.gk.study.map.examples;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates LinkedHashMap's two ordering modes (insertion order vs access order) and the
 * {@code removeEldestEntry} hook for a self-evicting bounded LRU cache.
 *
 * See notes/06-map/02-linkedhashmap-lru.md for the internals explanation.
 */
public final class LinkedHashMapLruDemo {

    private LinkedHashMapLruDemo() {
    }

    public static void main(String[] args) {
        System.out.println("--- insertion order (default) ---");
        Map<Integer, String> insertionOrder = new LinkedHashMap<>();
        insertionOrder.put(3, "c");
        insertionOrder.put(1, "a");
        insertionOrder.put(2, "b");
        System.out.println("put order 3,1,2 -> keySet() = " + insertionOrder.keySet());

        System.out.println();
        System.out.println("--- access order (accessOrder = true) ---");
        Map<Integer, String> accessOrder = new LinkedHashMap<>(16, 0.75f, true);
        accessOrder.put(3, "c");
        accessOrder.put(1, "a");
        accessOrder.put(2, "b");
        System.out.println("put order 3,1,2 -> keySet() before any get() = " + accessOrder.keySet());
        accessOrder.get(3); // touching 3 moves it to the tail (most recently used)
        System.out.println("after get(3)                  -> keySet() = " + accessOrder.keySet());

        System.out.println();
        System.out.println("--- removeEldestEntry-based LRU cache, capacity 2 ---");
        LruCache<Integer, String> lru = new LruCache<>(2);
        lru.put(1, "a");
        lru.put(2, "b");
        System.out.println("after put(1,a) put(2,b) -> " + lru.keySet());
        lru.get(1); // touch 1 -> 2 becomes the least-recently-used
        lru.put(3, "c"); // over capacity -> evicts 2 (LRU)
        System.out.println("after get(1), put(3,c) -> " + lru.keySet() + " (2 evicted)");
    }

    /** Minimal LRU cache built on top of {@link LinkedHashMap}'s access-order + eviction hook. */
    private static final class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        LruCache(int capacity) {
            super(16, 0.75f, true); // accessOrder = true is required for LRU semantics
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
