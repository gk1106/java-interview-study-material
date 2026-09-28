package com.gk.study.streams.exercises;

import com.gk.study.streams.model.AccountTotal;
import com.gk.study.streams.model.Transaction;
import java.util.List;
import java.util.Map;

/**
 * B01 [Medium] Banking dataset. Group {@code transactions} by {@code accountId} and compute the
 * total DEBIT amount and total CREDIT amount for each account.
 * Input:  {@link com.gk.study.streams.model.SampleBankingData#transactions()}
 * Output: a map of accountId -> {@link AccountTotal}, one entry per account that has at least
 *         one transaction (an account with only debits has totalCredit = 0, and vice versa)
 * Constraint: use BigDecimal arithmetic throughout (no double conversion) to avoid rounding error.
 * Pattern: groupingBy + downstream reduce/summing per transaction type
 */
public class AccountTransactionTotals {

    /**
     * @param transactions all transactions to aggregate
     * @return map of accountId to its {@link AccountTotal}
     */
    public static Map<String, AccountTotal> solve(List<Transaction> transactions) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
