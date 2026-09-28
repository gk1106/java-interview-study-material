package com.gk.study.list.exercises;

/**
 * H01 [Hard] Reverse the nodes of a singly linked list, k at a time. If the number of remaining
 * nodes is not a multiple of k, leave the final group as-is.
 * Input:  1-&gt;2-&gt;3-&gt;4-&gt;5-&gt;null, k=2  → Output: 2-&gt;1-&gt;4-&gt;3-&gt;5-&gt;null
 * Input:  1-&gt;2-&gt;3-&gt;4-&gt;5-&gt;null, k=3  → Output: 3-&gt;2-&gt;1-&gt;4-&gt;5-&gt;null
 * Constraint: O(n) time, O(1) extra space (iterative) or O(n/k) recursion depth. Assume k &gt;= 1.
 * Pattern: in-place reversal
 */
public class ReverseKGroup {

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
     * @param k    group size, {@code k >= 1}
     * @return head of the list with every full group of {@code k} nodes reversed in place
     */
    public static Node reverseKGroup(Node head, int k) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
