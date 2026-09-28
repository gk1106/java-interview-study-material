package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SingleNumberSolutionTest {

    @Test
    void xorApproachFindsTheSingleton() {
        assertThat(SingleNumberSolution.solveXor(new int[] {4, 1, 2, 1, 2})).isEqualTo(4);
    }

    @Test
    void hashSetApproachFindsTheSingleton() {
        assertThat(SingleNumberSolution.solveHashSet(new int[] {4, 1, 2, 1, 2})).isEqualTo(4);
    }

    @Test
    void xorApproachHandlesNegativeNumbers() {
        assertThat(SingleNumberSolution.solveXor(new int[] {-1, -1, -2})).isEqualTo(-2);
    }

    @Test
    void hashSetApproachHandlesNegativeNumbers() {
        assertThat(SingleNumberSolution.solveHashSet(new int[] {-1, -1, -2})).isEqualTo(-2);
    }

    @Test
    void singleElementArrayReturnsThatElement() {
        assertThat(SingleNumberSolution.solveXor(new int[] {42})).isEqualTo(42);
        assertThat(SingleNumberSolution.solveHashSet(new int[] {42})).isEqualTo(42);
    }

    @Test
    void bothApproachesAgreeOnLargerInput() {
        int[] nums = {17, 3, 17, 5, 3};
        assertThat(SingleNumberSolution.solveXor(nums)).isEqualTo(SingleNumberSolution.solveHashSet(nums));
    }
}
