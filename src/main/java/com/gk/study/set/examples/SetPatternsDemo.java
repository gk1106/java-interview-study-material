package com.gk.study.set.examples;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates the core DSA patterns that use a {@link Set} to turn an O(n^2) brute-force
 * comparison into O(n): duplicate detection via {@code add()}'s boolean return value, set algebra
 * (union/intersection/difference), and the O(n) "start of run" trick for longest consecutive
 * sequence.
 *
 * See notes/05-set/03-dsa-patterns-sets.md for the internals explanation.
 */
public final class SetPatternsDemo {

    private SetPatternsDemo() {
    }

    public static void main(String[] args) {
        duplicateDetectionDemo();
        System.out.println();
        setAlgebraDemo();
        System.out.println();
        longestConsecutiveSequenceDemo();
    }

    private static void duplicateDetectionDemo() {
        System.out.println("--- Duplicate detection via Set.add() ---");
        int[] nums = {4, 3, 2, 7, 8, 2, 3, 1};
        Set<Integer> seen = new HashSet<>();
        Set<Integer> duplicates = new HashSet<>();
        for (int n : nums) {
            if (!seen.add(n)) { // add() returns false -> already present -> duplicate
                duplicates.add(n);
            }
        }
        System.out.println("input      = " + Arrays.toString(nums));
        System.out.println("duplicates = " + duplicates); // {2, 3}
    }

    private static void setAlgebraDemo() {
        System.out.println("--- Set algebra: union / intersection / difference ---");
        Set<Integer> a = new HashSet<>(List.of(1, 2, 3, 4));
        Set<Integer> b = new HashSet<>(List.of(3, 4, 5, 6));

        Set<Integer> union = new HashSet<>(a);
        union.addAll(b);

        Set<Integer> intersection = new HashSet<>(a);
        intersection.retainAll(b);

        Set<Integer> difference = new HashSet<>(a);
        difference.removeAll(b);

        System.out.println("a            = " + a);
        System.out.println("b            = " + b);
        System.out.println("union        = " + union);        // {1,2,3,4,5,6}
        System.out.println("intersection = " + intersection);  // {3,4}
        System.out.println("difference   = " + difference);    // {1,2}
    }

    private static void longestConsecutiveSequenceDemo() {
        System.out.println("--- Longest consecutive sequence: O(n) HashSet 'start of run' trick ---");
        int[] nums = {100, 4, 200, 1, 3, 2};
        Set<Integer> set = new HashSet<>();
        for (int n : nums) {
            set.add(n);
        }

        int longest = 0;
        for (int n : set) {
            if (!set.contains(n - 1)) { // only expand from a true run-start -> keeps this O(n) total
                int length = 1;
                while (set.contains(n + length)) {
                    length++;
                }
                if (length > longest) {
                    System.out.println("new best run starting at " + n + ", length=" + length);
                }
                longest = Math.max(longest, length);
            }
        }
        System.out.println("input   = " + Arrays.toString(nums));
        System.out.println("longest = " + longest); // 4 (the run {1,2,3,4})
    }
}
