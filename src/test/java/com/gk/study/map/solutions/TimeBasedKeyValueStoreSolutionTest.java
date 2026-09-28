package com.gk.study.map.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TimeBasedKeyValueStoreSolutionTest {

    @Test
    void getExactTimestampMatch() {
        TimeBasedKeyValueStoreSolution store = new TimeBasedKeyValueStoreSolution();
        store.set("a", "1", 1);
        assertThat(store.get("a", 1)).isEqualTo("1");
    }

    @Test
    void getBeforeAnySetReturnsEmpty() {
        TimeBasedKeyValueStoreSolution store = new TimeBasedKeyValueStoreSolution();
        store.set("a", "1", 1);
        assertThat(store.get("a", 0)).isEqualTo("");
    }

    @Test
    void getReturnsFloorValue() {
        TimeBasedKeyValueStoreSolution store = new TimeBasedKeyValueStoreSolution();
        store.set("a", "1", 1);
        store.set("a", "2", 3);
        assertThat(store.get("a", 2)).isEqualTo("1");
        assertThat(store.get("a", 4)).isEqualTo("2");
    }

    @Test
    void unknownKeyReturnsEmpty() {
        TimeBasedKeyValueStoreSolution store = new TimeBasedKeyValueStoreSolution();
        assertThat(store.get("missing", 5)).isEqualTo("");
    }

    @Test
    void multipleKeysAreIndependent() {
        TimeBasedKeyValueStoreSolution store = new TimeBasedKeyValueStoreSolution();
        store.set("a", "a1", 1);
        store.set("b", "b1", 1);
        store.set("a", "a2", 5);
        assertThat(store.get("a", 5)).isEqualTo("a2");
        assertThat(store.get("b", 5)).isEqualTo("b1");
    }

    @Test
    void manySetsFindsCorrectFloorEfficiently() {
        TimeBasedKeyValueStoreSolution store = new TimeBasedKeyValueStoreSolution();
        for (long t = 0; t < 1000; t += 10) {
            store.set("k", "v" + t, t);
        }
        assertThat(store.get("k", 55)).isEqualTo("v50");
        assertThat(store.get("k", 999)).isEqualTo("v990");
    }
}
