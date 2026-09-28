package com.gk.study.foundations.solutions;

import java.math.BigDecimal;
import java.util.Objects;

/** Reference solution for {@code exercises.Money}. */
public final class MoneySolution {
    private final BigDecimal amount;
    private final String currency;

    public MoneySolution(BigDecimal amount, String currency) {
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof MoneySolution other)) {
            return false;
        }
        return amount.compareTo(other.amount) == 0 && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
