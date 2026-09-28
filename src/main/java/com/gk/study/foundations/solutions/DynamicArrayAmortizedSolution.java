package com.gk.study.foundations.solutions;

/** Reference solution for {@code exercises.DynamicArrayAmortized}. */
public class DynamicArrayAmortizedSolution {

    private int[] data;
    private int size;
    private long totalElementCopies;

    public DynamicArrayAmortizedSolution() {
        this.data = new int[1];
        this.size = 0;
        this.totalElementCopies = 0;
    }

    public void push(int value) {
        if (size == data.length) {
            int[] bigger = new int[data.length * 2];
            for (int i = 0; i < size; i++) {
                bigger[i] = data[i];
                totalElementCopies++;
            }
            data = bigger;
        }
        data[size] = value;
        size++;
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
