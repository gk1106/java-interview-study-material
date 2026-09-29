package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseScheduleTopoSortSolutionTest {

    @Test
    void linearChainCanFinish() {
        assertThat(CourseScheduleTopoSortSolution.solve(2, List.of(new int[] {1, 0}))).isTrue();
    }

    @Test
    void directCycleCannotFinish() {
        assertThat(CourseScheduleTopoSortSolution.solve(
                        2, List.of(new int[] {1, 0}, new int[] {0, 1})))
                .isFalse();
    }

    @Test
    void noPrerequisitesCanFinish() {
        assertThat(CourseScheduleTopoSortSolution.solve(3, List.of())).isTrue();
    }

    @Test
    void singleCourseNoPrerequisites() {
        assertThat(CourseScheduleTopoSortSolution.solve(1, List.of())).isTrue();
    }

    @Test
    void diamondDependencyCanFinish() {
        List<int[]> prerequisites = List.of(
                new int[] {1, 0}, new int[] {2, 0}, new int[] {3, 1}, new int[] {3, 2});
        assertThat(CourseScheduleTopoSortSolution.solve(4, prerequisites)).isTrue();
    }

    @Test
    void largerCycleAmongOtherwiseValidCoursesCannotFinish() {
        List<int[]> prerequisites = new ArrayList<>();
        for (int i = 1; i < 50; i++) {
            prerequisites.add(new int[] {i, i - 1});
        }
        prerequisites.add(new int[] {25, 30});
        assertThat(CourseScheduleTopoSortSolution.solve(50, prerequisites)).isFalse();
    }
}
