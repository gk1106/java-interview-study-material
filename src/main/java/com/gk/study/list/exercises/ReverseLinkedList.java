package com.gk.study.list.exercises;

/**
 * M04 [Medium] Reverse a singly linked list, both iteratively and recursively.
 * Input:  1-&gt;2-&gt;3-&gt;null  → Output: 3-&gt;2-&gt;1-&gt;null
 * Constraint: O(n) time. Iterative version must use O(1) extra space; recursive version may
 * use O(n) call-stack space.
 * Pattern: in-place reversal
 */
public class ReverseLinkedList {

    /** Minimal singly linked node used by this exercise. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    /**
     * @param head head of the list (may be null)
     * @return head of the reversed list, using an iterative approach (O(1) extra space)
     */
    public static Node reverseIterative(Node head) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param head head of the list (may be null)
     * @return head of the reversed list, using a recursive approach
     */
    public static Node reverseRecursive(Node head) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
