package com.gk.study.list.examples;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * Demonstrates the classic "remove while iterating" bug (ConcurrentModificationException), then
 * the two correct fixes: {@link Iterator#remove()} and {@link List#removeIf}.
 *
 * See notes/03-list/07-listiterator-sublist-gotchas.md for the fail-fast mechanism explanation.
 */
public final class ListIteratorGotchasDemo {

    private ListIteratorGotchasDemo() {
    }

    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

        // WRONG: mutating the list directly during a for-each loop.
        try {
            for (Integer n : nums) {
                if (n % 2 == 0) {
                    nums.remove(n);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("CME as expected: " + e.getClass().getSimpleName());
        }

        // RIGHT #1: Iterator.remove()
        List<Integer> viaIterator = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = viaIterator.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) {
                it.remove();
            }
        }
        System.out.println(viaIterator); // [1, 3, 5]

        // RIGHT #2: removeIf
        List<Integer> viaRemoveIf = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        viaRemoveIf.removeIf(n -> n % 2 == 0);
        System.out.println(viaRemoveIf); // [1, 3, 5]
    }
}
