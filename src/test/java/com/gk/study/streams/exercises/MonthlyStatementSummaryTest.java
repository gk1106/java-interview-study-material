package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.streams.model.MonthlyStatement;
import com.gk.study.streams.model.SampleBankingData;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link MonthlyStatementSummary} exercise stub. EXPECTED TO FAIL until implemented.
 */
class MonthlyStatementSummaryTest {

    @Test
    void accountA1JulyStatement() {
        Map<String, Map<YearMonth, MonthlyStatement>> result =
                MonthlyStatementSummary.solve(SampleBankingData.transactions());
        MonthlyStatement july = result.get("A1").get(YearMonth.of(2026, 7));
        assertThat(july.totalDebit()).isEqualByComparingTo(BigDecimal.valueOf(16500));
        assertThat(july.totalCredit()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(july.transactionCount()).isEqualTo(5);
    }

    @Test
    void accountA1AugustStatement() {
        Map<String, Map<YearMonth, MonthlyStatement>> result =
                MonthlyStatementSummary.solve(SampleBankingData.transactions());
        MonthlyStatement august = result.get("A1").get(YearMonth.of(2026, 8));
        assertThat(august.totalDebit()).isEqualByComparingTo(BigDecimal.valueOf(3800));
        assertThat(august.totalCredit()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        assertThat(august.transactionCount()).isEqualTo(3);
    }

    @Test
    void everyAccountWithTransactionsIsPresent() {
        Map<String, Map<YearMonth, MonthlyStatement>> result =
                MonthlyStatementSummary.solve(SampleBankingData.transactions());
        assertThat(result).hasSize(6);
    }

    @Test
    void emptyTransactionList() {
        assertThat(MonthlyStatementSummary.solve(java.util.List.of())).isEmpty();
    }
}
