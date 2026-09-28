package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TopKFrequentElements} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TopKFrequentElementsTest {

    @Test
    void typicalInput() {
        assertThat(TopKFrequentElements.solve(new int[] {1, 1, 1, 2, 2, 3}, 2))
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void kEqualsOne() {
        assertThat(TopKFrequentElements.solve(new int[] {4, 4, 4, 5, 5, 6}, 1)).containsExactly(4);
    }

    @Test
    void kEqualsDistinctCount() {
        assertThat(TopKFrequentElements.solve(new int[] {1, 2, 3}, 3)).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void singleElement() {
        assertThat(TopKFrequentElements.solve(new int[] {7}, 1)).containsExactly(7);
    }

    @Test
    void largerInput() {
        int[] nums = new int[3000];
        for (int i = 0; i < 3000; i++) {
            nums[i] = i % 3 == 0 ? 1 : i; // element 1 appears 1000 times, far more than any other
        }
        assertThat(TopKFrequentElements.solve(nums, 1)).containsExactly(1);
    }
}
