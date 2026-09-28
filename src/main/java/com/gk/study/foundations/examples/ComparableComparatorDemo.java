package com.gk.study.foundations.examples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Demonstrates Comparable natural ordering vs flexible Comparator chaining. */
public class ComparableComparatorDemo {

    public static void main(String[] args) {
        List<Employee> employees = new ArrayList<>(List.of(
                new Employee(3, "Zoe", "Ops", null),
                new Employee(1, "Amy", "Eng", 50000.0),
                new Employee(2, "Bob", "Eng", 50000.0)
        ));

        List<Employee> natural = new ArrayList<>(employees);
        natural.sort(null); // uses Comparable natural order (by id)
        System.out.println("Natural order (by id): " + summarize(natural));

        List<Employee> byDeptThenName = new ArrayList<>(employees);
        byDeptThenName.sort(Comparator.comparing(Employee::getDepartment).thenComparing(Employee::getName));
        System.out.println("By department then name: " + summarizeDeptName(byDeptThenName));

        List<Employee> bySalaryNullsFirst = new ArrayList<>(employees);
        bySalaryNullsFirst.sort(Comparator
                .comparing(Employee::getSalary, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(Employee::getName, Comparator.reverseOrder()));
        System.out.println("By salary nulls-first then name desc: " + summarizeSalaryName(bySalaryNullsFirst));

        List<Employee> reversedNatural = new ArrayList<>(employees);
        reversedNatural.sort(Comparator.reverseOrder());
        System.out.println("Reversed natural order: " + summarize(reversedNatural));
    }

    private static String summarize(List<Employee> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        return sb.append("]").toString();
    }

    private static String summarizeDeptName(List<Employee> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i).getDepartment()).append("/").append(list.get(i).getName());
        }
        return sb.append("]").toString();
    }

    private static String summarizeSalaryName(List<Employee> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i).getSalary()).append("/").append(list.get(i).getName());
        }
        return sb.append("]").toString();
    }

    static final class Employee implements Comparable<Employee> {
        private final int id;
        private final String name;
        private final String department;
        private final Double salary;

        Employee(int id, String name, String department, Double salary) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        int getId() { return id; }
        String getName() { return name; }
        String getDepartment() { return department; }
        Double getSalary() { return salary; }

        @Override
        public int compareTo(Employee other) {
            return Integer.compare(this.id, other.id);
        }

        @Override
        public String toString() {
            return "Employee{id=" + id + "}";
        }
    }
}
