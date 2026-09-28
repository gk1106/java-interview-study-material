package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link StackUsingTwoQueuesExercise} build-it-yourself stub. EXPECTED TO FAIL
 * until implemented.
 */
class StackUsingTwoQueuesExerciseTest {

    @Test
    void pushThenPopIsLifo() {
        StackUsingTwoQueuesExercise<Integer> stack = new StackUsingTwoQueuesExercise<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(1);
    }

    @Test
    void topDoesNotRemove() {
        StackUsingTwoQueuesExercise<String> stack = new StackUsingTwoQueuesExercise<>();
        stack.push("a");
        stack.push("b");
        assertThat(stack.top()).isEqualTo("b");
        assertThat(stack.size()).isEqualTo(2);
    }

    @Test
    void isEmptyReflectsState() {
        StackUsingTwoQueuesExercise<Integer> stack = new StackUsingTwoQueuesExercise<>();
        assertThat(stack.isEmpty()).isTrue();
        stack.push(1);
        assertThat(stack.isEmpty()).isFalse();
        stack.pop();
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void popOnEmptyThrows() {
        StackUsingTwoQueuesExercise<Integer> stack = new StackUsingTwoQueuesExercise<>();
        assertThatThrownBy(stack::pop).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void topOnEmptyThrows() {
        StackUsingTwoQueuesExercise<Integer> stack = new StackUsingTwoQueuesExercise<>();
        assertThatThrownBy(stack::top).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void interleavedPushAndPop() {
        StackUsingTwoQueuesExercise<Integer> stack = new StackUsingTwoQueuesExercise<>();
        stack.push(1);
        stack.push(2);
        assertThat(stack.pop()).isEqualTo(2);
        stack.push(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(1);
    }
}
