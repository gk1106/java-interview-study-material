package com.gk.study.dsaproblems.solutions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FixedSizeMRUTransactionLogSolutionTest {

    @Test
    void putAndGetTypicalUsage() {
        FixedSizeMRUTransactionLog log = new FixedSizeMRUTransactionLog(2);
        log.put("t1", "a");
        log.put("t2", "b");
        assertThat(log.get("t1")).isEqualTo("a");
        assertThat(log.get("t2")).isEqualTo("b");
        assertThat(log.size()).isEqualTo(2);
    }

    @Test
    void evictsLeastRecentlyTouchedOnOverflow() {
        FixedSizeMRUTransactionLog log = new FixedSizeMRUTransactionLog(2);
        log.put("t1", "a");
        log.put("t2", "b");
        log.put("t3", "c");
        assertThat(log.get("t1")).isNull();
        assertThat(log.get("t2")).isEqualTo("b");
        assertThat(log.get("t3")).isEqualTo("c");
    }

    @Test
    void getRefreshesRecency() {
        FixedSizeMRUTransactionLog log = new FixedSizeMRUTransactionLog(2);
        log.put("t1", "a");
        log.put("t2", "b");
        log.get("t1");
        log.put("t3", "c");
        assertThat(log.get("t2")).isNull();
        assertThat(log.get("t1")).isEqualTo("a");
        assertThat(log.get("t3")).isEqualTo("c");
    }

    @Test
    void missingTransactionReturnsNull() {
        FixedSizeMRUTransactionLog log = new FixedSizeMRUTransactionLog(2);
        assertThat(log.get("unknown")).isNull();
    }

    @Test
    void capacityOneEvictsImmediately() {
        FixedSizeMRUTransactionLog log = new FixedSizeMRUTransactionLog(1);
        log.put("t1", "a");
        log.put("t2", "b");
        assertThat(log.get("t1")).isNull();
        assertThat(log.get("t2")).isEqualTo("b");
        assertThat(log.size()).isEqualTo(1);
    }

    @Test
    void largerCapacityRetainsAllEntriesWithinLimit() {
        int capacity = 100;
        FixedSizeMRUTransactionLog log = new FixedSizeMRUTransactionLog(capacity);
        for (int i = 0; i < capacity; i++) {
            log.put("t" + i, "detail" + i);
        }
        assertThat(log.size()).isEqualTo(capacity);
        log.put("overflow", "x");
        assertThat(log.size()).isEqualTo(capacity);
        assertThat(log.get("t0")).isNull();
        assertThat(log.get("overflow")).isEqualTo("x");
    }
}
