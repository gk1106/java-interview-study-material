package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ValidParentheses} exercise stub. EXPECTED TO FAIL until implemented.
 */
class ValidParenthesesTest {

    @Test
    void allBracketTypesNestedCorrectly() {
        assertThat(ValidParentheses.isValid("({[]})")).isTrue();
    }

    @Test
    void mismatchedTypesReturnsFalse() {
        assertThat(ValidParentheses.isValid("(]")).isFalse();
    }

    @Test
    void wrongOrderReturnsFalseEvenIfCountsMatch() {
        assertThat(ValidParentheses.isValid("([)]")).isFalse();
    }

    @Test
    void unclosedOpenerReturnsFalse() {
        assertThat(ValidParentheses.isValid("(()")).isFalse();
    }

    @Test
    void emptyStringIsValid() {
        assertThat(ValidParentheses.isValid("")).isTrue();
    }

    @Test
    void singleTypeRepeated() {
        assertThat(ValidParentheses.isValid("()()()")).isTrue();
    }

    @Test
    void largerNestedInput() {
        assertThat(ValidParentheses.isValid("((()))[[[]]]{{{}}}")).isTrue();
    }

    @Test
    void unmatchedClosingBracketAlone() {
        assertThat(ValidParentheses.isValid(")")).isFalse();
    }
}
