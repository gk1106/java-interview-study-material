package com.gk.study.dsaproblems.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link CourseScheduleTopoSort} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class CourseScheduleTopoSortTest {

    @Test
    void linearChainCanFinish() {
        assertThat(CourseScheduleTopoSort.solve(2, List.of(new int[] {1, 0}))).isTrue();
    }

    @Test
    void directCycleCannotFinish() {
        assertThat(CourseScheduleTopoSort.solve(2, List.of(new int[] {1, 0}, new int[] {0, 1})))
                .isFalse();
    }

    @Test
    void noPrerequisitesCanFinish() {
        assertThat(CourseScheduleTopoSort.solve(3, List.of())).isTrue();
    }

    @Test
    void singleCourseNoPrerequisites() {
        assertThat(CourseScheduleTopoSort.solve(1, List.of())).isTrue();
    }

    @Test
    void diamondDependencyCanFinish() {
        // 0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3
        List<int[]> prerequisites = List.of(
                new int[] {1, 0}, new int[] {2, 0}, new int[] {3, 1}, new int[] {3, 2});
        assertThat(CourseScheduleTopoSort.solve(4, prerequisites)).isTrue();
    }

    @Test
    void largerCycleAmongOtherwiseValidCoursesCannotFinish() {
        List<int[]> prerequisites = new ArrayList<>();
        for (int i = 1; i < 50; i++) {
            prerequisites.add(new int[] {i, i - 1}); // long valid chain 0 -> 1 -> ... -> 49
        }
        prerequisites.add(new int[] {25, 30}); // but 30 depends (transitively) on 25 -> cycle
        assertThat(CourseScheduleTopoSort.solve(50, prerequisites)).isFalse();
    }
}
