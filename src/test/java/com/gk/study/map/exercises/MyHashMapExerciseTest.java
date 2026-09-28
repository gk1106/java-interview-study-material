package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MyHashMapExercise} build-it-yourself stub. EXPECTED TO FAIL until implemented.
 */
class MyHashMapExerciseTest {

    @Test
    void putAndGetTypicalUsage() {
        MyHashMapExercise<String, Integer> map = new MyHashMapExercise<>();
        map.put("a", 1);
        map.put("b", 2);
        assertThat(map.get("a")).isEqualTo(1);
        assertThat(map.get("b")).isEqualTo(2);
        assertThat(map.size()).isEqualTo(2);
    }

    @Test
    void putOverwritesExistingKey() {
        MyHashMapExercise<String, Integer> map = new MyHashMapExercise<>();
        map.put("a", 1);
        Integer old = map.put("a", 99);
        assertThat(old).isEqualTo(1);
        assertThat(map.get("a")).isEqualTo(99);
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    void getMissingKeyReturnsNull() {
        MyHashMapExercise<String, Integer> map = new MyHashMapExercise<>();
        assertThat(map.get("missing")).isNull();
    }

    @Test
    void removeReturnsValueAndDecrementsSize() {
        MyHashMapExercise<String, Integer> map = new MyHashMapExercise<>();
        map.put("a", 1);
        map.put("b", 2);
        Integer removed = map.remove("a");
        assertThat(removed).isEqualTo(1);
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.containsKey("a")).isFalse();
    }

    @Test
    void resizesCorrectlyAcrossManyInserts() {
        MyHashMapExercise<Integer, Integer> map = new MyHashMapExercise<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            map.put(i, i * 10);
        }
        assertThat(map.size()).isEqualTo(n);
        for (int i = 0; i < n; i++) {
            assertThat(map.get(i)).isEqualTo(i * 10);
        }
    }

    @Test
    void handlesCollidingHashCodes() {
        MyHashMapExercise<CollidingKey, String> map = new MyHashMapExercise<>();
        map.put(new CollidingKey(1), "one");
        map.put(new CollidingKey(2), "two");
        map.put(new CollidingKey(3), "three");
        assertThat(map.get(new CollidingKey(1))).isEqualTo("one");
        assertThat(map.get(new CollidingKey(2))).isEqualTo("two");
        assertThat(map.get(new CollidingKey(3))).isEqualTo("three");
        assertThat(map.size()).isEqualTo(3);
    }

    /** A key whose hashCode() is constant, forcing every instance into the same bucket. */
    private static final class CollidingKey {
        final int id;

        CollidingKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 42; // deliberately constant -> every key collides
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CollidingKey other && other.id == id;
        }
    }
}
