package com.gk.study.streams.exercises;

import com.gk.study.streams.model.MonthlyStatement;
import com.gk.study.streams.model.Transaction;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * B03 [Medium] Banking dataset. Build a monthly statement per account: for every (account,
 * calendar month) pair that has at least one transaction, compute total debit, total credit and
 * transaction count for that month.
 * Input:  {@link com.gk.study.streams.model.SampleBankingData#transactions()}
 * Output: e.g. solve(...).get("A1").get(YearMonth.of(2026,7))
 *         == MonthlyStatement[accountId=A1, month=2026-07, totalDebit=16500, totalCredit=20000, transactionCount=5]
 * Constraint: months with zero transactions for an account are simply absent (not zero-filled).
 * Pattern: nested groupingBy (accountId -> month) + a downstream collector that reduces to a
 * single {@link MonthlyStatement}
 */
public class MonthlyStatementSummary {

    /**
     * @param transactions all transactions to summarize
     * @return map of accountId -> (map of YearMonth -> {@link MonthlyStatement})
     */
    public static Map<String, Map<YearMonth, MonthlyStatement>> solve(List<Transaction> transactions) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
