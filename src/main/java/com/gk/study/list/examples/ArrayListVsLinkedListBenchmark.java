package com.gk.study.list.examples;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Measures ArrayList vs LinkedList on: add-at-end, add-at-front, indexed get, and full
 * iteration, with a JIT warm-up pass before the timed pass. Run this class directly to see
 * numbers on your own machine — exact milliseconds vary, but the relative shape (which
 * operations become quadratic on which structure) should not.
 *
 * See notes/03-list/04-arraylist-vs-linkedlist-benchmark.md for discussion of the results.
 */
public final class ArrayListVsLinkedListBenchmark {

    private ArrayListVsLinkedListBenchmark() {
    }

    public static void main(String[] args) {
        int n = 20_000; // kept modest so add-at-front on ArrayList / get on LinkedList finish quickly

        warmUp();

        System.out.println("n = " + n);
        benchAddAtEnd(n);
        benchAddAtFront(n);
        benchGetByIndex(n);
        benchIterate(n);
    }

    private static void warmUp() {
        for (int round = 0; round < 3; round++) {
            List<Integer> l = new ArrayList<>();
            for (int i = 0; i < 5_000; i++) l.add(i);
            for (int i = 0; i < l.size(); i++) l.get(i);
        }
    }

    private static void benchAddAtEnd(int n) {
        long t0 = System.nanoTime();
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < n; i++) arrayList.add(i);
        long arrayListMs = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) linkedList.add(i);
        long linkedListMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("add at end      -> ArrayList: " + arrayListMs + " ms, LinkedList: " + linkedListMs + " ms");
    }

    private static void benchAddAtFront(int n) {
        long t0 = System.nanoTime();
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < n; i++) arrayList.add(0, i);
        long arrayListMs = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        LinkedList<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) linkedList.addFirst(i);
        long linkedListMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("add at front    -> ArrayList: " + arrayListMs + " ms, LinkedList: " + linkedListMs + " ms");
    }

    private static void benchGetByIndex(int n) {
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        long t0 = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < arrayList.size(); i++) sum += arrayList.get(i);
        long arrayListMs = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        for (int i = 0; i < linkedList.size(); i++) sum += linkedList.get(i);
        long linkedListMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("get by index    -> ArrayList: " + arrayListMs + " ms, LinkedList: " + linkedListMs
                + " ms (checksum=" + sum + ")");
    }

    private static void benchIterate(int n) {
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        long t0 = System.nanoTime();
        long sum = 0;
        for (int x : arrayList) sum += x;
        long arrayListMs = (System.nanoTime() - t0) / 1_000_000;

        t0 = System.nanoTime();
        for (int x : linkedList) sum += x;
        long linkedListMs = (System.nanoTime() - t0) / 1_000_000;

        System.out.println("iterate         -> ArrayList: " + arrayListMs + " ms, LinkedList: " + linkedListMs
                + " ms (checksum=" + sum + ")");
    }
}
