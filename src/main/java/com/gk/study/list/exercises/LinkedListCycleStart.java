package com.gk.study.list.exercises;

/**
 * H02 [Hard] Detect whether a linked list has a cycle, and if so, return the node where the
 * cycle begins (return null if there is no cycle).
 * Input:  a-&gt;b-&gt;c-&gt;d-&gt;b (d.next points back to b)  → Output: node b
 * Input:  a-&gt;b-&gt;c-&gt;null                              → Output: null
 * Constraint: O(n) time, O(1) extra space (no HashSet/visited-set of nodes).
 * Pattern: Floyd's cycle detection (fast/slow pointers)
 */
public class LinkedListCycleStart {

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
     * @return the node where the cycle begins, or {@code null} if the list has no cycle
     */
    public static Node findCycleStart(Node head) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
