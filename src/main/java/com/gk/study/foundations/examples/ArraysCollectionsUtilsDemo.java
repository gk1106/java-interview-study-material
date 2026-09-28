package com.gk.study.foundations.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Demonstrates common Arrays / Collections utility methods and their sharp edges. */
public class ArraysCollectionsUtilsDemo {

    public static void main(String[] args) {
        int[] arr = {5, 3, 8, 1};
        Arrays.sort(arr);
        System.out.println("Sorted array: " + Arrays.toString(arr));
        System.out.println("binarySearch(8) index = " + Arrays.binarySearch(arr, 8));

        List<Integer> fixed = Arrays.asList(1, 2, 3);
        fixed.set(0, 99);
        System.out.println("Arrays.asList set(0,99): " + fixed);
        try {
            fixed.add(4);
            System.out.println("Arrays.asList add(4) blocked: NO (unexpected)");
        } catch (UnsupportedOperationException e) {
            System.out.println("Arrays.asList add(4) blocked: UnsupportedOperationException");
        }

        List<Integer> readOnly = Collections.unmodifiableList(new ArrayList<>(List.of(1, 2, 3)));
        try {
            readOnly.add(4);
            System.out.println("Collections.unmodifiableList mutation blocked: NO (unexpected)");
        } catch (UnsupportedOperationException e) {
            System.out.println("Collections.unmodifiableList mutation blocked: UnsupportedOperationException");
        }

        List<Integer> nums = List.of(5, 3, 8, 1);
        System.out.println("Collections.max/min of " + nums + ": max=" + Collections.max(nums)
                + ", min=" + Collections.min(nums));

        List<Integer> rotateMe = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Collections.rotate(rotateMe, 2);
        System.out.println("Collections.rotate([1,2,3,4,5], 2): " + rotateMe);
    }
}
