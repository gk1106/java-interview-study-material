package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.list.exercises.ReverseKGroup.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ReverseKGroup} exercise stub. EXPECTED TO FAIL until implemented.
 */
class ReverseKGroupTest {

    private static Node build(int... values) {
        Node dummy = new Node(0);
        Node tail = dummy;
        for (int v : values) {
            tail.next = new Node(v);
            tail = tail.next;
        }
        return dummy.next;
    }

    private static List<Integer> toList(Node head) {
        List<Integer> result = new ArrayList<>();
        for (Node n = head; n != null; n = n.next) {
            result.add(n.val);
        }
        return result;
    }

    @Test
    void groupSizeTwoEvenlyDivides() {
        Node result = ReverseKGroup.reverseKGroup(build(1, 2, 3, 4, 5, 6), 2);
        assertThat(toList(result)).containsExactly(2, 1, 4, 3, 6, 5);
    }

    @Test
    void groupSizeThreeWithPartialFinalGroup() {
        Node result = ReverseKGroup.reverseKGroup(build(1, 2, 3, 4, 5), 3);
        assertThat(toList(result)).containsExactly(3, 2, 1, 4, 5); // last 2 nodes untouched
    }

    @Test
    void groupSizeOneIsNoOp() {
        Node result = ReverseKGroup.reverseKGroup(build(1, 2, 3), 1);
        assertThat(toList(result)).containsExactly(1, 2, 3);
    }

    @Test
    void groupSizeEqualsListLength() {
        Node result = ReverseKGroup.reverseKGroup(build(1, 2, 3, 4), 4);
        assertThat(toList(result)).containsExactly(4, 3, 2, 1);
    }

    @Test
    void emptyListReturnsNull() {
        assertThat(ReverseKGroup.reverseKGroup(null, 2)).isNull();
    }

    @Test
    void groupSizeLargerThanListLeavesListUnchanged() {
        Node result = ReverseKGroup.reverseKGroup(build(1, 2), 5);
        assertThat(toList(result)).containsExactly(1, 2);
    }
}
