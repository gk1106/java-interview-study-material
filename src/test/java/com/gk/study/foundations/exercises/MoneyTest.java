package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void equalAmountsWithDifferentScaleAreEqual() {
        Money a = new Money(new BigDecimal("10.00"), "USD");
        Money b = new Money(new BigDecimal("10.0"), "USD");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentCurrencyIsNotEqual() {
        Money usd = new Money(new BigDecimal("10.00"), "USD");
        Money eur = new Money(new BigDecimal("10.00"), "EUR");
        assertThat(usd).isNotEqualTo(eur);
    }

    @Test
    void notEqualToNullOrOtherType() {
        Money a = new Money(new BigDecimal("1.00"), "USD");
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("1.00 USD");
    }
}
