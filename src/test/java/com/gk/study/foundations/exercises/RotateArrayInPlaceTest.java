package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RotateArrayInPlaceTest {

    @Test
    void rotatesRightByK() {
        int[] arr = {1, 2, 3, 4, 5, 6, 7};
        RotateArrayInPlace.rotateRight(arr, 3);
        assertThat(arr).containsExactly(5, 6, 7, 1, 2, 3, 4);
    }

    @Test
    void kEqualToLengthIsNoOp() {
        int[] arr = {1, 2, 3};
        RotateArrayInPlace.rotateRight(arr, 3);
        assertThat(arr).containsExactly(1, 2, 3);
    }

    @Test
    void kLargerThanLengthWraps() {
        int[] arr = {1, 2, 3, 4};
        RotateArrayInPlace.rotateRight(arr, 6); // equivalent to rotating by 2
        assertThat(arr).containsExactly(3, 4, 1, 2);
    }
}
