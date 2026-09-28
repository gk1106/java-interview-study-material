package com.gk.study.list.solutions;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A minimal, from-scratch singly linked list, built to internalize LinkedList's internals (see
 * notes/03-list/03-linkedlist-internals.md and notes/03-list/09-build-it-yourself-mylist.md).
 *
 * <p>Keeps both {@code head} and {@code tail} references so {@link #addLast} is O(1); without a
 * tail reference, addLast would require an O(n) walk on every call.
 *
 * @param <T> element type
 */
public final class MySinglyLinkedList<T> implements Iterable<T> {

    private static final class Node<T> {
        T item;
        Node<T> next;

        Node(T item) {
            this.item = item;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;
    private int modCount;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(T element) {
        Node<T> node = new Node<>(element);
        node.next = head;
        head = node;
        if (tail == null) {
            tail = node;
        }
        size++;
        modCount++;
    }

    public void addLast(T element) {
        Node<T> node = new Node<>(element);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        modCount++;
    }

    public T removeFirst() {
        if (head == null) {
            throw new NoSuchElementException("list is empty");
        }
        Node<T> removed = head;
        head = head.next;
        if (head == null) {
            tail = null; // must clear tail too, else it dangles and corrupts the next addLast
        }
        removed.next = null;
        size--;
        modCount++;
        return removed.item;
    }

    public T get(int index) {
        checkIndex(index);
        Node<T> curr = head;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.item;
    }

    public boolean contains(T element) {
        for (Node<T> curr = head; curr != null; curr = curr.next) {
            if (Objects.equals(curr.item, element)) {
                return true;
            }
        }
        return false;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<T> {
        private Node<T> cursor = head;
        private final int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return cursor != null;
        }

        @Override
        public T next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (cursor == null) {
                throw new NoSuchElementException();
            }
            T item = cursor.item;
            cursor = cursor.next;
            return item;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> curr = head;
        while (curr != null) {
            sb.append(curr.item);
            if (curr.next != null) {
                sb.append(", ");
            }
            curr = curr.next;
        }
        return sb.append(']').toString();
    }
}
