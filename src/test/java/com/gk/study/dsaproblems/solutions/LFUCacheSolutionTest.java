package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LFUCacheSolutionTest {

    @Test
    void typicalInput() {
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        assertThat(cache.get(1)).isEqualTo(1);
        cache.put(3, 3);
        assertThat(cache.get(2)).isEqualTo(-1);
        assertThat(cache.get(3)).isEqualTo(3);
        cache.put(4, 4);
        assertThat(cache.get(1)).isEqualTo(-1);
        assertThat(cache.get(3)).isEqualTo(3);
        assertThat(cache.get(4)).isEqualTo(4);
    }

    @Test
    void zeroCapacityNeverStoresAnything() {
        LFUCache cache = new LFUCache(0);
        cache.put(1, 1);
        assertThat(cache.get(1)).isEqualTo(-1);
    }

    @Test
    void missingKeyReturnsNegativeOne() {
        LFUCache cache = new LFUCache(2);
        assertThat(cache.get(99)).isEqualTo(-1);
    }

    @Test
    void putOnExistingKeyUpdatesValueAndFrequency() {
        LFUCache cache = new LFUCache(1);
        cache.put(1, 10);
        cache.put(1, 20);
        assertThat(cache.get(1)).isEqualTo(20);
    }

    @Test
    void capacityOneAlwaysEvictsThePreviousKey() {
        LFUCache cache = new LFUCache(1);
        cache.put(1, 1);
        cache.put(2, 2);
        assertThat(cache.get(1)).isEqualTo(-1);
        assertThat(cache.get(2)).isEqualTo(2);
    }

    @Test
    void largerCapacityRetainsMostFrequentlyUsedKeys() {
        int capacity = 50;
        LFUCache cache = new LFUCache(capacity);
        for (int i = 0; i < capacity; i++) {
            cache.put(i, i * 10);
        }
        for (int extra = 0; extra < 5; extra++) {
            for (int i = 0; i < 25; i++) {
                cache.get(i);
            }
        }
        cache.put(capacity, capacity * 10);
        assertThat(cache.get(25)).isEqualTo(-1);
        assertThat(cache.get(0)).isEqualTo(0);
        assertThat(cache.get(capacity)).isEqualTo(capacity * 10);
    }
}
