package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class StackUsingTwoQueuesSolutionTest {

    @Test
    void pushThenPopIsLifo() {
        StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(1);
    }

    @Test
    void topDoesNotRemove() {
        StackUsingTwoQueues<String> stack = new StackUsingTwoQueues<>();
        stack.push("a");
        stack.push("b");
        assertThat(stack.top()).isEqualTo("b");
        assertThat(stack.size()).isEqualTo(2);
    }

    @Test
    void isEmptyReflectsState() {
        StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
        assertThat(stack.isEmpty()).isTrue();
        stack.push(1);
        assertThat(stack.isEmpty()).isFalse();
        stack.pop();
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void popOnEmptyThrows() {
        StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
        assertThatThrownBy(stack::pop).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void topOnEmptyThrows() {
        StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
        assertThatThrownBy(stack::top).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void interleavedPushAndPop() {
        StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
        stack.push(1);
        stack.push(2);
        assertThat(stack.pop()).isEqualTo(2);
        stack.push(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(1);
    }

    @Test
    void largerInputPreservesLifoOrder() {
        StackUsingTwoQueues<Integer> stack = new StackUsingTwoQueues<>();
        for (int i = 0; i < 50; i++) {
            stack.push(i);
        }
        for (int i = 49; i >= 0; i--) {
            assertThat(stack.pop()).isEqualTo(i);
        }
    }
}
