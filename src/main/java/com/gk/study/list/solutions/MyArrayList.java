package com.gk.study.list.solutions;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A minimal, from-scratch reimplementation of {@link java.util.ArrayList}, built to internalize
 * ArrayList's internals (see notes/03-list/02-arraylist-internals.md and
 * notes/03-list/09-build-it-yourself-mylist.md).
 *
 * <p>Backed by an {@code Object[]}, since Java does not allow creating a generic array
 * ({@code new T[n]}) directly due to type erasure. Reads cast the {@code Object} back to
 * {@code T} inside a single private accessor — safe because every write into the array goes
 * through generically-typed methods ({@link #add}, {@link #set}).
 *
 * <p>Growth factor is ~1.5x ({@code oldCapacity + (oldCapacity >> 1)}), matching the real
 * ArrayList, to preserve amortized O(1) append.
 *
 * @param <T> element type
 */
public final class MyArrayList<T> implements Iterable<T> {

    private static final int DEFAULT_CAPACITY = 10;
    private static final Object[] EMPTY = new Object[0];

    private Object[] elements;
    private int size;
    private int modCount;

    public MyArrayList() {
        this.elements = EMPTY;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean add(T element) {
        ensureCapacityForOneMore();
        elements[size++] = element;
        modCount++;
        return true;
    }

    public void add(int index, T element) {
        checkIndexForInsert(index);
        ensureCapacityForOneMore();
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
        modCount++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) elements[index];
    }

    @SuppressWarnings("unchecked")
    public T set(int index, T element) {
        checkIndex(index);
        T old = (T) elements[index];
        elements[index] = element;
        return old;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkIndex(index);
        T removed = (T) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[--size] = null; // let GC reclaim the reference
        modCount++;
        return removed;
    }

    public boolean contains(T element) {
        return indexOf(element) >= 0;
    }

    public int indexOf(T element) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(elements[i], element)) {
                return i;
            }
        }
        return -1;
    }

    private void ensureCapacityForOneMore() {
        if (elements == EMPTY) {
            elements = new Object[DEFAULT_CAPACITY];
            return;
        }
        if (size == elements.length) {
            int oldCapacity = elements.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity <= oldCapacity) { // guards oldCapacity == 0 or 1
                newCapacity = oldCapacity + 1;
            }
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
    }

    private void checkIndexForInsert(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<T> {
        private int cursor = 0;
        private final int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            checkForComodification();
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            return (T) elements[cursor++];
        }

        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }
}
