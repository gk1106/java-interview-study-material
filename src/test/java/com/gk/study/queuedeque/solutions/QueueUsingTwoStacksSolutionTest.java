package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class QueueUsingTwoStacksSolutionTest {

    @Test
    void enqueueThenDequeueIsFifo() {
        QueueUsingTwoStacksSolution<Integer> queue = new QueueUsingTwoStacksSolution<>();
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        assertThat(queue.dequeue()).isEqualTo(1);
        assertThat(queue.dequeue()).isEqualTo(2);
        assertThat(queue.dequeue()).isEqualTo(3);
    }

    @Test
    void peekDoesNotRemove() {
        QueueUsingTwoStacksSolution<String> queue = new QueueUsingTwoStacksSolution<>();
        queue.enqueue("a");
        queue.enqueue("b");
        assertThat(queue.peek()).isEqualTo("a");
        assertThat(queue.peek()).isEqualTo("a");
        assertThat(queue.size()).isEqualTo(2);
    }

    @Test
    void isEmptyReflectsState() {
        QueueUsingTwoStacksSolution<Integer> queue = new QueueUsingTwoStacksSolution<>();
        assertThat(queue.isEmpty()).isTrue();
        queue.enqueue(1);
        assertThat(queue.isEmpty()).isFalse();
        queue.dequeue();
        assertThat(queue.isEmpty()).isTrue();
    }

    @Test
    void dequeueOnEmptyThrows() {
        QueueUsingTwoStacksSolution<Integer> queue = new QueueUsingTwoStacksSolution<>();
        assertThatThrownBy(queue::dequeue).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void peekOnEmptyThrows() {
        QueueUsingTwoStacksSolution<Integer> queue = new QueueUsingTwoStacksSolution<>();
        assertThatThrownBy(queue::peek).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void interleavedEnqueueDequeuePreservesFifoOrder() {
        QueueUsingTwoStacksSolution<Integer> queue = new QueueUsingTwoStacksSolution<>();
        queue.enqueue(1);
        queue.enqueue(2);
        assertThat(queue.dequeue()).isEqualTo(1);
        queue.enqueue(3);
        assertThat(queue.dequeue()).isEqualTo(2);
        queue.enqueue(4);
        assertThat(queue.dequeue()).isEqualTo(3);
        assertThat(queue.dequeue()).isEqualTo(4);
    }

    @Test
    void largerInputStaysInOrder() {
        QueueUsingTwoStacksSolution<Integer> queue = new QueueUsingTwoStacksSolution<>();
        for (int i = 0; i < 100; i++) {
            queue.enqueue(i);
        }
        for (int i = 0; i < 100; i++) {
            assertThat(queue.dequeue()).isEqualTo(i);
        }
    }
}
