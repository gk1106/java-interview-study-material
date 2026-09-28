package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeSolutionTest {

    @Test
    void sortsAscendingById() {
        List<EmployeeSolution> list = new ArrayList<>(List.of(
                new EmployeeSolution(3, "Zoe", "Ops", null),
                new EmployeeSolution(1, "Amy", "Eng", 1.0),
                new EmployeeSolution(2, "Bob", "Eng", 2.0)
        ));
        Collections.sort(list);
        assertThat(list).extracting(EmployeeSolution::getId).containsExactly(1, 2, 3);
    }

    @Test
    void compareToIsZeroForSameId() {
        EmployeeSolution a = new EmployeeSolution(1, "Amy", "Eng", 1.0);
        EmployeeSolution b = new EmployeeSolution(1, "Amy2", "Eng2", 2.0);
        assertThat(a.compareTo(b)).isEqualTo(0);
    }

    @Test
    void handlesExtremeIdsWithoutOverflow() {
        EmployeeSolution a = new EmployeeSolution(Integer.MIN_VALUE, "A", "X", null);
        EmployeeSolution b = new EmployeeSolution(1, "B", "X", null);
        assertThat(a.compareTo(b)).isNegative();
        assertThat(b.compareTo(a)).isPositive();
    }
}
