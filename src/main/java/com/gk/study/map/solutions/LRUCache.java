package com.gk.study.map.solutions;

import java.util.HashMap;
import java.util.Map;

/**
 * A from-scratch, O(1) get/put LRU (least-recently-used) cache, built to internalize the exact
 * mechanism behind {@code LinkedHashMap}'s access-order + {@code removeEldestEntry} trick (see
 * notes/06-map/02-linkedhashmap-lru.md) rather than delegating to it.
 *
 * <p>Two data structures working together:
 * <ul>
 *   <li>a {@code Map<K, Node>} for O(1) lookup of a key's node</li>
 *   <li>a hand-rolled intrusive doubly linked list, threaded through those same {@code Node}
 *       objects, ordered from least-recently-used (just after the {@code head} sentinel) to
 *       most-recently-used (just before the {@code tail} sentinel)</li>
 * </ul>
 * Both {@code get} and {@code put} are O(1): a map lookup plus a constant number of pointer
 * updates to unlink/relink a node, no scanning required.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class LRUCache<K, V> {

    private final int capacity;
    private final Map<K, Node<K, V>> lookup;
    private final Node<K, V> head; // sentinel: head.next is the least-recently-used real node
    private final Node<K, V> tail; // sentinel: tail.prev is the most-recently-used real node

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, was " + capacity);
        }
        this.capacity = capacity;
        this.lookup = new HashMap<>();
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    /** @return the value for {@code key}, marking it most recently used, or {@code null} if absent */
    public V get(K key) {
        Node<K, V> node = lookup.get(key);
        if (node == null) {
            return null;
        }
        moveToTail(node);
        return node.value;
    }

    /**
     * Inserts or updates {@code key}, marking it most recently used. If the cache exceeds
     * capacity after the insert, evicts the least-recently-used entry (the node right after the
     * head sentinel).
     */
    public void put(K key, V value) {
        Node<K, V> existing = lookup.get(key);
        if (existing != null) {
            existing.value = value;
            moveToTail(existing);
            return;
        }

        Node<K, V> node = new Node<>(key, value);
        lookup.put(key, node);
        appendAtTail(node);

        if (lookup.size() > capacity) {
            Node<K, V> lru = head.next; // least-recently-used real node
            unlink(lru);
            lookup.remove(lru.key);
        }
    }

    public int size() {
        return lookup.size();
    }

    // -- doubly linked list helpers, all O(1) -------------------------------------------------

    private void moveToTail(Node<K, V> node) {
        unlink(node);
        appendAtTail(node);
    }

    private void unlink(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void appendAtTail(Node<K, V> node) {
        Node<K, V> lastReal = tail.prev;
        lastReal.next = node;
        node.prev = lastReal;
        node.next = tail;
        tail.prev = node;
    }

    private static final class Node<K, V> {
        final K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
