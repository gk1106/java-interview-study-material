package com.gk.study.map.solutions;

import java.util.Objects;

/**
 * A minimal, from-scratch reimplementation of {@link java.util.HashMap}, built to internalize
 * HashMap's internals (see notes/06-map/01-hashmap-internals.md).
 *
 * <p>Backed by a {@code Node<K,V>[] table} using separate chaining for collisions. Mirrors the
 * real HashMap's key design choices:
 * <ul>
 *   <li>hash spreading ({@code h ^ (h >>> 16)}) before masking with {@code (capacity - 1)}</li>
 *   <li>capacity always a power of two, so {@code (capacity - 1) & hash} is a fast substitute
 *       for {@code hash % capacity}</li>
 *   <li>load factor 0.75, doubling resize once {@code size > capacity * 0.75}</li>
 * </ul>
 * Treeification is intentionally omitted here — it is a defense-in-depth optimization for
 * pathological hash distributions, not required for correctness, and is covered conceptually
 * (not reimplemented) in the notes.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private Node<K, V>[] table;
    private int size;
    private int threshold;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.table = (Node<K, V>[]) new Node[DEFAULT_CAPACITY];
        this.threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V put(K key, V value) {
        int hash = spread(key);
        int index = indexFor(hash, table.length);

        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && keysMatch(node.key, key)) {
                V old = node.value;
                node.value = value;
                return old;
            }
        }

        // no existing entry -> prepend a new node (O(1), order within a bucket is not significant)
        table[index] = new Node<>(hash, key, value, table[index]);
        size++;
        if (size > threshold) {
            resize();
        }
        return null;
    }

    public V get(K key) {
        int hash = spread(key);
        int index = indexFor(hash, table.length);
        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && keysMatch(node.key, key)) {
                return node.value;
            }
        }
        return null;
    }

    public V remove(K key) {
        int hash = spread(key);
        int index = indexFor(hash, table.length);

        Node<K, V> prev = null;
        Node<K, V> node = table[index];
        while (node != null) {
            if (node.hash == hash && keysMatch(node.key, key)) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        int hash = spread(key);
        int index = indexFor(hash, table.length);
        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && keysMatch(node.key, key)) {
                return true;
            }
        }
        return false;
    }

    /** Doubles capacity and rehashes every entry into the new table. O(n), amortized O(1)/put. */
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCapacity];

        for (Node<K, V> head : oldTable) {
            Node<K, V> node = head;
            while (node != null) {
                Node<K, V> next = node.next; // save before relinking
                int newIndex = indexFor(node.hash, newCapacity);
                node.next = newTable[newIndex];
                newTable[newIndex] = node;
                node = next;
            }
        }

        table = newTable;
        threshold = (int) (newCapacity * LOAD_FACTOR);
    }

    /** Hash spreading, mirroring java.util.HashMap: fold the high 16 bits into the low 16 bits. */
    private static int spread(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    private static int indexFor(int hash, int capacity) {
        return (capacity - 1) & hash;
    }

    private static <K> boolean keysMatch(K existingKey, K key) {
        return existingKey == key || Objects.equals(existingKey, key);
    }

    private static final class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
}
