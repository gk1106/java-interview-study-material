package com.gk.study.streams.model;

import java.math.BigDecimal;

/**
 * Aggregated debit/credit totals for one account, e.g. the result of grouping
 * {@link Transaction}s by {@link Transaction#accountId()} and summing by {@link TransactionType}.
 *
 * @param accountId  the account these totals belong to
 * @param totalDebit sum of all DEBIT transaction amounts for this account
 * @param totalCredit sum of all CREDIT transaction amounts for this account
 */
public record AccountTotal(String accountId, BigDecimal totalDebit, BigDecimal totalCredit) {
}
