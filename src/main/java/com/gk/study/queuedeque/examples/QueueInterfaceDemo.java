package com.gk.study.queuedeque.examples;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Demonstrates the two parallel method families on {@link Queue}: throwing
 * ({@code add}/{@code remove}/{@code element}) vs. returning a sentinel
 * ({@code offer}/{@code poll}/{@code peek}) -- on both an unbounded and a capacity-restricted
 * queue.
 *
 * See notes/04-queue-deque/01-queue-interface.md.
 */
public final class QueueInterfaceDemo {

    private QueueInterfaceDemo() {
    }

    public static void main(String[] args) {
        Queue<Integer> queue = new ArrayDeque<>();

        System.out.println("-- empty queue: poll()/peek() return a sentinel, do NOT throw --");
        System.out.println("poll() on empty -> " + queue.poll());
        System.out.println("peek() on empty -> " + queue.peek());

        System.out.println();
        System.out.println("-- empty queue: remove()/element() throw NoSuchElementException --");
        try {
            queue.remove();
        } catch (NoSuchElementException e) {
            System.out.println("remove() on empty -> threw NoSuchElementException");
        }
        try {
            queue.element();
        } catch (NoSuchElementException e) {
            System.out.println("element() on empty -> threw NoSuchElementException");
        }

        System.out.println();
        System.out.println("-- unbounded queue: offer()/add() both simply succeed --");
        queue.offer(1);
        queue.add(2);
        System.out.println("queue after offer(1), add(2) -> " + queue);

        System.out.println();
        System.out.println("-- capacity-restricted queue (ArrayBlockingQueue, capacity 2) --");
        Queue<Integer> bounded = new ArrayBlockingQueue<>(2);
        bounded.add(1);
        bounded.add(2);
        boolean offered = bounded.offer(3);
        System.out.println("offer(3) when full -> " + offered + "  (returns false, no exception)");
        try {
            bounded.add(3);
        } catch (IllegalStateException e) {
            System.out.println("add(3) when full -> threw IllegalStateException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("-- FIFO drain order --");
        System.out.println("poll() -> " + queue.poll());
        System.out.println("poll() -> " + queue.poll());
    }
}
