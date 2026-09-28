package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CaseInsensitiveStringSolutionTest {

    @Test
    void differentCaseIsEqual() {
        CaseInsensitiveStringSolution a = new CaseInsensitiveStringSolution("Apple");
        CaseInsensitiveStringSolution b = new CaseInsensitiveStringSolution("APPLE");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void deduplicatesInHashSet() {
        Set<CaseInsensitiveStringSolution> set = new HashSet<>();
        set.add(new CaseInsensitiveStringSolution("apple"));
        set.add(new CaseInsensitiveStringSolution("Apple"));
        set.add(new CaseInsensitiveStringSolution("APPLE"));
        set.add(new CaseInsensitiveStringSolution("banana"));
        assertThat(set).hasSize(2);
    }

    @Test
    void differentWordsAreNotEqual() {
        assertThat(new CaseInsensitiveStringSolution("apple"))
                .isNotEqualTo(new CaseInsensitiveStringSolution("banana"));
    }
}
