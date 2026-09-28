package com.gk.study.collectionsoverview.examples;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Demonstrates the Java Collections Framework interface hierarchy:
 * Iterable -&gt; Collection -&gt; {List, Set, Queue}, with Deque extending
 * Queue, and Map living outside the Collection hierarchy entirely.
 *
 * Run with: java -cp target/classes com.gk.study.collectionsoverview.examples.HierarchyDemo
 */
public final class HierarchyDemo {

    private HierarchyDemo() {
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(3, 1, 2));
        Set<Integer> set = new TreeSet<>(list);
        Deque<Integer> deque = new ArrayDeque<>(list);
        Queue<Integer> priorityQueue = new PriorityQueue<>(list);
        Map<String, Integer> map = new HashMap<>();
        map.put("checking", 100);
        map.put("savings", 500);

        System.out.println("list instanceof Collection          : " + (list instanceof Collection));
        System.out.println("set instanceof Collection            : " + (set instanceof Collection));
        System.out.println("deque instanceof Collection           : " + (deque instanceof Collection));
        System.out.println("map  instanceof Collection            : " + (map instanceof Collection));
        System.out.println("deque instanceof Queue                : " + (deque instanceof Queue));
        System.out.println("priorityQueue instanceof Deque        : " + (priorityQueue instanceof Deque));
        System.out.println("map.entrySet() instanceof Collection  : " + (map.entrySet() instanceof Collection));
        System.out.println("map.keySet() instanceof Set           : " + (map.keySet() instanceof Set));

        System.out.println();
        System.out.println("-- classify() demo --");
        System.out.println("list          -> " + classify(list));
        System.out.println("set           -> " + classify(set));
        System.out.println("deque         -> " + classify(deque));
        System.out.println("priorityQueue -> " + classify(priorityQueue));
        System.out.println("map           -> " + classify(map));
        System.out.println("plain String  -> " + classify("hello"));

        System.out.println();
        System.out.println("-- PriorityQueue iterator order vs poll() order --");
        Queue<Integer> pq = new PriorityQueue<>(List.of(5, 1, 3, 4, 2));
        StringBuilder iterationOrder = new StringBuilder();
        for (int x : pq) {
            iterationOrder.append(x).append(' ');
        }
        System.out.println("iterator order (heap-array order, NOT sorted): " + iterationOrder.toString().trim());
        StringBuilder pollOrder = new StringBuilder();
        while (!pq.isEmpty()) {
            pollOrder.append(pq.poll()).append(' ');
        }
        System.out.println("poll() order (guaranteed sorted)             : " + pollOrder.toString().trim());
    }

    /**
     * Classifies an arbitrary object into the most specific Collections
     * Framework interface it implements. Mirrors exercise E01.
     */
    static String classify(Object o) {
        if (o instanceof Deque) {
            return "Deque";
        }
        if (o instanceof Queue) {
            return "Queue";
        }
        if (o instanceof List) {
            return "List";
        }
        if (o instanceof Set) {
            return "Set";
        }
        if (o instanceof Map) {
            return "Map";
        }
        if (o instanceof Collection) {
            return "Collection (unspecialized)";
        }
        return "not a collection type";
    }
}
