package com.gk.study.collectionsoverview.solutions;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapEntriesSortedByValueSolutionTest {

    @Test
    void sortsEntriesByValueDescending() {
        Map<String, Integer> balances = new LinkedHashMap<>();
        balances.put("checking", 100);
        balances.put("savings", 500);
        balances.put("credit", -50);

        assertThat(MapEntriesSortedByValueSolution.sortedEntries(balances))
                .containsExactly("savings=500", "checking=100", "credit=-50");
    }

    @Test
    void emptyMapProducesEmptyList() {
        assertThat(MapEntriesSortedByValueSolution.sortedEntries(Map.of())).isEmpty();
    }

    @Test
    void singleEntryMap() {
        assertThat(MapEntriesSortedByValueSolution.sortedEntries(Map.of("only", 42)))
                .containsExactly("only=42");
    }
}
