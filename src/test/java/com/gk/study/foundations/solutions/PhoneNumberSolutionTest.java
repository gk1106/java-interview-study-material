package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberSolutionTest {

    @Test
    void sameFieldsAreEqual() {
        PhoneNumberSolution a = new PhoneNumberSolution("91", "9876543210");
        PhoneNumberSolution b = new PhoneNumberSolution("91", "9876543210");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentCountryCodeIsNotEqual() {
        PhoneNumberSolution a = new PhoneNumberSolution("91", "9876543210");
        PhoneNumberSolution b = new PhoneNumberSolution("1", "9876543210");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void notEqualToNullOrOtherType() {
        PhoneNumberSolution a = new PhoneNumberSolution("91", "9876543210");
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("919876543210");
    }
}
