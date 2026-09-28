package com.gk.study.streams.model;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * One account's summary for a single calendar month.
 *
 * @param accountId         the account this statement is for
 * @param month             the calendar month covered
 * @param totalDebit        sum of DEBIT amounts posted in that month
 * @param totalCredit       sum of CREDIT amounts posted in that month
 * @param transactionCount  number of transactions posted in that month
 */
public record MonthlyStatement(
        String accountId,
        YearMonth month,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        long transactionCount) {
}
