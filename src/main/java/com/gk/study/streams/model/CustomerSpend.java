package com.gk.study.streams.model;

import java.math.BigDecimal;

/**
 * A customer's total spend (sum of DEBIT transactions across all of their accounts), used for
 * top-N spender reports.
 *
 * @param customerId   the customer's id
 * @param customerName the customer's display name
 * @param totalSpend   sum of DEBIT amounts across every account owned by this customer
 */
public record CustomerSpend(String customerId, String customerName, BigDecimal totalSpend) {
}
