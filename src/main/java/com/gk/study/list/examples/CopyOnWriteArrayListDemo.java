package com.gk.study.list.examples;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demonstrates {@link CopyOnWriteArrayList}'s snapshot iterator: a mutation made after an
 * iterator is obtained is invisible to that already-in-flight iterator, and the iterator never
 * throws {@code ConcurrentModificationException}.
 *
 * See notes/03-list/06-copyonwritearraylist.md for the internals explanation.
 */
public final class CopyOnWriteArrayListDemo {

    private CopyOnWriteArrayListDemo() {
    }

    public static void main(String[] args) {
        List<String> list = new CopyOnWriteArrayList<>(List.of("A", "B", "C"));

        Iterator<String> it = list.iterator(); // snapshot taken here
        list.add("D");                          // mutation after the snapshot was taken

        while (it.hasNext()) {
            System.out.println(it.next()); // still only prints A, B, C
        }

        System.out.println(list); // [A, B, C, D] — the underlying list did change
    }
}
