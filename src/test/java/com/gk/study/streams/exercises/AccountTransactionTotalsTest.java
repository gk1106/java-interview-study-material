package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.streams.model.AccountTotal;
import com.gk.study.streams.model.SampleBankingData;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link AccountTransactionTotals} exercise stub. EXPECTED TO FAIL until
 * implemented.
 */
class AccountTransactionTotalsTest {

    @Test
    void everyAccountWithTransactionsIsPresent() {
        Map<String, AccountTotal> result = AccountTransactionTotals.solve(SampleBankingData.transactions());
        assertThat(result).hasSize(6);
    }

    @Test
    void accountA1Totals() {
        Map<String, AccountTotal> result = AccountTransactionTotals.solve(SampleBankingData.transactions());
        AccountTotal a1 = result.get("A1");
        assertThat(a1.totalDebit()).isEqualByComparingTo(BigDecimal.valueOf(20300));
        assertThat(a1.totalCredit()).isEqualByComparingTo(BigDecimal.valueOf(40000));
    }

    @Test
    void accountA4Totals() {
        Map<String, AccountTotal> result = AccountTransactionTotals.solve(SampleBankingData.transactions());
        AccountTotal a4 = result.get("A4");
        assertThat(a4.totalDebit()).isEqualByComparingTo(BigDecimal.valueOf(95000));
        assertThat(a4.totalCredit()).isEqualByComparingTo(BigDecimal.valueOf(140000));
    }

    @Test
    void emptyTransactionList() {
        assertThat(AccountTransactionTotals.solve(java.util.List.of())).isEmpty();
    }
}
