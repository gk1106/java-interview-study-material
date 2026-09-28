package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NextGreaterElementSolutionTest {

    @Test
    void classicExample() {
        assertThat(NextGreaterElementSolution.nextGreaterElements(new int[]{2, 1, 2, 4, 3}))
                .containsExactly(4, 2, 4, -1, -1);
    }

    @Test
    void increasingSequence() {
        assertThat(NextGreaterElementSolution.nextGreaterElements(new int[]{1, 2, 3, 4}))
                .containsExactly(2, 3, 4, -1);
    }

    @Test
    void decreasingSequenceHasNoNextGreater() {
        assertThat(NextGreaterElementSolution.nextGreaterElements(new int[]{4, 3, 2, 1}))
                .containsExactly(-1, -1, -1, -1);
    }

    @Test
    void singleElement() {
        assertThat(NextGreaterElementSolution.nextGreaterElements(new int[]{5}))
                .containsExactly(-1);
    }

    @Test
    void emptyArray() {
        assertThat(NextGreaterElementSolution.nextGreaterElements(new int[]{})).isEmpty();
    }

    @Test
    void duplicateValues() {
        assertThat(NextGreaterElementSolution.nextGreaterElements(new int[]{2, 2, 2, 3}))
                .containsExactly(3, 3, 3, -1);
    }
}
