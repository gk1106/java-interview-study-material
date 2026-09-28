package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LRUCacheSolutionTest {

    @Test
    void putAndGetTypicalUsage() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        assertThat(cache.get(1)).isEqualTo("a");
        assertThat(cache.get(2)).isEqualTo("b");
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void getMissingKeyReturnsNull() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        assertThat(cache.get(99)).isNull();
    }

    @Test
    void evictsLeastRecentlyUsedOnCapacityOverflow() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c"); // over capacity -> evicts 1 (never touched after insertion)
        assertThat(cache.get(1)).isNull();
        assertThat(cache.get(2)).isEqualTo("b");
        assertThat(cache.get(3)).isEqualTo("c");
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void getRefreshesRecencyProtectingFromEviction() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.get(1); // 1 is now most-recently-used; 2 becomes least-recently-used
        cache.put(3, "c"); // evicts 2, not 1
        assertThat(cache.get(1)).isEqualTo("a");
        assertThat(cache.get(2)).isNull();
        assertThat(cache.get(3)).isEqualTo("c");
    }

    @Test
    void putOnExistingKeyUpdatesValueAndRefreshesRecency() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(1, "updated"); // updates value AND marks 1 as most-recently-used
        cache.put(3, "c"); // evicts 2, not 1
        assertThat(cache.get(1)).isEqualTo("updated");
        assertThat(cache.get(2)).isNull();
        assertThat(cache.get(3)).isEqualTo("c");
    }

    @Test
    void capacityOneAlwaysEvictsThePreviousEntry() {
        LRUCache<Integer, String> cache = new LRUCache<>(1);
        cache.put(1, "a");
        cache.put(2, "b");
        assertThat(cache.get(1)).isNull();
        assertThat(cache.get(2)).isEqualTo("b");
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void repeatedGetsOnSameKeyDoNotChangeEvictionOrder() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);
        cache.put(1, "a");
        cache.put(2, "b");
        cache.get(2);
        cache.get(2);
        cache.get(2); // repeatedly touching 2 keeps 1 as the least-recently-used
        cache.put(3, "c");
        assertThat(cache.get(1)).isNull();
        assertThat(cache.get(2)).isEqualTo("b");
        assertThat(cache.get(3)).isEqualTo("c");
    }

    @Test
    void longerSequenceMatchesExpectedLruOrder() {
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);          // order (LRU->MRU): 2, 3, 1
        cache.put(4, 4);       // evicts 2 -> order: 3, 1, 4
        assertThat(cache.get(2)).isNull();
        cache.get(3);           // order: 1, 4, 3
        cache.put(5, 5);        // evicts 1 -> order: 4, 3, 5
        assertThat(cache.get(1)).isNull();
        assertThat(cache.get(4)).isEqualTo(4);
        assertThat(cache.get(3)).isEqualTo(3);
        assertThat(cache.get(5)).isEqualTo(5);
    }

    @Test
    void zeroOrNegativeCapacityThrows() {
        assertThatThrownBy(() -> new LRUCache<Integer, Integer>(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LRUCache<Integer, Integer>(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void manyOperationsStayWithinCapacity() {
        int capacity = 50;
        LRUCache<Integer, Integer> cache = new LRUCache<>(capacity);
        for (int i = 0; i < 1000; i++) {
            cache.put(i, i);
            assertThat(cache.size()).isLessThanOrEqualTo(capacity);
        }
        // only the last `capacity` keys should still be present
        for (int i = 1000 - capacity; i < 1000; i++) {
            assertThat(cache.get(i)).isEqualTo(i);
        }
        assertThat(cache.get(0)).isNull();
    }
}
