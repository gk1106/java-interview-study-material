package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MyCircularQueueSolutionTest {

    @Test
    void enqueueUntilFullThenRejects() {
        MyCircularQueue q = new MyCircularQueue(3);
        assertThat(q.enqueue(1)).isTrue();
        assertThat(q.enqueue(2)).isTrue();
        assertThat(q.enqueue(3)).isTrue();
        assertThat(q.enqueue(4)).isFalse();
    }

    @Test
    void isEmptyAndIsFullReflectState() {
        MyCircularQueue q = new MyCircularQueue(2);
        assertThat(q.isEmpty()).isTrue();
        assertThat(q.isFull()).isFalse();
        q.enqueue(1);
        q.enqueue(2);
        assertThat(q.isFull()).isTrue();
        assertThat(q.isEmpty()).isFalse();
    }

    @Test
    void frontAndRearOnEmptyReturnMinusOne() {
        MyCircularQueue q = new MyCircularQueue(2);
        assertThat(q.front()).isEqualTo(-1);
        assertThat(q.rear()).isEqualTo(-1);
    }

    @Test
    void dequeueOnEmptyReturnsFalse() {
        MyCircularQueue q = new MyCircularQueue(2);
        assertThat(q.dequeue()).isFalse();
    }

    @Test
    void frontAndRearAfterOperations() {
        MyCircularQueue q = new MyCircularQueue(3);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        assertThat(q.front()).isEqualTo(1);
        assertThat(q.rear()).isEqualTo(3);
    }

    @Test
    void wrapAroundReusesFreedSlots() {
        MyCircularQueue q = new MyCircularQueue(3);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        q.dequeue();
        q.dequeue();
        q.enqueue(4);
        q.enqueue(5);
        assertThat(q.isFull()).isTrue();
        assertThat(q.front()).isEqualTo(3);
        assertThat(q.rear()).isEqualTo(5);
    }

    @Test
    void invalidCapacityThrows() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new MyCircularQueue(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
