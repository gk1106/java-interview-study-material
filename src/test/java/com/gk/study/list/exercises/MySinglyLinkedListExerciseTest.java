package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MySinglyLinkedListExercise} build-it-yourself stub. EXPECTED TO FAIL
 * until implemented.
 */
class MySinglyLinkedListExerciseTest {

    @Test
    void addLastAppendsInOrder() {
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        assertThat(list.size()).isEqualTo(3);
        assertThat(toJavaList(list)).containsExactly(1, 2, 3);
    }

    @Test
    void addFirstPrepends() {
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        list.addLast(2);
        list.addLast(3);
        list.addFirst(1);
        assertThat(toJavaList(list)).containsExactly(1, 2, 3);
    }

    @Test
    void removeFirstReturnsAndRemovesHead() {
        MySinglyLinkedListExercise<String> list = new MySinglyLinkedListExercise<>();
        list.addLast("a");
        list.addLast("b");
        String removed = list.removeFirst();
        assertThat(removed).isEqualTo("a");
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo("b");
    }

    @Test
    void removeFirstOnEmptyListThrows() {
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        assertThatThrownBy(list::removeFirst).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void removeLastRemainingElementThenAddLastWorksCorrectly() {
        // Regression test for the classic "forgot to null out tail" bug.
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        list.addLast(1);
        list.removeFirst();
        assertThat(list.isEmpty()).isTrue();
        list.addLast(2);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(2);
    }

    @Test
    void getByIndexWalksFromHead() {
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        for (int i = 0; i < 5; i++) {
            list.addLast(i);
        }
        assertThat(list.get(0)).isEqualTo(0);
        assertThat(list.get(4)).isEqualTo(4);
    }

    @Test
    void getOutOfBoundsThrows() {
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        list.addLast(1);
        assertThatThrownBy(() -> list.get(5)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void containsUsesEquals() {
        MySinglyLinkedListExercise<String> list = new MySinglyLinkedListExercise<>();
        list.addLast("x");
        list.addLast("y");
        assertThat(list.contains("y")).isTrue();
        assertThat(list.contains("z")).isFalse();
    }

    @Test
    void iterationVisitsAllElementsInOrder() {
        MySinglyLinkedListExercise<Integer> list = new MySinglyLinkedListExercise<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        List<Integer> collected = new ArrayList<>();
        for (Integer x : list) {
            collected.add(x);
        }
        assertThat(collected).containsExactly(1, 2, 3);
    }

    @Test
    void iteratorSupportsGenericTypeSafety() {
        MySinglyLinkedListExercise<String> list = new MySinglyLinkedListExercise<>();
        list.addLast("only-strings-allowed");
        Iterator<String> it = list.iterator();
        String value = it.next();
        assertThat(value).isEqualTo("only-strings-allowed");
    }

    private static <T> List<T> toJavaList(MySinglyLinkedListExercise<T> list) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            result.add(item);
        }
        return result;
    }
}
