package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FirstNonRepeatingChar} exercise stub. EXPECTED TO FAIL until implemented.
 */
class FirstNonRepeatingCharTest {

    @Test
    void typicalInput() {
        assertThat(FirstNonRepeatingChar.solve("leetcode")).isEqualTo(0);
    }

    @Test
    void noNonRepeatingCharacter() {
        assertThat(FirstNonRepeatingChar.solve("aabb")).isEqualTo(-1);
    }

    @Test
    void emptyString() {
        assertThat(FirstNonRepeatingChar.solve("")).isEqualTo(-1);
    }

    @Test
    void singleCharacter() {
        assertThat(FirstNonRepeatingChar.solve("z")).isEqualTo(0);
    }

    @Test
    void nonRepeatingCharacterInTheMiddle() {
        assertThat(FirstNonRepeatingChar.solve("aabbcddc")).isEqualTo(-1);
        assertThat(FirstNonRepeatingChar.solve("aabbcdec")).isEqualTo(5);
    }
}
