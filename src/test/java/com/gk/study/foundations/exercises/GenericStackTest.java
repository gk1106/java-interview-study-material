package com.gk.study.foundations.exercises;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GenericStackTest {

    @Test
    void pushPopIsLifoOrder() {
        GenericStack<Integer> stack = new GenericStack<>(2);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.size()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.peek()).isEqualTo(2);
        assertThat(stack.size()).isEqualTo(2);
    }

    @Test
    void popOnEmptyStackThrows() {
        GenericStack<String> stack = new GenericStack<>(4);
        assertThrows(NoSuchElementException.class, stack::pop);
    }
}
