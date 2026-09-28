package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link DepartmentHeadcount} exercise stub. EXPECTED TO FAIL until implemented.
 */
class DepartmentHeadcountTest {

    @Test
    void typicalInput() {
        Map<String, String> employees = Map.of(
                "Alice", "ENGINEERING",
                "Bob", "ENGINEERING",
                "Cara", "SALES",
                "Dan", "SALES",
                "Eve", "SALES",
                "Finn", "HR");
        assertThat(DepartmentHeadcount.solve(employees))
                .containsEntry("ENGINEERING", 2L)
                .containsEntry("SALES", 3L)
                .containsEntry("HR", 1L);
    }

    @Test
    void emptyInput() {
        assertThat(DepartmentHeadcount.solve(Map.of())).isEmpty();
    }

    @Test
    void singleEmployee() {
        assertThat(DepartmentHeadcount.solve(Map.of("Alice", "OPS"))).containsExactly(Map.entry("OPS", 1L));
    }
}
