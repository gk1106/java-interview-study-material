package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TopKFrequentElementsSolutionTest {

    @Test
    void typicalInput() {
        assertThat(TopKFrequentElementsSolution.solve(new int[] {1, 1, 1, 2, 2, 3}, 2))
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void kEqualsOne() {
        assertThat(TopKFrequentElementsSolution.solve(new int[] {4, 4, 4, 5, 5, 6}, 1)).containsExactly(4);
    }

    @Test
    void kEqualsDistinctCount() {
        assertThat(TopKFrequentElementsSolution.solve(new int[] {1, 2, 3}, 3))
                .containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void singleElement() {
        assertThat(TopKFrequentElementsSolution.solve(new int[] {7}, 1)).containsExactly(7);
    }

    @Test
    void largerInput() {
        int[] nums = new int[3000];
        for (int i = 0; i < 3000; i++) {
            nums[i] = i % 3 == 0 ? 1 : i;
        }
        assertThat(TopKFrequentElementsSolution.solve(nums, 1)).containsExactly(1);
    }
}
