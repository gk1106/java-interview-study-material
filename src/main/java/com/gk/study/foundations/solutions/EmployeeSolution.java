package com.gk.study.foundations.solutions;

/** Reference solution for {@code exercises.Employee}. Standalone copy used by the solutions package. */
public class EmployeeSolution implements Comparable<EmployeeSolution> {
    private final int id;
    private final String name;
    private final String department;
    private final Double salary;

    public EmployeeSolution(int id, String name, String department, Double salary) {
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
    public int compareTo(EmployeeSolution other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name='" + name + "', dept='" + department + "', salary=" + salary + "}";
    }
}
