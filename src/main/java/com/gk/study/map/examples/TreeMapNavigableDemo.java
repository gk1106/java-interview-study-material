package com.gk.study.map.examples;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Demonstrates TreeMap's sorted iteration and the NavigableMap range/neighbor query methods.
 *
 * See notes/06-map/03-treemap-navigablemap.md for the red-black tree internals explanation.
 */
public final class TreeMapNavigableDemo {

    private TreeMapNavigableDemo() {
    }

    public static void main(String[] args) {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(20, "twenty");
        map.put(10, "ten");
        map.put(35, "thirty-five");
        map.put(15, "fifteen");

        System.out.println("keys in sorted order: " + map.keySet());
        System.out.println("firstKey()  = " + map.firstKey());
        System.out.println("lastKey()   = " + map.lastKey());
        System.out.println("floorKey(23)   = " + map.floorKey(23));   // largest key <= 23
        System.out.println("ceilingKey(23) = " + map.ceilingKey(23)); // smallest key >= 23
        System.out.println("lowerKey(20)   = " + map.lowerKey(20));   // strictly < 20
        System.out.println("higherKey(20)  = " + map.higherKey(20));  // strictly > 20

        SortedMap<Integer, String> range = map.subMap(10, true, 20, true);
        System.out.println("subMap(10,true,20,true).keySet() = " + range.keySet());

        Map.Entry<Integer, String> polled = map.pollFirstEntry();
        System.out.println("pollFirstEntry() removed = " + polled + ", remaining keys = " + map.keySet());

        System.out.println("descendingMap keys = " + map.descendingMap().keySet());
    }
}
