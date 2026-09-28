package com.gk.study.collectionsoverview.exercises;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for E04 MapEntriesSortedByValue. Expected to fail until implemented.
 */
class MapEntriesSortedByValueTest {

    @Test
    void sortsEntriesByValueDescending() {
        Map<String, Integer> balances = new LinkedHashMap<>();
        balances.put("checking", 100);
        balances.put("savings", 500);
        balances.put("credit", -50);

        assertThat(MapEntriesSortedByValue.sortedEntries(balances))
                .containsExactly("savings=500", "checking=100", "credit=-50");
    }

    @Test
    void emptyMapProducesEmptyList() {
        assertThat(MapEntriesSortedByValue.sortedEntries(Map.of())).isEmpty();
    }

    @Test
    void singleEntryMap() {
        assertThat(MapEntriesSortedByValue.sortedEntries(Map.of("only", 42)))
                .containsExactly("only=42");
    }
}
