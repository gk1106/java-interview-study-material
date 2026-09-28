package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VectorValueTest {

    @Test
    void differentArrayInstancesSameContentAreEqual() {
        VectorValue a = new VectorValue(new int[] {1, 2, 3});
        VectorValue b = new VectorValue(new int[] {1, 2, 3});
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentContentIsNotEqual() {
        VectorValue a = new VectorValue(new int[] {1, 2, 3});
        VectorValue b = new VectorValue(new int[] {1, 2, 4});
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void emptyArraysAreEqual() {
        assertThat(new VectorValue(new int[] {})).isEqualTo(new VectorValue(new int[] {}));
    }
}
