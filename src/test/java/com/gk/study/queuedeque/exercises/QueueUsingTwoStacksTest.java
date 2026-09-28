package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link QueueUsingTwoStacks} exercise stub. EXPECTED TO FAIL until implemented.
 */
class QueueUsingTwoStacksTest {

    @Test
    void enqueueThenDequeueIsFifo() {
        QueueUsingTwoStacks<Integer> queue = new QueueUsingTwoStacks<>();
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        assertThat(queue.dequeue()).isEqualTo(1);
        assertThat(queue.dequeue()).isEqualTo(2);
        assertThat(queue.dequeue()).isEqualTo(3);
    }

    @Test
    void peekDoesNotRemove() {
        QueueUsingTwoStacks<String> queue = new QueueUsingTwoStacks<>();
        queue.enqueue("a");
        queue.enqueue("b");
        assertThat(queue.peek()).isEqualTo("a");
        assertThat(queue.peek()).isEqualTo("a");
        assertThat(queue.size()).isEqualTo(2);
    }

    @Test
    void isEmptyReflectsState() {
        QueueUsingTwoStacks<Integer> queue = new QueueUsingTwoStacks<>();
        assertThat(queue.isEmpty()).isTrue();
        queue.enqueue(1);
        assertThat(queue.isEmpty()).isFalse();
        queue.dequeue();
        assertThat(queue.isEmpty()).isTrue();
    }

    @Test
    void dequeueOnEmptyThrows() {
        QueueUsingTwoStacks<Integer> queue = new QueueUsingTwoStacks<>();
        assertThatThrownBy(queue::dequeue).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void peekOnEmptyThrows() {
        QueueUsingTwoStacks<Integer> queue = new QueueUsingTwoStacks<>();
        assertThatThrownBy(queue::peek).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void interleavedEnqueueDequeuePreservesFifoOrder() {
        QueueUsingTwoStacks<Integer> queue = new QueueUsingTwoStacks<>();
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
        QueueUsingTwoStacks<Integer> queue = new QueueUsingTwoStacks<>();
        for (int i = 0; i < 100; i++) {
            queue.enqueue(i);
        }
        for (int i = 0; i < 100; i++) {
            assertThat(queue.dequeue()).isEqualTo(i);
        }
    }
}
