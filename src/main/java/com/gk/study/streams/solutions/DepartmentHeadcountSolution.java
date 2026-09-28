package com.gk.study.streams.solutions;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.DepartmentHeadcount}.
 *
 * <p>groupingBy(identity, counting()) over the department values is the textbook frequency-map
 * idiom. O(n) time, O(d) space (d = distinct departments).
 */
public final class DepartmentHeadcountSolution {

    private DepartmentHeadcountSolution() {
    }

    public static Map<String, Long> solve(Map<String, String> employeeNameToDepartment) {
        return employeeNameToDepartment.values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
