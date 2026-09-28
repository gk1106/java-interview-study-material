package com.gk.study.set.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MyHashSetExercise} build-it-yourself stub. EXPECTED TO FAIL until
 * implemented.
 */
class MyHashSetExerciseTest {

    @Test
    void addReturnsTrueOnFirstInsertFalseOnDuplicate() {
        MyHashSetExercise<String> set = new MyHashSetExercise<>();
        assertThat(set.add("A")).isTrue();
        assertThat(set.add("A")).isFalse();
        assertThat(set.size()).isEqualTo(1);
    }

    @Test
    void containsReflectsCurrentMembership() {
        MyHashSetExercise<Integer> set = new MyHashSetExercise<>();
        set.add(1);
        set.add(2);
        assertThat(set.contains(1)).isTrue();
        assertThat(set.contains(3)).isFalse();
    }

    @Test
    void removeDeletesAndReturnsWhetherPresent() {
        MyHashSetExercise<Integer> set = new MyHashSetExercise<>();
        set.add(1);
        assertThat(set.remove(1)).isTrue();
        assertThat(set.remove(1)).isFalse();
        assertThat(set.contains(1)).isFalse();
        assertThat(set.size()).isEqualTo(0);
    }

    @Test
    void isEmptyReflectsSize() {
        MyHashSetExercise<Integer> set = new MyHashSetExercise<>();
        assertThat(set.isEmpty()).isTrue();
        set.add(1);
        assertThat(set.isEmpty()).isFalse();
    }

    @Test
    void growsPastLoadFactorThresholdCorrectly() {
        MyHashSetExercise<Integer> set = new MyHashSetExercise<>();
        for (int i = 0; i < 1000; i++) {
            set.add(i);
        }
        assertThat(set.size()).isEqualTo(1000);
        for (int i = 0; i < 1000; i++) {
            assertThat(set.contains(i)).isTrue();
        }
    }

    @Test
    void iterationVisitsEveryElementExactlyOnce() {
        MyHashSetExercise<Integer> set = new MyHashSetExercise<>();
        set.add(10);
        set.add(20);
        set.add(30);
        List<Integer> collected = new ArrayList<>();
        for (Integer x : set) {
            collected.add(x);
        }
        assertThat(collected).containsExactlyInAnyOrder(10, 20, 30);
    }

    @Test
    void supportsOneNullElement() {
        MyHashSetExercise<String> set = new MyHashSetExercise<>();
        assertThat(set.add(null)).isTrue();
        assertThat(set.contains(null)).isTrue();
        assertThat(set.add(null)).isFalse();
    }
}
