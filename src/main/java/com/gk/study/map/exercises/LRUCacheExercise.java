package com.gk.study.map.exercises;

/**
 * B2 [Build-it-yourself] Implement an O(1) get/put LRU (least-recently-used) cache.
 * Required API: get(K) (returns the value, or null if absent, and marks the key as most recently
 * used), put(K,V) (inserts/updates and marks most recently used, evicting the least-recently-used
 * entry if the cache is over capacity).
 * Constraint: must NOT wrap java.util.LinkedHashMap — build it from your own HashMap&lt;K, Node&gt;
 * plus a hand-rolled doubly linked list with sentinel head/tail nodes. Both operations O(1).
 * Pattern: HashMap (O(1) node lookup) + intrusive doubly linked list (O(1) reorder/evict)
 *
 * @param <K> key type
 * @param <V> value type
 */
public class LRUCacheExercise<K, V> {

    // TODO: add fields, e.g. a Map<K, Node> lookup, sentinel head/tail Nodes, capacity

    public LRUCacheExercise(int capacity) {
        // TODO: store the capacity and initialize your lookup map + sentinel linked-list nodes
    }

    /**
     * @return the value for {@code key}, marking it most recently used, or {@code null} if absent
     */
    public V get(K key) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Inserts or updates {@code key}, marking it most recently used. If the cache is over
     * capacity after the insert, evicts the least-recently-used entry.
     */
    public void put(K key, V value) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
