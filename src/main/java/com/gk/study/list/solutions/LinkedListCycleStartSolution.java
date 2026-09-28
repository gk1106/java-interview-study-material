package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.LinkedListCycleStart}.
 * Floyd's tortoise-and-hare: detect a meeting point with slow (1 step)/fast (2 steps) pointers,
 * then reset one pointer to head and advance both by one step at a time — they meet exactly at
 * the cycle's start.
 */
public class LinkedListCycleStartSolution {

    /** Minimal singly linked node used by this solution. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    public static Node findCycleStart(Node head) {
        Node slow = head;
        Node fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                Node ptr = head;
                while (ptr != slow) {
                    ptr = ptr.next;
                    slow = slow.next;
                }
                return ptr;
            }
        }
        return null; // no cycle
    }
}
