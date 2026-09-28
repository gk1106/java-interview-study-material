package com.gk.study.streams.solutions;

import com.gk.study.streams.model.AccountTotal;
import com.gk.study.streams.model.Transaction;
import com.gk.study.streams.model.TransactionType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solution for {@link com.gk.study.streams.exercises.AccountTransactionTotals}.
 *
 * <p>Groups by accountId, then reduces each group's DEBIT and CREDIT amounts separately with
 * {@code BigDecimal::add} (never {@code doubleValue()}, to avoid binary floating-point rounding
 * error on money). O(n) time, O(a) space (a = distinct accounts).
 */
public final class AccountTransactionTotalsSolution {

    private AccountTransactionTotalsSolution() {
    }

    public static Map<String, AccountTotal> solve(List<Transaction> transactions) {
        Map<String, List<Transaction>> byAccount =
                transactions.stream().collect(Collectors.groupingBy(Transaction::accountId));

        return byAccount.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new AccountTotal(
                                entry.getKey(),
                                sumByType(entry.getValue(), TransactionType.DEBIT),
                                sumByType(entry.getValue(), TransactionType.CREDIT))));
    }

    private static BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.type() == type)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
