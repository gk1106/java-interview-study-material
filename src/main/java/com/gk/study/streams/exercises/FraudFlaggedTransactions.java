package com.gk.study.streams.exercises;

import com.gk.study.streams.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

/**
 * B04 [Hard] Banking dataset. Flag transactions that look suspicious: amount strictly greater
 * than {@code largeAmountThreshold} AND posted at an "odd hour" — before 06:00 or at/after 23:00
 * local time. Build the predicate as a composition of two smaller predicates (one for the amount
 * check, one for the odd-hour check) combined with {@code Predicate.and}, rather than one
 * monolithic lambda.
 * Input:  {@link com.gk.study.streams.model.SampleBankingData#transactions()}, threshold = 8000
 * Output: ["T0005", "T0021", "T0031", "T0040"]  (sorted ascending by id; in the sample dataset
 *         T0016 is posted at an odd hour but is below the threshold, so it must NOT be flagged)
 * Constraint: the comparison must use BigDecimal.compareTo, not equals (scale-sensitive) or
 * doubleValue().
 * Pattern: composed Predicate chain (Predicate.and) + filter + sorted
 */
public class FraudFlaggedTransactions {

    /**
     * @param transactions          all transactions to scan
     * @param largeAmountThreshold  a transaction must exceed this amount to be eligible
     * @return ids of flagged transactions, sorted ascending
     */
    public static List<String> solve(List<Transaction> transactions, BigDecimal largeAmountThreshold) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO");
    }
}
