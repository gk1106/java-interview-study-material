package com.gk.study.map.examples;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates HashMap's resize behaviour without reflection: the JDK does not expose current
 * capacity publicly, so this demo infers resize boundaries from the documented default capacity
 * (16) and load factor (0.75), printing size/theoretical-capacity/threshold at each observed
 * resize boundary as elements are inserted.
 *
 * See notes/06-map/01-hashmap-internals.md for the internals explanation (hash spreading, index
 * computation, low/high-list split resize, treeification).
 */
public final class HashMapInternalsDemo {

    private HashMapInternalsDemo() {
    }

    public static void main(String[] args) {
        Map<Integer, String> map = new HashMap<>();

        int capacity = 16;            // default capacity, allocated lazily on first put
        int threshold = (int) (capacity * 0.75f);
        boolean allocated = false;

        System.out.println("--- resize behaviour (default capacity 16, load factor 0.75) ---");
        for (int i = 0; i < 40; i++) {
            map.put(i, "v" + i);

            if (!allocated) {
                allocated = true;
                System.out.println("size=" + map.size() + " -> table allocated, capacity=" + capacity
                        + ", threshold=" + threshold);
            } else if (map.size() > threshold) {
                int oldCapacity = capacity;
                capacity = oldCapacity * 2;          // HashMap always DOUBLES, unlike ArrayList's 1.5x
                threshold = (int) (capacity * 0.75f);
                System.out.println("size=" + map.size() + " -> resized: " + oldCapacity + " -> " + capacity
                        + ", new threshold=" + threshold);
            }
        }
        System.out.println("final size=" + map.size() + ", theoretical capacity=" + capacity);

        System.out.println();
        System.out.println("--- hash spreading demo: h ^ (h >>> 16) ---");
        demoSpread("A");
        demoSpread("AaAa"); // classic example: same hashCode as "BBBB" but different key
        demoSpread("BBBB");

        System.out.println();
        System.out.println("--- iteration order is not insertion order (do not depend on it) ---");
        Map<Integer, String> unordered = new HashMap<>();
        unordered.put(100, "a");
        unordered.put(1, "b");
        unordered.put(50, "c");
        System.out.println("inserted order: 100, 1, 50 -> iteration order: " + unordered.keySet());

        System.out.println();
        System.out.println("--- treeification threshold constants (documented, not reflected) ---");
        System.out.println("TREEIFY_THRESHOLD=8, UNTREEIFY_THRESHOLD=6, MIN_TREEIFY_CAPACITY=64");
        System.out.println("A bucket only treeifies once its chain reaches 8 AND capacity >= 64;");
        System.out.println("below capacity 64, HashMap resizes instead of treeifying that bucket.");
    }

    private static void demoSpread(String key) {
        int h = key.hashCode();
        int spread = h ^ (h >>> 16);
        System.out.printf("key=%-6s hashCode=%12d spread=%12d (index for capacity 16 = %d)%n",
                key, h, spread, spread & 15);
    }
}
