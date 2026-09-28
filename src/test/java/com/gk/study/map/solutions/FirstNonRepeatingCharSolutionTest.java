package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FirstNonRepeatingCharSolutionTest {

    @Test
    void typicalInput() {
        assertThat(FirstNonRepeatingCharSolution.solve("leetcode")).isEqualTo(0);
    }

    @Test
    void noNonRepeatingCharacter() {
        assertThat(FirstNonRepeatingCharSolution.solve("aabb")).isEqualTo(-1);
    }

    @Test
    void emptyString() {
        assertThat(FirstNonRepeatingCharSolution.solve("")).isEqualTo(-1);
    }

    @Test
    void singleCharacter() {
        assertThat(FirstNonRepeatingCharSolution.solve("z")).isEqualTo(0);
    }

    @Test
    void nonRepeatingCharacterInTheMiddle() {
        assertThat(FirstNonRepeatingCharSolution.solve("aabbcddc")).isEqualTo(-1);
        assertThat(FirstNonRepeatingCharSolution.solve("aabbcdec")).isEqualTo(5);
    }
}
