package com.gk.study.dsaproblems.exercises;

import java.util.List;

/**
 * M11 [Medium] Given a list of meeting {@code [start, end]} intervals, compute the minimum
 * number of rooms required so that no two overlapping meetings share a room.
 * Input: [[0,30],[5,10],[15,20]] &rarr; Output: 2 (the [5,10] and [15,20] meetings both overlap
 * with [0,30] but not with each other, so they can share one room while [0,30] uses another)
 * Constraint: O(n log n) time (dominated by sorting).
 * Pattern: greedy + min-heap of end times
 * Collections: PriorityQueue + List (sorted by start time)
 */
public class MeetingRoomsII {

    public static int solve(List<int[]> intervals) {
        // TODO: implement by sorting meetings by start time, then scanning left to right while
        // maintaining a min-heap of the end times of currently-occupied rooms; before assigning
        // a room to the next meeting, pop every room whose end time is <= this meeting's start
        // (that room has freed up and can be reused); the number of rooms needed is the largest
        // the heap ever grows to
        throw new UnsupportedOperationException("TODO");
    }
}
