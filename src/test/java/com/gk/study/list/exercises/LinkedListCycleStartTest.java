package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.list.exercises.LinkedListCycleStart.Node;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LinkedListCycleStart} exercise stub. EXPECTED TO FAIL until implemented.
 */
class LinkedListCycleStartTest {

    @Test
    void noCycleReturnsNull() {
        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        a.next = b;
        b.next = c;
        assertThat(LinkedListCycleStart.findCycleStart(a)).isNull();
    }

    @Test
    void cycleStartingInTheMiddleIsFound() {
        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        Node d = new Node(4);
        a.next = b;
        b.next = c;
        c.next = d;
        d.next = b; // cycle back to b
        assertThat(LinkedListCycleStart.findCycleStart(a)).isSameAs(b);
    }

    @Test
    void emptyListReturnsNull() {
        assertThat(LinkedListCycleStart.findCycleStart(null)).isNull();
    }

    @Test
    void singleNodeSelfCycle() {
        Node a = new Node(1);
        a.next = a; // points to itself
        assertThat(LinkedListCycleStart.findCycleStart(a)).isSameAs(a);
    }

    @Test
    void singleNodeNoCycle() {
        Node a = new Node(1);
        assertThat(LinkedListCycleStart.findCycleStart(a)).isNull();
    }

    @Test
    void cycleAtTheHead() {
        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        a.next = b;
        b.next = c;
        c.next = a; // cycle back to head
        assertThat(LinkedListCycleStart.findCycleStart(a)).isSameAs(a);
    }
}
