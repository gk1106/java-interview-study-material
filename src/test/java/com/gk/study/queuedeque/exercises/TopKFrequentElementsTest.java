package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TopKFrequentElements} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TopKFrequentElementsTest {

    @Test
    void classicExample() {
        int[] nums = {1, 1, 1, 2, 2, 3};
        assertThat(TopKFrequentElements.topKFrequent(nums, 2))
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void kEqualsOneReturnsMostFrequent() {
        int[] nums = {1, 1, 2, 2, 2, 3, 3, 3, 3, 4};
        assertThat(TopKFrequentElements.topKFrequent(nums, 1))
                .containsExactly(3);
    }

    @Test
    void kEqualsDistinctCountReturnsAll() {
        int[] nums = {1, 2, 3, 4};
        assertThat(TopKFrequentElements.topKFrequent(nums, 4))
                .containsExactlyInAnyOrder(1, 2, 3, 4);
    }

    @Test
    void singleElementArray() {
        int[] nums = {7};
        assertThat(TopKFrequentElements.topKFrequent(nums, 1))
                .containsExactly(7);
    }
}
