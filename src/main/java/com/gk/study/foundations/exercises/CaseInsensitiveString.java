package com.gk.study.foundations.exercises;

/**
 * E03 [Medium] Implement a case-insensitive String wrapper: two wrappers are equal iff their
 * values are equal ignoring case, and MUST produce the same hashCode() in that case (so it works
 * correctly as a HashSet/HashMap key, deduplicating "Apple" and "apple").
 * Input:  new CaseInsensitiveString("Apple").equals(new CaseInsensitiveString("APPLE"))
 * Output: true; both must return the same hashCode()
 * Constraint: normalize with the SAME method (e.g. toLowerCase(Locale.ROOT)) in both equals() and
 * hashCode() - using different normalization in each is the classic bug this exercise tests for.
 * Pattern: normalized/canonicalized hashing
 */
public final class CaseInsensitiveString {
    private final String value;

    public CaseInsensitiveString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        // TODO: case-insensitive comparison of value (use value.equalsIgnoreCase(...) or
        // normalize both sides with toLowerCase(Locale.ROOT) before comparing)
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int hashCode() {
        // TODO: must use the SAME normalization approach as equals() (e.g. hash
        // value.toLowerCase(Locale.ROOT) rather than value itself)
        throw new UnsupportedOperationException("TODO");
    }
}
