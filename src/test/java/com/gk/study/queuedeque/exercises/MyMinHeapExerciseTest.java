package com.gk.study.queuedeque.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MyMinHeapExercise} build-it-yourself stub. EXPECTED TO FAIL until
 * implemented.
 */
class MyMinHeapExerciseTest {

    @Test
    void extractsInAscendingOrder() {
        MyMinHeapExercise<Integer> heap = new MyMinHeapExercise<>();
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
        MyMinHeapExercise<Integer> heap = new MyMinHeapExercise<>();
        heap.insert(4);
        heap.insert(2);
        assertThat(heap.peek()).isEqualTo(2);
        assertThat(heap.size()).isEqualTo(2);
    }

    @Test
    void extractMinOnEmptyThrows() {
        MyMinHeapExercise<Integer> heap = new MyMinHeapExercise<>();
        assertThatThrownBy(heap::extractMin).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void peekOnEmptyThrows() {
        MyMinHeapExercise<Integer> heap = new MyMinHeapExercise<>();
        assertThatThrownBy(heap::peek).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void handlesDuplicateValues() {
        MyMinHeapExercise<Integer> heap = new MyMinHeapExercise<>();
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
        MyMinHeapExercise<String> heap = new MyMinHeapExercise<>();
        heap.insert("banana");
        heap.insert("apple");
        heap.insert("cherry");
        assertThat(heap.extractMin()).isEqualTo("apple");
        assertThat(heap.extractMin()).isEqualTo("banana");
        assertThat(heap.extractMin()).isEqualTo("cherry");
    }

    @Test
    void isEmptyReflectsState() {
        MyMinHeapExercise<Integer> heap = new MyMinHeapExercise<>();
        assertThat(heap.isEmpty()).isTrue();
        heap.insert(1);
        assertThat(heap.isEmpty()).isFalse();
        heap.extractMin();
        assertThat(heap.isEmpty()).isTrue();
    }
}
