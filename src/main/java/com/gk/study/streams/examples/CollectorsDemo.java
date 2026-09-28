package com.gk.study.streams.examples;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tour of {@code reduce}, {@code collect}, and the most commonly used {@link Collectors}
 * factories: {@code toList}, {@code toMap} (with a merge function), {@code groupingBy} (with
 * downstream collectors), {@code partitioningBy}, {@code joining}, {@code counting},
 * {@code mapping}, and {@code teeing} (Java 12+).
 */
public class CollectorsDemo {

    record Employee(String name, String department, int salary) {
    }

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", "ENGINEERING", 95000),
                new Employee("Bob", "ENGINEERING", 88000),
                new Employee("Cara", "SALES", 60000),
                new Employee("Dan", "SALES", 65000),
                new Employee("Eve", "SALES", 70000),
                new Employee("Finn", "HR", 55000));

        // reduce — combine elements into one value with an identity + accumulator (+ optional
        // combiner when parallel). Prefer Collectors.summingInt for this specific case; reduce
        // shown here to illustrate the mechanics.
        int totalSalary = employees.stream().reduce(0, (acc, e) -> acc + e.salary(), Integer::sum);
        System.out.println("total salary via reduce = " + totalSalary);

        // collect(toList) — the common case
        List<String> names = employees.stream().map(Employee::name).collect(Collectors.toList());
        System.out.println("names = " + names);

        // toMap WITHOUT a merge function throws IllegalStateException on a duplicate key.
        // toMap WITH a merge function resolves the collision instead of throwing.
        Map<String, Integer> firstLetterToTotalSalary = employees.stream()
                .collect(Collectors.toMap(
                        e -> e.name().substring(0, 1),
                        Employee::salary,
                        Integer::sum)); // merge function: sum salaries that share a first letter
        System.out.println("firstLetterToTotalSalary = " + firstLetterToTotalSalary);

        // groupingBy — classic "group and count"
        Map<String, Long> countByDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
        System.out.println("countByDept = " + countByDept);

        // groupingBy with a downstream mapping + toList (extract just names per group)
        Map<String, List<String>> namesByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department, Collectors.mapping(Employee::name, Collectors.toList())));
        System.out.println("namesByDept = " + namesByDept);

        // groupingBy with averagingInt downstream
        Map<String, Double> avgSalaryByDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.averagingInt(Employee::salary)));
        System.out.println("avgSalaryByDept = " + avgSalaryByDept);

        // partitioningBy — always exactly two groups (true/false), even if one is empty
        Map<Boolean, List<Employee>> partitionedByHighEarner = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.salary() >= 70000));
        System.out.println("highEarners = " + partitionedByHighEarner.get(true).stream().map(Employee::name).toList());
        System.out.println("otherEarners = " + partitionedByHighEarner.get(false).stream().map(Employee::name).toList());

        // joining — string concatenation with delimiter/prefix/suffix
        String csv = employees.stream().map(Employee::name).collect(Collectors.joining(", ", "[", "]"));
        System.out.println("csv = " + csv);

        // teeing (Java 12+) — run two downstream collectors over the SAME stream in one pass and
        // combine their results; avoids materializing the stream twice for "min AND max"-style
        // two-answer aggregations.
        record MinMax(Employee min, Employee max) {
        }
        MinMax minMax = employees.stream()
                .collect(Collectors.teeing(
                        Collectors.minBy(Comparator.comparingInt(Employee::salary)),
                        Collectors.maxBy(Comparator.comparingInt(Employee::salary)),
                        (min, max) -> new MinMax(min.orElseThrow(), max.orElseThrow())));
        System.out.println("lowest paid = " + minMax.min().name() + ", highest paid = " + minMax.max().name());
    }
}
