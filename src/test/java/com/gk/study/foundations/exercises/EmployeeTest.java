package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeTest {

    @Test
    void sortsAscendingById() {
        List<Employee> list = new ArrayList<>(List.of(
                new Employee(3, "Zoe", "Ops", null),
                new Employee(1, "Amy", "Eng", 1.0),
                new Employee(2, "Bob", "Eng", 2.0)
        ));
        Collections.sort(list);
        assertThat(list).extracting(Employee::getId).containsExactly(1, 2, 3);
    }

    @Test
    void compareToIsZeroForSameId() {
        Employee a = new Employee(1, "Amy", "Eng", 1.0);
        Employee b = new Employee(1, "Amy2", "Eng2", 2.0);
        assertThat(a.compareTo(b)).isEqualTo(0);
    }

    @Test
    void compareToIsAntisymmetric() {
        Employee a = new Employee(1, "Amy", "Eng", 1.0);
        Employee b = new Employee(2, "Bob", "Eng", 2.0);
        assertThat(Integer.signum(a.compareTo(b))).isEqualTo(-Integer.signum(b.compareTo(a)));
    }
}
