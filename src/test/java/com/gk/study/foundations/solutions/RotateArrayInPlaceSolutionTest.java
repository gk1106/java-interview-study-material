package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RotateArrayInPlaceSolutionTest {

    @Test
    void rotatesRightByK() {
        int[] arr = {1, 2, 3, 4, 5, 6, 7};
        RotateArrayInPlaceSolution.rotateRight(arr, 3);
        assertThat(arr).containsExactly(5, 6, 7, 1, 2, 3, 4);
    }

    @Test
    void kOfZeroIsNoOp() {
        int[] arr = {1, 2, 3};
        RotateArrayInPlaceSolution.rotateRight(arr, 0);
        assertThat(arr).containsExactly(1, 2, 3);
    }

    @Test
    void kEqualToLengthIsNoOp() {
        int[] arr = {1, 2, 3};
        RotateArrayInPlaceSolution.rotateRight(arr, 3);
        assertThat(arr).containsExactly(1, 2, 3);
    }

    @Test
    void kLargerThanLengthWraps() {
        int[] arr = {1, 2, 3, 4};
        RotateArrayInPlaceSolution.rotateRight(arr, 6);
        assertThat(arr).containsExactly(3, 4, 1, 2);
    }

    @Test
    void emptyArrayDoesNotThrow() {
        int[] arr = {};
        RotateArrayInPlaceSolution.rotateRight(arr, 5);
        assertThat(arr).isEmpty();
    }

    @Test
    void singleElementArray() {
        int[] arr = {42};
        RotateArrayInPlaceSolution.rotateRight(arr, 10);
        assertThat(arr).containsExactly(42);
    }
}
