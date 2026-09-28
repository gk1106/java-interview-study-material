package com.gk.study.map.examples;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Demonstrates WeakHashMap (weak keys + lazy cleanup), IdentityHashMap (== instead of equals()),
 * and EnumMap (array-backed by ordinal, deterministic declaration-order iteration).
 *
 * See notes/06-map/05-weak-identity-enummap.md for the internals explanation.
 */
public final class WeakIdentityEnumMapDemo {

    private enum TransactionStatus { PENDING, APPROVED, SETTLED, REJECTED }

    private WeakIdentityEnumMapDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- IdentityHashMap: == identity, not equals() ---");
        String a = new String("key"); // deliberately not interned, to prove reference distinctness
        String b = new String("key");
        System.out.println("a.equals(b) = " + a.equals(b) + ", a == b = " + (a == b));

        Map<String, Integer> identityMap = new IdentityHashMap<>();
        identityMap.put(a, 1);
        identityMap.put(b, 2);
        System.out.println("IdentityHashMap size after putting both = " + identityMap.size() + " (treated as distinct keys)");

        Map<String, Integer> normalMap = new HashMap<>();
        normalMap.put(a, 1);
        normalMap.put(b, 2);
        System.out.println("HashMap size after putting both        = " + normalMap.size() + " (collapsed via equals())");

        System.out.println();
        System.out.println("--- EnumMap: array-backed by ordinal, declaration-order iteration ---");
        Map<TransactionStatus, String> handlers = new EnumMap<>(TransactionStatus.class);
        handlers.put(TransactionStatus.SETTLED, "settle-handler");
        handlers.put(TransactionStatus.PENDING, "pending-handler");
        handlers.put(TransactionStatus.APPROVED, "approval-handler");
        System.out.println("inserted SETTLED, PENDING, APPROVED -> iteration order: " + handlers.keySet());
        System.out.println("(matches enum declaration order: PENDING, APPROVED, SETTLED, REJECTED)");

        System.out.println();
        System.out.println("--- WeakHashMap: entries can vanish once the key is unreachable ---");
        WeakHashMap<Object, String> cache = new WeakHashMap<>();
        Object key = new Object();
        cache.put(key, "cached-value");
        System.out.println("size right after put = " + cache.size());

        key = null; // drop the only strong reference to the key
        // Nudge the GC; cleanup is lazy so we also call size() afterward to trigger the expunge pass.
        System.gc();
        Thread.sleep(200);
        System.out.println("size after key becomes unreachable + GC + a map operation = " + cache.size()
                + " (0 expected, though GC timing is not strictly guaranteed by the JVM spec)");
    }
}
