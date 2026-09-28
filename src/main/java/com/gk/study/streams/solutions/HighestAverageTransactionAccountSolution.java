package com.gk.study.streams.solutions;

import com.gk.study.streams.model.AccountAverage;
import com.gk.study.streams.model.Transaction;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.HighestAverageTransactionAccount}.
 *
 * <p>{@code groupingBy(accountId, averagingDouble(amount))} computes every account's average in
 * one pass, then a {@code max} over the entry set (by value) picks the winner. O(n) time, O(a)
 * space (a = distinct accounts).
 */
public final class HighestAverageTransactionAccountSolution {

    private HighestAverageTransactionAccountSolution() {
    }

    public static AccountAverage solve(List<Transaction> transactions) {
        Map<String, Double> avgByAccount = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::accountId, Collectors.averagingDouble(t -> t.amount().doubleValue())));

        return avgByAccount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new AccountAverage(e.getKey(), e.getValue()))
                .orElseThrow(() -> new IllegalArgumentException("transactions must not be empty"));
    }
}
