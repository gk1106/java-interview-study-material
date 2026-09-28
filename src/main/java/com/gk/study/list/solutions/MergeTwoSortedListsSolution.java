package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.MergeTwoSortedLists}.
 * Uses a dummy head + a running tail pointer; repeatedly splices the smaller of the two current
 * nodes onto the result, then attaches whatever remains of the non-exhausted list.
 */
public class MergeTwoSortedListsSolution {

    /** Minimal singly linked node used by this solution. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    public static Node merge(Node a, Node b) {
        Node dummy = new Node(0);
        Node tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) {
                tail.next = a;
                a = a.next;
            } else {
                tail.next = b;
                b = b.next;
            }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;
        return dummy.next;
    }
}
