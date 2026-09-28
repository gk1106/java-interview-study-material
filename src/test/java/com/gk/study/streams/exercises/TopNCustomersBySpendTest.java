package com.gk.study.streams.exercises;

import static org.assertj.core.api.Assertions.assertThat;

import com.gk.study.streams.model.CustomerSpend;
import com.gk.study.streams.model.SampleBankingData;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TopNCustomersBySpend} exercise stub. EXPECTED TO FAIL until implemented.
 */
class TopNCustomersBySpendTest {

    @Test
    void top3ByspendOrderedDescending() {
        List<CustomerSpend> top3 = TopNCustomersBySpend.solve(
                SampleBankingData.customers(), SampleBankingData.accounts(), SampleBankingData.transactions(), 3);
        assertThat(top3).extracting(CustomerSpend::customerId).containsExactly("C3", "C1", "C2");
        assertThat(top3.get(0).totalSpend()).isEqualByComparingTo(BigDecimal.valueOf(95000));
        assertThat(top3.get(1).totalSpend()).isEqualByComparingTo(BigDecimal.valueOf(26600));
        assertThat(top3.get(2).totalSpend()).isEqualByComparingTo(BigDecimal.valueOf(10150));
    }

    @Test
    void topNLargerThanCustomerCountReturnsAll() {
        List<CustomerSpend> all = TopNCustomersBySpend.solve(
                SampleBankingData.customers(), SampleBankingData.accounts(), SampleBankingData.transactions(), 10);
        assertThat(all).hasSize(5);
        assertThat(all).extracting(CustomerSpend::customerId).containsExactly("C3", "C1", "C2", "C4", "C5");
    }

    @Test
    void topNZeroReturnsEmpty() {
        List<CustomerSpend> none = TopNCustomersBySpend.solve(
                SampleBankingData.customers(), SampleBankingData.accounts(), SampleBankingData.transactions(), 0);
        assertThat(none).isEmpty();
    }
}
