package com.gk.study.concurrency.solutions;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Reference solution for {@code ProducerConsumerWaitNotify}: a hand-rolled bounded buffer on top
 * of a plain {@link ArrayDeque}, guarded entirely by {@code synchronized}/{@code wait}/
 * {@code notifyAll} -- see notes/09-multithreading-concurrency/02-synchronized-intrinsic-locks-wait-notify.md.
 * Both put() and take() re-check their condition in a {@code while} loop (never {@code if}) to
 * correctly handle spurious wakeups and multiple waiters sharing the one wait-set.
 */
public class ProducerConsumerWaitNotifySolution<T> {

    private final Deque<T> items = new ArrayDeque<>();
    private final int capacity;
    private final Object lock = new Object();

    public ProducerConsumerWaitNotifySolution(int capacity) {
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
}
