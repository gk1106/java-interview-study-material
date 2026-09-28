package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberTest {

    @Test
    void sameFieldsAreEqual() {
        PhoneNumber a = new PhoneNumber("91", "9876543210");
        PhoneNumber b = new PhoneNumber("91", "9876543210");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentNumberIsNotEqual() {
        PhoneNumber a = new PhoneNumber("91", "9876543210");
        PhoneNumber b = new PhoneNumber("91", "9999999999");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void notEqualToNull() {
        PhoneNumber a = new PhoneNumber("91", "9876543210");
        assertThat(a).isNotEqualTo(null);
    }
}
