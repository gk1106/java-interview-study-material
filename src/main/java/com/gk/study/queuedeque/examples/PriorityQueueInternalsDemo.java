package com.gk.study.queuedeque.examples;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Prints the PriorityQueue's backing heap array after every offer/poll, using the public,
 * module-safe {@code toArray()} (which returns {@code Arrays.copyOf(internalArray, size)} --
 * the exact storage order, not a sorted copy) to make sift-up and sift-down visible without any
 * reflection.
 *
 * See notes/04-queue-deque/03-priorityqueue-internals.md.
 */
public final class PriorityQueueInternalsDemo {

    private PriorityQueueInternalsDemo() {
    }

    public static void main(String[] args) {
        System.out.println("== Min-heap (natural ordering) ==");
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        int[] offers = {5, 3, 8, 1, 9, 2};
        for (int v : offers) {
            minHeap.offer(v);
            System.out.println("offer(" + v + ") -> heap array = " + Arrays.toString(minHeap.toArray()));
        }
        System.out.println("NOTE: the array above is NOT fully sorted -- only the heap-order");
        System.out.println("invariant (parent <= both children) holds at every index.");

        System.out.println();
        System.out.println("polling drains in ascending order (sift-down restores the heap each time):");
        while (!minHeap.isEmpty()) {
            int head = minHeap.peek();
            minHeap.poll();
            System.out.println("poll() -> " + head + "  remaining heap array = " + Arrays.toString(minHeap.toArray()));
        }

        System.out.println();
        System.out.println("== Max-heap (custom Comparator.reverseOrder()) ==");
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        for (int v : offers) {
            maxHeap.offer(v);
        }
        System.out.println("max-heap array = " + Arrays.toString(maxHeap.toArray()) + "  peek() = " + maxHeap.peek());

        System.out.println();
        System.out.println("== Iteration order != sorted order (common interview gotcha) ==");
        PriorityQueue<Integer> pq = new PriorityQueue<>(offers.length);
        for (int v : offers) {
            pq.offer(v);
        }
        StringBuilder iterationOrder = new StringBuilder();
        for (int v : pq) {
            iterationOrder.append(v).append(' ');
        }
        System.out.println("for-each iteration -> " + iterationOrder.toString().trim() + "  (raw heap array order, NOT sorted)");
        StringBuilder pollOrder = new StringBuilder();
        while (!pq.isEmpty()) {
            pollOrder.append(pq.poll()).append(' ');
        }
        System.out.println("repeated poll()     -> " + pollOrder.toString().trim() + "  (this IS sorted ascending)");
    }
}
