package com.gk.study.foundations.examples;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Demonstrates fail-fast (ArrayList) vs fail-safe (CopyOnWriteArrayList) iterator behavior. */
public class IteratorDemo {

    public static void main(String[] args) {
        // --- Fail-fast: structural modification during iteration throws ---
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));
        try {
            for (Integer value : list) {
                if (value == 2) {
                    list.remove(value); // BUG on purpose: structural modification mid-iteration
                }
            }
            System.out.println("Fail-fast demo: no exception (unexpected)");
        } catch (ConcurrentModificationException e) {
            System.out.println("Fail-fast demo: caught " + e.getClass().getName() + " as expected");
        }

        // --- Correct removal using Iterator.remove() ---
        List<Integer> list2 = new ArrayList<>(List.of(1, 2, 3));
        Iterator<Integer> it = list2.iterator();
        while (it.hasNext()) {
            if (it.next() == 2) {
                it.remove();
            }
        }
        System.out.println("Correct removal via Iterator.remove(): " + list2);

        // --- Fail-safe: CopyOnWriteArrayList iterator sees a snapshot ---
        CopyOnWriteArrayList<Integer> cow = new CopyOnWriteArrayList<>(List.of(1, 2, 3));
        Iterator<Integer> cowIt = cow.iterator();
        cow.add(4); // does NOT throw, and is invisible to cowIt
        StringBuilder seen = new StringBuilder();
        while (cowIt.hasNext()) {
            if (seen.length() > 0) seen.append(", ");
            seen.append(cowIt.next());
        }
        System.out.println("Fail-safe demo (CopyOnWriteArrayList): iteration saw [" + seen
                + "] while a concurrent add(4) happened; list is now " + cow);
    }
}
