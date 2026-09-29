package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * M05 [Medium] There are {@code numCourses} courses, labeled {@code 0} to
 * {@code numCourses - 1}. Given a list of prerequisite pairs {@code [course, prerequisite]}
 * (meaning {@code prerequisite} must be completed before {@code course}), determine whether it
 * is possible to finish all courses.
 * Input: numCourses=2, prerequisites=[[1,0]] &rarr; Output: true (take 0, then 1)
 * Input: numCourses=2, prerequisites=[[1,0],[0,1]] &rarr; Output: false (0 needs 1, 1 needs 0)
 * Constraint: O(V + E) time where V=numCourses, E=prerequisites.size().
 * Pattern: topological sort (Kahn's BFS)
 * Collections: HashMap (adjacency list + in-degree count) + Queue (ArrayDeque)
 */
public class CourseScheduleTopoSort {

    public static boolean solve(int numCourses, List<int[]> prerequisites) {
        // TODO: implement using Kahn's algorithm: build an adjacency list (prerequisite ->
        // dependent courses) and an in-degree count per course with a HashMap or int[]; seed an
        // ArrayDeque with every course of in-degree 0; repeatedly poll a course, "complete" it,
        // and decrement the in-degree of its dependents, enqueueing any that reach 0. All
        // courses can be finished iff the total number of courses processed equals numCourses
        // (otherwise a cycle exists among the unprocessed courses)
        throw new UnsupportedOperationException("TODO");
    }
}
