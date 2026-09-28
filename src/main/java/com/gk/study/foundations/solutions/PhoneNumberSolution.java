package com.gk.study.foundations.solutions;

import java.util.Objects;

/** Reference solution for {@code exercises.PhoneNumber}. */
public final class PhoneNumberSolution {
    private final String countryCode;
    private final String number;

    public PhoneNumberSolution(String countryCode, String number) {
        this.countryCode = countryCode;
        this.number = number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhoneNumberSolution other)) {
            return false;
        }
        return Objects.equals(countryCode, other.countryCode) && Objects.equals(number, other.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, number);
    }
}
