package com.gk.study.foundations.solutions;

import java.util.Locale;

/** Reference solution for {@code exercises.CaseInsensitiveString}. */
public final class CaseInsensitiveStringSolution {
    private final String value;

    public CaseInsensitiveStringSolution(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CaseInsensitiveStringSolution other)) {
            return false;
        }
        return value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }
}
