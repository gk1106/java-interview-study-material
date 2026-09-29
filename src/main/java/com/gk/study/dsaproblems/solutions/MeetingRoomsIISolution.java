package com.gk.study.dsaproblems.solutions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Solution for {@link com.gk.study.dsaproblems.exercises.MeetingRoomsII}.
 *
 * <p>Sort meetings by start time. Walk through them while maintaining a min-heap of the end
 * times of currently-occupied rooms: before assigning a room to the next meeting, pop every
 * room whose meeting has already ended (its end time &lt;= this meeting's start) — that room is
 * now free and gets reused rather than a new one being opened. Push this meeting's end time onto
 * the heap either way. The answer is the largest the heap ever grows to across the whole scan.
 * O(n log n) time (sort + heap ops), O(n) space.
 */
public final class MeetingRoomsIISolution {

    private MeetingRoomsIISolution() {
    }

    public static int solve(List<int[]> intervals) {
        if (intervals.isEmpty()) {
            return 0;
        }
        List<int[]> sorted = new ArrayList<>(intervals);
        sorted.sort(Comparator.comparingInt(interval -> interval[0]));

        PriorityQueue<Integer> roomEndTimes = new PriorityQueue<>();
        int maxRooms = 0;
        for (int[] meeting : sorted) {
            while (!roomEndTimes.isEmpty() && roomEndTimes.peek() <= meeting[0]) {
                roomEndTimes.poll();
            }
            roomEndTimes.offer(meeting[1]);
            maxRooms = Math.max(maxRooms, roomEndTimes.size());
        }
        return maxRooms;
    }
}
