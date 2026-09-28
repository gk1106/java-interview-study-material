package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionComparatorSolutionTest {

    private final VersionComparatorSolution comparator = new VersionComparatorSolution();

    @Test
    void numericComparisonBeatsLexicographic() {
        assertThat(comparator.compare("1.2.10", "1.2.9")).isPositive();
    }

    @Test
    void missingTrailingSegmentTreatedAsZero() {
        assertThat(comparator.compare("1.2", "1.2.0")).isEqualTo(0);
        assertThat(comparator.compare("1.2.0", "1.2")).isEqualTo(0);
    }

    @Test
    void equalVersionsCompareToZero() {
        assertThat(comparator.compare("2.4.6", "2.4.6")).isEqualTo(0);
    }

    @Test
    void differentMajorVersion() {
        assertThat(comparator.compare("2.0.0", "1.9.9")).isPositive();
        assertThat(comparator.compare("1.9.9", "2.0.0")).isNegative();
    }

    @Test
    void sortsListOfVersionsNumerically() {
        java.util.List<String> versions = new java.util.ArrayList<>(
                java.util.List.of("1.2.9", "1.10.0", "1.2.10", "1.2.2"));
        versions.sort(comparator);
        assertThat(versions).containsExactly("1.2.2", "1.2.9", "1.2.10", "1.10.0");
    }
}
