package com.gk.study.queuedeque.examples;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Part 1 demonstrates the real {@link ArrayDeque} used as both a stack (LIFO, via
 * push/pop = addFirst/removeFirst) and a queue (FIFO, via offer/poll = addLast/removeFirst).
 *
 * <p>Part 2 is a small hand-written teaching model, {@link RingBufferModel}, that mirrors what
 * ArrayDeque does internally: a circular array with {@code head}/{@code tail} indices and
 * doubling growth. This is NOT reflection into the real JDK class (its fields are private, and
 * java.util does not open itself for deep reflection without extra JVM flags) -- it's a
 * from-scratch model built to make the index arithmetic visible step by step.
 *
 * See notes/04-queue-deque/02-arraydeque-internals.md.
 */
public final class ArrayDequeInternalsDemo {

    private ArrayDequeInternalsDemo() {
    }

    public static void main(String[] args) {
        System.out.println("== Part 1: real ArrayDeque as stack vs queue ==");
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println("stack push(1,2,3) -> pop,pop,pop = "
                + stack.pop() + ", " + stack.pop() + ", " + stack.pop() + "  (LIFO)");

        Deque<Integer> queue = new ArrayDeque<>();
        queue.offer(1);
        queue.offer(2);
        queue.offer(3);
        System.out.println("queue offer(1,2,3) -> poll,poll,poll = "
                + queue.poll() + ", " + queue.poll() + ", " + queue.poll() + "  (FIFO)");

        System.out.println();
        System.out.println("== Part 2: circular-array model (head/tail wraparound + doubling growth) ==");
        RingBufferModel<String> ring = new RingBufferModel<>(4);
        ring.addLast("A");
        ring.addLast("B");
        ring.addLast("C");
        ring.print("addLast A,B,C");

        ring.removeFirst();
        ring.print("removeFirst() drops A, head advances");

        ring.addFirst("Z");
        ring.print("addFirst Z, head steps back by one");

        ring.addFirst("Y");
        ring.print("addFirst Y, head WRAPS from 0 to capacity-1");

        ring.addLast("D");
        ring.print("addLast D -> size==capacity triggers doubling growth BEFORE the insert");

        ring.addLast("E");
        ring.print("addLast E, plenty of room now");
    }

    /**
     * Teaching model of a circular-array deque, capacity doubling when full. Real ArrayDeque
     * additionally avoids storing a separate {@code size} field at all -- it computes size on
     * demand as {@code (tail - head) & (capacity - 1)}, relying on capacity always being a power
     * of two so wraparound is a bitmask instead of a modulo. This model keeps an explicit
     * {@code size} counter instead, since it doesn't enforce power-of-two capacities.
     */
    private static final class RingBufferModel<T> {
        private Object[] elements;
        private int head;
        private int tail;
        private int size;

        RingBufferModel(int initialCapacity) {
            this.elements = new Object[initialCapacity];
        }

        void addLast(T value) {
            growIfFull();
            elements[tail] = value;
            tail = (tail + 1) % elements.length;
            size++;
        }

        void addFirst(T value) {
            growIfFull();
            head = (head - 1 + elements.length) % elements.length;
            elements[head] = value;
            size++;
        }

        @SuppressWarnings("unchecked")
        T removeFirst() {
            T value = (T) elements[head];
            elements[head] = null;
            head = (head + 1) % elements.length;
            size--;
            return value;
        }

        private void growIfFull() {
            if (size == elements.length) {
                int newCapacity = elements.length * 2;
                Object[] newElements = new Object[newCapacity];
                for (int i = 0; i < size; i++) {
                    newElements[i] = elements[(head + i) % elements.length];
                }
                elements = newElements;
                head = 0;
                tail = size;
            }
        }

        @SuppressWarnings("unchecked")
        List<T> logicalOrder() {
            List<T> result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                result.add((T) elements[(head + i) % elements.length]);
            }
            return result;
        }

        void print(String label) {
            System.out.println(label + "\n  raw=" + arrayString()
                    + " capacity=" + elements.length + " head=" + head + " tail=" + tail
                    + " size=" + size + "\n  logical order=" + logicalOrder());
        }

        private String arrayString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < elements.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                if (i == head) {
                    sb.append("H:");
                }
                if (i == tail) {
                    sb.append("T:");
                }
                sb.append(elements[i] == null ? "_" : elements[i]);
            }
            return sb.append(']').toString();
        }
    }
}
