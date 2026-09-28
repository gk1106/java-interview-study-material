package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.list.solutions.LinkedListCycleStartSolution.Node;
import org.junit.jupiter.api.Test;

class LinkedListCycleStartSolutionTest {

    @Test
    void noCycleReturnsNull() {
        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        a.next = b;
        b.next = c;
        assertThat(LinkedListCycleStartSolution.findCycleStart(a)).isNull();
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
        d.next = b;
        assertThat(LinkedListCycleStartSolution.findCycleStart(a)).isSameAs(b);
    }

    @Test
    void emptyListReturnsNull() {
        assertThat(LinkedListCycleStartSolution.findCycleStart(null)).isNull();
    }

    @Test
    void singleNodeSelfCycle() {
        Node a = new Node(1);
        a.next = a;
        assertThat(LinkedListCycleStartSolution.findCycleStart(a)).isSameAs(a);
    }

    @Test
    void singleNodeNoCycle() {
        Node a = new Node(1);
        assertThat(LinkedListCycleStartSolution.findCycleStart(a)).isNull();
    }

    @Test
    void cycleAtTheHead() {
        Node a = new Node(1);
        Node b = new Node(2);
        Node c = new Node(3);
        a.next = b;
        b.next = c;
        c.next = a;
        assertThat(LinkedListCycleStartSolution.findCycleStart(a)).isSameAs(a);
    }
}
