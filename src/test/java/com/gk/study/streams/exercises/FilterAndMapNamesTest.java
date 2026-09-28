package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FilterAndMapNames} exercise stub. EXPECTED TO FAIL until implemented.
 */
class FilterAndMapNamesTest {

    @Test
    void typicalInput() {
        List<String> result = FilterAndMapNames.solve(
                List.of("alice smith", "Bob Jones", "Ivy Chen", "oscar wilde", "Uma Rao", "Ken Adams"));
        assertThat(result).containsExactly("ALICE", "IVY", "OSCAR", "UMA");
    }

    @Test
    void emptyInput() {
        assertThat(FilterAndMapNames.solve(List.of())).isEmpty();
    }

    @Test
    void noVowelStartingNames() {
        assertThat(FilterAndMapNames.solve(List.of("Bob Jones", "Ken Adams"))).isEmpty();
    }

    @Test
    void allVowelStartingNames() {
        List<String> result = FilterAndMapNames.solve(List.of("amy ray", "eli stone", "ivy chen"));
        assertThat(result).containsExactly("AMY", "ELI", "IVY");
    }
}
