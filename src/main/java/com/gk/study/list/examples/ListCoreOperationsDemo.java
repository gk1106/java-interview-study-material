package com.gk.study.list.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates core {@link List} operations: positional add/set/get, indexOf,
 * and the fact that {@link List#subList(int, int)} returns a live view, not a copy.
 *
 * See notes/03-list/01-list-interface.md for the accompanying explanation.
 */
public final class ListCoreOperationsDemo {

    private ListCoreOperationsDemo() {
    }

    public static void main(String[] args) {
        List<String> accounts = new ArrayList<>(List.of("A100", "A200", "A300"));

        accounts.add(1, "A150");               // [A100, A150, A200, A300]
        accounts.set(0, "A101");               // [A101, A150, A200, A300]

        System.out.println(accounts.indexOf("A200")); // 2

        List<String> view = accounts.subList(1, 3);   // live view: [A150, A200]
        view.clear();                                   // mutates the parent too

        System.out.println(accounts); // [A101, A300]
    }
}
