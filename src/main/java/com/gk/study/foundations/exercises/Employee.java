package com.gk.study.foundations.exercises;

/**
 * E01 [Easy] Implement natural ordering for Employee by ascending id.
 * Input:  sorting [Employee(3,...), Employee(1,...), Employee(2,...)] via Collections.sort(list)
 * Output: ids appear in order [1, 2, 3]
 * Constraint: use Integer.compare (not subtraction, to avoid overflow); compareTo must be a total
 * order over id (ids are unique in this domain).
 * Pattern: Comparable natural ordering
 *
 * NOTE: this class is also the shared data model used by the EmployeeComparators (E02, E03)
 * and is not itself a value class with equals/hashCode overridden (see topic 3 for that).
 */
public class Employee implements Comparable<Employee> {
    private final int id;
    private final String name;
    private final String department;
    private final Double salary; // nullable: models "salary not yet set"

    public Employee(int id, String name, String department, Double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public Double getSalary() {
        return salary;
    }

    @Override
    public int compareTo(Employee other) {
        // TODO: order by id ascending using Integer.compare
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "', dept='" + department + "', salary=" + salary + "}";
    }
}
