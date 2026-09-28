package com.gk.study.streams.exercises;

import java.util.Map;

/**
 * M02 [Medium] Given a map of employee name -> department, return a frequency map of
 * department -> headcount.
 * Input:  {"Alice":"ENGINEERING","Bob":"ENGINEERING","Cara":"SALES","Dan":"SALES","Eve":"SALES","Finn":"HR"}
 * Output: {"ENGINEERING":2,"SALES":3,"HR":1}
 * Constraint: employee names are unique (map keys already guarantee this).
 * Pattern: Collectors.groupingBy(identity, counting())
 */
public class DepartmentHeadcount {

    /**
     * @param employeeNameToDepartment map of employee name to their department
     * @return map of department to number of employees in it
     */
    public static Map<String, Long> solve(Map<String, String> employeeNameToDepartment) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
