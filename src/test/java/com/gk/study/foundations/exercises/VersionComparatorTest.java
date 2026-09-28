package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionComparatorTest {

    private final VersionComparator comparator = new VersionComparator();

    @Test
    void numericComparisonBeatsLexicographic() {
        assertThat(comparator.compare("1.2.10", "1.2.9")).isPositive();
    }

    @Test
    void missingTrailingSegmentTreatedAsZero() {
        assertThat(comparator.compare("1.2", "1.2.0")).isEqualTo(0);
    }

    @Test
    void differentMajorVersion() {
        assertThat(comparator.compare("2.0.0", "1.9.9")).isPositive();
        assertThat(comparator.compare("1.9.9", "2.0.0")).isNegative();
    }
}
