package com.gk.study.list.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MyArrayListExercise} build-it-yourself stub. EXPECTED TO FAIL until
 * implemented.
 */
class MyArrayListExerciseTest {

    @Test
    void addAndGetTypicalUsage() {
        MyArrayListExercise<String> list = new MyArrayListExercise<>();
        list.add("A");
        list.add("B");
        list.add("C");
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0)).isEqualTo("A");
        assertThat(list.get(2)).isEqualTo("C");
    }

    @Test
    void addAtIndexShiftsSubsequentElements() {
        MyArrayListExercise<Integer> list = new MyArrayListExercise<>();
        list.add(1);
        list.add(2);
        list.add(4);
        list.add(2, 3); // insert 3 before 4
        assertThat(toJavaList(list)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void setReplacesAndReturnsOldValue() {
        MyArrayListExercise<String> list = new MyArrayListExercise<>();
        list.add("X");
        String old = list.set(0, "Y");
        assertThat(old).isEqualTo("X");
        assertThat(list.get(0)).isEqualTo("Y");
    }

    @Test
    void removeShiftsElementsAndDecreasesSize() {
        MyArrayListExercise<Integer> list = new MyArrayListExercise<>();
        list.add(10);
        list.add(20);
        list.add(30);
        int removed = list.remove(1);
        assertThat(removed).isEqualTo(20);
        assertThat(list.size()).isEqualTo(2);
        assertThat(toJavaList(list)).containsExactly(10, 30);
    }

    @Test
    void isEmptyReflectsSize() {
        MyArrayListExercise<Integer> list = new MyArrayListExercise<>();
        assertThat(list.isEmpty()).isTrue();
        list.add(1);
        assertThat(list.isEmpty()).isFalse();
    }

    @Test
    void containsAndIndexOfUseEquals() {
        MyArrayListExercise<String> list = new MyArrayListExercise<>();
        list.add("a");
        list.add("b");
        assertThat(list.contains("b")).isTrue();
        assertThat(list.contains("z")).isFalse();
        assertThat(list.indexOf("b")).isEqualTo(1);
        assertThat(list.indexOf("z")).isEqualTo(-1);
    }

    @Test
    void getOutOfBoundsThrows() {
        MyArrayListExercise<Integer> list = new MyArrayListExercise<>();
        list.add(1);
        assertThatThrownBy(() -> list.get(5)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> list.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void growsPastInitialCapacityCorrectly() {
        MyArrayListExercise<Integer> list = new MyArrayListExercise<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertThat(list.size()).isEqualTo(100);
        for (int i = 0; i < 100; i++) {
            assertThat(list.get(i)).isEqualTo(i);
        }
    }

    @Test
    void iterationVisitsAllElementsInOrder() {
        MyArrayListExercise<Integer> list = new MyArrayListExercise<>();
        list.add(1);
        list.add(2);
        list.add(3);
        List<Integer> collected = new ArrayList<>();
        for (Integer x : list) {
            collected.add(x);
        }
        assertThat(collected).containsExactly(1, 2, 3);
    }

    @Test
    void iteratorSupportsGenericTypeSafety() {
        MyArrayListExercise<String> list = new MyArrayListExercise<>();
        list.add("only-strings-allowed");
        Iterator<String> it = list.iterator();
        String value = it.next(); // no cast needed by the caller -> proves generic typing works
        assertThat(value).isEqualTo("only-strings-allowed");
    }

    private static <T> List<T> toJavaList(MyArrayListExercise<T> list) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            result.add(item);
        }
        return result;
    }
}
