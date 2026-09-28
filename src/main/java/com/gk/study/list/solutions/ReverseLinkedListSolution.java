package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.ReverseLinkedList}.
 * Iterative: rewire prev/curr/next pointers one node at a time, O(1) extra space.
 * Recursive: reverse the tail first, then fix the single link at the head, O(n) call stack.
 */
public class ReverseLinkedListSolution {

    /** Minimal singly linked node used by this solution. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    public static Node reverseIterative(Node head) {
        Node prev = null;
        Node curr = head;
        while (curr != null) {
            Node next = curr.next; // save before overwriting curr.next
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    public static Node reverseRecursive(Node head) {
        if (head == null || head.next == null) {
            return head;
        }
        Node newHead = reverseRecursive(head.next);
        head.next.next = head; // the node after head now points back to head
        head.next = null;
        return newHead;
    }
}
