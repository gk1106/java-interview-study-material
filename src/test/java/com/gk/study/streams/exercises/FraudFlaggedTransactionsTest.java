package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.streams.model.SampleBankingData;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link FraudFlaggedTransactions} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class FraudFlaggedTransactionsTest {

    @Test
    void flagsLargeOddHourTransactions() {
        List<String> flagged =
                FraudFlaggedTransactions.solve(SampleBankingData.transactions(), BigDecimal.valueOf(8000));
        assertThat(flagged).containsExactly("T0005", "T0021", "T0031", "T0040");
    }

    @Test
    void oddHourAloneIsNotEnough() {
        // T0016 is posted at 23:50 (odd hour) but its amount (1200) is below the threshold.
        List<String> flagged =
                FraudFlaggedTransactions.solve(SampleBankingData.transactions(), BigDecimal.valueOf(8000));
        assertThat(flagged).doesNotContain("T0016");
    }

    @Test
    void veryHighThresholdFlagsNothing() {
        List<String> flagged =
                FraudFlaggedTransactions.solve(SampleBankingData.transactions(), BigDecimal.valueOf(50000));
        assertThat(flagged).isEmpty();
    }

    @Test
    void emptyTransactionList() {
        assertThat(FraudFlaggedTransactions.solve(List.of(), BigDecimal.valueOf(8000))).isEmpty();
    }
}
