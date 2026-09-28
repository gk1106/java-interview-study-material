package com.gk.study.foundations.exercises;

import java.util.List;

/**
 * E03 [Medium] Copy every element from src into dest (appending), following PECS: src is a
 * producer (read-only, "extends"), dest is a consumer (write-only, "super").
 * Input:  src = List.of(1, 2, 3), dest = new ArrayList<Number>() (initially empty)
 * Output: dest becomes [1, 2, 3] (as Numbers)
 * Constraint: O(n) time, O(1) extra space beyond dest's own growth; must compile with
 * src typed List<? extends T> and dest typed List<? super T> (not exact List<T> for either).
 * Pattern: PECS (Producer Extends, Consumer Super)
 */
public class PecsCopy {

    public static <T> void copy(List<? extends T> src, List<? super T> dest) {
        // TODO: append every element of src onto dest, in order
        throw new UnsupportedOperationException("TODO");
    }
}
