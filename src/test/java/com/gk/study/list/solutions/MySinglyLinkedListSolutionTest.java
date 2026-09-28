package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class MySinglyLinkedListSolutionTest {

    @Test
    void addLastAppendsInOrder() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        assertThat(list.size()).isEqualTo(3);
        assertThat(toJavaList(list)).containsExactly(1, 2, 3);
    }

    @Test
    void addFirstPrepends() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        list.addLast(2);
        list.addLast(3);
        list.addFirst(1);
        assertThat(toJavaList(list)).containsExactly(1, 2, 3);
    }

    @Test
    void removeFirstReturnsAndRemovesHead() {
        MySinglyLinkedList<String> list = new MySinglyLinkedList<>();
        list.addLast("a");
        list.addLast("b");
        String removed = list.removeFirst();
        assertThat(removed).isEqualTo("a");
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo("b");
    }

    @Test
    void removeFirstOnEmptyListThrows() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        assertThatThrownBy(list::removeFirst).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void removeLastRemainingElementThenAddLastWorksCorrectly() {
        // Regression test for the classic "forgot to null out tail" bug: if tail isn't reset
        // to null when the list becomes empty, this addLast would silently corrupt the list.
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        list.addLast(1);
        list.removeFirst();
        assertThat(list.isEmpty()).isTrue();
        list.addLast(2);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(2);
        assertThat(toJavaList(list)).containsExactly(2);
    }

    @Test
    void getByIndexWalksFromHead() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        for (int i = 0; i < 5; i++) {
            list.addLast(i);
        }
        assertThat(list.get(0)).isEqualTo(0);
        assertThat(list.get(4)).isEqualTo(4);
    }

    @Test
    void getOutOfBoundsThrows() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        list.addLast(1);
        assertThatThrownBy(() -> list.get(5)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> list.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void containsUsesEquals() {
        MySinglyLinkedList<String> list = new MySinglyLinkedList<>();
        list.addLast("x");
        list.addLast("y");
        assertThat(list.contains("y")).isTrue();
        assertThat(list.contains("z")).isFalse();
    }

    @Test
    void iterationVisitsAllElementsInOrder() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
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
    void iteratorThrowsOnConcurrentStructuralModification() {
        MySinglyLinkedList<Integer> list = new MySinglyLinkedList<>();
        list.addLast(1);
        list.addLast(2);
        Iterator<Integer> it = list.iterator();
        it.next();
        list.addLast(3); // structural change after the iterator was created
        assertThatThrownBy(it::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void iteratorSupportsGenericTypeSafety() {
        MySinglyLinkedList<String> list = new MySinglyLinkedList<>();
        list.addLast("only-strings-allowed");
        Iterator<String> it = list.iterator();
        String value = it.next();
        assertThat(value).isEqualTo("only-strings-allowed");
    }

    private static <T> List<T> toJavaList(MySinglyLinkedList<T> list) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            result.add(item);
        }
        return result;
    }
}
