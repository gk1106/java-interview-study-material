package com.gk.study.foundations.exercises;

import java.util.Comparator;

/**
 * E02 [Easy] byDepartmentThenName(): Comparator that sorts Employees by department ascending,
 * then by name ascending on ties.
 * Input:  [(dept=Ops,name=Zoe), (dept=Eng,name=Amy), (dept=Eng,name=Bob)]
 * Output: [(Eng,Amy), (Eng,Bob), (Ops,Zoe)]
 * Constraint: build via Comparator.comparing(...).thenComparing(...); O(1) to construct.
 * Pattern: multi-field Comparator chaining
 *
 * E03 [Medium] bySalaryNullsFirstThenNameDesc(): Comparator that sorts Employees with a null
 * salary first, then by salary ascending, then by name DESCENDING for ties.
 * Input:  [(salary=null,name=Amy), (salary=50000,name=Bob), (salary=50000,name=Amy)]
 * Output order: [(null,Amy), (50000,Bob), (50000,Amy)]   // Bob before Amy: name DESC breaks the tie
 * Constraint: use Comparator.nullsFirst(...) for the salary key and Comparator.reverseOrder() for
 * the name tie-break; O(1) to construct.
 * Pattern: Comparator.nullsFirst + reversed composition
 */
public class EmployeeComparators {

    public static Comparator<Employee> byDepartmentThenName() {
        // TODO: Comparator.comparing(Employee::getDepartment).thenComparing(Employee::getName)
        throw new UnsupportedOperationException("TODO");
    }

    public static Comparator<Employee> bySalaryNullsFirstThenNameDesc() {
        // TODO: Comparator.comparing(Employee::getSalary, Comparator.nullsFirst(Comparator.naturalOrder()))
        //           .thenComparing(Employee::getName, Comparator.reverseOrder())
        throw new UnsupportedOperationException("TODO");
    }
}
