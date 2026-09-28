package com.gk.study.foundations.solutions;

import java.util.Comparator;

/** Reference solution for {@code exercises.EmployeeComparators}. */
public class EmployeeComparatorsSolution {

    public static Comparator<EmployeeSolution> byDepartmentThenName() {
        return Comparator.comparing(EmployeeSolution::getDepartment)
                .thenComparing(EmployeeSolution::getName);
    }

    public static Comparator<EmployeeSolution> bySalaryNullsFirstThenNameDesc() {
        return Comparator
                .comparing(EmployeeSolution::getSalary, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(EmployeeSolution::getName, Comparator.reverseOrder());
    }
}
