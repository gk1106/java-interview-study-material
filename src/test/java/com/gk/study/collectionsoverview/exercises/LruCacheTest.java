package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E03 LruCache. Expected to fail until implemented.
 */
class LruCacheTest {

    @Test
    void evictsLeastRecentlyUsedEntry() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");       // touches "a" -> "b" is now least recently used
        cache.put("c", 3);    // evicts "b"

        assertThat(cache.get("b")).isNull();
        assertThat(cache.get("a")).isEqualTo(1);
        assertThat(cache.get("c")).isEqualTo(3);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void withinCapacityKeepsEverything() {
        LruCache<String, Integer> cache = new LruCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);

        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get("a")).isEqualTo(1);
        assertThat(cache.get("b")).isEqualTo(2);
    }

    @Test
    void capacityOfOneAlwaysKeepsMostRecentOnly() {
        LruCache<String, Integer> cache = new LruCache<>(1);
        cache.put("a", 1);
        cache.put("b", 2);

        assertThat(cache.get("a")).isNull();
        assertThat(cache.get("b")).isEqualTo(2);
    }

    @Test
    void rejectsNonPositiveCapacity() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new LruCache<String, Integer>(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
