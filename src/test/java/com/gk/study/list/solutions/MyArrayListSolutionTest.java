package com.gk.study.list.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

class MyArrayListSolutionTest {

    @Test
    void addAndGetTypicalUsage() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0)).isEqualTo("A");
        assertThat(list.get(2)).isEqualTo("C");
    }

    @Test
    void addAtIndexShiftsSubsequentElements() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        list.add(2);
        list.add(4);
        list.add(2, 3);
        assertThat(toJavaList(list)).containsExactly(1, 2, 3, 4);
    }

    @Test
    void addAtIndexZeroInsertsAtFront() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(2);
        list.add(3);
        list.add(0, 1);
        assertThat(toJavaList(list)).containsExactly(1, 2, 3);
    }

    @Test
    void setReplacesAndReturnsOldValue() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("X");
        String old = list.set(0, "Y");
        assertThat(old).isEqualTo("X");
        assertThat(list.get(0)).isEqualTo("Y");
    }

    @Test
    void removeShiftsElementsAndDecreasesSize() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(10);
        list.add(20);
        list.add(30);
        int removed = list.remove(1);
        assertThat(removed).isEqualTo(20);
        assertThat(list.size()).isEqualTo(2);
        assertThat(toJavaList(list)).containsExactly(10, 30);
    }

    @Test
    void removeLastElementIsCheap() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        list.add(2);
        int removed = list.remove(1);
        assertThat(removed).isEqualTo(2);
        assertThat(list.size()).isEqualTo(1);
    }

    @Test
    void isEmptyReflectsSize() {
        MyArrayList<Integer> list = new MyArrayList<>();
        assertThat(list.isEmpty()).isTrue();
        list.add(1);
        assertThat(list.isEmpty()).isFalse();
        list.remove(0);
        assertThat(list.isEmpty()).isTrue();
    }

    @Test
    void containsAndIndexOfUseEquals() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("a");
        list.add("b");
        assertThat(list.contains("b")).isTrue();
        assertThat(list.contains("z")).isFalse();
        assertThat(list.indexOf("b")).isEqualTo(1);
        assertThat(list.indexOf("z")).isEqualTo(-1);
    }

    @Test
    void getOutOfBoundsThrows() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        assertThatThrownBy(() -> list.get(5)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> list.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void addAtInvalidIndexThrows() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        assertThatThrownBy(() -> list.add(5, 99)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> list.add(-1, 99)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void growthBoundaryExactlyAtDefaultCapacity() {
        MyArrayList<Integer> list = new MyArrayList<>();
        for (int i = 0; i < 10; i++) { // fills the default capacity exactly
            list.add(i);
        }
        assertThat(list.size()).isEqualTo(10);
        list.add(10); // triggers the first growth event
        assertThat(list.size()).isEqualTo(11);
        for (int i = 0; i <= 10; i++) {
            assertThat(list.get(i)).isEqualTo(i);
        }
    }

    @Test
    void growsAcrossMultipleGrowthCycles() {
        MyArrayList<Integer> list = new MyArrayList<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        assertThat(list.size()).isEqualTo(n);
        for (int i = 0; i < n; i++) {
            assertThat(list.get(i)).isEqualTo(i);
        }
    }

    @Test
    void iterationVisitsAllElementsInOrder() {
        MyArrayList<Integer> list = new MyArrayList<>();
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
    void iteratorThrowsOnConcurrentStructuralModification() {
        MyArrayList<Integer> list = new MyArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        Iterator<Integer> it = list.iterator();
        it.next();
        list.add(4); // structural change after the iterator was created
        assertThatThrownBy(it::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void iteratorSupportsGenericTypeSafety() {
        MyArrayList<String> list = new MyArrayList<>();
        list.add("only-strings-allowed");
        Iterator<String> it = list.iterator();
        String value = it.next(); // no cast needed by the caller -> proves generic typing works
        assertThat(value).isEqualTo("only-strings-allowed");
    }

    private static <T> List<T> toJavaList(MyArrayList<T> list) {
        List<T> result = new ArrayList<>();
        for (T item : list) {
            result.add(item);
        }
        return result;
    }
}
