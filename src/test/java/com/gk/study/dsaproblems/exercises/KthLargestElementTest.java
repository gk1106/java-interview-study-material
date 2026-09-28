package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link KthLargestElement} exercise stub. EXPECTED TO FAIL until implemented.
 */
class KthLargestElementTest {

    @Test
    void typicalInput() {
        assertThat(KthLargestElement.solve(List.of(3, 2, 1, 5, 6, 4), 2)).isEqualTo(5);
    }

    @Test
    void kEqualsOneReturnsMaximum() {
        assertThat(KthLargestElement.solve(List.of(3, 2, 1, 5, 6, 4), 1)).isEqualTo(6);
    }

    @Test
    void kEqualsSizeReturnsMinimum() {
        assertThat(KthLargestElement.solve(List.of(3, 2, 1, 5, 6, 4), 6)).isEqualTo(1);
    }

    @Test
    void singleElement() {
        assertThat(KthLargestElement.solve(List.of(7), 1)).isEqualTo(7);
    }

    @Test
    void allSameElements() {
        assertThat(KthLargestElement.solve(List.of(4, 4, 4, 4), 3)).isEqualTo(4);
    }

    @Test
    void duplicatesCountSeparately() {
        assertThat(KthLargestElement.solve(List.of(5, 5, 5, 1), 2)).isEqualTo(5);
    }
}
