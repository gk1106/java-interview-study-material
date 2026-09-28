package com.gk.study.foundations.solutions;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericStackSolutionTest {

    @Test
    void pushPopIsLifoOrder() {
        GenericStackSolution<Integer> stack = new GenericStackSolution<>(2);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.size()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.peek()).isEqualTo(2);
        assertThat(stack.size()).isEqualTo(2);
    }

    @Test
    void growsPastInitialCapacity() {
        GenericStackSolution<Integer> stack = new GenericStackSolution<>(1);
        for (int i = 0; i < 100; i++) {
            stack.push(i);
        }
        assertThat(stack.size()).isEqualTo(100);
        for (int i = 99; i >= 0; i--) {
            assertThat(stack.pop()).isEqualTo(i);
        }
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void popOnEmptyStackThrows() {
        GenericStackSolution<String> stack = new GenericStackSolution<>(4);
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @Test
    void peekOnEmptyStackThrows() {
        GenericStackSolution<String> stack = new GenericStackSolution<>(4);
        assertThrows(NoSuchElementException.class, stack::peek);
    }

    @Test
    void peekDoesNotRemove() {
        GenericStackSolution<String> stack = new GenericStackSolution<>(4);
        stack.push("a");
        assertThat(stack.peek()).isEqualTo("a");
        assertThat(stack.size()).isEqualTo(1);
    }
}
