package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CaseInsensitiveStringTest {

    @Test
    void differentCaseIsEqual() {
        CaseInsensitiveString a = new CaseInsensitiveString("Apple");
        CaseInsensitiveString b = new CaseInsensitiveString("APPLE");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void deduplicatesInHashSet() {
        Set<CaseInsensitiveString> set = new HashSet<>();
        set.add(new CaseInsensitiveString("apple"));
        set.add(new CaseInsensitiveString("Apple"));
        set.add(new CaseInsensitiveString("APPLE"));
        set.add(new CaseInsensitiveString("banana"));
        assertThat(set).hasSize(2);
    }

    @Test
    void differentWordsAreNotEqual() {
        assertThat(new CaseInsensitiveString("apple")).isNotEqualTo(new CaseInsensitiveString("banana"));
    }
}
