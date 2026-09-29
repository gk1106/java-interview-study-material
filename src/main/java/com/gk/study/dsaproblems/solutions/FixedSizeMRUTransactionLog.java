package com.gk.study.dsaproblems.solutions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.FixedSizeMRUTransactionLog}.
 *
 * <p>Wraps a {@code LinkedHashMap} constructed with {@code accessOrder = true} (so every
 * {@code get} AND {@code put} moves that entry to the end of the iteration order) and overrides
 * {@code removeEldestEntry} to evict the head of that order — the least-recently-touched entry —
 * once the map grows past capacity. This is exactly the mechanism {@code LinkedHashMap} exposes
 * for building an LRU cache without hand-rolling a doubly linked list (contrast with
 * {@code com.gk.study.map.solutions.LRUCache}, which builds that list from scratch). O(1)
 * amortized get/put.
 */
public final class FixedSizeMRUTransactionLog {

    private final int capacity;
    private final Map<String, String> log;

    public FixedSizeMRUTransactionLog(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, was " + capacity);
        }
        this.capacity = capacity;
        this.log = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > FixedSizeMRUTransactionLog.this.capacity;
            }
        };
    }

    public void put(String transactionId, String detail) {
        log.put(transactionId, detail);
    }

    public String get(String transactionId) {
        return log.get(transactionId);
    }

    public int size() {
        return log.size();
    }
}
