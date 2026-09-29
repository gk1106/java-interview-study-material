package com.gk.study.concurrency.solutions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reference solution for {@code BoundedBlockingQueueExercise}: a generic fixed-capacity FIFO
 * queue built entirely on {@code synchronized}/{@code wait}/{@code notifyAll}, mirroring the shape
 * of {@code java.util.concurrent.ArrayBlockingQueue}'s {@code put}/{@code take} contract without
 * using any {@code java.util.concurrent} queue/synchronizer classes internally. See
 * notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md.
 */
public class BoundedBlockingQueue<T> {

    private final Deque<T> items = new ArrayDeque<>();
    private final int capacity;
    private final Object lock = new Object();

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public void put(T item) throws InterruptedException {
        synchronized (lock) {
            while (items.size() == capacity) {
                lock.wait();
            }
            items.addLast(item);
            lock.notifyAll();
        }
    }

    public T take() throws InterruptedException {
        synchronized (lock) {
            while (items.isEmpty()) {
                lock.wait();
            }
            T item = items.removeFirst();
            lock.notifyAll();
            return item;
        }
    }

    public int size() {
        synchronized (lock) {
            return items.size();
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return items.isEmpty();
        }
    }

    public boolean isFull() {
        synchronized (lock) {
            return items.size() == capacity;
        }
    }
}
