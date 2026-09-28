package com.gk.study.foundations.solutions;

import java.util.NoSuchElementException;

/** Reference solution for {@code exercises.GenericStack}. */
public class GenericStackSolution<T> {

    private Object[] data;
    private int size;

    public GenericStackSolution(int initialCapacity) {
        this.data = new Object[Math.max(1, initialCapacity)];
        this.size = 0;
    }

    public void push(T item) {
        if (size == data.length) {
            Object[] bigger = new Object[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, size);
            data = bigger;
        }
        data[size] = item;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        size--;
        T top = (T) data[size];
        data[size] = null; // avoid holding a stale reference (memory leak guard)
        return top;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return (T) data[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
