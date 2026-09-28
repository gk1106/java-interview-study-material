package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.queuedeque.exercises.MergeKSortedLists.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MergeKSortedLists} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MergeKSortedListsTest {

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
    void mergesThreeTypicalLists() {
        Node[] lists = {build(1, 4, 5), build(1, 3, 4), build(2, 6)};
        Node merged = MergeKSortedLists.mergeKLists(lists);
        assertThat(toList(merged)).containsExactly(1, 1, 2, 3, 4, 4, 5, 6);
    }

    @Test
    void emptyListsArrayReturnsNull() {
        assertThat(MergeKSortedLists.mergeKLists(new Node[]{})).isNull();
    }

    @Test
    void someNullEntriesAreSkipped() {
        Node[] lists = {null, build(1, 2, 3), null};
        Node merged = MergeKSortedLists.mergeKLists(lists);
        assertThat(toList(merged)).containsExactly(1, 2, 3);
    }

    @Test
    void allNullEntriesReturnNull() {
        Node[] lists = {null, null};
        assertThat(MergeKSortedLists.mergeKLists(lists)).isNull();
    }

    @Test
    void singleListIsReturnedInOrder() {
        Node[] lists = {build(1, 2, 3)};
        Node merged = MergeKSortedLists.mergeKLists(lists);
        assertThat(toList(merged)).containsExactly(1, 2, 3);
    }

    @Test
    void listsWithDuplicateValuesAcrossLists() {
        Node[] lists = {build(1, 1), build(1, 1)};
        Node merged = MergeKSortedLists.mergeKLists(lists);
        assertThat(toList(merged)).containsExactly(1, 1, 1, 1);
    }
}
