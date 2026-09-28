package com.gk.study.queuedeque.solutions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.QueueUsingTwoStacks}.
 * {@code inStack} absorbs every {@code enqueue} in O(1). {@code outStack} is only refilled (by
 * popping all of {@code inStack} into it, reversing order back to FIFO) when it runs dry -- so
 * each element is moved from inStack to outStack at most once over its lifetime, giving
 * amortized O(1) dequeue/peek even though a single call can be O(n) in the worst case.
 */
public class QueueUsingTwoStacksSolution<T> {

    private final Deque<T> inStack = new ArrayDeque<>();
    private final Deque<T> outStack = new ArrayDeque<>();

    public void enqueue(T item) {
        inStack.push(item);
    }

    public T dequeue() {
        moveIfNeeded();
        if (outStack.isEmpty()) {
            throw new NoSuchElementException("queue is empty");
        }
        return outStack.pop();
    }

    public T peek() {
        moveIfNeeded();
        if (outStack.isEmpty()) {
            throw new NoSuchElementException("queue is empty");
        }
        return outStack.peek();
    }

    public boolean isEmpty() {
        return inStack.isEmpty() && outStack.isEmpty();
    }

    public int size() {
        return inStack.size() + outStack.size();
    }

    private void moveIfNeeded() {
        if (outStack.isEmpty()) {
            while (!inStack.isEmpty()) {
                outStack.push(inStack.pop());
            }
        }
    }
}
