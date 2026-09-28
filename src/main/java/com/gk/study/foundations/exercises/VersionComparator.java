package com.gk.study.foundations.exercises;

import java.util.Comparator;

/**
 * E04 [Hard] Compare dotted version strings NUMERICALLY, not lexicographically:
 * "1.2.10" must compare greater than "1.2.9" even though "10" < "9" as strings.
 * Input:  compare("1.2.10", "1.2.9")
 * Output: a positive int (v1 > v2)
 * Input:  compare("1.2", "1.2.0")
 * Output: 0 (equal - a missing trailing segment is treated as 0)
 * Constraint: O(k) time where k = max number of dotted segments; segments are non-negative
 * integers; versions may have differing numbers of segments.
 * Pattern: custom Comparator with tokenizing/normalization
 */
public class VersionComparator implements Comparator<String> {

    @Override
    public int compare(String v1, String v2) {
        // TODO: split both on "\\.", compare segment by segment as parsed integers; treat a
        // missing segment past the shorter version's length as 0. Return on the first
        // non-zero segment comparison, else 0.
        throw new UnsupportedOperationException("TODO");
    }
}
