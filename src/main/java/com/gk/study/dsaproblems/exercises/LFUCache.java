package com.gk.study.dsaproblems.exercises;

/**
 * H08 [Hard] {@code get}/{@code put} with capacity eviction, but unlike LRU: when full, evict
 * the LEAST-FREQUENTLY-used key; ties among equally-frequent keys are broken by
 * least-recently-used. Every {@code get} and {@code put} on an existing key counts as one more
 * use of that key (incrementing its frequency).
 * Input: capacity=2; put(1,1); put(2,2); get(1)&rarr;1; put(3,3) (evicts key 2, the least
 * frequently used); get(2)&rarr;-1; get(3)&rarr;3
 * Constraint: {@code get} and {@code put} must both run in O(1).
 * Pattern: HashMap + frequency buckets
 * Collections: HashMap&lt;key, Node&gt; (O(1) lookup) + HashMap&lt;frequency,
 * LinkedHashSet&lt;key&gt;&gt; (per-frequency bucket, where the LinkedHashSet's iteration order
 * preserves recency for the tie-break) + a running {@code minFrequency} pointer
 */
public class LFUCache {

    // TODO: add fields, e.g.:
    //  - int capacity
    //  - Map<Integer, Integer> keyToValue
    //  - Map<Integer, Integer> keyToFrequency
    //  - Map<Integer, LinkedHashSet<Integer>> frequencyToKeys (iteration order = recency within
    //    that frequency, oldest first)
    //  - int minFrequency

    public LFUCache(int capacity) {
        // TODO: store capacity and initialize the maps above
    }

    /**
     * @return the value for {@code key}, incrementing its use frequency, or {@code -1} if absent
     */
    public int get(int key) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Inserts or updates {@code key}, incrementing its use frequency (a fresh key starts at
     * frequency 1). If the cache is over capacity after the insert, evicts the
     * least-frequently-used key, breaking ties by least-recently-used.
     */
    public void put(int key, int value) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
