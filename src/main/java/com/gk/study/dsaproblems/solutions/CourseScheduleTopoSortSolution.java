package com.gk.study.dsaproblems.solutions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.CourseScheduleTopoSort}.
 *
 * <p>Kahn's BFS topological sort: build an adjacency list (prerequisite -&gt; courses that depend
 * on it) plus an in-degree count for every course, seed the queue with every in-degree-0 course
 * (no unmet prerequisites), then repeatedly dequeue a course, "complete" it, and decrement its
 * dependents' in-degrees, enqueueing any that drop to 0. If every course gets processed, there is
 * a valid order (no cycle); if some remain stuck at in-degree &gt; 0, they form a cycle. O(V + E)
 * time and space.
 */
public final class CourseScheduleTopoSortSolution {

    private CourseScheduleTopoSortSolution() {
    }

    public static boolean solve(int numCourses, List<int[]> prerequisites) {
        Map<Integer, List<Integer>> adjacency = new HashMap<>();
        int[] inDegree = new int[numCourses];
        for (int course = 0; course < numCourses; course++) {
            adjacency.put(course, new ArrayList<>());
        }
        for (int[] pair : prerequisites) {
            int course = pair[0];
            int prerequisite = pair[1];
            adjacency.get(prerequisite).add(course);
            inDegree[course]++;
        }

        Queue<Integer> ready = new ArrayDeque<>();
        for (int course = 0; course < numCourses; course++) {
            if (inDegree[course] == 0) {
                ready.offer(course);
            }
        }

        int completed = 0;
        while (!ready.isEmpty()) {
            int course = ready.poll();
            completed++;
            for (int dependent : adjacency.get(course)) {
                inDegree[dependent]--;
                if (inDegree[dependent] == 0) {
                    ready.offer(dependent);
                }
            }
        }
        return completed == numCourses;
    }
}
