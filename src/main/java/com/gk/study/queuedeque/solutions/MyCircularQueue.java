package com.gk.study.queuedeque.solutions;

/**
 * A minimal, from-scratch fixed-capacity circular queue, built to internalize the
 * circular-array idea behind {@code ArrayDeque}/{@code ArrayBlockingQueue} (see
 * notes/04-queue-deque/02-arraydeque-internals.md and
 * notes/04-queue-deque/06-build-it-yourself-queue-heap.md).
 *
 * <p>Backed by a single fixed-size {@code int[]}. {@code head} is the index of the front
 * element; the logical element count ({@code count}) -- not a separate {@code tail} index -- is
 * used to tell "empty" and "full" apart and to compute the insertion index as
 * {@code (head + count) % capacity}. Every operation is O(1).
 */
public class MyCircularQueue {

    private final int[] data;
    private int head;
    private int count;

    public MyCircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.data = new int[capacity];
        this.head = 0;
        this.count = 0;
    }

    public boolean enqueue(int value) {
        if (isFull()) {
            return false;
        }
        int insertIndex = (head + count) % data.length;
        data[insertIndex] = value;
        count++;
        return true;
    }

    public boolean dequeue() {
        if (isEmpty()) {
            return false;
        }
        head = (head + 1) % data.length;
        count--;
        return true;
    }

    public int front() {
        return isEmpty() ? -1 : data[head];
    }

    public int rear() {
        return isEmpty() ? -1 : data[(head + count - 1) % data.length];
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean isFull() {
        return count == data.length;
    }
}
