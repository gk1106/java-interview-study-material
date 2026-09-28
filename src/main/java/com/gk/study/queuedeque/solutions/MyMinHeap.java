package com.gk.study.queuedeque.solutions;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A minimal, from-scratch generic binary min-heap, built to internalize
 * {@code PriorityQueue}'s sift-up/sift-down internals (see
 * notes/04-queue-deque/03-priorityqueue-internals.md and
 * notes/04-queue-deque/06-build-it-yourself-queue-heap.md).
 *
 * <p>Stored as a complete binary tree in array (here, {@code List}) form: for a node at index
 * {@code i}, its children live at {@code 2i+1} and {@code 2i+2}, and its parent at
 * {@code (i-1)/2}. {@code insert} appends at the end then sifts up; {@code extractMin} swaps the
 * last element into the root then sifts it down -- both O(log n).
 *
 * @param <T> element type; must be naturally ordered
 */
public class MyMinHeap<T extends Comparable<T>> {

    private final List<T> heap = new ArrayList<>();

    public void insert(T value) {
        heap.add(value);
        siftUp(heap.size() - 1);
    }

    public T extractMin() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
        T min = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            siftDown(0);
        }
        return min;
    }

    public T peek() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (heap.get(i).compareTo(heap.get(parent)) < 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    private void siftDown(int i) {
        int size = heap.size();
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left < size && heap.get(left).compareTo(heap.get(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && heap.get(right).compareTo(heap.get(smallest)) < 0) {
                smallest = right;
            }
            if (smallest == i) {
                break;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
