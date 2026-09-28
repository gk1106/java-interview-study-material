package com.gk.study.streams.exercises;

import com.gk.study.streams.model.AccountAverage;
import com.gk.study.streams.model.Transaction;
import java.util.List;

/**
 * B05 [Hard] Banking dataset. Find the account with the highest average transaction amount,
 * averaged across ALL of that account's transactions (both DEBIT and CREDIT).
 * Input:  {@link com.gk.study.streams.model.SampleBankingData#transactions()}
 * Output: AccountAverage[accountId=A4, averageAmount=29375.0]  (Chitra Rao's corporate current
 *         account has much larger average transaction sizes than the retail accounts)
 * Constraint: transactions is non-empty and every account referenced has at least one transaction.
 * Pattern: groupingBy + averagingDouble, then max by value
 */
public class HighestAverageTransactionAccount {

    /**
     * @param transactions all transactions (non-empty)
     * @return the {@link AccountAverage} for the account with the highest average transaction amount
     */
    public static AccountAverage solve(List<Transaction> transactions) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
