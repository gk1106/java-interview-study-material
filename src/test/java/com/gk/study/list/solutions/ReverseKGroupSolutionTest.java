package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.list.solutions.ReverseKGroupSolution.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReverseKGroupSolutionTest {

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
        Node result = ReverseKGroupSolution.reverseKGroup(build(1, 2, 3, 4, 5, 6), 2);
        assertThat(toList(result)).containsExactly(2, 1, 4, 3, 6, 5);
    }

    @Test
    void groupSizeThreeWithPartialFinalGroup() {
        Node result = ReverseKGroupSolution.reverseKGroup(build(1, 2, 3, 4, 5), 3);
        assertThat(toList(result)).containsExactly(3, 2, 1, 4, 5);
    }

    @Test
    void groupSizeOneIsNoOp() {
        Node result = ReverseKGroupSolution.reverseKGroup(build(1, 2, 3), 1);
        assertThat(toList(result)).containsExactly(1, 2, 3);
    }

    @Test
    void groupSizeEqualsListLength() {
        Node result = ReverseKGroupSolution.reverseKGroup(build(1, 2, 3, 4), 4);
        assertThat(toList(result)).containsExactly(4, 3, 2, 1);
    }

    @Test
    void emptyListReturnsNull() {
        assertThat(ReverseKGroupSolution.reverseKGroup(null, 2)).isNull();
    }

    @Test
    void groupSizeLargerThanListLeavesListUnchanged() {
        Node result = ReverseKGroupSolution.reverseKGroup(build(1, 2), 5);
        assertThat(toList(result)).containsExactly(1, 2);
    }
}
