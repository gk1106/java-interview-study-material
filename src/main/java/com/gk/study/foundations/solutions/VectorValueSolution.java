package com.gk.study.foundations.solutions;

import java.util.Arrays;

/** Reference solution for {@code exercises.VectorValue}. */
public final class VectorValueSolution {
    private final int[] components;

    public VectorValueSolution(int[] components) {
        this.components = components.clone();
    }

    public int[] getComponents() {
        return components.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VectorValueSolution other)) {
            return false;
        }
        return Arrays.equals(components, other.components);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(components);
    }
}
