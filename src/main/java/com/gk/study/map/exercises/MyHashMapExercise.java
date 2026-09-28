package com.gk.study.map.exercises;

/**
 * B1 [Build-it-yourself] Implement a HashMap from scratch.
 * Required API: put(K,V), get(K), remove(K), containsKey(K), size(), isEmpty().
 * Constraint: separate chaining via your own Node&lt;K,V&gt;[] bucket array; resize (double
 * capacity, rehash every entry) once size exceeds capacity * 0.75; O(1) average get/put/remove.
 * Pattern: hashing + dynamic array of buckets (mirrors java.util.HashMap's internals)
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MyHashMapExercise<K, V> {

    // TODO: add fields, e.g. a Node<K,V>[] table, size, threshold

    /**
     * @return the previous value associated with {@code key}, or {@code null} if there was none
     */
    public V put(K key, V value) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the value associated with {@code key}, or {@code null} if absent
     */
    public V get(K key) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the removed value, or {@code null} if the key was not present
     */
    public V remove(K key) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public boolean containsKey(K key) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isEmpty() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
