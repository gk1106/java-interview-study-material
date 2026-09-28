package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeComparatorsSolutionTest {

    @Test
    void byDepartmentThenNameSortsCorrectly() {
        List<EmployeeSolution> list = new ArrayList<>(List.of(
                new EmployeeSolution(1, "Zoe", "Ops", 1.0),
                new EmployeeSolution(2, "Amy", "Eng", 1.0),
                new EmployeeSolution(3, "Bob", "Eng", 1.0)
        ));
        list.sort(EmployeeComparatorsSolution.byDepartmentThenName());
        assertThat(list).extracting(EmployeeSolution::getName).containsExactly("Amy", "Bob", "Zoe");
    }

    @Test
    void bySalaryNullsFirstThenNameDescSortsCorrectly() {
        List<EmployeeSolution> list = new ArrayList<>(List.of(
                new EmployeeSolution(1, "Amy", "Eng", 50000.0),
                new EmployeeSolution(2, "Bob", "Eng", 50000.0),
                new EmployeeSolution(3, "Cid", "Eng", null)
        ));
        list.sort(EmployeeComparatorsSolution.bySalaryNullsFirstThenNameDesc());
        assertThat(list).extracting(EmployeeSolution::getName).containsExactly("Cid", "Bob", "Amy");
    }

    @Test
    void allNullSalariesFallBackToNameTieBreak() {
        List<EmployeeSolution> list = new ArrayList<>(List.of(
                new EmployeeSolution(1, "Amy", "Eng", null),
                new EmployeeSolution(2, "Bob", "Eng", null)
        ));
        list.sort(EmployeeComparatorsSolution.bySalaryNullsFirstThenNameDesc());
        assertThat(list).extracting(EmployeeSolution::getName).containsExactly("Bob", "Amy");
    }
}
