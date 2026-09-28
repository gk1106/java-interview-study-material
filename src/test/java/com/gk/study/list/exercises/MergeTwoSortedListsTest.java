package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.list.exercises.MergeTwoSortedLists.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MergeTwoSortedLists} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MergeTwoSortedListsTest {

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
    void mergesTwoTypicalLists() {
        Node merged = MergeTwoSortedLists.merge(build(1, 3, 5), build(2, 4, 6));
        assertThat(toList(merged)).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void firstListEmpty() {
        Node merged = MergeTwoSortedLists.merge(null, build(1, 2, 3));
        assertThat(toList(merged)).containsExactly(1, 2, 3);
    }

    @Test
    void secondListEmpty() {
        Node merged = MergeTwoSortedLists.merge(build(1, 2, 3), null);
        assertThat(toList(merged)).containsExactly(1, 2, 3);
    }

    @Test
    void bothListsEmpty() {
        assertThat(MergeTwoSortedLists.merge(null, null)).isNull();
    }

    @Test
    void listsOfDifferentLengths() {
        Node merged = MergeTwoSortedLists.merge(build(1, 2, 3, 4, 5), build(10));
        assertThat(toList(merged)).containsExactly(1, 2, 3, 4, 5, 10);
    }

    @Test
    void listsWithDuplicateValues() {
        Node merged = MergeTwoSortedLists.merge(build(1, 1, 3), build(1, 2, 2));
        assertThat(toList(merged)).containsExactly(1, 1, 1, 2, 2, 3);
    }
}
