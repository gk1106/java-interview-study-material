package com.gk.study.list.examples;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Demonstrates {@link ArrayDeque} used as both a stack (push/pop/peek at the head) and a FIFO
 * queue (offer/poll at the tail) — the modern replacement for the legacy {@code Stack} and
 * {@code Vector} classes.
 *
 * See notes/03-list/05-vector-stack-arraydeque.md for why Stack/Vector should be avoided.
 */
public final class VectorStackArrayDequeDemo {

    private VectorStackArrayDequeDemo() {
    }

    public static void main(String[] args) {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println(stack.pop());   // 3
        System.out.println(stack.peek());  // 2

        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(1);
        queue.offer(2);
        System.out.println(queue.poll());  // 1 (FIFO)
    }
}
