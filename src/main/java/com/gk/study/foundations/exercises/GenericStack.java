package com.gk.study.foundations.exercises;

import java.util.NoSuchElementException;

/**
 * E04 [Hard] Generic LIFO stack backed by an Object[] (the JDK's own generic collections use this
 * same "erased array + cast" technique since you cannot legally write `new T[capacity]`).
 * Input:  push(1); push(2); push(3); pop() -> 3; peek() -> 2; size() -> 2
 * Output: see above
 * Constraint: push/pop/peek amortized O(1); resize by DOUBLING when full; pop()/peek() on an
 * empty stack must throw java.util.NoSuchElementException.
 * Pattern: generic array workaround (Object[] + unchecked cast)
 */
public class GenericStack<T> {

    private Object[] data;
    private int size;

    public GenericStack(int initialCapacity) {
        this.data = new Object[Math.max(1, initialCapacity)];
        this.size = 0;
    }

    public void push(T item) {
        // TODO: if full, double capacity (allocate new Object[], copy existing elements);
        // then store item at index `size` and increment size
        throw new UnsupportedOperationException("TODO");
    }

    public T pop() {
        // TODO: if empty, throw new NoSuchElementException(); else remove and return the top
        // element (cast with @SuppressWarnings("unchecked")), decrementing size
        throw new UnsupportedOperationException("TODO");
    }

    public T peek() {
        // TODO: if empty, throw new NoSuchElementException(); else return the top element
        // without removing it (cast with @SuppressWarnings("unchecked"))
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
