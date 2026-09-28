package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.list.solutions.ReverseLinkedListSolution.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReverseLinkedListSolutionTest {

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
    void iterativeReversesTypicalList() {
        Node reversed = ReverseLinkedListSolution.reverseIterative(build(1, 2, 3));
        assertThat(toList(reversed)).containsExactly(3, 2, 1);
    }

    @Test
    void iterativeHandlesEmptyList() {
        assertThat(ReverseLinkedListSolution.reverseIterative(null)).isNull();
    }

    @Test
    void iterativeHandlesSingleNode() {
        Node reversed = ReverseLinkedListSolution.reverseIterative(build(42));
        assertThat(toList(reversed)).containsExactly(42);
    }

    @Test
    void recursiveReversesTypicalList() {
        Node reversed = ReverseLinkedListSolution.reverseRecursive(build(1, 2, 3, 4));
        assertThat(toList(reversed)).containsExactly(4, 3, 2, 1);
    }

    @Test
    void recursiveHandlesEmptyList() {
        assertThat(ReverseLinkedListSolution.reverseRecursive(null)).isNull();
    }

    @Test
    void bothApproachesAgreeOnLargerInput() {
        int n = 500;
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = i;
        }
        List<Integer> iterativeResult = toList(ReverseLinkedListSolution.reverseIterative(build(values)));
        List<Integer> recursiveResult = toList(ReverseLinkedListSolution.reverseRecursive(build(values)));
        assertThat(iterativeResult).isEqualTo(recursiveResult);
        assertThat(iterativeResult.get(0)).isEqualTo(n - 1);
    }
}
