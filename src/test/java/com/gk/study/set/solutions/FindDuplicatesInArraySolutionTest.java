package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FindDuplicatesInArraySolutionTest {

    @Test
    void typicalInputReturnsDuplicates() {
        int[] nums = {4, 3, 2, 7, 8, 2, 3, 1};
        assertThat(FindDuplicatesInArraySolution.solve(nums)).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void noDuplicatesReturnsEmptySet() {
        int[] nums = {1, 2, 3};
        assertThat(FindDuplicatesInArraySolution.solve(nums)).isEmpty();
    }

    @Test
    void emptyArrayReturnsEmptySet() {
        assertThat(FindDuplicatesInArraySolution.solve(new int[0])).isEmpty();
    }

    @Test
    void allElementsIdenticalReturnsSingleDuplicate() {
        int[] nums = {5, 5, 5, 5};
        assertThat(FindDuplicatesInArraySolution.solve(nums)).containsExactly(5);
    }

    @Test
    void singleElementReturnsEmptySet() {
        assertThat(FindDuplicatesInArraySolution.solve(new int[] {9})).isEmpty();
    }
}
