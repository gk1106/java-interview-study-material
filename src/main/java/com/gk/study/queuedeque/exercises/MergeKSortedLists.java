package com.gk.study.queuedeque.exercises;

/**
 * H02 [Hard] Merge k sorted (ascending) singly linked lists into one sorted list.
 * Input:  lists = [1-&gt;4-&gt;5, 1-&gt;3-&gt;4, 2-&gt;6]  -&gt; Output: 1-&gt;1-&gt;2-&gt;3-&gt;4-&gt;4-&gt;5-&gt;6
 * Constraint: O(n log k) time where n is the total number of nodes across all lists and k is the
 * number of lists; O(k) extra space for the heap.
 * Pattern: k-way merge with a heap
 */
public class MergeKSortedLists {

    /** Minimal singly linked node used by this exercise. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    /**
     * @param lists array of heads of k already-sorted lists (any entry may be null; the array
     *              itself may be empty)
     * @return head of one fully merged, sorted list built by splicing existing nodes
     */
    public static Node mergeKLists(Node[] lists) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
