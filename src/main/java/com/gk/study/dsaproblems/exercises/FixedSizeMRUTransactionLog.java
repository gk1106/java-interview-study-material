package com.gk.study.dsaproblems.exercises;

/**
 * M09 [Medium] A fixed-capacity transaction log keyed by transaction ID. Once the log is full,
 * writing a new entry evicts the least-recently-<em>touched</em> entry (a "touch" is either a
 * {@code get} or a {@code put}) — i.e. an LRU-eviction cache, built this time on top of
 * {@code LinkedHashMap}'s built-in access-order mode instead of a hand-rolled linked list
 * (contrast with module 06's from-scratch {@code LRUCacheExercise}).
 * Input: capacity=2; put("t1","a"); put("t2","b"); put("t3","c") &rarr; t1 is evicted (t2, t3
 * remain); get("t1") &rarr; null
 * Constraint: get/put O(1) amortized.
 * Pattern: access-order eviction
 * Collections: LinkedHashMap
 */
public class FixedSizeMRUTransactionLog {

    // TODO: add a field, e.g. a LinkedHashMap<String, String> constructed with
    // (initialCapacity, loadFactor, accessOrder=true) and an overridden removeEldestEntry that
    // returns true once size() exceeds the configured capacity

    public FixedSizeMRUTransactionLog(int capacity) {
        // TODO: store capacity and initialize the access-order LinkedHashMap
    }

    /** Records/updates {@code detail} for {@code transactionId}, touching it as most recent. */
    public void put(String transactionId, String detail) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the detail for {@code transactionId}, touching it as most recent, or {@code null}
     *     if absent
     */
    public String get(String transactionId) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
