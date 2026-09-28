package com.gk.study.foundations.exercises;

import java.util.Objects;

/**
 * E02 [Easy] Implement a contract-correct equals()/hashCode() for an immutable PhoneNumber value object.
 * Input:  new PhoneNumber("91", "9876543210").equals(new PhoneNumber("91", "9876543210"))
 * Output: true; equal PhoneNumber objects must also return the same hashCode().
 * Constraint: both fields significant; must be null-safe (use Objects.equals/Objects.hash),
 * reflexive, symmetric, transitive, consistent, and equals(null) must return false.
 * Pattern: equals/hashCode contract on an immutable value class
 */
public final class PhoneNumber {
    private final String countryCode;
    private final String number;

    public PhoneNumber(String countryCode, String number) {
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
        // TODO: compare countryCode and number using Objects.equals (null-safe); type-check with instanceof
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int hashCode() {
        // TODO: Objects.hash(countryCode, number)
        throw new UnsupportedOperationException("TODO");
    }
}
