package com.gk.study.queuedeque.solutions;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * A minimal, from-scratch LIFO stack built entirely on top of two FIFO queues (see
 * notes/04-queue-deque/06-build-it-yourself-queue-heap.md). This is the "costly push" design:
 * {@code push} is O(n), while {@code pop}/{@code top} are O(1).
 *
 * <p>{@code q1} always holds the stack in "pop order" (current top at its front). On push, the
 * new item is offered to {@code q2} first, then every existing element is rotated out of
 * {@code q1} into {@code q2} behind it, and finally the two queue references are swapped -- so
 * the newest element ends up at the front of (the new) {@code q1}.
 */
public class StackUsingTwoQueues<T> {

    private Queue<T> q1 = new ArrayDeque<>();
    private Queue<T> q2 = new ArrayDeque<>();

    public void push(T item) {
        q2.offer(item);
        while (!q1.isEmpty()) {
            q2.offer(q1.poll());
        }
        Queue<T> temp = q1;
        q1 = q2;
        q2 = temp;
    }

    public T pop() {
        if (q1.isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return q1.poll();
    }

    public T top() {
        if (q1.isEmpty()) {
            throw new NoSuchElementException("stack is empty");
        }
        return q1.peek();
    }

    public boolean isEmpty() {
        return q1.isEmpty();
    }

    public int size() {
        return q1.size();
    }
}
