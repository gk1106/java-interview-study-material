package com.gk.study.queuedeque.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class MyMinHeapSolutionTest {

    @Test
    void extractsInAscendingOrder() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        for (int v : new int[]{5, 3, 8, 1, 9, 2}) {
            heap.insert(v);
        }
        assertThat(heap.extractMin()).isEqualTo(1);
        assertThat(heap.extractMin()).isEqualTo(2);
        assertThat(heap.extractMin()).isEqualTo(3);
        assertThat(heap.extractMin()).isEqualTo(5);
        assertThat(heap.extractMin()).isEqualTo(8);
        assertThat(heap.extractMin()).isEqualTo(9);
    }

    @Test
    void peekDoesNotRemove() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        heap.insert(4);
        heap.insert(2);
        assertThat(heap.peek()).isEqualTo(2);
        assertThat(heap.size()).isEqualTo(2);
    }

    @Test
    void extractMinOnEmptyThrows() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        assertThatThrownBy(heap::extractMin).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void peekOnEmptyThrows() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        assertThatThrownBy(heap::peek).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void handlesDuplicateValues() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        heap.insert(2);
        heap.insert(2);
        heap.insert(1);
        heap.insert(1);
        assertThat(heap.extractMin()).isEqualTo(1);
        assertThat(heap.extractMin()).isEqualTo(1);
        assertThat(heap.extractMin()).isEqualTo(2);
        assertThat(heap.extractMin()).isEqualTo(2);
    }

    @Test
    void worksWithStringsViaComparable() {
        MyMinHeap<String> heap = new MyMinHeap<>();
        heap.insert("banana");
        heap.insert("apple");
        heap.insert("cherry");
        assertThat(heap.extractMin()).isEqualTo("apple");
        assertThat(heap.extractMin()).isEqualTo("banana");
        assertThat(heap.extractMin()).isEqualTo("cherry");
    }

    @Test
    void isEmptyReflectsState() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        assertThat(heap.isEmpty()).isTrue();
        heap.insert(1);
        assertThat(heap.isEmpty()).isFalse();
        heap.extractMin();
        assertThat(heap.isEmpty()).isTrue();
    }

    @Test
    void largeRandomInputExtractsSorted() {
        MyMinHeap<Integer> heap = new MyMinHeap<>();
        int[] values = {42, 17, 5, 99, 3, 61, 23, 8, 77, 1, 55, 30};
        for (int v : values) {
            heap.insert(v);
        }
        int[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        for (int expected : sorted) {
            assertThat(heap.extractMin()).isEqualTo(expected);
        }
        assertThat(heap.isEmpty()).isTrue();
    }
}
