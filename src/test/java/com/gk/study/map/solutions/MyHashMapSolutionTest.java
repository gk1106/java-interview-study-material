package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MyHashMapSolutionTest {

    @Test
    void putAndGetTypicalUsage() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertThat(map.get("a")).isEqualTo(1);
        assertThat(map.get("b")).isEqualTo(2);
        assertThat(map.get("c")).isEqualTo(3);
        assertThat(map.size()).isEqualTo(3);
    }

    @Test
    void putOverwritesExistingKeyAndReturnsOldValue() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("a", 1);
        Integer old = map.put("a", 99);
        assertThat(old).isEqualTo(1);
        assertThat(map.get("a")).isEqualTo(99);
        assertThat(map.size()).isEqualTo(1); // overwrite must not grow size
    }

    @Test
    void getMissingKeyReturnsNull() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("a", 1);
        assertThat(map.get("missing")).isNull();
    }

    @Test
    void containsKeyReflectsPresence() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertThat(map.containsKey("a")).isFalse();
        map.put("a", 1);
        assertThat(map.containsKey("a")).isTrue();
        map.remove("a");
        assertThat(map.containsKey("a")).isFalse();
    }

    @Test
    void removeReturnsValueAndDecrementsSize() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        Integer removed = map.remove("a");
        assertThat(removed).isEqualTo(1);
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.get("a")).isNull();
        assertThat(map.get("b")).isEqualTo(2);
    }

    @Test
    void removeMissingKeyReturnsNullAndLeavesSizeUnchanged() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("a", 1);
        Integer removed = map.remove("missing");
        assertThat(removed).isNull();
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    void isEmptyReflectsSize() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        assertThat(map.isEmpty()).isTrue();
        map.put("a", 1);
        assertThat(map.isEmpty()).isFalse();
        map.remove("a");
        assertThat(map.isEmpty()).isTrue();
    }

    @Test
    void handlesCollidingHashCodesViaSeparateChaining() {
        MyHashMap<CollidingKey, String> map = new MyHashMap<>();
        for (int i = 0; i < 20; i++) {
            map.put(new CollidingKey(i), "value-" + i);
        }
        assertThat(map.size()).isEqualTo(20);
        for (int i = 0; i < 20; i++) {
            assertThat(map.get(new CollidingKey(i))).isEqualTo("value-" + i);
        }
        // remove a few, from the middle and the ends of the (long) collision chain
        map.remove(new CollidingKey(0));
        map.remove(new CollidingKey(10));
        map.remove(new CollidingKey(19));
        assertThat(map.size()).isEqualTo(17);
        assertThat(map.get(new CollidingKey(0))).isNull();
        assertThat(map.get(new CollidingKey(10))).isNull();
        assertThat(map.get(new CollidingKey(19))).isNull();
        assertThat(map.get(new CollidingKey(5))).isEqualTo("value-5");
    }

    @Test
    void resizesCorrectlyAcrossManyInsertsPastDefaultThreshold() {
        MyHashMap<Integer, Integer> map = new MyHashMap<>();
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            map.put(i, i * 10);
        }
        assertThat(map.size()).isEqualTo(n);
        // every key must still resolve correctly after multiple resize/rehash cycles
        for (int i = 0; i < n; i++) {
            assertThat(map.get(i)).isEqualTo(i * 10);
        }
    }

    @Test
    void resizeDoesNotDuplicateOrDropEntries() {
        MyHashMap<Integer, Integer> map = new MyHashMap<>();
        int n = 5000;
        Set<Integer> expectedKeys = new HashSet<>();
        for (int i = 0; i < n; i++) {
            map.put(i, i);
            expectedKeys.add(i);
        }
        int found = 0;
        for (Integer key : expectedKeys) {
            if (map.containsKey(key)) {
                found++;
            }
        }
        assertThat(found).isEqualTo(n);
        assertThat(map.size()).isEqualTo(n);
    }

    @Test
    void nullKeyIsSupportedLikeJdkHashMap() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put(null, 42);
        assertThat(map.get(null)).isEqualTo(42);
        assertThat(map.containsKey(null)).isTrue();
        map.put(null, 100);
        assertThat(map.get(null)).isEqualTo(100);
        assertThat(map.size()).isEqualTo(1);
    }

    /** A key whose hashCode() is constant, forcing every instance into the same bucket. */
    private static final class CollidingKey {
        final int id;

        CollidingKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 7; // deliberately constant -> every instance collides into one bucket
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CollidingKey other && other.id == id;
        }
    }
}
