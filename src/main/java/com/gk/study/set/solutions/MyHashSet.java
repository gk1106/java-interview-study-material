package com.gk.study.set.solutions;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A minimal, from-scratch hash set implementation, built to internalize how {@link java.util.HashSet}
 * works internally (see notes/05-set/01-hashset-linkedhashset-treeset.md): a bucket array of
 * singly linked chains (separate chaining), hash spreading matching {@code HashMap}'s
 * {@code h ^ (h >>> 16)} trick, and a resize (double capacity, rehash everything) once
 * {@code size > capacity * 0.75}.
 *
 * @param <T> element type
 */
public final class MyHashSet<T> implements Iterable<T> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private Node<T>[] buckets;
    private int size;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public MyHashSet() {
        this.buckets = new Node[DEFAULT_CAPACITY];
    }

    public boolean add(T element) {
        int index = bucketIndex(element, buckets.length);
        Node<T> curr = buckets[index];
        while (curr != null) {
            if (Objects.equals(curr.value, element)) {
                return false; // already present
            }
            curr = curr.next;
        }
        buckets[index] = new Node<>(element, buckets[index]);
        size++;
        if (size > buckets.length * LOAD_FACTOR) {
            resize();
        }
        return true;
    }

    public boolean remove(T element) {
        int index = bucketIndex(element, buckets.length);
        Node<T> curr = buckets[index];
        Node<T> prev = null;
        while (curr != null) {
            if (Objects.equals(curr.value, element)) {
                if (prev == null) {
                    buckets[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return true;
            }
            prev = curr;
            curr = curr.next;
        }
        return false;
    }

    public boolean contains(T element) {
        int index = bucketIndex(element, buckets.length);
        Node<T> curr = buckets[index];
        while (curr != null) {
            if (Objects.equals(curr.value, element)) {
                return true;
            }
            curr = curr.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Spreads a hash code's high bits into its low bits before masking, the same trick
     * {@code HashMap} uses, so that a small table (power-of-two capacity) doesn't ignore high-order
     * bits when computing {@code hash & (capacity - 1)}.
     */
    private static int bucketIndex(Object element, int capacity) {
        int h = element == null ? 0 : element.hashCode();
        h ^= (h >>> 16);
        return h & (capacity - 1);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void resize() {
        Node<T>[] oldBuckets = buckets;
        buckets = new Node[oldBuckets.length * 2];
        for (Node<T> head : oldBuckets) {
            Node<T> curr = head;
            while (curr != null) {
                Node<T> next = curr.next;
                int index = bucketIndex(curr.value, buckets.length);
                curr.next = buckets[index];
                buckets[index] = curr;
                curr = next;
            }
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<T> {
        private int bucketIndex = 0;
        private Node<T> next = advanceToNextBucket();

        private Node<T> advanceToNextBucket() {
            while (bucketIndex < buckets.length) {
                Node<T> head = buckets[bucketIndex];
                bucketIndex++;
                if (head != null) {
                    return head;
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            T value = next.value;
            next = next.next != null ? next.next : advanceToNextBucket();
            return value;
        }
    }

    private static final class Node<T> {
        final T value;
        Node<T> next;

        Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }
}
