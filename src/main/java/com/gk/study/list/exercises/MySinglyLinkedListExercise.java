package com.gk.study.list.exercises;

import java.util.Iterator;

/**
 * B02 [Build it yourself] Implement a singly linked list from scratch.
 * Constraint: {@code addFirst}/{@code addLast}/{@code removeFirst} O(1); {@code get(index)}
 * O(n); must keep a {@code tail} reference for O(1) {@code addLast} and correctly clear it when
 * the list becomes empty.
 * Pattern: node chain, head/tail bookkeeping
 *
 * See notes/03-list/09-build-it-yourself-mylist.md for the full design discussion.
 */
public class MySinglyLinkedListExercise<T> implements Iterable<T> {

    // TODO: add a private static Node<T> class (item + next), plus head, tail, size, modCount
    //       fields on the outer class.

    /**
     * Inserts {@code element} at the front of the list.
     *
     * @param element the element to insert
     */
    public void addFirst(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Appends {@code element} at the end of the list.
     *
     * @param element the element to append
     */
    public void addLast(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Removes and returns the first element.
     *
     * @return the removed element
     * @throws java.util.NoSuchElementException if the list is empty
     */
    public T removeFirst() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param index a valid index, {@code 0 <= index < size()}
     * @return the element at {@code index}, found by walking from the head
     */
    public T get(int index) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return the number of elements currently stored
     */
    public int size() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @return true if {@link #size()} is 0
     */
    public boolean isEmpty() {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param element the element to search for
     * @return true if some element in the list is equal to {@code element}
     */
    public boolean contains(T element) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Iterator<T> iterator() {
        // TODO: implement a fail-fast iterator (checks modCount on each next())
        throw new UnsupportedOperationException("TODO");
    }
}
