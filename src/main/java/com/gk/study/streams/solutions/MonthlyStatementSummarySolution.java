package com.gk.study.streams.solutions;

import com.gk.study.streams.model.MonthlyStatement;
import com.gk.study.streams.model.Transaction;
import com.gk.study.streams.model.TransactionType;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.MonthlyStatementSummary}.
 *
 * <p>A nested {@code groupingBy(accountId, groupingBy(month))} buckets transactions into
 * per-account, per-month lists; each bucket is then reduced to one {@link MonthlyStatement}.
 * O(n) time, O(n) space in the worst case (every transaction in its own account/month bucket).
 */
public final class MonthlyStatementSummarySolution {

    private MonthlyStatementSummarySolution() {
    }

    public static Map<String, Map<YearMonth, MonthlyStatement>> solve(List<Transaction> transactions) {
        Map<String, Map<YearMonth, List<Transaction>>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::accountId, Collectors.groupingBy(t -> YearMonth.from(t.timestamp()))));

        Map<String, Map<YearMonth, MonthlyStatement>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Map<YearMonth, List<Transaction>>> accountEntry : grouped.entrySet()) {
            String accountId = accountEntry.getKey();
            Map<YearMonth, MonthlyStatement> monthMap = new LinkedHashMap<>();
            for (Map.Entry<YearMonth, List<Transaction>> monthEntry : accountEntry.getValue().entrySet()) {
                List<Transaction> txs = monthEntry.getValue();
                BigDecimal debit = sumByType(txs, TransactionType.DEBIT);
                BigDecimal credit = sumByType(txs, TransactionType.CREDIT);
                monthMap.put(
                        monthEntry.getKey(),
                        new MonthlyStatement(accountId, monthEntry.getKey(), debit, credit, txs.size()));
            }
            result.put(accountId, monthMap);
        }
        return result;
    }

    private static BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.type() == type)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
