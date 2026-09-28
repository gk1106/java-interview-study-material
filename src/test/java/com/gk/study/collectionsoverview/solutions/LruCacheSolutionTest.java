package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LruCacheSolutionTest {

    @Test
    void evictsLeastRecentlyUsedEntry() {
        LruCacheSolution<String, Integer> cache = new LruCacheSolution<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");
        cache.put("c", 3);

        assertThat(cache.get("b")).isNull();
        assertThat(cache.get("a")).isEqualTo(1);
        assertThat(cache.get("c")).isEqualTo(3);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void withinCapacityKeepsEverything() {
        LruCacheSolution<String, Integer> cache = new LruCacheSolution<>(3);
        cache.put("a", 1);
        cache.put("b", 2);

        assertThat(cache.size()).isEqualTo(2);
        assertThat(cache.get("a")).isEqualTo(1);
        assertThat(cache.get("b")).isEqualTo(2);
    }

    @Test
    void capacityOfOneAlwaysKeepsMostRecentOnly() {
        LruCacheSolution<String, Integer> cache = new LruCacheSolution<>(1);
        cache.put("a", 1);
        cache.put("b", 2);

        assertThat(cache.get("a")).isNull();
        assertThat(cache.get("b")).isEqualTo(2);
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThatThrownBy(() -> new LruCacheSolution<String, Integer>(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
