package com.gk.study.list.solutions;

/**
 * Reference solution for {@link com.gk.study.list.exercises.ReverseKGroup}.
 * For each group: first verify k nodes remain (else return the remainder unchanged), reverse
 * exactly k nodes with the standard iterative technique bounded to the group, then recursively
 * process the rest and connect the current group's original head (now its tail) to the head of
 * the next processed group.
 */
public class ReverseKGroupSolution {

    /** Minimal singly linked node used by this solution. */
    public static final class Node {
        public int val;
        public Node next;

        public Node(int val) {
            this.val = val;
        }
    }

    public static Node reverseKGroup(Node head, int k) {
        Node node = head;
        int count = 0;
        while (node != null && count < k) {
            node = node.next;
            count++;
        }
        if (count < k) {
            return head; // fewer than k nodes remain -> leave this final group unreversed
        }

        // Reverse exactly k nodes starting at `head`; `node` is the first node of the NEXT group.
        Node prev = reverseKGroup(node, k); // process the rest first, get its (already reversed) head
        Node curr = head;
        for (int i = 0; i < k; i++) {
            Node next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev; // new head of this group, which is now the overall head returned upward
    }
}
