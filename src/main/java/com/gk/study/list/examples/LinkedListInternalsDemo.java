package com.gk.study.list.examples;

import java.util.LinkedList;

/**
 * Demonstrates {@link LinkedList} used both as a positional {@code List} and as a {@code Deque}
 * (addFirst/addLast/peekFirst/pollFirst), all O(1) at the ends because LinkedList keeps direct
 * first/last node references.
 *
 * See notes/03-list/03-linkedlist-internals.md for the internals explanation.
 */
public final class LinkedListInternalsDemo {

    private LinkedListInternalsDemo() {
    }

    public static void main(String[] args) {
        LinkedList<String> queue = new LinkedList<>();
        queue.addLast("txn-1");
        queue.addLast("txn-2");
        queue.addFirst("txn-0");

        System.out.println(queue);              // [txn-0, txn-1, txn-2]
        System.out.println(queue.peekFirst());   // txn-0 (no removal)
        System.out.println(queue.pollFirst());   // txn-0 (removes and returns)
        System.out.println(queue);               // [txn-1, txn-2]
    }
}
