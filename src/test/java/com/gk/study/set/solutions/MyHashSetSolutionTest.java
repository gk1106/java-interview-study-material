package com.gk.study.set.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MyHashSetSolutionTest {

    @Test
    void addReturnsTrueOnFirstInsertFalseOnDuplicate() {
        MyHashSet<String> set = new MyHashSet<>();
        assertThat(set.add("A")).isTrue();
        assertThat(set.add("A")).isFalse();
        assertThat(set.size()).isEqualTo(1);
    }

    @Test
    void containsReflectsCurrentMembership() {
        MyHashSet<Integer> set = new MyHashSet<>();
        set.add(1);
        set.add(2);
        assertThat(set.contains(1)).isTrue();
        assertThat(set.contains(3)).isFalse();
    }

    @Test
    void removeDeletesAndReturnsWhetherPresent() {
        MyHashSet<Integer> set = new MyHashSet<>();
        set.add(1);
        assertThat(set.remove(1)).isTrue();
        assertThat(set.remove(1)).isFalse();
        assertThat(set.contains(1)).isFalse();
        assertThat(set.size()).isEqualTo(0);
    }

    @Test
    void isEmptyReflectsSize() {
        MyHashSet<Integer> set = new MyHashSet<>();
        assertThat(set.isEmpty()).isTrue();
        set.add(1);
        assertThat(set.isEmpty()).isFalse();
    }

    @Test
    void growsPastLoadFactorThresholdCorrectly() {
        MyHashSet<Integer> set = new MyHashSet<>();
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
        MyHashSet<Integer> set = new MyHashSet<>();
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
        MyHashSet<String> set = new MyHashSet<>();
        assertThat(set.add(null)).isTrue();
        assertThat(set.contains(null)).isTrue();
        assertThat(set.add(null)).isFalse();
    }

    @Test
    void removeRebalancesChainCorrectly() {
        MyHashSet<Integer> set = new MyHashSet<>();
        for (int i = 0; i < 50; i++) {
            set.add(i);
        }
        for (int i = 0; i < 50; i += 2) {
            set.remove(i);
        }
        assertThat(set.size()).isEqualTo(25);
        for (int i = 1; i < 50; i += 2) {
            assertThat(set.contains(i)).isTrue();
        }
        for (int i = 0; i < 50; i += 2) {
            assertThat(set.contains(i)).isFalse();
        }
    }
}
