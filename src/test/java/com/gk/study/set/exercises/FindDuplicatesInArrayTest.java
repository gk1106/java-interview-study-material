package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FindDuplicatesInArray} exercise stub. EXPECTED TO FAIL until implemented.
 */
class FindDuplicatesInArrayTest {

    @Test
    void typicalInputReturnsDuplicates() {
        int[] nums = {4, 3, 2, 7, 8, 2, 3, 1};
        assertThat(FindDuplicatesInArray.solve(nums)).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void noDuplicatesReturnsEmptySet() {
        int[] nums = {1, 2, 3};
        assertThat(FindDuplicatesInArray.solve(nums)).isEmpty();
    }

    @Test
    void emptyArrayReturnsEmptySet() {
        assertThat(FindDuplicatesInArray.solve(new int[0])).isEmpty();
    }

    @Test
    void allElementsIdenticalReturnsSingleDuplicate() {
        int[] nums = {5, 5, 5, 5};
        assertThat(FindDuplicatesInArray.solve(nums)).containsExactly(5);
    }

    @Test
    void singleElementReturnsEmptySet() {
        assertThat(FindDuplicatesInArray.solve(new int[] {9})).isEmpty();
    }
}
