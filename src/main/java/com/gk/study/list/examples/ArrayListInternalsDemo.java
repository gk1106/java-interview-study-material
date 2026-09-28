package com.gk.study.list.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates ArrayList's growth behaviour without reflection: the JDK does not expose
 * current capacity publicly, so this demo infers growth boundaries by knowing the documented
 * default capacity (10) and growth factor (1.5x, via {@code oldCapacity + (oldCapacity >> 1)})
 * and printing the theoretical capacity milestones alongside the actual size as elements are
 * added.
 *
 * See notes/03-list/02-arraylist-internals.md for the internals explanation.
 */
public final class ArrayListInternalsDemo {

    private ArrayListInternalsDemo() {
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();

        int capacity = 0; // 0 until first add (lazy allocation of the empty-array sentinel)
        int nextMilestone = -1;

        for (int i = 0; i < 40; i++) {
            list.add(i);

            if (capacity == 0) {
                capacity = 10; // default capacity allocated on first add
                System.out.println("size=" + list.size() + " -> backing array allocated, capacity=" + capacity);
            } else if (list.size() > capacity) {
                int oldCapacity = capacity;
                capacity = oldCapacity + (oldCapacity >> 1); // 1.5x growth, matching the JDK formula
                if (capacity < list.size()) {
                    capacity = list.size();
                }
                System.out.println("size=" + list.size() + " -> grew: " + oldCapacity + " -> " + capacity);
            }
        }

        System.out.println("final size=" + list.size() + ", theoretical capacity=" + capacity);
    }
}
