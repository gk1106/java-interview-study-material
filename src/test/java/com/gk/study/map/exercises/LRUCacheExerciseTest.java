package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link LRUCacheExercise} build-it-yourself stub. EXPECTED TO FAIL until implemented.
 */
class LRUCacheExerciseTest {

    @Test
    void putAndGetTypicalUsage() {
        LRUCacheExercise<Integer, String> cache = new LRUCacheExercise<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        assertThat(cache.get(1)).isEqualTo("a");
        assertThat(cache.get(2)).isEqualTo("b");
    }

    @Test
    void evictsLeastRecentlyUsedOnCapacityOverflow() {
        LRUCacheExercise<Integer, String> cache = new LRUCacheExercise<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c"); // capacity 2 -> evicts key 1 (least recently used, never touched again)
        assertThat(cache.get(1)).isNull();
        assertThat(cache.get(2)).isEqualTo("b");
        assertThat(cache.get(3)).isEqualTo("c");
    }

    @Test
    void getRefreshesRecency() {
        LRUCacheExercise<Integer, String> cache = new LRUCacheExercise<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.get(1); // touch 1 -> now 2 is the least recently used
        cache.put(3, "c"); // evicts 2, not 1
        assertThat(cache.get(2)).isNull();
        assertThat(cache.get(1)).isEqualTo("a");
        assertThat(cache.get(3)).isEqualTo("c");
    }

    @Test
    void putOnExistingKeyUpdatesValueAndRecency() {
        LRUCacheExercise<Integer, String> cache = new LRUCacheExercise<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(1, "updated"); // updates value, also refreshes recency of key 1
        cache.put(3, "c"); // evicts 2, not 1
        assertThat(cache.get(1)).isEqualTo("updated");
        assertThat(cache.get(2)).isNull();
    }

    @Test
    void capacityOneEvictsImmediately() {
        LRUCacheExercise<Integer, String> cache = new LRUCacheExercise<>(1);
        cache.put(1, "a");
        cache.put(2, "b");
        assertThat(cache.get(1)).isNull();
        assertThat(cache.get(2)).isEqualTo("b");
    }
}
