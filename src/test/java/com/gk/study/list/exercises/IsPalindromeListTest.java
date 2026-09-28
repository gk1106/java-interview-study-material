package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link IsPalindromeList} exercise stub. EXPECTED TO FAIL until implemented.
 */
class IsPalindromeListTest {

    @Test
    void oddLengthPalindromeReturnsTrue() {
        assertThat(IsPalindromeList.solve(List.of(1, 2, 3, 2, 1))).isTrue();
    }

    @Test
    void nonPalindromeReturnsFalse() {
        assertThat(IsPalindromeList.solve(List.of(1, 2, 3))).isFalse();
    }

    @Test
    void emptyListIsPalindrome() {
        assertThat(IsPalindromeList.solve(List.of())).isTrue();
    }

    @Test
    void singleElementIsPalindrome() {
        assertThat(IsPalindromeList.solve(List.of(9))).isTrue();
    }

    @Test
    void evenLengthPalindromeReturnsTrue() {
        assertThat(IsPalindromeList.solve(List.of(1, 2, 2, 1))).isTrue();
    }

    @Test
    void evenLengthNonPalindromeReturnsFalse() {
        assertThat(IsPalindromeList.solve(List.of(1, 2, 3, 4))).isFalse();
    }
}
