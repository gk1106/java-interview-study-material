package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.gk.study.streams.model.AccountAverage;
import com.gk.study.streams.model.SampleBankingData;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link HighestAverageTransactionAccount} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class HighestAverageTransactionAccountTest {

    @Test
    void corporateAccountHasHighestAverage() {
        AccountAverage result = HighestAverageTransactionAccount.solve(SampleBankingData.transactions());
        assertThat(result.accountId()).isEqualTo("A4");
        assertThat(result.averageAmount()).isCloseTo(29375.0, within(0.01));
    }
}
