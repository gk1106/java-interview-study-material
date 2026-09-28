package com.gk.study.foundations.exercises;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * E01 [Easy] Implement a contract-correct equals()/hashCode() for an immutable Money value object.
 * Input:  new Money(new BigDecimal("10.00"), "USD").equals(new Money(new BigDecimal("10.0"), "USD"))
 * Output: true (amounts are numerically equal even though their BigDecimal scale differs);
 *         equal Money objects must also return the same hashCode().
 * Constraint: compare amount using BigDecimal.compareTo (NOT BigDecimal.equals, which also
 * compares scale); compare currency case-sensitively; must be reflexive, symmetric, transitive,
 * consistent and null-safe (equals(null) must return false, never throw).
 * Pattern: equals/hashCode contract on an immutable value class
 */
public final class Money {
    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = Objects.requireNonNull(amount, "amount");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        // TODO: use BigDecimal.compareTo(...) == 0 for amount (not .equals()), and
        // currency.equals(...) for currency; must be null-safe and type-safe (instanceof check).
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int hashCode() {
        // TODO: must be consistent with equals() above - use a canonical form of amount
        // (e.g. amount.stripTrailingZeros()) so 10.0 and 10.00 hash the same way.
        throw new UnsupportedOperationException("TODO");
    }
}
