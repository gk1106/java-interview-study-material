package com.gk.study.foundations.exercises;

import java.util.Arrays;

/**
 * E04 [Hard] Implement a contract-correct equals()/hashCode() for a value class wrapping an int[]
 * array field - the classic pitfall where == or the array's default hashCode() compares identity,
 * not content.
 * Input:  new VectorValue(new int[]{1,2,3}).equals(new VectorValue(new int[]{1,2,3}))
 * Output: true (different array instances, same content), even though the two int[] arrays are
 * NOT == to each other and are NOT .equals() to each other via Object.equals.
 * Constraint: must use java.util.Arrays.equals(int[], int[]) and java.util.Arrays.hashCode(int[]) -
 * not the array's own equals()/hashCode() (which are identity-based) and not manual element loops.
 * Pattern: Arrays.equals / Arrays.hashCode for array-valued fields
 */
public final class VectorValue {
    private final int[] components;

    public VectorValue(int[] components) {
        this.components = components.clone(); // defensive copy
    }

    public int[] getComponents() {
        return components.clone(); // defensive copy on the way out too
    }

    @Override
    public boolean equals(Object o) {
        // TODO: use Arrays.equals(this.components, other.components)
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int hashCode() {
        // TODO: use Arrays.hashCode(components)
        throw new UnsupportedOperationException("TODO");
    }
}
