package com.gk.study.queuedeque.solutions;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Reference solution for {@link com.gk.study.queuedeque.exercises.MergeKSortedLists}.
 * Seeds a min-heap with the head node of every non-empty list (O(k log k)). Repeatedly polls the
 * smallest head, splices it onto the result, and if that node had a next node, offers it back
 * into the heap. Each of the n total nodes is offered and polled exactly once, each heap
 * operation is O(log k), giving O(n log k) total -- versus O(n*k) for repeatedly doing a plain
 * pairwise merge of all k lists.
 */
public class MergeKSortedListsSolution {

    /** Minimal singly linked node used by this solution. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    public static Node mergeKLists(Node[] lists) {
        PriorityQueue<Node> heap = new PriorityQueue<>(Comparator.comparingInt(node -> node.val));
        for (Node head : lists) {
            if (head != null) {
                heap.offer(head);
            }
        }

        Node dummy = new Node(0);
        Node tail = dummy;
        while (!heap.isEmpty()) {
            Node smallest = heap.poll();
            tail.next = smallest;
            tail = tail.next;
            if (smallest.next != null) {
                heap.offer(smallest.next);
            }
        }
        tail.next = null;
        return dummy.next;
    }
}
