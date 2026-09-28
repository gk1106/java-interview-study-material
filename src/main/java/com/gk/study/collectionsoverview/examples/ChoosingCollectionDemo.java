package com.gk.study.collectionsoverview.examples;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Demonstrates picking the right collection for a scenario: dedupe with
 * order preserved (LinkedHashSet), a bounded LRU cache (LinkedHashMap
 * access-order mode), and sorted range queries (TreeSet / NavigableSet).
 *
 * Run with: java -cp target/classes com.gk.study.collectionsoverview.examples.ChoosingCollectionDemo
 */
public final class ChoosingCollectionDemo {

    private ChoosingCollectionDemo() {
    }

    public static void main(String[] args) {
        // Scenario 1: dedupe account IDs, keep first-seen order
        Set<String> seen = new LinkedHashSet<>();
        for (String id : List.of("acc-3", "acc-1", "acc-3", "acc-2", "acc-1")) {
            seen.add(id);
        }
        System.out.println("Deduped, insertion order: " + seen);

        // Scenario 2: bounded LRU cache backed by LinkedHashMap access-order mode
        Map<String, Integer> lru = new LinkedHashMap<>(16, 0.75f, true) {
            private static final int CAPACITY = 3;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > CAPACITY;
            }
        };
        lru.put("a", 1);
        lru.put("b", 2);
        lru.put("c", 3);
        lru.get("a");       // touch "a" -> most recently used
        lru.put("d", 4);    // evicts "b" (least recently used), not "a"
        System.out.println("Bounded LRU cache after inserts+touch (capacity 3): " + lru);

        // Scenario 3: sorted set with range queries via NavigableSet
        NavigableSet<Integer> amounts = new TreeSet<>(List.of(50, 150, 200, 400, 900));
        NavigableSet<Integer> inRange = amounts.subSet(100, true, 400, true);
        System.out.println("Transaction amounts in [100, 400]: " + inRange);
        System.out.println("Smallest amount >= 250 (ceiling): " + amounts.ceiling(250));
        System.out.println("Largest amount <= 175 (floor)   : " + amounts.floor(175));
    }
}
