package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class KthLargestElementSolutionTest {

    @Test
    void typicalInput() {
        assertThat(KthLargestElementSolution.solve(List.of(3, 2, 1, 5, 6, 4), 2)).isEqualTo(5);
    }

    @Test
    void kEqualsOneReturnsMaximum() {
        assertThat(KthLargestElementSolution.solve(List.of(3, 2, 1, 5, 6, 4), 1)).isEqualTo(6);
    }

    @Test
    void kEqualsSizeReturnsMinimum() {
        assertThat(KthLargestElementSolution.solve(List.of(3, 2, 1, 5, 6, 4), 6)).isEqualTo(1);
    }

    @Test
    void singleElement() {
        assertThat(KthLargestElementSolution.solve(List.of(7), 1)).isEqualTo(7);
    }

    @Test
    void allSameElements() {
        assertThat(KthLargestElementSolution.solve(List.of(4, 4, 4, 4), 3)).isEqualTo(4);
    }

    @Test
    void duplicatesCountSeparately() {
        assertThat(KthLargestElementSolution.solve(List.of(5, 5, 5, 1), 2)).isEqualTo(5);
    }

    @Test
    void invalidKThrows() {
        assertThatThrownBy(() -> KthLargestElementSolution.solve(List.of(1, 2, 3), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KthLargestElementSolution.solve(List.of(1, 2, 3), 4))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void largerInput() {
        java.util.List<Integer> nums = new java.util.ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            nums.add(i);
        }
        java.util.Collections.shuffle(nums, new java.util.Random(7));
        assertThat(KthLargestElementSolution.solve(nums, 10)).isEqualTo(991);
    }
}
