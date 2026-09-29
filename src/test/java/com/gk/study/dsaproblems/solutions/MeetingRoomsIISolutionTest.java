package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MeetingRoomsIISolutionTest {

    @Test
    void typicalInput() {
        List<int[]> intervals = List.of(new int[] {0, 30}, new int[] {5, 10}, new int[] {15, 20});
        assertThat(MeetingRoomsIISolution.solve(intervals)).isEqualTo(2);
    }

    @Test
    void nonOverlappingNeedsOneRoom() {
        List<int[]> intervals = List.of(new int[] {7, 10}, new int[] {2, 4});
        assertThat(MeetingRoomsIISolution.solve(intervals)).isEqualTo(1);
    }

    @Test
    void emptyInputNeedsNoRooms() {
        assertThat(MeetingRoomsIISolution.solve(List.of())).isEqualTo(0);
    }

    @Test
    void singleMeetingNeedsOneRoom() {
        assertThat(MeetingRoomsIISolution.solve(List.of(new int[] {1, 5}))).isEqualTo(1);
    }

    @Test
    void backToBackMeetingsShareOneRoom() {
        List<int[]> intervals = List.of(new int[] {1, 5}, new int[] {5, 10}, new int[] {10, 15});
        assertThat(MeetingRoomsIISolution.solve(intervals)).isEqualTo(1);
    }

    @Test
    void allMeetingsFullyOverlapNeedsOneRoomPerMeeting() {
        int n = 50;
        List<int[]> intervals = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            intervals.add(new int[] {0, 100});
        }
        assertThat(MeetingRoomsIISolution.solve(intervals)).isEqualTo(n);
    }
}
