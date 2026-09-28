package com.gk.study.foundations.solutions;

import java.util.Comparator;

/** Reference solution for {@code exercises.VersionComparator}. */
public class VersionComparatorSolution implements Comparator<String> {

    @Override
    public int compare(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int seg1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int seg2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            int cmp = Integer.compare(seg1, seg2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
