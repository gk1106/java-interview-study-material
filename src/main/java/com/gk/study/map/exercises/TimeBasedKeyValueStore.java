package com.gk.study.map.exercises;

/**
 * H2 [Hard] Design a time-based key-value store.
 * Operations:
 *   set(key, value, timestamp) — stores the value for the key at the given timestamp.
 *   get(key, timestamp)        — returns the value set at the largest timestamp &lt;= the given
 *                                 timestamp for that key, or "" if no such value exists.
 * Example: set("a","1",1); set("a","2",3); get("a",2) → "1"; get("a",4) → "2"; get("a",0) → ""
 * Constraint: timestamps passed to set() for the same key are strictly increasing;
 *             get() should run in O(log n).
 * Pattern: TreeMap per key + floorKey binary search
 */
public class TimeBasedKeyValueStore {

    // TODO: add a backing field, e.g. Map<String, TreeMap<Long, String>>

    public TimeBasedKeyValueStore() {
        // TODO: initialize your backing field
    }

    /**
     * Stores {@code value} for {@code key} at {@code timestamp}. Timestamps for a given key are
     * guaranteed to be passed in strictly increasing order.
     */
    public void set(String key, String value, long timestamp) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the value set for {@code key} at the largest timestamp {@code <= timestamp}, or
     *         {@code ""} if the key doesn't exist or has no value at or before that timestamp
     */
    public String get(String key, long timestamp) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
