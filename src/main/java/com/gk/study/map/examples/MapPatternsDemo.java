package com.gk.study.map.examples;

import com.gk.study.map.solutions.GroupAnagramsSolution;
import com.gk.study.map.solutions.SubarraySumEqualsKSolution;
import com.gk.study.map.solutions.TopKFrequentElementsSolution;
import com.gk.study.map.solutions.TwoSumHashMapSolution;
import java.util.Arrays;
import java.util.List;

/**
 * Runs the core Map-based DSA patterns (two-sum, subarray-sum-K, group anagrams, top-K frequent)
 * against small hard-coded inputs and prints the results.
 *
 * See notes/06-map/06-dsa-patterns-map.md for the pattern explanations.
 */
public final class MapPatternsDemo {

    private MapPatternsDemo() {
    }

    public static void main(String[] args) {
        System.out.println("--- two-sum ---");
        int[] twoSum = TwoSumHashMapSolution.solve(new int[] {2, 7, 11, 15}, 9);
        System.out.println("nums=[2,7,11,15], target=9 -> " + Arrays.toString(twoSum));

        System.out.println();
        System.out.println("--- subarray sum equals K (prefix-sum + HashMap) ---");
        int count = SubarraySumEqualsKSolution.solve(new int[] {1, 2, 3}, 3);
        System.out.println("nums=[1,2,3], k=3 -> count = " + count);

        System.out.println();
        System.out.println("--- group anagrams ---");
        List<List<String>> groups =
                GroupAnagramsSolution.solve(List.of("eat", "tea", "tan", "ate", "nat", "bat"));
        System.out.println("input=[eat,tea,tan,ate,nat,bat] -> groups = " + groups);

        System.out.println();
        System.out.println("--- top-K frequent elements ---");
        int[] topK = TopKFrequentElementsSolution.solve(new int[] {1, 1, 1, 2, 2, 3}, 2);
        System.out.println("nums=[1,1,1,2,2,3], k=2 -> " + Arrays.toString(topK));
    }
}
