package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class IsPalindromeListSolutionTest {

    @Test
    void oddLengthPalindromeReturnsTrue() {
        assertThat(IsPalindromeListSolution.solve(List.of(1, 2, 3, 2, 1))).isTrue();
    }

    @Test
    void nonPalindromeReturnsFalse() {
        assertThat(IsPalindromeListSolution.solve(List.of(1, 2, 3))).isFalse();
    }

    @Test
    void emptyListIsPalindrome() {
        assertThat(IsPalindromeListSolution.solve(List.of())).isTrue();
    }

    @Test
    void singleElementIsPalindrome() {
        assertThat(IsPalindromeListSolution.solve(List.of(9))).isTrue();
    }

    @Test
    void evenLengthPalindromeReturnsTrue() {
        assertThat(IsPalindromeListSolution.solve(List.of(1, 2, 2, 1))).isTrue();
    }

    @Test
    void evenLengthNonPalindromeReturnsFalse() {
        assertThat(IsPalindromeListSolution.solve(List.of(1, 2, 3, 4))).isFalse();
    }
}
