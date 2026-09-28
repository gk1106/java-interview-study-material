package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MyCircularQueueExercise} build-it-yourself stub. EXPECTED TO FAIL until
 * implemented.
 */
class MyCircularQueueExerciseTest {

    @Test
    void enqueueUntilFullThenRejects() {
        MyCircularQueueExercise q = new MyCircularQueueExercise(3);
        assertThat(q.enqueue(1)).isTrue();
        assertThat(q.enqueue(2)).isTrue();
        assertThat(q.enqueue(3)).isTrue();
        assertThat(q.enqueue(4)).isFalse();
    }

    @Test
    void isEmptyAndIsFullReflectState() {
        MyCircularQueueExercise q = new MyCircularQueueExercise(2);
        assertThat(q.isEmpty()).isTrue();
        assertThat(q.isFull()).isFalse();
        q.enqueue(1);
        q.enqueue(2);
        assertThat(q.isFull()).isTrue();
        assertThat(q.isEmpty()).isFalse();
    }

    @Test
    void frontAndRearOnEmptyReturnMinusOne() {
        MyCircularQueueExercise q = new MyCircularQueueExercise(2);
        assertThat(q.front()).isEqualTo(-1);
        assertThat(q.rear()).isEqualTo(-1);
    }

    @Test
    void dequeueOnEmptyReturnsFalse() {
        MyCircularQueueExercise q = new MyCircularQueueExercise(2);
        assertThat(q.dequeue()).isFalse();
    }

    @Test
    void frontAndRearAfterOperations() {
        MyCircularQueueExercise q = new MyCircularQueueExercise(3);
        q.enqueue(1);
        q.enqueue(2);
        q.enqueue(3);
        assertThat(q.front()).isEqualTo(1);
        assertThat(q.rear()).isEqualTo(3);
    }

    @Test
    void wrapAroundReusesFreedSlots() {
        MyCircularQueueExercise q = new MyCircularQueueExercise(3);
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
}
