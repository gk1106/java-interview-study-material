package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeComparatorsTest {

    @Test
    void byDepartmentThenNameSortsCorrectly() {
        List<Employee> list = new ArrayList<>(List.of(
                new Employee(1, "Zoe", "Ops", 1.0),
                new Employee(2, "Amy", "Eng", 1.0),
                new Employee(3, "Bob", "Eng", 1.0)
        ));
        list.sort(EmployeeComparators.byDepartmentThenName());
        assertThat(list).extracting(Employee::getName).containsExactly("Amy", "Bob", "Zoe");
    }

    @Test
    void bySalaryNullsFirstThenNameDescSortsCorrectly() {
        List<Employee> list = new ArrayList<>(List.of(
                new Employee(1, "Amy", "Eng", 50000.0),
                new Employee(2, "Bob", "Eng", 50000.0),
                new Employee(3, "Cid", "Eng", null)
        ));
        list.sort(EmployeeComparators.bySalaryNullsFirstThenNameDesc());
        assertThat(list).extracting(Employee::getName).containsExactly("Cid", "Bob", "Amy");
    }
}
