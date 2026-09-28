package com.gk.study.foundations.exercises;

/**
 * E03 [Medium] Implement a growable int array that doubles capacity when full, while tracking the
 * total number of element copies performed across all resizes — to empirically demonstrate that
 * push() is amortized O(1) even though an individual resizing push is O(n).
 * Input:  push 0..999 (1000 pushes) starting from capacity 1, doubling each time it's full
 * Output: size() == 1000; getTotalElementCopies() is well under 2 * 1000 (amortized bound)
 * Constraint: push() must be amortized O(1); resize must DOUBLE capacity (not grow by a fixed step).
 * Pattern: amortized analysis / dynamic array growth
 */
public class DynamicArrayAmortized {

    private int[] data;
    private int size;
    private long totalElementCopies;

    public DynamicArrayAmortized() {
        this.data = new int[1];
        this.size = 0;
        this.totalElementCopies = 0;
    }

    public void push(int value) {
        // TODO: if the backing array is full, allocate a new array of DOUBLE the capacity,
        // copy every existing element into it (incrementing totalElementCopies once per element
        // copied), then replace the backing array. Either way, place `value` at index `size`
        // and increment `size`.
        throw new UnsupportedOperationException("TODO");
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return data[index];
    }

    public int size() {
        return size;
    }

    public long getTotalElementCopies() {
        return totalElementCopies;
    }
}
