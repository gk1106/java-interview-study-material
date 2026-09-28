package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidParenthesesSolutionTest {

    @Test
    void allBracketTypesNestedCorrectly() {
        assertThat(ValidParenthesesSolution.isValid("({[]})")).isTrue();
    }

    @Test
    void mismatchedTypesReturnsFalse() {
        assertThat(ValidParenthesesSolution.isValid("(]")).isFalse();
    }

    @Test
    void wrongOrderReturnsFalseEvenIfCountsMatch() {
        assertThat(ValidParenthesesSolution.isValid("([)]")).isFalse();
    }

    @Test
    void unclosedOpenerReturnsFalse() {
        assertThat(ValidParenthesesSolution.isValid("(()")).isFalse();
    }

    @Test
    void emptyStringIsValid() {
        assertThat(ValidParenthesesSolution.isValid("")).isTrue();
    }

    @Test
    void singleTypeRepeated() {
        assertThat(ValidParenthesesSolution.isValid("()()()")).isTrue();
    }

    @Test
    void largerNestedInput() {
        assertThat(ValidParenthesesSolution.isValid("((()))[[[]]]{{{}}}")).isTrue();
    }

    @Test
    void unmatchedClosingBracketAlone() {
        assertThat(ValidParenthesesSolution.isValid(")")).isFalse();
    }
}
