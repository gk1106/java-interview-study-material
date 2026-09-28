package com.gk.study.map.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TimeBasedKeyValueStore} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TimeBasedKeyValueStoreTest {

    @Test
    void getExactTimestampMatch() {
        TimeBasedKeyValueStore store = new TimeBasedKeyValueStore();
        store.set("a", "1", 1);
        assertThat(store.get("a", 1)).isEqualTo("1");
    }

    @Test
    void getBeforeAnySetReturnsEmpty() {
        TimeBasedKeyValueStore store = new TimeBasedKeyValueStore();
        store.set("a", "1", 1);
        assertThat(store.get("a", 0)).isEqualTo("");
    }

    @Test
    void getReturnsFloorValue() {
        TimeBasedKeyValueStore store = new TimeBasedKeyValueStore();
        store.set("a", "1", 1);
        store.set("a", "2", 3);
        assertThat(store.get("a", 2)).isEqualTo("1");
        assertThat(store.get("a", 4)).isEqualTo("2");
    }

    @Test
    void unknownKeyReturnsEmpty() {
        TimeBasedKeyValueStore store = new TimeBasedKeyValueStore();
        assertThat(store.get("missing", 5)).isEqualTo("");
    }

    @Test
    void multipleKeysAreIndependent() {
        TimeBasedKeyValueStore store = new TimeBasedKeyValueStore();
        store.set("a", "a1", 1);
        store.set("b", "b1", 1);
        store.set("a", "a2", 5);
        assertThat(store.get("a", 5)).isEqualTo("a2");
        assertThat(store.get("b", 5)).isEqualTo("b1");
    }
}
