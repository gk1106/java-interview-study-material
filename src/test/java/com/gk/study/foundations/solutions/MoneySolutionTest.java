package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MoneySolutionTest {

    @Test
    void equalAmountsWithDifferentScaleAreEqual() {
        MoneySolution a = new MoneySolution(new BigDecimal("10.00"), "USD");
        MoneySolution b = new MoneySolution(new BigDecimal("10.0"), "USD");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void differentCurrencyIsNotEqual() {
        MoneySolution usd = new MoneySolution(new BigDecimal("10.00"), "USD");
        MoneySolution eur = new MoneySolution(new BigDecimal("10.00"), "EUR");
        assertThat(usd).isNotEqualTo(eur);
    }

    @Test
    void reflexiveSymmetricAndNullSafe() {
        MoneySolution a = new MoneySolution(new BigDecimal("1.00"), "USD");
        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("1.00 USD");
    }

    @Test
    void worksAsHashSetKey() {
        Set<MoneySolution> set = new HashSet<>();
        set.add(new MoneySolution(new BigDecimal("5.00"), "USD"));
        set.add(new MoneySolution(new BigDecimal("5.0"), "USD"));
        assertThat(set).hasSize(1);
    }
}
