package com.gk.study.streams.solutions;

import com.gk.study.streams.model.Transaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

/**
 * Solution for {@link com.gk.study.streams.exercises.FraudFlaggedTransactions}.
 *
 * <p>Two independent, individually testable predicates composed with {@code Predicate.and} —
 * clearer than one big lambda, and either half can be reused or unit-tested on its own.
 * O(n log n) time (the final sort), O(n) space.
 */
public final class FraudFlaggedTransactionsSolution {

    private FraudFlaggedTransactionsSolution() {
    }

    public static List<String> solve(List<Transaction> transactions, BigDecimal largeAmountThreshold) {
        Predicate<Transaction> isLargeAmount = t -> t.amount().compareTo(largeAmountThreshold) > 0;
        Predicate<Transaction> isOddHour = t -> {
            int hour = t.timestamp().getHour();
            return hour < 6 || hour >= 23;
        };
        Predicate<Transaction> isSuspicious = isLargeAmount.and(isOddHour);

        return transactions.stream()
                .filter(isSuspicious)
                .map(Transaction::id)
                .sorted()
                .toList();
    }
}
