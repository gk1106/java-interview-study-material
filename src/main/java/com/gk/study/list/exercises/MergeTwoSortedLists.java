package com.gk.study.list.exercises;

/**
 * M05 [Medium] Merge two sorted (ascending) singly linked lists into one sorted list.
 * Input:  a=1-&gt;3-&gt;5, b=2-&gt;4-&gt;6  → Output: 1-&gt;2-&gt;3-&gt;4-&gt;5-&gt;6
 * Constraint: O(n+m) time, O(1) extra space (splice existing nodes; a temporary dummy head
 * node is allowed as a technique and doesn't count against the space bound).
 * Pattern: merge / two pointers
 */
public class MergeTwoSortedLists {

    /** Minimal singly linked node used by this exercise. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    /**
     * @param a head of the first sorted list (may be null)
     * @param b head of the second sorted list (may be null)
     * @return head of a single merged, sorted list built by splicing {@code a} and {@code b}'s
     *         nodes together
     */
    public static Node merge(Node a, Node b) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
